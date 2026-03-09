package com.mutualidad.modulo_credito.Controllers;

import com.mutualidad.modulo_credito.Main;
import com.mutualidad.modulo_credito.Models.ModelCredito;
import com.mutualidad.modulo_credito.Models.ModelEmpresa;
import com.mutualidad.modulo_credito.Models.ModelSocio;
import com.mutualidad.modulo_credito.Models.ModelSolicitud;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

@Component
public class ReimpresionContratosController implements Initializable {

    @FXML
    private TextField txtNumSocio;

    @FXML
    private ComboBox cmbEmpresa;

    @FXML
    private Button btnBuscar, btnLimpiar, btnPagare, btnPlan, btnContrato;

    @FXML
    private ImageView imgBusqueda;

    @FXML
    private Label lblBusquedaFolio, lblNumYFolio;

    @FXML
    private TableView<Map<String, String>>tblProyeccion;

    @FXML
    private TableColumn<Map<String, String>, String> colCuota;

    @FXML
    private TableColumn<Map<String, String>, String> colFechaPro;

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
    private TableView<ModelCredito> tblCreditos;

    @FXML
    private TableColumn<ModelCredito, String> colFolio;

    @FXML
    private TableColumn<ModelCredito, String> colMonto;

    @FXML
    private TableColumn<ModelCredito, String> colEstado;

    @FXML
    private TableColumn<ModelCredito, String> colFecha;

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

