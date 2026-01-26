package com.mutualidad.modulo_credito.Controllers;

import com.mutualidad.modulo_credito.Models.ModelCredito;
import com.mutualidad.modulo_credito.Models.ModelSocio;
import com.mutualidad.modulo_credito.Services.Servicio;
import com.sun.jdi.event.StepEvent;
import com.tenpisoft.n2w.MoneyConverters;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
    private TextArea taDictamen;

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
            nombreSocio = "", codigoEmpresa = "", plazo = "", firmas    ="" , aviso="",
    html="";
    int idRetornado = 0;


    DecimalFormat formatoPorcentaje = new DecimalFormat("#0.00'%'");

    NumberFormat formatoMXN = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");


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


    }

    public void settearDatos(int idCredito) throws ParseException {


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
                    ""
            );


            try {
                idRetornado = Integer.parseInt(res);
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("ÉXITO");
                alert.setHeaderText("SOLICITUD ACEPTADA CORRECTAMENTE.");
                alert.setContentText("PUEDE PROCEDER A GENERAR LOS DOCUMENTOS");
                alert.showAndWait();

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
        Dotenv dotenv = Dotenv.load();
        String montoAutorizado = txtMontoAut.getText().trim();
        String tasa = txtTasa.getText().trim();
        String plazo = txtPlazo.getText().trim() + " Meses";
        String nombre = txtNomSocio.getText().trim();
        String numeroSocio = txtNumSocio.getText().trim();
        String tipo = txtTipoCred.getText().trim();
        String empresa = lblEmpresaEmisora.getText().trim();

        ModelCredito credito = servicio.encontrarCredito(idRetornado);

        String estado = "VIGENTE";

        LocalDate fecha = LocalDate.now();

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

        try {
            InputStream isRepo = getClass().getResourceAsStream("/Reports/tabla_amortizacion.jasper");
            JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
            Connection conn = DriverManager.getConnection(dotenv.get("DATABASE_URL"), dotenv.get("DATABASE_USERNAME"), dotenv.get("DATABASE_PASSWORD"));
            JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, conn);
            JasperViewer viewer = new JasperViewer(jpRepo, false);
            viewer.setSize(800, 600);
            viewer.setAlwaysOnTop(true);
            viewer.setLocationRelativeTo(null);
            viewer.setTitle("PLAN DE PAGOS");
            viewer.setVisible(true);

        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void mostrarContrato(){
        int numAvales = servicio.traerNumAvales(idRetornado);
        List<String> numSocioAval = new ArrayList<>();
        List<String> nomAval = new ArrayList<>();
        List<String> parentescoAval = new ArrayList<>();
        List<Object[]> avales = servicio.traerAvales(idRetornado);
        String apoderado = "ING. CARLOS ALBERTO VILLANUEVA RUIZ";
        String apoderadoSinTitulo = "CARLOS ALBERTO VILLANUEVA RUIZ";
        String empresaCompleto = "";
        String empresaRS = "";
        String tituloApoderado = "Ingeniero Civil";
        String direcApdoerado = "ciento ochenta y tres, de la calle nueve";

        String ciudadApoderado = "ciento ochenta y tres, de la calle nueve";


        if(nomEmpresa.equals("NUEVA GENERACION DE UMAN")){
             empresaCompleto = "NUEVA GENERACION DE UMAN ASOCIACION CIVIL";

            empresaRS = "NUEVA GENERACION DE UMAN, A.C.";
        }

        if(numAvales!=0){
            for (Object[] aval : avales) {
                numSocioAval.add(aval[1].toString());
                nomAval.add(aval[2].toString());
                parentescoAval.add(aval[4].toString());
            }
        }

        switch (numAvales){
            case 1:
                Object[] aval1 = null;
                Object[] prestamista = servicio.traerInfoSocio(Integer.parseInt(numSocio)) ;
                if(Integer.parseInt(numSocioAval.get(0)) != 0){
                    aval1 = servicio.traerInfoSocio(Integer.parseInt(numSocioAval.get(0))) ;
                }



                html =
                        "<html>" +
                                "<body style='font-size:11pt; font-family:Serif;'>" +

                                "<b>CONTRATO</b> QUE CELEBRAN POR UNA PARTE EL "+ apoderado + " COMO " +
                                "APODERADO DE <b>“"+ empresaCompleto+ "”</b> y sus correspondientes " +
                                "abreviaturas <b>“"+ empresaRS+ "”</b> Y POR LA OTRA "+ nombreSocio +
                                " Y " + nomAval.get(1).toString() +", AL TENOR DE LAS SIGUIENTES CLAUSULAS." +

                                "<br/><br/>" +

                                "<div style='text-align:center;'><b>DECLARACIONES</b></div>" +

                                "<br/>" +

                                "<b>1.</b>" + apoderadoSinTitulo+", como Apoderado Legal de " +
                                "<b>“"+ empresaCompleto +"”</b>, también conocida como y sus correspondientes " +
                                "abreviaturas <b>“"+empresaRS+"”</b>, manifiesta ser "+ tituloApoderado+", " +
                                "mayor de edad legal, continúa declarando tener como domicilio el predio marcado con el " +
                                "numero "+direcApdoerado+ ", en la ciudad de "+ ciudadApoderado+", México." +

                                "<br/><br/>" +

                                "<b>Prestamista</b>" +

                                "<br/><br/>" +

                                "<b>2.</b> "+   nombreSocio +", manifiesta ser <b>"+ prestamista[7].toString() + "</b>, CON OFICIO " +
                                "<b>"+ prestamista[10].toString()+"</b>, continúa declarando tener como domicilio el predio ubicado " +
                                "en la calle "+ prestamista[5].toString() +", de la Col. "+ prestamista[4].toString().toUpperCase()  +", " +
                                "en la ciudad de UMAN, YUCATAN, México." +

                                "<br/><br/>" +

                                "<b>Aval</b>" +

                                "<br/><br/>" +

                                "<b>2.</b> "+ nomAval.get(0).toString()  +", manifiesta ser <b>"+ aval1[7].toString() + "</b>, CON OFICIO " +
                                "<b>"+ aval1[10].toString()+"</b>, continúa declarando tener como domicilio el predio ubicado " +
                                "en la calle "+ aval1[5].toString() +", de la Col. "+ aval1[4].toString().toUpperCase()  +", " +
                                "en la ciudad de UMAN, YUCATAN, México." +

                                "<br/><br/><br/>" +

                                "<div style='text-align:center;'><b>CLAUSULAS</b></div>" +

                                "<br/><br/>" +

                                "<b>I.-</b> MARIA JUSTINA CHE IUIT Y MARIA ANGELICA CHE IUIT, acepta(n) estar interesado(s) " +
                                "en hacer préstamo de dinero en efectivo que proporciona " +
                                "<b>“NUEVA GENERACION DE UMAN ASOCIACION CIVIL”</b> y sus correspondientes abreviaturas " +
                                "<b>“NUEVA GENERACION DE UMAN A.C.”</b>." +

                                "<br/><br/>" +

                                "<b>II.-</b> NUEVA GENERACION DE UMAN ASOCIACION CIVIL y sus correspondientes abreviaturas " +
                                "<b>“NUEVA GENERACION DE UMAN A.C.”</b>, concederá los préstamos que se le requieran, " +
                                "siempre que se desarrolle de la siguiente manera: proporcionando a los deudores la " +
                                "ubicación correcta del domicilio en el cual se le haga las respectivas notificaciones " +
                                "en caso de demora de pago del préstamo establecido." +

                                "<br/><br/>" +

                                "<b>III.-</b> El domicilio señalado, será en común para notificar a todos y cada uno de los " +
                                "deudores solidarios, así como para todo lo relativo al cumplimiento de pago del préstamo " +
                                "solicitado, con renuncia expresa de señalar otro domicilio y vecindad." +

                                "<br/><br/>" +

                                "<b>IV.-</b> Finalmente, MARIA JUSTINA CHE IUIT Y MARIA ANGELICA CHE IUIT, declaran estar de " +
                                "acuerdo señalando como domicilio en el cual deban recibir todo tipo de notificaciones el " +
                                "predio ubicado en la calle C-20 B N°93-B X 7 Y 9, COL. SAN FRANCISCO, de la Colonia " +
                                "SAN FRANCISCO, en la ciudad de UMAN, YUCATAN, México." +

                                "<br/><br/>" +

                                "<b>V.-</b> En caso de controversia en juicio serán notificadas a todos y cada uno de los " +
                                "deudores en el domicilio del deudor principal el cual es el predio ubicado en la calle " +
                                "C-20 B N°93-B X 7 Y 9, COL. SAN FRANCISCO, de la Colonia SAN FRANCISCO, en la ciudad de " +
                                "UMAN, YUCATAN, México." +

                                "<br/><br/>" +

                                "<b>VI.-</b> Los comparecientes se someten de una manera expresa a la jurisdicción y " +
                                "competencia de los jueces y tribunales de esta ciudad de Umán, Yucatán, México, " +
                                "renunciando a cualquier fuero que pudiera corresponderles por razón de su origen o " +
                                "domicilio." +

                                "<br/><br/>" +

                                "Enteradas las partes y conformes con el contenido y alcance legal del presente contrato, " +
                                "lo firman el día 28 de Febrero de dos mil veintidós." +

                                "</body></html>";


                 firmas =
                        "<html>" +
                                "<body>" +

                                "<br/><br/>" +

                                "<center><b>EL ACREEDOR</b></center>" +

                                "<br/><br/><br/>" +

                                "<center>_____________________________<br/>" +
                                "<b>ING. CARLOS ALBERTO VILLANUEVA RUIZ</b></center>" +

                                "<br/><br/>" +

                                "<center><b>LOS DEUDORES</b></center>" +

                                "<br/><br/><br/>" +

                                "<center>_____________________________<br/>" +
                                "<b>MARIA JUSTINA CHE IUIT</b></center>" +

                                "<br/><br/><br/>" +

                                "<center>_____________________________<br/>" +
                                "<b>MARIA ANGELICA CHE IUIT</b></center>" +


                                "</body>" +
                                "</html>";


                aviso =
                        "<html>" +
                                "<body>" +

                                "<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>" +

                                "<span style='font-size:8pt;'>Aviso de privacidad para las operaciones con espacios reducidos (datos deben de incluirse). " +
                                "Nueva Generación de Umán Asociación Civil, con domicilio en calle veintitrés número ciento " +
                                "ochenta y tres letra “B” de la ciudad de Umán, Estado de Yucatán; utilizará sus datos " +
                                "personales aquí recabados para la obtención del crédito solicitado. Para mayor información " +
                                "acerca del tratamiento y de los derechos que puede hacer valer, usted puede acceder al " +
                                "aviso de privacidad completo a través de la publicación y exhibición permanente que se " +
                                "realiza en la entrada o recepción del domicilio antes citado de esta Asociación.</span>" +

                                "</body>" +
                                "</html>";
                break;
        }
        Map pars = new HashMap<>();

        pars.put("textHtml", html);
        pars.put("textFirmas", firmas);
        pars.put("textAviso", aviso);
        try {
            InputStream isRepo = getClass().getResourceAsStream("/Reports/Blank_A4.jasper");
            JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
            JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());

            JasperViewer viewer = new JasperViewer(jpRepo, false);

            viewer.setAlwaysOnTop(true);
            viewer.setSize(800, 600);
            viewer.setLocationRelativeTo(null);
            viewer.setTitle("PAGARE DE CRÉDITO");
            viewer.setVisible(true);

        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void mostrarPagare() {


        ModelCredito credito = servicio.encontrarCredito(idRetornado);

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
        pars.put("nombreDeudor", "Nombre: " + txtNomSocio.getText() +"\nDirección: CALLE " + socio.getCalle() + " " + socio.getCruzamiento() + " UMÁN, YUCATÁN.");
        pars.put("mostrarLinea0", true);

        if(!isRiesgo){
            pars.put("avalTitulo1", "");
            pars.put("nombreAval1", "");
            pars.put("firmaAval1", "");
            pars.put("mostrarLinea1", false);

            pars.put("avalTitulo2", "");
            pars.put("nombreAval2", "");
            pars.put("firmaAval2", "");
            pars.put("mostrarLinea2", false);

            pars.put("avalTitulo3", "");
            pars.put("nombreAval3", "");
            pars.put("firmaAval3", "");
            pars.put("mostrarLinea3", false);

            pars.put("avalTitulo4", "");
            pars.put("nombreAval4", "");
            pars.put("firmaAval4", "");
            pars.put("mostrarLinea4", false);

            pars.put("avalTitulo5", "");
            pars.put("nombreAval5", "");
            pars.put("firmaAval5", "");
            pars.put("mostrarLinea5", false);
        }else{
            //Si si es de riesgo
            //Saber cuantos avales son
            int numAvales = servicio.traerNumAvales(idRetornado);
            List<Object[]> avales = servicio.traerAvales(idRetornado);

            int MAX_AVALES = 5;

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
                        "Nombre: " + nombreAval + "\nDirección: " + direccionAvalEnviar
                );
                pars.put("firmaAval" + idx, "FIRMA");
                pars.put("mostrarLinea" + idx, true);
            }


        }

        pars.put("elabora", "ELABORA: " + asesor.toString());
        pars.put("revisa", "REVISA: "+ "CONTABILIDAD");

        try {
            InputStream isRepo = getClass().getResourceAsStream("/Reports/pagare.jasper");
            JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
            JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());

            JasperViewer viewer = new JasperViewer(jpRepo, false);

            viewer.setAlwaysOnTop(true);
            viewer.setSize(800, 600);
            viewer.setLocationRelativeTo(null);
            viewer.setTitle("PAGARE DE CRÉDITO");
            viewer.setVisible(true);

        }catch (Exception e) {
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
