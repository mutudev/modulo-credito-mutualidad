package com.mutualidad.modulo_credito.Controllers;

import com.mutualidad.modulo_credito.Models.ModelEmpresa;
import com.mutualidad.modulo_credito.Services.Servicio;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class CreditosAutorizadosController implements Initializable {
    @FXML
    public ComboBox cmbEmpresa;

    @FXML
    public DatePicker dpFechainicio,dpFechafinal;

    @FXML
    public Button btnConfirmar;

    @Autowired
    private Servicio servicio;

    @FXML
    private TableView<Map<String, Object>> tblCreditos;

    @FXML
    private TableColumn<Map<String, Object>, String> colIdCredito;

    @FXML
    private TableColumn<Map<String, Object>, String> colEmpresa;

    @FXML
    private TableColumn<Map<String, Object>, String> colNumero;

    @FXML
    private TableColumn<Map<String, Object>, String> colNombre;

    @FXML
    private TableColumn<Map<String, Object>, String> colFecha;

    @FXML
    private TableColumn<Map<String, Object>, String> colMonto;

    @FXML
    private TableColumn<Map<String, Object>, String> colTipo;

    @FXML
    private TableColumn<Map<String, Object>, String> colTasa;

    NumberFormat formatoMXN = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        colIdCredito.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("IdCredito"))
                )
        );

        colEmpresa.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("empresa"))
                )
        );

        colNumero.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("num_socio"))
                )
        );

        colNombre.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("nombre_socio"))
                )
        );

        colFecha.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("fecha_desembolso"))
                )
        );

        colMonto.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("monto_desembolso"))
                )
        );

        colTipo.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("tipo"))
                )
        );


        colTasa.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("tasa"))
                )
        );


        dpFechainicio.setValue(servicio.traerFechaHoy());
        dpFechafinal.setValue(servicio.traerFechaHoy());

        List<ModelEmpresa> empresas = servicio.traerCatalogoEmpresas();

        for(ModelEmpresa empresa : empresas){
            cmbEmpresa.getItems().add(empresa.getNombre());
        }
        cmbEmpresa.getItems().add("AMBAS");
        cmbEmpresa.getSelectionModel().selectFirst();
    }


    @FXML
    public void traerRegistros(){

        String codEmpresa ="", nombreEmpresa = "";

        LocalDate fechainicio = dpFechainicio.getValue();
        LocalDate fechafin = dpFechafinal.getValue();
        if (cmbEmpresa.getSelectionModel().getSelectedItem().toString().equalsIgnoreCase("AMBAS")) {
            codEmpresa = "0000";
        } else {
            nombreEmpresa = cmbEmpresa.getSelectionModel().getSelectedItem().toString();
            codEmpresa = servicio.obtenerEmpresaXNombre(nombreEmpresa).getCodigo();
        }
        List<Map<String, Object>> registros =
                servicio.obtenerCreditos(fechainicio.toString(), fechafin.toString(), codEmpresa);

        tblCreditos.getItems().clear();
        tblCreditos.getItems().addAll(registros);

        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.initStyle(StageStyle.UNDECORATED);
        loadingStage.setAlwaysOnTop(true);

        VBox loadingPane = new VBox(20);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setPadding(new Insets(30));
        loadingPane.setStyle("-fx-background-color: white; -fx-border-color: #185754; -fx-border-width: 2;");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(100, 100);

        Label loadingLabel = new Label("Generando Créditos Autorizados...");
        loadingLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        loadingLabel.setTextFill(Color.web("#39577c"));

        loadingPane.getChildren().addAll(progressIndicator, loadingLabel);

        Scene loadingScene = new Scene(loadingPane, 400, 180);
        loadingStage.setScene(loadingScene);

        loadingStage.centerOnScreen();

        Task<Void> task = getVoidTask(loadingStage);

        loadingStage.show();
        new Thread(task).start();

    }



    private Task<Void> getVoidTask(Stage loadingStage) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {

                    String fechaInicioFormateada = formatter.format(dpFechainicio.getValue());
                    String fechaFinalFormateada = formatter.format(dpFechafinal.getValue());


                    InputStream direccionImg = null;
                    String titulo = "CRÉDITOS AUTORIZADOS DEL " + fechaInicioFormateada + " AL " + fechaFinalFormateada;

                    String empresaEmisora = "";

                    if (cmbEmpresa.getSelectionModel().getSelectedItem().toString().equalsIgnoreCase("AMBAS")) {
                        empresaEmisora = "MUTUALIDAD DOCE DE AGOSTO Y NUEVA GENERACIÓN DE UMÁN";
                    } else {
                        empresaEmisora = servicio.obtenerEmpresaXNombre(cmbEmpresa.getSelectionModel().getSelectedItem().toString()).getRazonSocial();
                    }

                    InputStream direccionImg2 = null;
                    if (cmbEmpresa.getSelectionModel().getSelectedItem().toString().equalsIgnoreCase("MUTUALIDAD DOCE DE AGOSTO")) {
                        direccionImg = getClass().getResourceAsStream("/assets/images/logo-mut.png");
                    } else if (cmbEmpresa.getSelectionModel().getSelectedItem().toString().equalsIgnoreCase("NUEVA GENERACION DE UMAN")){
                        direccionImg = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
                    }else{
                        direccionImg2 = getClass().getResourceAsStream("/assets/images/logo-mut.png");
                        direccionImg = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
                    }

                    double totalDesembolsado = 0.0;

                    ObservableList<Map<String, Object>> items = tblCreditos.getItems();

                    Collection<Map<String, ?>> datosTabla = new ArrayList<>();

                    for (Map<String, Object> item : items) {

                        Map<String, Object> fila = new HashMap<>();

                        Object montoObj = item.get("monto_desembolso");

                        if (montoObj != null) {

                            String montoStr = montoObj.toString()
                                    .replace("$", "")
                                    .replace(",", "")
                                    .trim();

                            totalDesembolsado += Double.parseDouble(montoStr);
                        }
                        fila.put("indice", String.valueOf(item.get("indice")));
                        fila.put("IdCredito", String.valueOf(item.get("IdCredito")));
                        fila.put("empresa", String.valueOf(item.get("empresa")));
                        fila.put("num_socio", String.valueOf(item.get("num_socio")));
                        fila.put("nombre_socio", String.valueOf(item.get("nombre_socio")));
                        fila.put("fecha_desembolso", String.valueOf(item.get("fecha_desembolso")));
                        fila.put("monto_desembolso", String.valueOf(item.get("monto_desembolso")));
                        fila.put("tipo", String.valueOf(item.get("tipo")));
                        fila.put("tasa", String.valueOf(item.get("tasa")));


                        datosTabla.add(fila);
                    }

                    JRMapCollectionDataSource dataSource =
                            new JRMapCollectionDataSource(datosTabla);

                    String totalFormateado = formatoMXN.format(totalDesembolsado);
                    Map<String, Object> parametros = new HashMap<>();

                    String fechaImp = formatter.format(servicio.traerFechaHoy());
                    parametros.put("titulo", titulo);
                    parametros.put("fechaImpre", fechaImp);
                    parametros.put("empresaEmisora", empresaEmisora);
                    parametros.put("totCreditos", totalFormateado);
                    parametros.put("LogoImg", direccionImg);
                    parametros.put("LogoImg2", direccionImg2);
                    parametros.put("dataSource", dataSource);

                    InputStream isRepo = getClass().getResourceAsStream("/Reports/creditos_autorizados.jasper");
                    JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
                    JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, parametros, new JREmptyDataSource());

                    Platform.runLater(() -> {
                        JasperViewer viewer = new JasperViewer(jpRepo, false);
                        viewer.setSize(800, 600);
                        viewer.setAlwaysOnTop(true);
                        viewer.setLocationRelativeTo(null);
                        viewer.setTitle("CARTERA DE CRÉDITO");
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
        return task;
    }

}
