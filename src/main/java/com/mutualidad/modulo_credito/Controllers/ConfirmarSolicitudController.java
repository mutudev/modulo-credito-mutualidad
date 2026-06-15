package com.mutualidad.modulo_credito.Controllers;

import com.mutualidad.modulo_credito.Models.ModelCredito;
import com.mutualidad.modulo_credito.Models.ModelSocio;
import com.mutualidad.modulo_credito.Services.Servicio;
import com.sun.jdi.event.StepEvent;
import com.tenpisoft.n2w.MoneyConverters;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;
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
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

@Component
public class ConfirmarSolicitudController implements Initializable {

    @FXML
    private TextField txtNumSocio, txtNomSocio, txtMontoSol, txtTasa, txtMora,
            txtIva, txtTipoCred, txtId, txtMontoAut, txtPlazo;

    @FXML
    private TextArea txtDictamen;

    @FXML
    private Button btnLimpiarAut, btnConfirmar, btnPagare, btnPlan, btnContrarto;

    @FXML
    private Label lblSelecCajero, lblEmpresaEmisora;

    @FXML
    private CheckBox chkDesembolso;

    @FXML
    private ComboBox cmbCajero;

    @Autowired
    private Servicio servicio;

    public  int idCreditoRecibido = 0;
    public boolean isRiesgo=false;
    String asesor = "", numSocio = "", nomEmpresa = "", monto = "",
            tasa = "", mora = "", iva = "",
            nombreCredito = "",
            nombreSocio = "", codigoEmpresa = "", plazo = "", firmas    ="" , aviso="", html="";
    int idRetornado = 0;
    Dotenv dotenv = Dotenv.load();
    DecimalFormat formatoPorcentaje = new DecimalFormat("#0.00'%'");
    NumberFormat formatoMXN = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public ListadoSolicitudesController controller;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        txtMontoAut.setTextFormatter(
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

        txtDictamen.setTextFormatter(
                new TextFormatter<>(
                        change -> {
                            change.setText(change.getText().toUpperCase());
                            return change;
                        }));

        Platform.runLater(
                () -> {
                    Stage stage = (Stage) btnConfirmar.getScene().getWindow();
                    stage.setOnCloseRequest(event -> cierreDeVentana(event));
                });


    }

    public void cierreDeVentana(Event event) {
        event.consume();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("CIERRE DE CONFIRMACIÓN");
        alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA CONFIRMACIÓN?");
        alert.setContentText(
                "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            controller.cargarSolicitudes();
            Stage ventanaActual = (Stage) btnConfirmar.getScene().getWindow();
            ventanaActual.close();
        }
    }

    public void settearDatos(int idCredito, ListadoSolicitudesController controller) throws ParseException {

        this.controller = controller;
        this.idCreditoRecibido = idCredito;

        List<Object[]> solicitudPendiente = servicio.traerDatosSolicitudPendiente(idCredito);

        for (Object[] fila : solicitudPendiente) {

            asesor = fila[1].toString();
            numSocio = fila[2].toString();
            nomEmpresa = fila[3].toString();
            monto = fila[4].toString();
            plazo = fila[5].toString();
            tasa = fila[8].toString();
            mora = fila[9].toString();
            iva = fila[10].toString();
            isRiesgo = Boolean.parseBoolean(fila[11].toString());
            nombreCredito = fila[16].toString();
            nombreSocio = fila[18].toString();
            codigoEmpresa = fila[19].toString();
        }

        txtNomSocio.setText(nombreSocio);
        txtNumSocio.setText(numSocio);
        txtMontoSol.setText(monto);
        txtTasa.setText(tasa);
        txtMora.setText(mora);
        txtIva.setText(iva);
        txtTipoCred.setText(nombreCredito);
        lblEmpresaEmisora.setText(nomEmpresa);
        txtPlazo.setText(plazo);
        tasa = txtTasa.getText().toString();
        tasa = tasa.replace(",", ".")
                .replace(" ", "");
        txtTasa.setText(tasa);
        txtNomSocio.setEditable(false);
        txtNumSocio.setEditable(false);
        txtMontoSol.setEditable(false);
        txtTasa.setEditable(false);
        txtMora.setEditable(false);
        txtIva.setEditable(false);
        txtTipoCred.setEditable(false);




    }

