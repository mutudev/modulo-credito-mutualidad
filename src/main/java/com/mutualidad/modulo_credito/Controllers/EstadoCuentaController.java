package com.mutualidad.modulo_credito.Controllers;


import com.mutualidad.modulo_credito.Main;
import com.mutualidad.modulo_credito.Models.ModelCredito;
import com.mutualidad.modulo_credito.Models.ModelEmpresa;
import com.mutualidad.modulo_credito.Models.ModelSolicitud;
import com.mutualidad.modulo_credito.Models.ModelTransaccion;
import com.mutualidad.modulo_credito.Services.Servicio;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class EstadoCuentaController implements Initializable {

    @FXML
    private TextField txtSocio, txtNombre;

    @FXML
    private ComboBox cmbOpcion, cmbEmpresa;

    @FXML
    private Button btnCargar, btnGenerar, btnLimpiar;
    @FXML
    private TableView<ModelCredito> tblCreditos;

    @FXML
    private TableColumn<ModelCredito, String> colMonto;

    @FXML
    private TableColumn<ModelCredito, String> colFecha;

    @FXML
    private TableColumn<ModelCredito, String> colFolio;

    @FXML
    private ImageView imgBusqueda;

    @Autowired
    private Servicio servicio;



    Dotenv dotenv = Dotenv.load();
    NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DecimalFormat formatoPorcentaje = new DecimalFormat("#0.00'%'");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<ModelEmpresa> empresas = servicio.traerCatalogoEmpresas();
        for(ModelEmpresa empresa : empresas){
            cmbEmpresa.getItems().add(empresa.getNombre());
        }
        cmbEmpresa.getSelectionModel().selectFirst();
        cmbOpcion.getItems().add("ESTADO DE CUENTA");
        cmbOpcion.getItems().add("MOVIMIENTOS");
        cmbOpcion.getSelectionModel().selectFirst();



        btnGenerar.setDisable(true);
        colFolio.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf( data.getValue().getId())));
// MONTO
        colMonto.setCellValueFactory(data ->
                new SimpleStringProperty(
                        formatoMoneda.format(data.getValue().getMonto())
                )
        );

