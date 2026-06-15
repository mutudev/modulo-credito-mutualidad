package com.mutualidad.modulo_credito.Controllers;

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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class ProyeccionController  implements Initializable {





    @FXML
    private TextField txtMonto;

    @FXML
    private ComboBox cmbTipo, cmbPlazo, cmbTasa;

    @FXML
    private CheckBox chkIVA;

    @FXML
    private Button btnCalcular, btnLimpiar, btnImprimir;

    @FXML
    private TableView<Map<String, String>>tblProyeccion;

    @FXML
    private TableColumn<Map<String, String>, String> colCuota;

    @FXML
    private TableColumn<Map<String, String>, String> colFecha;

    @FXML
    private TableColumn<Map<String, String>, String>  colCapital;

    @FXML
    private TableColumn<Map<String, String>, String>  colIntereses;

    @FXML
    private TableColumn<Map<String, String>, String>  colIVA;

    @FXML
    private TableColumn<Map<String, String>, String>  colTotal;

    @FXML
    private TableColumn<Map<String, String>, String>  colSaldo;

    @FXML
    private TableColumn<Map<String, String>, String>  colDias;


    @Autowired
    private Servicio servicio;

    NumberFormat formatoMXN = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    DecimalFormat formatoPorcentaje = new DecimalFormat("#0.00'%'");


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        txtMonto.setTextFormatter(
                new TextFormatter<>(
                        change -> {
                            // Permite solo dígitos y el punto decimal
                            change.setText(change.getText().replaceAll("[^0-9.]", ""));

                            // Verifica si ya hay más de un punto decimal
                            if (change.getText().matches(".*\\..*\\..*")) {
                                change.setText(change.getText().substring(0, change.getText().lastIndexOf('.')));
                            }

                            return change;
                        }));


        colCapital.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("capital"))
        );

        colCuota.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("cuota"))
        );

        colDias.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("dias"))
        );

        colFecha.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("fecha"))
        );

        colIVA.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("iva"))
        );

        colIntereses.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("intereses"))
        );

        colTotal.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("total"))
        );
        colSaldo.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("saldo"))
        );

        List<Object[]> tasas;
        tasas = servicio.traerTasas();
        cmbTasa.getItems().clear();
        for (Object[] fila : tasas) {
            double tasa = ((Number) fila[1]).doubleValue();
            String tasaFormateada = formatoPorcentaje.format(tasa);
            cmbTasa.getItems().add(tasaFormateada);
        }

        cmbTasa.getSelectionModel().selectFirst();

        List<Object[]> plazos;
        plazos = servicio.traerPlazos();
        cmbPlazo.getItems().clear();
        for (Object[] fila : plazos) {
            String plazo = fila[1].toString();
            cmbPlazo.getItems().add(plazo);
        }


        cmbPlazo.getSelectionModel().selectFirst();


    }

    @FXML
    public void limpiar() {
        txtMonto.setText("");
        cmbTipo.getItems().clear();
        btnCalcular.setDisable(false);
        tblProyeccion.getItems().clear();
        btnImprimir.setDisable(true);
        cmbPlazo.getSelectionModel().selectFirst();
        cmbTasa.getSelectionModel().selectFirst();
        txtMonto.setTextFormatter(
                new TextFormatter<>(
                        change -> {
                            // Permite solo dígitos y el punto decimal
                            change.setText(change.getText().replaceAll("[^0-9.]", ""));

                            // Verifica si ya hay más de un punto decimal
                            if (change.getText().matches(".*\\..*\\..*")) {
                                change.setText(change.getText().substring(0, change.getText().lastIndexOf('.')));
                            }

                            return change;
                        }));
        txtMonto.setEditable(true);
    }

    @FXML
    public void cargarTipos() {

        List<Object[]> tiposCredito;
        tiposCredito = servicio.traerTiposCreditoCaso1();

        if (Double.parseDouble(txtMonto.getText().trim()) <= 5000) {
            cmbTipo.getItems().clear();
            for (Object[] fila : tiposCredito) {
                String nombreCredito = fila[2].toString();
                if (!nombreCredito.equalsIgnoreCase("CREDITO TRADICIONAL DE TIPO 2") && !nombreCredito.equalsIgnoreCase("CREDITO PACSE")) {
                    cmbTipo.getItems().add(nombreCredito);
                }
            }
        } else {
            cmbTipo.getItems().clear();
            for (Object[] fila : tiposCredito) {
                String nombreCredito = fila[2].toString();
                double montoCredito = Double.parseDouble(fila[6].toString());
                if (montoCredito > 5000) {
                    cmbTipo.getItems().add(nombreCredito);
                }
            }
        }

        String montoFormateado = formatoMXN.format(Double.parseDouble(txtMonto.getText().trim()));

        txtMonto.setTextFormatter(null);
        txtMonto.setText(montoFormateado);
        txtMonto.setEditable(false);
        cmbTipo.getSelectionModel().selectFirst();
    }

    @FXML
    public void cargarProyeccion() {

        tblProyeccion.getItems().clear();

        BigDecimal monto = BigDecimal.valueOf(
                Double.parseDouble(txtMonto.getText().replace("$", "").replace(",", ""))
        );

        int plazo = Integer.parseInt(cmbPlazo.getValue().toString());

        BigDecimal tasa = BigDecimal.valueOf(
                Double.parseDouble(cmbTasa.getValue().toString().replace("%", "")) / 100
        );

        BigDecimal iva = chkIVA.isSelected()
                ? BigDecimal.valueOf(0.16)
                : BigDecimal.ZERO;

        BigDecimal capitalMensual = monto
                .divide(BigDecimal.valueOf(plazo), 2, RoundingMode.HALF_UP);

        BigDecimal capitalMensualAmortizado = monto
                .subtract(capitalMensual.multiply(BigDecimal.valueOf(plazo - 1)))
                .setScale(2, RoundingMode.HALF_UP);

        LocalDate fechaBase = servicio.traerFechaHoy();
        int diaOriginal = fechaBase.getDayOfMonth();
        LocalDate fechaPagoAnterior = null;

        int numCuota = 1;
        int i = plazo;

        BigDecimal saldo = monto;

        while (i != 0) {

            LocalDate fechaTentativa = fechaBase.plusMonths(1);
            LocalDate fechaPago;

            int ultimoDiaMes = fechaTentativa.lengthOfMonth();

            fechaPago = (diaOriginal > ultimoDiaMes)
                    ? fechaTentativa.withDayOfMonth(ultimoDiaMes)
                    : fechaTentativa.withDayOfMonth(diaOriginal);

            // Guardar fecha natural ANTES del ajuste de domingo
            LocalDate fechaNatural = fechaPago;

            // Ajuste si cae en domingo
            if (fechaPago.getDayOfWeek() == DayOfWeek.SUNDAY) {
                fechaPago = fechaPago.plusDays(1);
            }

            // Días: desde fecha_base en cuota 1, desde fecha_pago anterior en las demás
            long dias = (numCuota == 1)
                    ? ChronoUnit.DAYS.between(fechaBase, fechaPago)
                    : ChronoUnit.DAYS.between(fechaPagoAnterior, fechaPago);

            // ===== INTERÉS =====
            BigDecimal interes = tasa
                    .multiply(BigDecimal.valueOf(12))
                    .divide(BigDecimal.valueOf(360), 10, RoundingMode.HALF_UP)
                    .multiply(saldo)
                    .multiply(BigDecimal.valueOf(dias))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal ivaInteres = interes
                    .multiply(iva)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal capital = (i > 1)
                    ? capitalMensual
                    : capitalMensualAmortizado;

            BigDecimal total = capital
                    .add(interes)
                    .add(ivaInteres);

            saldo = saldo.subtract(capital);

            String fechaFormateada = fechaPago.getDayOfMonth() + "/" + fechaPago.getMonthValue() + "/" + fechaPago.getYear();

            Map<String, String> fila = Map.of(
                    "cuota", String.valueOf(numCuota),
                    "fecha", fechaFormateada,
                    "capital", formatoMXN.format(capital),
                    "intereses", formatoMXN.format(interes),
                    "iva", formatoMXN.format(ivaInteres),
                    "total", formatoMXN.format(total),
                    "saldo", formatoMXN.format(saldo.max(BigDecimal.ZERO)),
                    "dias", String.valueOf(dias)
            );

            tblProyeccion.getItems().add(fila);

            // Base avanza con fecha NATURAL para anclar bien el día del siguiente mes
            fechaBase = fechaNatural;
            fechaPagoAnterior = fechaPago;
            numCuota++;
            i--;
        }

        btnImprimir.setDisable(false);
    }

    @FXML
    public void generarReporte(){
        ObservableList<Map<String, String>> items = tblProyeccion.getItems();
        Collection<Map<String, ?>> datosTabla = new ArrayList<>();

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

        Label loadingLabel = new Label("Generando reporte...");
        loadingLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        loadingLabel.setTextFill(Color.web("#39577c"));

        loadingPane.getChildren().addAll(progressIndicator, loadingLabel);

        Scene loadingScene = new Scene(loadingPane, 400, 180);
        loadingStage.setScene(loadingScene);

        loadingStage.centerOnScreen();

        try {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    try {

                        for (Map<String, String> item : items) {
                            Map<String, Object> fila = new HashMap<>();
                            fila.put("cuota", item.get("cuota"));
                            fila.put("fecha", item.get("fecha"));
                            fila.put("capital", item.get("capital"));
                            fila.put("intereses", item.get("intereses"));
                            fila.put("iva", item.get("iva"));
                            fila.put("total", item.get("total"));
                            fila.put("saldo", item.get("saldo"));
                            fila.put("dias", item.get("dias"));
                            datosTabla.add(fila);
                        }

                        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(datosTabla);

                        LocalDate fecha = servicio.traerFechaHoy();
                        String fechaForm = fecha.getDayOfMonth() + "/" + fecha.getMonthValue() + "/" + fecha.getYear();

                        Map<String, Object> parametros = new HashMap<>();
                        parametros.put("montoCredito", txtMonto.getText());
                        parametros.put("tasaCredito", cmbTasa.getValue().toString());
                        parametros.put("plazoCredito", cmbPlazo.getValue().toString());
                        parametros.put("asesor", LoginController.usuarioLoggeado);
                        parametros.put("fechaImpresion", fechaForm);
                        parametros.put("tipoCredito", cmbTipo.getValue().toString());
                        parametros.put("dataSource", dataSource);

                        InputStream isRepo = getClass().getResourceAsStream("/Reports/proyeccion.jasper");
                        JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
                        JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, parametros, new JREmptyDataSource());

                        Platform.runLater(() -> {
                            JasperViewer viewer = new JasperViewer(jpRepo, false);
                            viewer.setSize(800, 600);
                            viewer.setAlwaysOnTop(true);
                            viewer.setLocationRelativeTo(null);
                            viewer.setTitle("GRADUALIDAD");
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

    }








}
