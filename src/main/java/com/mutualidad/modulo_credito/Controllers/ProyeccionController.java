package com.mutualidad.modulo_credito.Controllers;

import com.mutualidad.modulo_credito.Services.Servicio;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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

        LocalDate fechaBase = LocalDate.now();
        int diaOriginal = fechaBase.getDayOfMonth();

        boolean huboAjusteAnterior = false;

        int numCuota = 1;
        int i = plazo;

        BigDecimal saldo = monto;

        while (i != 0) {

            boolean ajusteDomingo = false;

            LocalDate fechaTentativa = fechaBase.plusMonths(1);
            LocalDate fechaPago;

            int ultimoDiaMes = fechaTentativa.lengthOfMonth();

            fechaPago = (diaOriginal > ultimoDiaMes)
                    ? fechaTentativa.withDayOfMonth(ultimoDiaMes)
                    : fechaTentativa.withDayOfMonth(diaOriginal);

            int mesPago = fechaPago.getMonthValue();
            int anioPago = fechaPago.getYear();

            int dias = (mesPago == 1)
                    ? YearMonth.of(anioPago - 1, 12).lengthOfMonth()
                    : YearMonth.of(anioPago, mesPago - 1).lengthOfMonth();

            if (huboAjusteAnterior) {
                dias--;
                huboAjusteAnterior = false;
            }

            if (fechaPago.getDayOfWeek() == DayOfWeek.SUNDAY) {
                fechaPago = fechaPago.plusDays(1);
                ajusteDomingo = true;
                huboAjusteAnterior = true;
            }

            if (ajusteDomingo) {
                dias++;
            }

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
                    "fecha", fechaFormateada.toString(),
                    "capital", formatoMXN.format(capital),
                    "intereses", formatoMXN.format(interes),
                    "iva", formatoMXN.format(ivaInteres),
                    "total", formatoMXN.format(total),
                    "saldo", formatoMXN.format(saldo.max(BigDecimal.ZERO)),
                    "dias", String.valueOf(dias)
            );

            tblProyeccion.getItems().add(fila);

            fechaBase = fechaPago;
            numCuota++;
            i--;
        }

        btnImprimir.setDisable(false);
    }

    @FXML
    public void generarReporte(){
        ObservableList<Map<String, String>> items = tblProyeccion.getItems();
        Collection<Map<String, ?>> datosTabla = new ArrayList<>();

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

        LocalDateTime fecha = LocalDateTime.now();
        String fechaForm = fecha.getDayOfMonth() + "/" + fecha.getMonthValue() + "/" + fecha.getYear();

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("montoCredito", txtMonto.getText());
        parametros.put("tasaCredito", cmbTasa.getValue().toString());
        parametros.put("plazoCredito", cmbPlazo.getValue().toString());
        parametros.put("asesor", LoginController.usuarioLoggeado);
        parametros.put("fechaImpresion", fechaForm);
        parametros.put("tipoCredito", cmbTipo.getValue().toString());
        parametros.put("dataSource", dataSource);

        try {
            InputStream isRepo = getClass().getResourceAsStream("/Reports/proyeccion.jasper");
            JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);

            // ✅ CAMBIAR ESTO - Usar JREmptyDataSource para el reporte principal
            JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, parametros, new JREmptyDataSource());

            JasperViewer viewer = new JasperViewer(jpRepo, false);
            viewer.setAlwaysOnTop(true);
            viewer.setSize(800, 600);
            viewer.setLocationRelativeTo(null);
            viewer.setTitle("PROYECCIÓN DE CRÉDITO");
            viewer.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }








}