    @FXML
    public void  guardarSolicitud() throws ParseException {


        int isDesembolsoEfectivo = 0;
        if(chkDesembolso.isSelected()){
            isDesembolsoEfectivo = 1;
        }

        if(txtMontoAut.getText().isEmpty() || parseMoneda(txtMontoAut.getText()) <= 0){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL INTENTAR CONFIRMAR");
            alert.setHeaderText("ERROR AL INTENTAR CONFIRMAR LA SOLICITUD DE CRÉDITO.");
            alert.setContentText("EL MONTO AUTORIZADO NO PUEDE ESTAR VACÍO O SER 0");
            alert.showAndWait();
            return;
        }

        if (txtDictamen.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL INTENTAR CONFIRMAR");
            alert.setHeaderText("ERROR AL INTENTAR CONFIRMAR LA SOLICITUD DE CRÉDITO.");
            alert.setContentText("POR FAVOR, INSERTE UN DICTAMEN DE CRÉDITO");
            alert.showAndWait();
            return;
        }

        String dictamen = txtDictamen.getText().trim();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("CONFIRMACIÓN DE SOLICITUD");
        alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CONFIRMAR LA SOLICITUD?");
        alert.setContentText(
                "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR");

        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.OK){

            String res = servicio.confirmarSolicitud(
                    idCreditoRecibido,
                    Integer.parseInt(plazo),

                   parsePorcentaje(txtTasa.getText()),
                    parsePorcentaje(txtIva.getText()),
                    parsePorcentaje(txtMora.getText()),
                    isDesembolsoEfectivo,
                    BigDecimal.valueOf(parseMoneda(txtMontoAut.getText())),
                    dictamen,
                    ""
            );


            try {
                idRetornado = Integer.parseInt(res);
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("ÉXITO");
                alert.setHeaderText("SOLICITUD ACEPTADA CORRECTAMENTE.");
                alert.setContentText("PUEDE PROCEDER A GENERAR LOS DOCUMENTOS");
                alert.showAndWait();

                btnConfirmar.setDisable(true);
                btnContrarto.setVisible(true);
                btnPagare.setVisible(true);
                btnPlan.setVisible(true);
            } catch (Exception e) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setHeaderText("NO SE PUDO CONFIRMAR LA SOLICITUD");
                alert.setContentText(res.toUpperCase());
                alert.showAndWait();
            }
        }else {
            return;
        }
    }

    @FXML
    public void mostrarTablaAmortizacion() {

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

        try {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    try {
                        String montoAutorizado = txtMontoAut.getText().trim();
                        String tasa = txtTasa.getText().trim();
                        String plazo = txtPlazo.getText().trim() + " Meses";
                        String nombre = txtNomSocio.getText().trim();
                        String numeroSocio = txtNumSocio.getText().trim();
                        String tipo = txtTipoCred.getText().trim();
                        String empresa = lblEmpresaEmisora.getText().trim();

                        ModelCredito credito = servicio.encontrarCredito(idRetornado);

                        String estado = "VIGENTE";

                        LocalDate fecha = servicio.traerFechaHoy();

                        Map pars = new HashMap<>();
                        pars.put("CreditoID", idRetornado);
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

                        InputStream isRepo = getClass().getResourceAsStream("/Reports/tabla_amortizacion.jasper");
                        JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
                        Connection conn = DriverManager.getConnection(dotenv.get("DATABASE_URL"), dotenv.get("DATABASE_USERNAME"), dotenv.get("DATABASE_PASSWORD"));
                        JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, conn);

                        Platform.runLater(() -> {
                            JasperViewer viewer = new JasperViewer(jpRepo, false);
                            viewer.setSize(800, 600);
                            viewer.setAlwaysOnTop(true);
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

        } catch (Exception e) {
            e.printStackTrace();
        }




    }

    @FXML
    public void mostrarContrato() {


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


        try {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    try {


                        String encabezado = "";
                        String avalesDetalle = "";
                        //Obtener a carli
                        String apoderado = servicio.obtenerApoderadoLegal();
                        String apoderadoAbreviado = dotenv.get("APODERADO_ABREVIADO");
                        //Obtener empresa por la que sale
                        ModelCredito credito = servicio.encontrarCredito(idRetornado);
                        String empresaEmisoraCompleto = servicio.traerEmpresa(credito.getEmpresa()).getRazonSocial();
                        String abreviaturas = servicio.traerEmpresa(credito.getEmpresa()).getNombre();
                        LocalDate fecha = servicio.traerFechaHoy();
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
                                "<b>"+ apoderado + "</b></center>" +

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
                        String municipioSocioPrestamista = "";
                        String estadoSocioPrestamista = "";

                        List<Object[]> socioPrestamista = servicio.traerDetalleSocio(Integer.parseInt(numSocio));
                        if (!socioPrestamista.isEmpty()) {
                            Object[] fila = socioPrestamista.get(0);
                            direccionSocioPrestamista = fila[2] != null
                                    ? fila[2].toString().toUpperCase()
                                    : "";
                            trabajoPrestamista = fila[7] != null ? fila[7].toString().toUpperCase() : "";
                            estadoCivilPrestamista = fila[4] != null ? fila[4].toString().toUpperCase() : "";
                            municipioSocioPrestamista = fila[15] != null ? fila[15].toString().toUpperCase() : "";
                            estadoSocioPrestamista = fila[16] != null ? fila[16].toString().toUpperCase() : "";

                        }

                        String prestamistaParrafo = "<b>2.</b> "+ txtNomSocio.getText().trim() + ", manifiesta ser <b>" + estadoCivilPrestamista + "</b>, CON OFICIO " +
                                "<b>" + trabajoPrestamista + "</b>, continúa declarando tener como domicilio el predio ubicado " +
                                "en la: "+ direccionSocioPrestamista + ", " +
                                "en la ciudad de " + municipioSocioPrestamista.toUpperCase() + ", " + estadoSocioPrestamista.toUpperCase() + ", MÉXICO.";

                        //Evaluar si es de riesgo o no para el primer parráfo, saber si paso al deudor y a los avales o solo al primero
                        if (isRiesgo) {

                            aval = "<br/><br/>" +

                                    "<b>Aval</b>" +

                                    "<br/><br/>";

                            //Obtener a los avales
                            int numAvales = servicio.traerNumAvales(idRetornado);
                            List<Object[]> avales = servicio.traerAvales(idRetornado);

                            String avalesJuntosEncabezado = txtNomSocio.getText().trim();
                            firmaAvalesYPrestamista = "<center>_____________________________<br/>" +
                                    "<b>"+ txtNomSocio.getText().trim() +  "</b></center>"+"<br/><br/><br/>";
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
                                                "en la " + direccionAval;
                                    } else {
                                        avalesDetalle = avalesDetalle +  "<b>" + (i + 3) + ".</b> " + nombreAval + ", manifiesta ser <b>" + parentescoAval + "</b>, DEL PRESTAMISTA " +
                                                "<b></b>, continúa declarando tener como domicilio el predio ubicado " +
                                                "en la " + direccionAval + "<br/><br/>";
                                    }



                                } else {
                                    //El aval si es socio
                                    String direccionAvalPrestamista = "";
                                    String trabajoAvalPrestamista = "";
                                    String estadoCivilAvalPrestamista = "";
                                    String municipioAvalPrestamista = "";
                                    String estadoAvalPrestamista = "";
                                    List<Object[]> avalPrestamista = servicio.traerDetalleSocio(numSocioAVal);
                                    if (!avalPrestamista.isEmpty()) {
                                        Object[] fila = avalPrestamista.get(0);
                                        direccionAvalPrestamista = fila[2] != null
                                                ? fila[2].toString().toUpperCase()
                                                : "";
                                        trabajoAvalPrestamista = fila[7] != null ? fila[7].toString().toUpperCase() : "";
                                        estadoCivilAvalPrestamista = fila[4] != null ? fila[4].toString().toUpperCase() : "";
                                        municipioAvalPrestamista = fila[15] != null ? fila[15].toString().toUpperCase() : "";
                                        estadoAvalPrestamista = fila[16] != null ? fila[16].toString().toUpperCase() : "";
                                    }

                                    if (i == numAvales - 1) {
                                        avalesDetalle = avalesDetalle +  "<b>"+ (i + 3) + ".</b> "+ nombreAval  +", manifiesta ser <b>"+ estadoCivilAvalPrestamista + "</b>, CON OFICIO " +
                                                "<b>"+ trabajoAvalPrestamista+"</b>, continúa declarando tener como domicilio el predio ubicado " +
                                                "en la: "+ direccionAvalPrestamista + ", " +
                                                "en la ciudad de " + municipioAvalPrestamista.toUpperCase() + ", " + estadoAvalPrestamista.toUpperCase() + ", MÉXICO.";
                                    } else {
                                        avalesDetalle = avalesDetalle +  "<b>"+ (i + 3) + ".</b> "+ nombreAval  +", manifiesta ser <b>"+ estadoCivilAvalPrestamista + "</b>, CON OFICIO " +
                                                "<b>"+ trabajoAvalPrestamista+"</b>, continúa declarando tener como domicilio el predio ubicado " +
                                                "en la: "+ direccionAvalPrestamista + ", " +
                                                "en la ciudad de " + municipioAvalPrestamista.toUpperCase() + ", " + estadoAvalPrestamista.toUpperCase() + ", MÉXICO." + "<br/><br/>";
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
                                    "predio ubicado en la " + direccionSocioPrestamista
                                    + ", en la ciudad de " + municipioSocioPrestamista.toUpperCase() + ", " + estadoSocioPrestamista.toUpperCase() + ", MÉXICO." +

                                    "<br/><br/>";

                            clausula5 = "<b>V.- </b> En caso de controversia en juicio serán notificadas a todos y cada uno de los " +
                                    "deudores en el domicilio del deudor principal el cual es el predio ubicado en la " + direccionSocioPrestamista
                                    + ", en la ciudad de " + municipioSocioPrestamista.toUpperCase() + ", " + estadoSocioPrestamista.toUpperCase() + ", MÉXICO." +

                                    "<br/><br/>";

                        } else {
                            //Solo el deudor
                            encabezado = "<html>" + "<body style='font-size:11pt; font-family:Serif;'>" +
                                    "<b>CONTRATO</b> QUE CELEBRAN POR UNA PARTE EL "+ apoderado + " COMO " +
                                    "APODERADO DE <b>“"+ empresaEmisoraCompleto+ "”</b> y sus correspondientes " +
                                    "abreviaturas <b>“"+ abreviaturas+ "”</b> Y POR LA OTRA "+ txtNomSocio.getText().trim() +
                                    ", AL TENOR DE LAS SIGUIENTES CLAUSULAS.";

                            clausula1 = "<b>I.- </b>" + txtNomSocio.getText().trim() + ", acepta(n) estar interesado(s) " +
                                    "en hacer préstamo de dinero en efectivo que proporciona " +
                                    "<b>“"+ empresaEmisoraCompleto + "”</b> y sus correspondientes abreviaturas " +
                                    "<b>“"+ abreviaturas + "”</b>.";

                            clausula4 = "<b>IV.-</b> Finalmente, " + txtNomSocio.getText().trim() + ", declara estar de " +
                                    "acuerdo señalando como domicilio en el cual deba recibir todo tipo de notificaciones el " +
                                    "predio ubicado en la " + direccionSocioPrestamista
                                    + ", en la ciudad de " + municipioSocioPrestamista.toUpperCase() + ", " + estadoSocioPrestamista.toUpperCase() + ", MÉXICO." +

                                    "<br/><br/>";

                            clausula5 = "<b>V.- </b> En caso de controversia en juicio será notificado el " +
                                    "deudor en el domicilio del deudor principal el cual es el predio ubicado en la " + direccionSocioPrestamista
                                    + ", en la ciudad de " + municipioSocioPrestamista.toUpperCase() + ", " + estadoSocioPrestamista.toUpperCase() + ", MÉXICO." +

                                    "<br/><br/>";

                            firmaAvalesYPrestamista = "<center>_____________________________<br/>" +
                                    "<b>"+ txtNomSocio.getText().trim() +  "</b></center>";
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
                            viewer.setSize(800, 600);
                            viewer.setAlwaysOnTop(true);
                            viewer.setLocationRelativeTo(null);
                            viewer.setTitle("CONTRATO DE CREDITO");
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

    @FXML
    public void mostrarPagare() {


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


        try {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    try {

                        ModelCredito credito = servicio.encontrarCredito(idRetornado);

                        String dineroLetras = converter.asWords(BigDecimal.valueOf(credito.getMonto()));
                        LocalDate hoy = servicio.traerFechaHoy();
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

                        ModelSocio socio = servicio.traerSocioXNumero(Integer.parseInt(numSocio));


                        Map pars = new HashMap<>();
                        pars.put("folio", String.valueOf(idRetornado));
                        pars.put("numSocio", numSocio);
                        pars.put("fechaVencimiento", credito.getFv().getDayOfMonth() + "/" + credito.getFv().getMonthValue() + "/" + credito.getFv().getYear());
                        pars.put("importe", formatoMXN.format(credito.getMonto()));
                        pars.put("parrafo1", "Por el presente Pagaré reconozco deber y me obligo a pagar incondicionalmente al día de su " +
                                "vencimiento en esta ciudad de UMAN, YUCATAN a la orden de " +
                                servicio.obtenerEmpresaXNombre(lblEmpresaEmisora.getText()).getRazonSocial() + " la cantidad de: " +
                                formatoMXN.format(credito.getMonto()) +
                                " (Son: " + dineroLetras.toUpperCase() + " M.N.). " +
                                "Valor que he recibido en efectivo a mi entera satisfacción.");
                        pars.put("parrafoOrdinarios",
                                "I. Intereses Ordinarios. El Suscriptor se obliga incondicionalmente a pagar intereses " +
                                        "ordinarios sobre el monto principal del presente Pagaré, mismo que será por una" +
                                        " tasa mensual del " + txtTasa.getText() + " sobre saldos insolutos y que se calcularán desde" +
                                        " la fecha de Suscripción y hasta la Fecha de Vencimiento," +
                                        " pagaderos precisamente en la Fecha de Vencimiento.");

                        pars.put("suscrito", suscrito);
                        pars.put("nombreDeudor", "Nombre: " + txtNomSocio.getText() +"\nDirección: " + socio.getDireccion() + " UMÁN, YUCATÁN.");
                        pars.put("mostrarLinea0", true);

                        int MAX_AVALES = 5;


                        if(!isRiesgo){
                            for (int i = 1; i <= MAX_AVALES; i++) {
                                pars.put("avalTitulo" + i, "");
                                pars.put("nombreAval" + i, "");
                                pars.put("firmaAval" + i, "");
                                pars.put("mostrarLinea" + i, false);
                            }
                        }else{
                            //Si si es de riesgo
                            //Saber cuantos avales son
                            int numAvales = servicio.traerNumAvales(idRetornado);
                            List<Object[]> avales = servicio.traerAvales(idRetornado);


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

                                String municipioSocioAval = "";

                                String estadioSocioAval = "";

                                if (numSocio == null || numSocio == 0) {
                                    direccionAvalEnviar = aval[10] != null ? aval[10].toString() : "";
                                } else {
                                    List<Object[]> socioParaDireccion = servicio.traerDetalleSocio(numSocio);
                                    if (!socioParaDireccion.isEmpty()) {
                                        Object[] fila = socioParaDireccion.get(0);
                                        direccionAvalEnviar = fila[2] != null
                                                ? fila[2].toString().toUpperCase()
                                                : "";

                                        municipioSocioAval = fila[15] != null ? fila[15].toString().toUpperCase() : "";
                                        estadioSocioAval = fila[16] != null ? fila[16].toString().toUpperCase() : "";

                                    }
                                }

                                pars.put("avalTitulo" + idx, "Aval:");
                                pars.put(
                                        "nombreAval" + idx,
                                        "Nombre: " + nombreAval + "\nDirección: " + direccionAvalEnviar + " "
                                        + municipioSocioAval + " "+ estadioSocioAval
                                );
                                pars.put("firmaAval" + idx, "FIRMA");
                                pars.put("mostrarLinea" + idx, true);
                            }


                        }

                        pars.put("elabora", "ELABORA: " + asesor.toString());
                        pars.put("revisa", "REVISA: "+ "CONTABILIDAD");

                        InputStream isRepo = getClass().getResourceAsStream("/Reports/pagare.jasper");
                        JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
                        JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());


                        Platform.runLater(() -> {
                            JasperViewer viewer = new JasperViewer(jpRepo, false);
                            viewer.setSize(800, 600);
                            viewer.setAlwaysOnTop(true);
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

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void settearMontoAutorizado() {

        if (Double.parseDouble(txtMontoAut.getText().trim()) > parseMoneda(txtMontoSol.getText().trim())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("ERROR AL PONER EL MONTO AUTORIZADO");
            alert.setContentText("EL MONTO AUTORIZADO NO PUEDE SER MAYOR AL SOLICITADO.");
            alert.showAndWait();
            txtMontoAut.setText("");
            btnLimpiarAut.setVisible(false);
            return;
        }

        txtMontoAut.setTextFormatter(null);
        txtMontoAut.setText(formatoMXN.format(Double.parseDouble(txtMontoAut.getText().trim())));
        txtMontoAut.setEditable(false);
        btnLimpiarAut.setVisible(true);
    }

    @FXML
    public void limpiarAut() {
        txtMontoAut.setText("");
        btnLimpiarAut.setVisible(false);
        txtMontoAut.setEditable(true);
    }

    private double parseMoneda(String moneda) {
        try {
            Number numero = formatoMXN.parse(moneda);
            return numero.doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private double parsePorcentaje(String tasa) throws ParseException {
        if (tasa == null || tasa.isBlank()) {
            return 0;
        }

        // Normalizar formato
        tasa = tasa.replace(" ", "")   // quita espacios
                .replace("%", "")   // quita %
                .replace(",", "."); // coma → punto

        return Double.parseDouble(tasa);
    }

}