// FECHA (LocalDate)
        colFecha.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getFd().format(formatoFecha)
                )
        );
    }

    public void cargarSocioBuscado(String numero, String nombre) {
        txtSocio.setText(numero);
        txtNombre.setText(nombre);
    }

    @FXML
    public void buscarSocioPorNombre() {
        try {
            Stage nuevaVentana = new Stage();
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/busquedaSocio.fxml"));
            fxml.setControllerFactory(Main.context::getBean);
            Scene nuevaEscena = new Scene(fxml.load());
            BusquedaController controlador = fxml.getController();
            controlador.setEstadoCuentaController(this);
            nuevaEscena
                    .getStylesheets()
                    .add(getClass().getResource("/assets/css/estilos.css").toExternalForm());
            nuevaVentana.setTitle("BUSQUEDA DE SOCIO POR NOMBRE");
            nuevaVentana.setScene(nuevaEscena);
            nuevaVentana.setResizable(false);
            nuevaVentana.centerOnScreen();
            nuevaVentana.initModality(Modality.APPLICATION_MODAL);
            nuevaVentana.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    public void cargarSocio() {
        if(txtSocio.getText().equalsIgnoreCase("")){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL INTENTAR BUSCAR AL SOCIO");
            alert.setHeaderText("ERROR AL INTENTAR BUSCAR AL SOCIO");
            alert.setContentText(
                    "INTRODUZCA UN DATO VALIDO");
            alert.showAndWait();
            return;
        }
        if(servicio.obtenerSocioConNumero(Integer.parseInt(txtSocio.getText())) == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL INTENTAR BUSCAR AL SOCIO");
            alert.setHeaderText("ERROR AL INTENTAR BUSCAR AL SOCIO");
            alert.setContentText(
                    "INTRODUZCA UN DATO VALIDO");
            alert.showAndWait();
            limpiarDatos();
            return;
        }
        txtNombre.setText( servicio.obtenerSocioConNumero(Integer.parseInt(txtSocio.getText())));
        txtSocio.setEditable(false);
        btnCargar.setDisable(true);
        if(!cmbOpcion.getSelectionModel().getSelectedItem().toString().equals("MOVIMIENTOS")){
            cargarCreditos();
        }

        btnGenerar.setDisable(false);

    }

    @FXML
    public void limpiarDatos() {
        txtSocio.setText("");
        txtNombre.setText("");
        txtSocio.setEditable(true);
        btnCargar.setDisable(false);
        tblCreditos.setVisible(false);
        tblCreditos.getItems().clear();
    }


    @FXML
    public void setTipoReporte() {
        if(cmbOpcion.getSelectionModel().getSelectedItem().toString().equals("MOVIMIENTOS")){
            cmbEmpresa.setDisable(true);
            tblCreditos.setVisible(false);
        }else{
            cmbEmpresa.setDisable(false);
            cargarCreditos();

        }
    }


    @FXML
    public void cambioEmpreas() {

        if(!cmbOpcion.getSelectionModel().getSelectedItem().toString().equals("MOVIMIENTOS")){
            String codEmpresa = servicio.obtenerEmpresaXNombre(cmbEmpresa.getSelectionModel().getSelectedItem().toString()).getCodigo();
            List<ModelCredito> creditos = servicio.obtenerParaEstadoDeCuenta(Integer.parseInt(txtSocio.getText()), codEmpresa, 2);
            tblCreditos.getItems().clear();
            tblCreditos.getItems().addAll(creditos);
            tblCreditos.setVisible(true);
        }
    }

    public void cargarCreditos(){
        String codEmpresa = servicio.obtenerEmpresaXNombre(cmbEmpresa.getSelectionModel().getSelectedItem().toString()).getCodigo();

        if (!txtSocio.getText().isEmpty()) {
            List<ModelCredito> creditos = servicio.obtenerParaEstadoDeCuenta(Integer.parseInt(txtSocio.getText()), codEmpresa, 2);
            tblCreditos.getItems().clear();
            tblCreditos.getItems().addAll(creditos);
            tblCreditos.setVisible(true);
        }

    }

    @FXML
    public void cargarConTecla(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            cargarSocio();
        }else if(event.getCode().equals(KeyCode.ESCAPE)){
            limpiarDatos();
        }
    }


    @FXML
    public void cargarConClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            generar();
        }
    }



    @FXML
    public void generar() {

        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.initStyle(StageStyle.UNDECORATED);
        loadingStage.setAlwaysOnTop(true);

        VBox loadingPane = new VBox(20);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setPadding(new Insets(30));
        loadingPane.setStyle("-fx-background-color: white; -fx-border-color: #185754; -fx-border-width: 2;");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(60, 60);

        Label loadingLabel = new Label("Generando reporte...");
        loadingLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        loadingLabel.setTextFill(Color.web("#39577c"));

        loadingPane.getChildren().addAll(progressIndicator, loadingLabel);

        Scene loadingScene = new Scene(loadingPane, 300, 150);
        loadingStage.setScene(loadingScene);
        loadingStage.centerOnScreen();


        String nomEmpresa = servicio.obtenerEmpresaXNombre(cmbEmpresa.getSelectionModel().getSelectedItem().toString()).getNombre();

        if(cmbOpcion.getSelectionModel().getSelectedItem().toString().equals("MOVIMIENTOS")){

            //Validar si tiene algún registro, sino, cortar
            List<Integer> ids = List.of(1, 4, 5, 10, 11, 12, 13);
            List<ModelTransaccion> transacciones = servicio.traerTransaccionesPorTipoOperacion(Integer.parseInt(txtSocio.getText().trim()), true, ids);
            if (transacciones.size() == 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("ERROR");
                alert.setHeaderText("ADVERTENCIA DE REPORTE");
                alert.setContentText("EL SOCIO NO TIENE OPERACIONES ACTIVAS RELACIONADAS AL AHORRO.");
                alert.showAndWait();
                return;
            }


            try {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        try {

                            int numero = Integer.parseInt(txtSocio.getText());
                            Map pars = new HashMap<>();
                            pars.put("numSocio",txtSocio.getText().toString());
                            pars.put("nomSocio",txtNombre.getText().toString());
                            pars.put("saldoAhorro",formatoMoneda.format(servicio.traerAhorro(numero).getSaldo()));
                            pars.put("saldoCongelado",formatoMoneda.format(servicio.traerAhorro(numero).getSaldo_congelado()));
                            pars.put("saldoTotal",formatoMoneda.format(servicio.traerAhorro(numero).getSaldo_congelado() + servicio.traerAhorro(numero).getSaldo()));
                            pars.put("fechaImp",formatoFecha.format(servicio.traerFechaHoy()));

                            InputStream isRepo = getClass().getResourceAsStream("/Reports/movimientos_ahorro.jasper");
                            JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
                            Connection conn = DriverManager.getConnection(dotenv.get("DATABASE_URL"), dotenv.get("DATABASE_USERNAME"), dotenv.get("DATABASE_PASSWORD"));
                            JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, conn);


                            Platform.runLater(() -> {
                                JasperViewer viewer = new JasperViewer(jpRepo, false);
                                viewer.setSize(800, 600);
                                viewer.setAlwaysOnTop(true);
                                viewer.setLocationRelativeTo(null);
                                viewer.setTitle("MOVIMIENTOS");
                                viewer.setVisible(true);
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("ERROR");
                                alert.setHeaderText("ERROR AL GENERAR EL REPORTE");
                                alert.setContentText("OCURRIÓ UN ERROR: " + e.getMessage());
                                alert.showAndWait();
                            });
                        }
                        return null;
                    }
                };

                task.setOnSucceeded(e -> loadingStage.close());
                task.setOnFailed(e -> loadingStage.close());

                loadingStage.show();
                new Thread(task).start();

            } catch (Exception e) {
                e.printStackTrace();
            }






        } else {
            int index = tblCreditos.getSelectionModel().getSelectedIndex();
            if(index >= 0){


                try {
                    Task<Void> task = new Task<>() {
                        @Override
                        protected Void call() {
                            try {
                                Map pars = new HashMap<>();
                                pars.put("numSocio",txtSocio.getText().toString());
                                pars.put("monto",formatoMoneda.format(tblCreditos.getSelectionModel().getSelectedItem().getMonto()));
                                pars.put("tasa",formatoPorcentaje.format(tblCreditos.getSelectionModel().getSelectedItem().getTasa()));
                                pars.put("cuotas",(String.valueOf(tblCreditos.getSelectionModel().getSelectedItem().getPlazo())));
                                pars.put("empresa",nomEmpresa);
                                pars.put("nomSocio",txtNombre.getText().toString());
                                pars.put("fechaImpr",formatoFecha.format(servicio.traerFechaHoy()));
                                pars.put("folio",String.valueOf(tblCreditos.getSelectionModel().getSelectedItem().getId()));

                                InputStream isRepo = getClass().getResourceAsStream("/Reports/estado_cuenta.jasper");
                                JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
                                Connection conn = DriverManager.getConnection(dotenv.get("DATABASE_URL"), dotenv.get("DATABASE_USERNAME"), dotenv.get("DATABASE_PASSWORD"));
                                JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, conn);

                                Platform.runLater(() -> {
                                    JasperViewer viewer = new JasperViewer(jpRepo, false);
                                    viewer.setSize(800, 600);
                                    viewer.setAlwaysOnTop(true);
                                    viewer.setLocationRelativeTo(null);
                                    viewer.setTitle("ESTADO DE CUENTA");
                                    viewer.setVisible(true);
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("ERROR");
                                    alert.setHeaderText("ERROR AL GENERAR EL REPORTE");
                                    alert.setContentText("OCURRIÓ UN ERROR: " + e.getMessage());
                                    alert.showAndWait();
                                });
                            }
                            return null;
                        }
                    };

                    task.setOnSucceeded(e -> loadingStage.close());
                    task.setOnFailed(e -> loadingStage.close());

                    loadingStage.show();
                    new Thread(task).start();

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR AL INTENTAR GENERAR EL REPORTE");
                alert.setHeaderText("ERROR AL INTENTAR GENERAR EL REPORTE");
                alert.setContentText(
                        "SELECCIONE UN CREDITO");
                alert.showAndWait();
                return;
            }

        }
    }



}
