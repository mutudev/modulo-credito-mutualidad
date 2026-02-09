package com.mutualidad.modulo_credito.Controllers;

import com.mutualidad.modulo_credito.Models.ModelEmpresa;
import com.mutualidad.modulo_credito.Models.ModelSocio;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Component
public class CarterasController implements Initializable {

    @Autowired
    private Servicio servicio;

    NumberFormat formatoMXN = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String[] opciones = {"VIGENTES", "ATRASADSOS", "VENCIDOS" , "TODOS"};
    @FXML
    private ComboBox cmbEmpresas, cmbEstado;

    @FXML
    private TableView<Map<String, Object>> tblCarteras;

    @FXML
    private TableColumn<Map<String, Object>, String> colID;

    @FXML
    private TableColumn<Map<String, Object>, String> colEmpresa;

    @FXML
    private TableColumn<Map<String, Object>, String>  colSocio;

    @FXML
    private TableColumn<Map<String, Object>, String>  colNombre;

    @FXML
    private TableColumn<Map<String, Object>, String>  colDesembolso;

    @FXML
    private TableColumn<Map<String, Object>, String>  colMonto;

    @FXML
    private TableColumn<Map<String, Object>, String>  colCuotaPen;

    @FXML
    private TableColumn<Map<String, Object>, String>  colCuotas;

    @FXML
    private TableColumn<Map<String, Object>, String>  colTasa;

    @FXML
    private TableColumn<Map<String, Object>, String>  colSaldo;

    @FXML
    private TableColumn<Map<String, Object>, String>  colUltimaPago;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        colID.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("IdCredito"))
                )
        );

        colEmpresa.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("empresa"))
                )
        );

        colSocio.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("num_socio"))
                )
        );

        colNombre.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("nombre_socio"))
                )
        );

        colDesembolso.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("fecha_desembolso"))
                )
        );

        colMonto.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("monto_desembolso"))
                )
        );

        colCuotaPen.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("fecha_cuota_pendiente"))
                )
        );

        colCuotas.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("cuotas"))
                )
        );

        colTasa.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("tasa"))
                )
        );

        colSaldo.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("saldo_credito"))
                )
        );

        colUltimaPago.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(data.getValue().get("fecha_ultima_pago"))
                )
        );


        List<ModelEmpresa> empresas = servicio.traerCatalogoEmpresas();

        for(ModelEmpresa empresa : empresas){
            cmbEmpresas.getItems().add(empresa.getNombre());
        }
        cmbEmpresas.getItems().add("AMBAS");
        cmbEmpresas.getSelectionModel().selectFirst();
        cmbEstado.getItems().addAll(opciones);
        cmbEstado.getSelectionModel().selectFirst();

    }

    @FXML
    public void traerRegistros() {
        int opcion = cmbEstado.getSelectionModel().getSelectedIndex();
        String nombreEmpresa = "";
        String codEmpresa = "";
        if (cmbEmpresas.getSelectionModel().getSelectedItem().toString().equalsIgnoreCase("AMBAS")) {
            codEmpresa = "0000";
        } else {
            nombreEmpresa = cmbEmpresas.getSelectionModel().getSelectedItem().toString();
            codEmpresa = servicio.obtenerEmpresaXNombre(nombreEmpresa).getCodigo();
        }

        List<Map<String, Object>> registros =
                servicio.obtenerCarteras(opcion, codEmpresa);

        tblCarteras.getItems().clear();
        tblCarteras.getItems().addAll(registros);

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

        Label loadingLabel = new Label("Generando Cartera de Créditos...");
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

                    InputStream direccionImg = null;
                    String tipoCartera = "CRÉDITOS " + cmbEstado.getSelectionModel().getSelectedItem().toString();

                    String empresaEmisora = "";

                    if (cmbEmpresas.getSelectionModel().getSelectedItem().toString().equalsIgnoreCase("AMBAS")) {
                        empresaEmisora = "MUTUALIDAD DOCE DE AGOSTO Y NUEVA GENERACIÓN DE UMÁN";
                    } else {
                        empresaEmisora = servicio.obtenerEmpresaXNombre(cmbEmpresas.getSelectionModel().getSelectedItem().toString()).getRazonSocial();
                    }

                    String fechaImp = formatter.format(LocalDate.now());
                    InputStream direccionImg2 = null;
                    if (cmbEmpresas.getSelectionModel().getSelectedItem().toString().equalsIgnoreCase("MUTUALIDAD DOCE DE AGOSTO")) {
                        direccionImg = getClass().getResourceAsStream("/assets/images/logo-mut.png");
                    } else if (cmbEmpresas.getSelectionModel().getSelectedItem().toString().equalsIgnoreCase("NUEVA GENERACION DE UMAN")){
                        direccionImg = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
                    }else{
                        direccionImg2 = getClass().getResourceAsStream("/assets/images/logo-mut.png");
                        direccionImg = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
                    }

                    double totalDesembolsado = 0.0;

                    ObservableList<Map<String, Object>> items = tblCarteras.getItems();

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
                        fila.put("fecha_cuota_pendiente", String.valueOf(item.get("fecha_cuota_pendiente")));
                        fila.put("cuotas", String.valueOf(item.get("cuotas")));
                        fila.put("tasa", String.valueOf(item.get("tasa")));
                        fila.put("saldo_credito", String.valueOf(item.get("saldo_credito")));
                        fila.put("fecha_ultima_pago", String.valueOf(item.get("fecha_ultima_pago")));


                        datosTabla.add(fila);
                    }

                    JRMapCollectionDataSource dataSource =
                            new JRMapCollectionDataSource(datosTabla);

                    String totalFormateado = formatoMXN.format(totalDesembolsado);
                    Map<String, Object> parametros = new HashMap<>();

                    parametros.put("tipoCartera", tipoCartera);
                    parametros.put("fechaImpre", fechaImp);
                    parametros.put("empresaEmisora", empresaEmisora);
                    parametros.put("totCreditos", totalFormateado);
                    parametros.put("LogoImg", direccionImg);
                    parametros.put("LogoImg2", direccionImg2);
                    parametros.put("dataSource", dataSource);

                    InputStream isRepo = getClass().getResourceAsStream("/Reports/carteras.jasper");
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