        colCapital.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("capital"))
        );

        colCuota.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("cuota"))
        );


        colFechaPro.setCellValueFactory(
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

    @FXML
    public void mostrarPagare() {

        int indexSeleccionado = tblCreditos.getSelectionModel().getSelectedIndex();
        if (indexSeleccionado < 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("ERROR AL INTENTAR REIMPRIMIR CONTRATOS");
            alert.setContentText(
                    "POR FAVOR, SELECCIONE UN CRÉDITO PRIMERO");
            alert.showAndWait();
            return;
        }

        ModelCredito primeraFila = (ModelCredito) tblCreditos.getItems().get(indexSeleccionado);
        ModelCredito credito = servicio.encontrarCredito(primeraFila.getId());

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

        Label loadingLabel = new Label("Generando pagaré...");
        loadingLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        loadingLabel.setTextFill(Color.web("#39577c"));

        loadingPane.getChildren().addAll(progressIndicator, loadingLabel);

        Scene loadingScene = new Scene(loadingPane, 300, 150);
        loadingStage.setScene(loadingScene);

        loadingStage.centerOnScreen();


        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {

                    String dineroLetras = converter.asWords(BigDecimal.valueOf(credito.getMonto()));
                    LocalDate hoy = LocalDate.now();
                    int dia = hoy.getDayOfMonth();
                    String pluralDia = (dia == 1) ? "día" : "días";
                    String mes = hoy.getMonth()
                            .getDisplayName(TextStyle.FULL, new Locale("es", "MX"));
                    int anio = hoy.getYear();
                    String suscrito = String.format(
                            "Suscrito en la Ciudad de UMAN, YUCATAN a los %d %s del mes %s de %d.",
                            dia,
                            pluralDia,
                            mes,
                            anio
                    );

                    ModelSocio socio = servicio.traerSocioXNumero(credito.getSocio());
                    String nombreSocio = socio.getPrimerNom() + " " + socio.getSegundoNom() + " " + socio.getApellidoP() + " " + socio.getApellidoM();

                    Map pars = new HashMap<>();
                    pars.put("folio", String.valueOf(credito.getId()));
                    pars.put("numSocio", String.valueOf(credito.getSocio()));

                    pars.put("fechaVencimiento", credito.getFv().getDayOfMonth() + "/" + credito.getFv().getMonthValue() + "/" + credito.getFv().getYear());
                    pars.put("importe", formatoMXN.format(credito.getMonto()));
                    pars.put("parrafo1", "Por el presente Pagaré reconozco deber y me obligo a pagar incondicionalmente al día de su " +
                            "vencimiento en esta ciudad de UMAN, YUCATAN a la orden de " +
                            servicio.traerEmpresa(credito.getEmpresa()).getRazonSocial() + " la cantidad de: " +
                            formatoMXN.format(credito.getMonto()) +
                            " (Son: " + dineroLetras.toUpperCase() + " M.N.). " +
                            "Valor que he recibido en efectivo a mi entera satisfacción.");
                    pars.put("parrafoOrdinarios",
                            "I. Intereses Ordinarios. El Suscriptor se obliga incondicionalmente a pagar intereses " +
                                    "ordinarios sobre el monto principal del presente Pagaré, mismo que será por una" +
                                    " tasa mensual del " + formatoPorcentaje.format(credito.getTasa()) + " sobre saldos insolutos y que se calcularán desde" +
                                    " la fecha de Suscripción y hasta la Fecha de Vencimiento," +
                                    " pagaderos precisamente en la Fecha de Vencimiento.");

                    pars.put("suscrito", suscrito);
                    pars.put("nombreDeudor", "Nombre: " + nombreSocio +"\nDirección: CALLE " + socio.getCalle() + " " + socio.getCruzamiento() + " UMÁN, YUCATÁN.");
                    pars.put("mostrarLinea0", true);

                    ModelSolicitud solicitud = servicio.obtenerSoliXId(credito.getSolicitud_id());


                    int MAX_AVALES = 5;
                    if(!solicitud.getRiesgo()){
                        for (int i = 1; i <= MAX_AVALES; i++) {
                            pars.put("avalTitulo" + i, "");
                            pars.put("nombreAval" + i, "");
                            pars.put("firmaAval" + i, "");
                            pars.put("mostrarLinea" + i, false);
                        }
                    } else {
                        //Si si es de riesgo
                        //Saber cuantos avales son
                        int numAvales = servicio.traerNumAvales(credito.getId());
                        List<Object[]> avales = servicio.traerAvales(credito.getId());


                        for (int i = 1; i <= MAX_AVALES; i++) {
                            pars.put("avalTitulo" + i, "");
                            pars.put("nombreAval" + i, "");
                            pars.put("firmaAval" + i, "");
                            pars.put("mostrarLinea" + i, false);
                        }

                        for (int i = 0; i < numAvales && i < MAX_AVALES; i++) {

                            Object[] aval = avales.get(i);
                            int idx = i + 1;

                            String nombreAval = aval[2] != null ? aval[2].toString() : "";
                            String direccionAvalEnviar = "";

                            Integer numSocio = aval[1] != null ? Integer.valueOf(aval[1].toString()) : null;

                            if (numSocio == null || numSocio == 0) {
                                direccionAvalEnviar = aval[10] != null ? aval[10].toString() : "";
                            } else {
                                List<Object[]> socioParaDireccion = servicio.traerDetalleSocio(numSocio);
                                if (!socioParaDireccion.isEmpty()) {
                                    Object[] fila = socioParaDireccion.get(0);
                                    direccionAvalEnviar = fila[5] != null
                                            ? fila[5].toString().toUpperCase()
                                            : "";
                                }
                            }

                            pars.put("avalTitulo" + idx, "Aval:");
                            pars.put(
                                    "nombreAval" + idx,
                                    "Nombre: " + nombreAval + "\nDirección: CALLE " + direccionAvalEnviar
                            );
                            pars.put("firmaAval" + idx, "FIRMA");
                            pars.put("mostrarLinea" + idx, true);
                        }
                    }

                    pars.put("elabora", "ELABORA: " + credito.getAsesor());
                    pars.put("revisa", "REVISA: "+ "CONTABILIDAD");


                    InputStream isRepo = getClass().getResourceAsStream("/Reports/pagare.jasper");
                    JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
                    JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());

                    Platform.runLater(() -> {
                        JasperViewer viewer = new JasperViewer(jpRepo, false);
                        viewer.setAlwaysOnTop(true);
                        viewer.setSize(800, 600);
                        viewer.setLocationRelativeTo(null);
                        viewer.setTitle("PAGARE DE CRÉDITO");
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
        new Thread(task).start();
    }

    public void cargarProyeccion() {

        tblProyeccion.getItems().clear();

        //Obtener las cosas del crédito
        int indexSeleccionado = tblCreditos.getSelectionModel().getSelectedIndex();
        ModelCredito primeraFila = (ModelCredito) tblCreditos.getItems().get(indexSeleccionado);
        ModelCredito credito = servicio.encontrarCredito(primeraFila.getId());

        BigDecimal monto = BigDecimal.valueOf(
                credito.getMonto()
        );

        int plazo = credito.getPlazo();

        BigDecimal tasa = credito.getTasa().divide(BigDecimal.valueOf(100.0));


        BigDecimal iva = BigDecimal.valueOf(credito.getIva()).divide(BigDecimal.valueOf(100.0));

        BigDecimal capitalMensual = monto
                .divide(BigDecimal.valueOf(plazo), 2, RoundingMode.HALF_UP);

        BigDecimal capitalMensualAmortizado = monto
                .subtract(capitalMensual.multiply(BigDecimal.valueOf(plazo - 1)))
                .setScale(2, RoundingMode.HALF_UP);



        LocalDate fechaBase = credito.getFd();
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
                    "saldo", formatoMXN.format(saldo.max(BigDecimal.ZERO))
            );

            tblProyeccion.getItems().add(fila);

            fechaBase = fechaPago;
            numCuota++;
            i--;
        }
    }

    @FXML
    public void mostrarTablaAmortizacion() {

        int indexSeleccionado = tblCreditos.getSelectionModel().getSelectedIndex();
        if (indexSeleccionado < 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("ERROR AL INTENTAR REIMPRIMIR CONTRATOS");
            alert.setContentText("POR FAVOR, SELECCIONE UN CRÉDITO PRIMERO");
            alert.showAndWait();
            return;
        }

        ModelCredito primeraFila = (ModelCredito) tblCreditos.getItems().get(indexSeleccionado);
        ModelCredito credito = servicio.encontrarCredito(primeraFila.getId());


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

        Label loadingLabel = new Label("Generando Plan de Pagos...");
        loadingLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        loadingLabel.setTextFill(Color.web("#39577c"));

        loadingPane.getChildren().addAll(progressIndicator, loadingLabel);

        Scene loadingScene = new Scene(loadingPane, 300, 150);
        loadingStage.setScene(loadingScene);

        loadingStage.centerOnScreen();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    String montoAutorizado = formatoMXN.format(credito.getMonto());
                    String tasa = formatoPorcentaje.format(credito.getTasa());
                    String plazo = String.valueOf(credito.getPlazo()) + " Meses";
                    String numeroSocio = String.valueOf(credito.getSocio());
                    ModelSocio socio = servicio.traerSocioXNumero(credito.getSocio());
                    String nombre = socio.getPrimerNom() + " " + socio.getSegundoNom() + " " + socio.getApellidoP() + " " + socio.getApellidoM();
                    String tipo = servicio.encontrarTipoCreditoConId(credito.getTipo_credito()).getNombre();
                    String empresa = servicio.traerEmpresa(credito.getEmpresa()).getNombre();

                    String estado = "";
                    if (credito.getStatus() == 2) {
                        estado = "VIGENTE";
                    } else if (credito.getStatus() == 1) {
                        estado = "CANCELADO";
                    } else {
                        estado = "PAGADO";
                    }

                    cargarProyeccion();

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
                        datosTabla.add(fila);
                    }

                    JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(datosTabla);

                    LocalDate fecha = LocalDate.now();
                    Map pars = new HashMap<>();
                    pars.put("CreditoID", credito.getId());
                    pars.put("nombreSocio", nombre);
                    pars.put("numSocio", numeroSocio);
                    pars.put("montoCredito", montoAutorizado);
                    pars.put("tasaCredito", tasa);
                    pars.put("plazoCredito", plazo);
                    pars.put("asesor", credito.getAsesor());
                    pars.put("fechaImpresion", fecha.format(formatter));
                    pars.put("estado", estado);
                    pars.put("tipoCredito", tipo);
                    pars.put("empresaEmisora", empresa);
                    pars.put("dataSource", dataSource);


                    InputStream isRepo = getClass().getResourceAsStream("/Reports/tabla_amortizacion_hist.jasper");
                    JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);

                    JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());



                    Platform.runLater(() -> {
                        JasperViewer viewer = new JasperViewer(jpRepo, false);
                        viewer.setAlwaysOnTop(true);
                        viewer.setSize(800, 600);
                        viewer.setLocationRelativeTo(null);
                        viewer.setTitle("PLAN DE PAGOS");
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

    @FXML
    public void mostrarContrato() {

        int indexSeleccionado = tblCreditos.getSelectionModel().getSelectedIndex();
        if (indexSeleccionado < 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("ERROR AL INTENTAR REIMPRIMIR CONTRATOS");
            alert.setContentText(
                    "POR FAVOR, SELECCIONE UN CRÉDITO PRIMERO");
            alert.showAndWait();
            return;
        }

        ModelCredito primeraFila = (ModelCredito) tblCreditos.getItems().get(indexSeleccionado);

        //Obtener empresa por la que sale
        ModelCredito credito = servicio.encontrarCredito(primeraFila.getId());

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

        Label loadingLabel = new Label("Generando contrato...");
        loadingLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        loadingLabel.setTextFill(Color.web("#39577c"));

        loadingPane.getChildren().addAll(progressIndicator, loadingLabel);

        Scene loadingScene = new Scene(loadingPane, 300, 150);
        loadingStage.setScene(loadingScene);

        loadingStage.centerOnScreen();


        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {

                    String encabezado = "";
                    String avalesDetalle = "";
                    String aviso = "";
                    String html = "";
                    String firmas = "";

                    //Obtener a carli
                    String apoderado = dotenv.get("APODERADO");
                    String apoderadoAbreviado = dotenv.get("APODERADO_ABREVIADO");

                    String empresaEmisoraCompleto = servicio.traerEmpresa(credito.getEmpresa()).getRazonSocial();
                    String abreviaturas = servicio.traerEmpresa(credito.getEmpresa()).getNombre();
                    LocalDate fecha = LocalDate.now();
                    int dia = fecha.getDayOfMonth();

                    String mes = fecha.getMonth()
                            .getDisplayName(TextStyle.FULL, new Locale("es", "MX"));
                    String anioLetrasAnterior = converter.asWords(BigDecimal.valueOf(fecha.getYear()));
                    String anioLetras = anioLetrasAnterior.substring(0, anioLetrasAnterior.length() - 11);
                    //Este no importa mucho de construir
                    String declaraciones =  "<br/><br/>" + "<div style='text-align:center;'><b>DECLARACIONES</b></div>" + "<br/>";

                    String segundoParrafoEncabezado =  "<b>1. </b>" + apoderadoAbreviado + ", como Apoderado Legal de " +
                            "<b>“"+ empresaEmisoraCompleto +"”</b>, también conocida como y sus correspondientes " +
                            "abreviaturas <b>“"+abreviaturas+"”</b>, manifiesta ser "+ dotenv.get("TITULO_APODERADO") + ", " +
                            "mayor de edad legal, continúa declarando tener como domicilio el predio marcado con el " +
                            "numero " + dotenv.get("DIREC_APODERADO") + ", en la ciudad de "+ dotenv.get("CIUDAD_APODERADO") + ", México.";

                    String prestamista = "<br/><br/>" +

                            "<b>Prestamista</b>" +

                            "<br/><br/>";

                    String aval = "";
                    String clausulas =  "<br/><br/>" +

                            "<div style='text-align:center;'><b>CLAUSULAS</b></div>" +

                            "<br/>";

                    String clausula1 = "";

                    String clausula2 = "<br/><br/>" +

                            "<b>II.- </b>"+ empresaEmisoraCompleto + " y sus correspondientes abreviaturas " +
                            "<b>“"+ abreviaturas + "”</b>, concederá los préstamos que se le requieran, " +
                            "siempre que se desarrolle de la siguiente manera: proporcionando a los deudores la " +
                            "ubicación correcta del domicilio en el cual se le haga las respectivas notificaciones " +
                            "en caso de demora de pago del préstamo establecido." +

                            "<br/><br/>";

                    String clausula3 = "<b>III.- </b> El domicilio señalado, será en común para notificar a todos y cada uno de los " +
                            "deudores solidarios, así como para todo lo relativo al cumplimiento de pago del préstamo " +
                            "solicitado, con renuncia expresa de señalar otro domicilio y vecindad." +

                            "<br/><br/>";

                    String clausula4 = "";

                    String clausula5 = "";
                    String clausula6 = "<b>VI.- </b> Los comparecientes se someten de una manera expresa a la jurisdicción y " +
                            "competencia de los jueces y tribunales de esta ciudad de Umán, Yucatán, México, " +
                            "renunciando a cualquier fuero que pudiera corresponderles por razón de su origen o " +
                            "domicilio." +

                            "<br/><br/>";

                    String enteradasLasPartes =
                            "Enteradas las partes y conformes con el contenido y alcance legal del presente contrato, " +
                                    "lo firman el día " + dia + " de " + mes + " de " +
                                    anioLetras + "." +
                                    "</body></html>";

                    String encabezadoFirmas = "<html> <body>" + "<br/><br/>";

                    String footerFirmas = "</body></html";

                    String firmaAcreedor = "<center><b>EL ACREEDOR</b></center>" +

                            "<br/><br/><br/>" +

                            "<center>_____________________________<br/>" +
                            "<b>"+ dotenv.get("APODERADO") + "</b></center>" +

                            "<br/><br/>";

                    String losDeudores = "<center><b>LOS DEUDORES</b></center>" +

                            "<br/><br/><br/>";
                    String firmaAvalesYPrestamista = "";

                    String avisoPrivacidad =
                            "<html>" +
                                    "<body>" +

                                    "<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>" +
                                    "<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>" +
                                    "<br/>" +

                                    "<span style='font-size:8pt;'>Aviso de privacidad para las operaciones con espacios reducidos (datos deben de incluirse). " +
                                    "Nueva Generación de Umán Asociación Civil, con domicilio en calle veintitrés número ciento " +
                                    "ochenta y tres letra “B” de la ciudad de Umán, Estado de Yucatán; utilizará sus datos " +
                                    "personales aquí recabados para la obtención del crédito solicitado. Para mayor información " +
                                    "acerca del tratamiento y de los derechos que puede hacer valer, usted puede acceder al " +
                                    "aviso de privacidad completo a través de la publicación y exhibición permanente que se " +
                                    "realiza en la entrada o recepción del domicilio antes citado de esta Asociación.</span>" +

                                    "</body>" +
                                    "</html>";

                    String direccionSocioPrestamista = "";
                    String trabajoPrestamista = "";
                    String estadoCivilPrestamista = "";
                    String soloColoniaPrestamista = "";

                    List<Object[]> socioPrestamista = servicio.traerDetalleSocio(credito.getSocio());
                    if (!socioPrestamista.isEmpty()) {
                        Object[] fila = socioPrestamista.get(0);
                        direccionSocioPrestamista = fila[5] != null
                                ? fila[5].toString().toUpperCase()
                                : "";
                        trabajoPrestamista = fila[10] != null ? fila[10].toString().toUpperCase() : "";
                        estadoCivilPrestamista = fila[7] != null ? fila[7].toString().toUpperCase() : "";
                        soloColoniaPrestamista = fila[4] != null ? fila[4].toString().toUpperCase() : "";
                    }

                    ModelSocio socio = servicio.traerSocioXNumero(credito.getSocio());
                    String nombreSocio = socio.getPrimerNom() + " " + socio.getSegundoNom() + " " + socio.getApellidoP() + " " + socio.getApellidoM();
                    String prestamistaParrafo = "<b>2.</b> "+ nombreSocio + ", manifiesta ser <b>" + estadoCivilPrestamista + "</b>, CON OFICIO " +
                            "<b>" + trabajoPrestamista + "</b>, continúa declarando tener como domicilio el predio ubicado " +
                            "en la calle "+ direccionSocioPrestamista +", de la Col. "+ soloColoniaPrestamista  +", " +
                            "en la ciudad de UMAN, YUCATAN, México.";

                    ModelSolicitud solicitud = servicio.obtenerSoliXId(credito.getSolicitud_id());

                    //Evaluar si es de riesgo o no para el primer parráfo, saber si paso al deudor y a los avales o solo al primero
                    if (solicitud.getRiesgo()) {

                        aval = "<br/><br/>" +

                                "<b>Aval</b>" +

                                "<br/><br/>";

                        //Obtener a los avales
                        int numAvales = servicio.traerNumAvales(credito.getId());
                        List<Object[]> avales = servicio.traerAvales(credito.getId());

                        String avalesJuntosEncabezado = nombreSocio;
                        firmaAvalesYPrestamista = "<center>_____________________________<br/>" +
                                "<b>"+ nombreSocio +  "</b></center>"+"<br/><br/><br/>";
                        for (int i = 0; i < numAvales; i++) {
                            Object[] avalIterar = avales.get(i);
                            String nombreAval = avalIterar[2] != null ? avalIterar[2].toString() : "";
                            String parentescoAval = avalIterar[4] != null ? avalIterar[4].toString() : "";
                            String direccionAval = avalIterar[10] != null ? avalIterar[10].toString() : "";
                            int numSocioAVal = Integer.parseInt(avalIterar[1].toString()) != 0 ? Integer.parseInt(avalIterar[1].toString()) : 0;

                            if (i == numAvales - 1) {
                                // última iteración
                                avalesJuntosEncabezado = avalesJuntosEncabezado + " Y " + nombreAval;
                                firmaAvalesYPrestamista = firmaAvalesYPrestamista + "<center>_____________________________<br/>" +
                                        "<b>"+ nombreAval + "</b></center>";
                            } else {
                                avalesJuntosEncabezado = avalesJuntosEncabezado + ", " + nombreAval;
                                firmaAvalesYPrestamista = firmaAvalesYPrestamista + "<center>_____________________________<br/>" +
                                        "<b>"+ nombreAval + "</b></center>" +
                                        "<br/><br/><br/>";
                            }



                            if (numSocioAVal == 0) {
                                //El aval no es socio

                                if (i == numAvales - 1) {
                                    avalesDetalle = avalesDetalle +  "<b>" + (i + 3) + ".</b> " + nombreAval + ", manifiesta ser <b>" + parentescoAval + "</b>, DEL PRESTAMISTA " +
                                            "<b></b>, continúa declarando tener como domicilio el predio ubicado " +
                                            "en la calle " + direccionAval + ", en la ciudad de UMAN, YUCATAN, México.";
                                } else {
                                    avalesDetalle = avalesDetalle +  "<b>" + (i + 3) + ".</b> " + nombreAval + ", manifiesta ser <b>" + parentescoAval + "</b>, DEL PRESTAMISTA " +
                                            "<b></b>, continúa declarando tener como domicilio el predio ubicado " +
                                            "en la calle " + direccionAval + ", en la ciudad de UMAN, YUCATAN, México.<br/><br/>";
                                }



                            } else {
                                //El aval si es socio
                                String direccionAvalPrestamista = "";
                                String trabajoAvalPrestamista = "";
                                String estadoCivilAvalPrestamista = "";
                                String soloColoniaAvalPrestamista = "";
                                List<Object[]> avalPrestamista = servicio.traerDetalleSocio(numSocioAVal);
                                if (!avalPrestamista.isEmpty()) {
                                    Object[] fila = avalPrestamista.get(0);
                                    direccionAvalPrestamista = fila[5] != null
                                            ? fila[5].toString().toUpperCase()
                                            : "";
                                    trabajoAvalPrestamista = fila[10] != null ? fila[10].toString().toUpperCase() : "";
                                    estadoCivilAvalPrestamista = fila[7] != null ? fila[7].toString().toUpperCase() : "";
                                    soloColoniaAvalPrestamista = fila[4] != null ? fila[4].toString().toUpperCase() : "";
                                }

                                if (i == numAvales - 1) {
                                    avalesDetalle = avalesDetalle +  "<b>"+ (i + 3) + ".</b> "+ nombreAval  +", manifiesta ser <b>"+ estadoCivilAvalPrestamista + "</b>, CON OFICIO " +
                                            "<b>"+ trabajoAvalPrestamista+"</b>, continúa declarando tener como domicilio el predio ubicado " +
                                            "en la calle "+ direccionAvalPrestamista +", de la Col. "+ soloColoniaAvalPrestamista  +", " +
                                            "en la ciudad de UMAN, YUCATAN, México.";
                                } else {
                                    avalesDetalle = avalesDetalle +  "<b>"+ (i + 3) + ".</b> "+ nombreAval  +", manifiesta ser <b>"+ estadoCivilAvalPrestamista + "</b>, CON OFICIO " +
                                            "<b>"+ trabajoAvalPrestamista+"</b>, continúa declarando tener como domicilio el predio ubicado " +
                                            "en la calle "+ direccionAvalPrestamista +", de la Col. "+ soloColoniaAvalPrestamista  +", " +
                                            "en la ciudad de UMAN, YUCATAN, México." + "<br/><br/>";
                                }


                            }

                        } // Fin del for de llenado dinámico de avales

                        encabezado = "<html>" + "<body style='font-size:11pt; font-family:Serif;'>" +
                                "<b>CONTRATO</b> QUE CELEBRAN POR UNA PARTE EL "+ apoderado + " COMO " +
                                "APODERADO DE <b>“"+ empresaEmisoraCompleto+ "”</b> y sus correspondientes " +
                                "abreviaturas <b>“"+ abreviaturas+ "”</b> Y POR LA OTRA "+ avalesJuntosEncabezado +
                                ", AL TENOR DE LAS SIGUIENTES CLAUSULAS.";

                        clausula1 = "<b>I.- </b>" + avalesJuntosEncabezado + ", acepta(n) estar interesado(s) " +
                                "en hacer préstamo de dinero en efectivo que proporciona " +
                                "<b>“"+ empresaEmisoraCompleto + "”</b> y sus correspondientes abreviaturas " +
                                "<b>“"+ abreviaturas + "”</b>.";

                        clausula4 = "<b>IV.-</b> Finalmente, " + avalesJuntosEncabezado + ", declaran estar de " +
                                "acuerdo señalando como domicilio en el cual deban recibir todo tipo de notificaciones el " +
                                "predio ubicado en la calle "+ direccionSocioPrestamista + ", de la Colonia " + soloColoniaPrestamista
                                + ", en la ciudad de UMAN, YUCATAN, México." +

                                "<br/><br/>";

                        clausula5 = "<b>V.- </b> En caso de controversia en juicio serán notificadas a todos y cada uno de los " +
                                "deudores en el domicilio del deudor principal el cual es el predio ubicado en la calle " + direccionSocioPrestamista +
                                ", de la Colonia "+ soloColoniaPrestamista + ", en la ciudad de " +
                                "UMAN, YUCATAN, México." +

                                "<br/><br/>";

                    }  else {
                        //Solo el deudor
                        encabezado = "<html>" + "<body style='font-size:11pt; font-family:Serif;'>" +
                                "<b>CONTRATO</b> QUE CELEBRAN POR UNA PARTE EL "+ apoderado + " COMO " +
                                "APODERADO DE <b>“"+ empresaEmisoraCompleto+ "”</b> y sus correspondientes " +
                                "abreviaturas <b>“"+ abreviaturas+ "”</b> Y POR LA OTRA "+ nombreSocio +
                                ", AL TENOR DE LAS SIGUIENTES CLAUSULAS.";

                        clausula1 = "<b>I.- </b>" + nombreSocio + ", acepta(n) estar interesado(s) " +
                                "en hacer préstamo de dinero en efectivo que proporciona " +
                                "<b>“"+ empresaEmisoraCompleto + "”</b> y sus correspondientes abreviaturas " +
                                "<b>“"+ abreviaturas + "”</b>.";

                        clausula4 = "<b>IV.-</b> Finalmente, " + nombreSocio + ", declara estar de " +
                                "acuerdo señalando como domicilio en el cual deba recibir todo tipo de notificaciones el " +
                                "predio ubicado en la calle "+ direccionSocioPrestamista + ", de la Colonia " + soloColoniaPrestamista
                                + ", en la ciudad de UMAN, YUCATAN, México." +

                                "<br/><br/>";

                        clausula5 = "<b>V.- </b> En caso de controversia en juicio será notificado el " +
                                "deudor en el domicilio del deudor principal el cual es el predio ubicado en la calle " + direccionSocioPrestamista
                                + ", de la Colonia "+ soloColoniaPrestamista + ", en la ciudad de " +
                                "UMAN, YUCATAN, México." +

                                "<br/><br/>";

                        firmaAvalesYPrestamista = "<center>_____________________________<br/>" +
                                "<b>"+ nombreSocio +  "</b></center>";
                    }

                    //Concatenar toda la chingadera que me acabo de aventar sin usar fokin chat
                    Map pars = new HashMap<>();

                    aviso = avisoPrivacidad;
                    html = encabezado + declaraciones + segundoParrafoEncabezado + prestamista + prestamistaParrafo + aval + avalesDetalle + clausulas
                            + clausula1 + clausula2 + clausula3 + clausula4 + clausula5 + clausula6 + enteradasLasPartes;
                    firmas = encabezadoFirmas + firmaAcreedor + losDeudores + firmaAvalesYPrestamista + footerFirmas;
                    pars.put("textHtml", html);
                    pars.put("textFirmas", firmas);
                    pars.put("textAviso", aviso);


                    InputStream isRepo = getClass().getResourceAsStream("/Reports/contrato.jasper");
                    JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
                    JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());

                    Platform.runLater(() -> {
                        JasperViewer viewer = new JasperViewer(jpRepo, false);
                        viewer.setAlwaysOnTop(true);
                        viewer.setSize(800, 600);
                        viewer.setLocationRelativeTo(null);
                        viewer.setTitle("CONTRATO DE CRÉDITO");
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
        new Thread(task).start();


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
            controlador.setReimpresionController(this);
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

}
