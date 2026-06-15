package com.mutualidad.modulo_credito.Controllers;

import com.mutualidad.modulo_credito.Main;
import com.mutualidad.modulo_credito.Models.ModelCredito;
import com.mutualidad.modulo_credito.Models.ModelEmpresa;
import com.mutualidad.modulo_credito.Models.ModelSocio;
import com.mutualidad.modulo_credito.Services.Servicio;
import com.tenpisoft.n2w.MoneyConverters;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlTemplateLoader;
import net.sf.jasperreports.view.JasperViewer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Component
public class HistorialController implements Initializable {

    @FXML
    public TextField txtNumSocio;

    @FXML
    public ComboBox cmbEmpresa;

    @FXML
    public Button btnBuscar, btnLimpiar, btnImprimir;

    @FXML
    private ImageView imgBusqueda;

    @FXML
    private Label lblBusquedaFolio, lblNumYFolio;

    @FXML
    private TableView<ModelCredito> tblCreditos;

    @FXML
    private TableColumn<ModelCredito, String> colFolio;

    @FXML
    private TableColumn<ModelCredito, String> colMonto;

    @FXML
    private TableColumn<ModelCredito, String> colEstado;

    @FXML
    private TableColumn<ModelCredito, String> colFecha;

    @FXML
    private TableView<Map<String, Object>> tblHistorial;

    @FXML private TableColumn<Map<String, Object>, String> colFechaPHist;
    @FXML private TableColumn<Map<String, Object>, String> colFechaVenHist;
    @FXML private TableColumn<Map<String, Object>, String> colCuotaHist;
    @FXML private TableColumn<Map<String, Object>, String> colCapHist;
    @FXML private TableColumn<Map<String, Object>, String> colInterHist;
    @FXML private TableColumn<Map<String, Object>, String> colIvaHist;
    @FXML private TableColumn<Map<String, Object>, String> colMoraHist;
    @FXML private TableColumn<Map<String, Object>, String> colTotalHist;
    @FXML private TableColumn<Map<String, Object>, String> colSaldoHist;
    @FXML private TableColumn<Map<String, Object>, String> colIDHist;

    @Autowired
    private Servicio servicio;

    NumberFormat formatoMXN = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DecimalFormat formatoPorcentaje = new DecimalFormat("#0.00'%'");
    MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
    Dotenv dotenv = Dotenv.load();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colFolio.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getId())));

        colMonto.setCellValueFactory(data ->
                new SimpleStringProperty(
                        formatoMXN.format(data.getValue().getMonto())
                ));

        colEstado.setCellValueFactory(data -> {
            int status = data.getValue().getStatus();
            String texto;

            if (status == 2) {
                texto = "ACTIVO";
            } else if (status == 0) {
                texto = "PAGADO";
            } else if (status == 1) {
                texto = "CANCELADO";
            } else {
                texto = "DESCONOCIDO";
            }

            return new SimpleStringProperty(texto);
        });

        colFecha.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getFd().format(formatter)
                ));


        txtNumSocio.setTextFormatter(
                new TextFormatter<>(change -> {
                    // Permite solo dígitos
                    if (change.getText().matches("[0-9]*")) {
                        return change;
                    }
                    return null;
                })
        );

        colFechaPHist.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("fecha_pago"))));

        colFechaVenHist.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("fecha_vencimiento"))));

        colCuotaHist.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("cuota"))));

        colCapHist.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("capital_pagado"))));

        colInterHist.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("intereses"))));

        colIvaHist.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("iva"))));

        colMoraHist.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("mora"))));

        colTotalHist.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("total_pagado"))));

        colSaldoHist.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("saldo_credito"))));

        colIDHist.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("id_operacion"))));


        rellenarCombo();
    }

    @FXML
    public void cargarCreditos() {

        if (txtNumSocio.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("ERROR AL INTENTAR BUSCAR LOS CRÉDITOS DEL SOCIO");
            alert.setContentText(
                    "POR FAVOR, RELLENE TODOS LOS CAMPOS");
            alert.showAndWait();
            return;
        }

        tblCreditos.getItems().clear();
        ObservableList<ModelCredito> data = null;

        int numSocio = Integer.parseInt(txtNumSocio.getText().trim());
        txtNumSocio.setEditable(false);
        imgBusqueda.setVisible(false);
        btnBuscar.setDisable(true);
        List<ModelCredito> creditos = new ArrayList<>();

        if (lblBusquedaFolio.getText().equals("[F3] Buscar Por Folio")) {
            //Obtener codigo de la empresa
            ModelEmpresa empresa = servicio.obtenerEmpresaXNombre(cmbEmpresa.getSelectionModel().getSelectedItem().toString());
            String codigo = empresa.getCodigo();
            creditos = servicio.obtenerParaReimpresion(numSocio, codigo);
        } else {
            ModelCredito creditoSimple = servicio.encontrarCredito(numSocio);
            if (creditoSimple != null) {
                creditos.add(creditoSimple);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("SIN RESULTADOS");
                alert.setHeaderText("NO SE ENCONTRÓ NINGÚN CRÉDITO");
                alert.setContentText("NO EXISTEN CRÉDITOS CON DICHO FOLIO");
                alert.showAndWait();
                txtNumSocio.setEditable(true);
                btnBuscar.setDisable(false);
                return;
            }
        }


        data = FXCollections.observableArrayList(creditos);
        tblCreditos.setItems(data);
    }

    @FXML
    public void cargarConTecla() {
        cargarCreditos();
    }

    @FXML
    public void cargarAlCambiarCombo() {
        if (txtNumSocio.getText().isEmpty()) {
            return;
        } else {
            cargarCreditos();
        }
    }

    @FXML
    public void limpiarDatos() {
        txtNumSocio.setText("");
        tblCreditos.getItems().clear();
        txtNumSocio.setEditable(true);
        imgBusqueda.setVisible(true);
        btnBuscar.setDisable(false);
    }

    @FXML
    public void habilitarFolio(KeyEvent event) {
        if (event.getCode().equals(KeyCode.F3) && lblBusquedaFolio.getText().equals("[F3] Buscar Por Folio")) {
            imgBusqueda.setVisible(false);
            cmbEmpresa.setDisable(true);
            lblNumYFolio.setText("Folio:");
            lblBusquedaFolio.setText("[F3] Buscar Por Número de Socio");
            txtNumSocio.setText("");
            txtNumSocio.setEditable(true);
            cmbEmpresa.getSelectionModel().selectFirst();
            btnBuscar.setDisable(false);
        } else if (event.getCode().equals(KeyCode.F3) && lblBusquedaFolio.getText().equals("[F3] Buscar Por Número de Socio")) {
            imgBusqueda.setVisible(true);
            cmbEmpresa.setDisable(false);
            btnBuscar.setDisable(false);
            lblNumYFolio.setText("Número de Socio:");
            lblBusquedaFolio.setText("[F3] Buscar Por Folio");
            txtNumSocio.setText("");
            txtNumSocio.setEditable(true);
        }
    }

    public void cargarSocioBuscado(String numSocio) {
        txtNumSocio.setText(numSocio);
        cargarCreditos();
    }

    @FXML
    public void buscarSocioPorNombre() {
        try {
            Stage nuevaVentana = new Stage();
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/busquedaSocio.fxml"));
            fxml.setControllerFactory(Main.context::getBean);
            Scene nuevaEscena = new Scene(fxml.load());
            BusquedaController controlador = fxml.getController();
            controlador.setHistorialController(this);
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

    private void rellenarCombo() {
        List<ModelEmpresa> empresas;
        empresas = servicio.traerCatalogoEmpresas();
        cmbEmpresa.getItems().clear();
        for (ModelEmpresa fila : empresas) {
            String nombre = fila.getNombre();
            cmbEmpresa.getItems().add(nombre);
        }
        cmbEmpresa.getSelectionModel().select(1);
    }


    @FXML
    public void cargarHistorial() {
        ModelCredito creditoSeleccionado =
                tblCreditos.getSelectionModel().getSelectedItem();



        if (creditoSeleccionado == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("ERROR AL GENERAR EL REPORTE");
            alert.setContentText("POR FAVOR, SELECCIONE UN CRÉDITO PRIMERO");
            alert.showAndWait();
            return; // nada seleccionado
        }

        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.initStyle(StageStyle.UNDECORATED);
        loadingStage.setAlwaysOnTop(true);


        VBox loadingPane = new VBox(20);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setPadding(new Insets(30));
        loadingPane.setStyle("-fx-background-color: white; -fx-border-color: #185754; -fx-border-width: 2;");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(80, 80);

        Label loadingLabel = new Label("Cargando Historial...");
        loadingLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        loadingLabel.setTextFill(Color.web("#39577c"));

        loadingPane.getChildren().addAll(progressIndicator, loadingLabel);

        Scene loadingScene = new Scene(loadingPane, 400, 180);
        loadingStage.setScene(loadingScene);

        loadingStage.centerOnScreen();

        Double saldo = servicio.chequeoSaldo(tblCreditos.getSelectionModel().getSelectedItem().getId());

        if (saldo == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("ERROR AL GENERAR EL REPORTE");
            alert.setContentText("EL CRÉDITO AÚN NO TIENE TRANSACCIONES.");
            alert.showAndWait();
            return;
        }


        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {



                    String creditoId = String.valueOf(creditoSeleccionado.getId());
                    LocalDate fecha = servicio.traerFechaHoy();
                    LocalDate fechades = tblCreditos.getSelectionModel().getSelectedItem().getFd();
                    String fechadesform = fechades.getDayOfMonth() + "/" + fechades.getMonthValue() + "/" + fechades.getYear();
                    String fechaForm = fecha.getDayOfMonth() + "/" + fecha.getMonthValue() + "/" + fecha.getYear();
                    LocalDate fechav = tblCreditos.getSelectionModel().getSelectedItem().getFv();
                    String fechavform = fechav.getDayOfMonth() + "/" + fechav.getMonthValue() + "/" + fechav.getYear();

                    ModelSocio socio = servicio.traerSocioXNumero(Integer.parseInt(txtNumSocio.getText()));





                    String tasa = creditoSeleccionado.getTasa() + "%";
                    String monto = formatoMXN.format(creditoSeleccionado.getMonto());

                    Map<String, Object> parametros = new HashMap<>();
                    parametros.put("nomSocio", socio.getNombre()+ " "
                            + socio.getApellidoP() + " "
                            + socio.getApellidoM());
                    parametros.put("numSocio", txtNumSocio.getText());
                    parametros.put("monto", monto);
                    parametros.put("saldo",formatoMXN.format(saldo));
                    parametros.put("fechaHoy", fechaForm);
                    parametros.put("SUBREPORT_DIR",
                            getClass().getResource("/Reports/sub_reporte_historial.jasper").toString());
                    parametros.put("fechaDes", fechadesform);
                    parametros.put("fechaVec", fechavform);
                    parametros.put("tasa", tasa);
                    parametros.put("folio", String.valueOf(tblCreditos.getSelectionModel().getSelectedItem().getId()));

                    InputStream isRepo = getClass().getResourceAsStream("/Reports/historial.jasper");
                    JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
                    Connection conn = DriverManager.getConnection(dotenv.get("DATABASE_URL"), dotenv.get("DATABASE_USERNAME"), dotenv.get("DATABASE_PASSWORD"));
                    JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, parametros, conn);


                    Platform.runLater(() -> {
                        JasperViewer viewer = new JasperViewer(jpRepo, false);
                        viewer.setSize(800, 600);
                        viewer.setAlwaysOnTop(true);
                        viewer.setLocationRelativeTo(null);
                        viewer.setTitle("HISTORIAL DE PAGOS");
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
    }



}
