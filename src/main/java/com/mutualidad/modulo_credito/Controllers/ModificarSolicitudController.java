package com.mutualidad.modulo_credito.Controllers;

import com.mutualidad.modulo_credito.Services.Servicio;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.jfree.data.json.impl.JSONArray;
import org.jfree.data.json.impl.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;


@Component
public class ModificarSolicitudController implements Initializable {

    @FXML
    private Label lblNumero, lblNombre, lblAhorros, lblPSmut, lblPSngu, lblTotal, lblGradualidad, lblSugerido,
            lblEmpresa, lblTasa, lblGrad2, lblPlazo, lblMora, lblIva, lblEmpresaSal, lblBonif, lblError, lblError2,
            lblError3, lblAvalAplica;

    @FXML
    private Button btnCargar, btnLimpiar, btnContinuar1, btnCargar2, btnLimpiar2, btnCargarAval, btnAddAval,
            btnLimpiar3, btnQuitar, btnQuitar2, btnGuardar;

    @FXML
    private TextField txtNumSocio, txtNombre, txtAhorros, txtPSmut, txtPSngu, txtTotalAhorros,
            txtGradualidad, txtEmpresa, txtMontoSoli, txtMora, txtIva, txtNumAval, txtNomAval, txtEmpresa2,
            txtDirecPropiedad, txtPropietario, txtNumSocio3, txtNomSocio3,txtTipo,
            txtAhorrosConf, txtMontoConf, txtRiesgoConf, txtEmisorConf, txtGradConf,
            txtTasaOrdConf, txtMoraConf, txtTasaIvaConf, txtBonifConf, txtTipoConf,
            txtAvalConf, txtTipoRiesgoConf, txtPlazoConf, txtAval, txtGrad;

    @FXML
    private ComboBox cmbTipo, cmbTasa, cmbGradualidad, cmbPlazo, cmbBonif, cmbOpcionAval, cmbAvalAplica,
            cmbIdAval, cmbAcreditacion, cmbGravamen, cmbParentescoAval;

    @FXML
    private Tab tabDatosGenerales, tabDatosCredito, tabAvales, tabConfirmar, tabPropiedad;

    @FXML
    private TableView<Map<String, String>> tblAvales, tblPropietarios;

    @FXML
    private TableColumn<Map<String, String>, String> colNumSocio;

    @FXML
    private TableColumn<Map<String, String>, String> colNombre;

    @FXML
    private TableColumn<Map<String, String>, String> colAhorro;

    @FXML
    private TableColumn<Map<String, String>, String> colParentesco;

    @FXML
    private TableColumn<Map<String, String>, String> colPropietario;

    @FXML
    private TableColumn<Map<String, String>, String> colIdentificacion;


    @FXML
    private TableColumn<Map<String, String>, String>  colIdPropietarios;

    @FXML
    private TableColumn<Map<String, String>, String>  colIdAvales;


    @FXML
    private ImageView imgBusqueda2, imgBusqueda1;

    @FXML
    private TabPane tabPane;

    @Autowired
    private Servicio servicio;

    private int numeroSocio;
    private String nombre, empresaPertenece;
    private BigDecimal ahorrosAntesPs, psMut, psNgu, ahorrosTotales, gradualidad, montoSolicitado;
    private boolean sobrePrestamo = false;
    public String[] opciones = {"Sí", "No"};

    private boolean isRiesgo = false;

    public int idCreditoRecibido = 0;
    NumberFormat formatoMXN = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    DecimalFormat formatoPorcentaje = new DecimalFormat("#0.00'%'");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        colIdAvales.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("idAval"))
        );

        colIdPropietarios.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("idPropietario"))
        );

        colNumSocio.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("numSocio"))
        );

        colNombre.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("nombre"))
        );



        colParentesco.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("parentesco"))
        );

        colIdentificacion.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("identificacion"))
        );


        colPropietario.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().get("nombrePropietario")
                )
        );


        tblAvales.setEditable(true);
        tblPropietarios.setEditable(true);

        colNombre.setCellFactory(TextFieldTableCell.forTableColumn());


        colNombre.setOnEditCommit(event -> {
            Map<String, String> fila = event.getRowValue();
            fila.put("nombre", event.getNewValue());
        });

        colPropietario.setCellFactory(TextFieldTableCell.forTableColumn());

        colPropietario.setOnEditCommit(event -> {
            Map<String, String> fila = event.getRowValue();
            fila.put("nombrePropietario", event.getNewValue());
        });

        Platform.runLater(
                () -> {
                    Stage stage = (Stage) tblAvales.getScene().getWindow();
                    stage.setOnCloseRequest(event -> cierreDeVentana(event));
                });


    }

    public void cierreDeVentana(Event event) {
        event.consume();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("CIERRE DE EDICIÓN DE SOLICITUD");
        alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA SOLICITUD?");
        alert.setContentText(
                "ADVERTENCIA: LOS CAMBIOS NO GUARDADOS SE PERDERÁN.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage ventanaActual = (Stage) tblAvales.getScene().getWindow();
            ventanaActual.close();
        }
    }

    public  void  settearDatos(int idCredito){


        String id  = "",  asesor  = "", numSocio = "", nomEmpresa = "", monto = "",
                plazo = "", ahorrosMomento= "", montoRiesgo = "", tasa  = "",  mora = "", iva = "",
                riesgo= "", gradualidad = "",  bonifAplica = "",  estado = "",    fr = "",   nombreCredito  = "",
                tipo    = "",  nombreSocio    = "";
        List<Object[]> solicitudPendiente = servicio.traerDatosSolicitudPendiente(idCredito);
        for (Object[] fila : solicitudPendiente) {

            id                = fila[0].toString();
            asesor            = fila[1].toString();
            numSocio          = fila[2].toString();
            nomEmpresa        = fila[3].toString();
            monto             = fila[4].toString();
            plazo             = fila[5].toString();
            ahorrosMomento    = fila[6].toString();
            montoRiesgo       = fila[7].toString();
            tasa              = fila[8].toString();
            mora              = fila[9].toString();
            iva               = fila[10].toString();
            riesgo            = fila[11].toString();
            gradualidad       = fila[12].toString();
            bonifAplica       = fila[13].toString();
            estado            = fila[14].toString();
            fr                = fila[15].toString();
            nombreCredito     = fila[16].toString();
            tipo              = fila[17].toString();
            nombreSocio       = fila[18].toString();
        }



        List<Object[]> tasas;
        tasas = servicio.traerTasas();
        cmbTasa.getItems().clear();
        for (Object[] fila : tasas) {
            double e = ((Number) fila[1]).doubleValue();
            String tasaFormateada = formatoPorcentaje.format(e);
            cmbTasa.getItems().add(tasaFormateada);
        }

        cmbTasa.getSelectionModel().select(tasa);

        cmbBonif.getItems().addAll(opciones);

        if (bonifAplica.equals("true")) {
            cmbBonif.getSelectionModel().selectFirst();
        } else {
            cmbBonif.getSelectionModel().selectLast();
        }

        this.idCreditoRecibido = idCredito;

        List<Object[]> plazos;
        plazos = servicio.traerPlazos();
        cmbPlazo.getItems().clear();
        for (Object[] fila : plazos) {
            String e = fila[1].toString();
            cmbPlazo.getItems().add(e);
        }

        cmbPlazo.getSelectionModel().select(plazo);



        txtMontoSoli.setText(monto);
        txtTipo.setText(nombreCredito);

        txtGrad.setText(gradualidad);

        txtMora.setText(mora);
        txtIva.setText(iva);
        txtEmpresa2.setText(nomEmpresa);


        if(riesgo.equals("true")){
            txtAval.setText(tipo);

        } else {
            lblAvalAplica.setVisible(false);
            txtAval.setVisible(false);
        }

        bloquearTxts();

        //Buscar avales
        List<Object[]> avales = servicio.traerAvalesSolicitudPendiente(idCredito);
        List<Object[]> propietarios = servicio.traerPropietariosSolicitudPendiente(idCredito);
        if (avales.size() != 0) {
            for (Object[] fila : avales) {
                Map<String, String> avalesLista = new HashMap<>();
                String numero = fila[1].toString();

                if (numero.equals("0")){
                    numero = "N/A";
                }

                String nombre = fila[2].toString();
                String parentesco = fila[4].toString();
                String ident = fila[7].toString().toUpperCase();

                avalesLista.put("idAval", fila[0].toString());
                avalesLista.put("numSocio", numero);
                avalesLista.put("nombre", nombre);
                avalesLista.put("parentesco", parentesco);
                avalesLista.put("identificacion", ident);
                tblAvales.getItems().add(avalesLista);
            }
        } else {
            tabAvales.setDisable(true);
        }


        if(propietarios.size() != 0){

            String acreditacion = "";
            String gravamen = "No";
            String direccion = "";
            for (Object[] fila : propietarios) {


                Map<String, String> propietariosLista = new HashMap<>();

                String nombre = fila[6].toString();

                direccion = fila[1].toString();
                if(fila[3].toString().equals("true")){
                    gravamen = "Sí";
                }
                acreditacion =  fila[5].toString();

                propietariosLista.put("idPropietario", fila[8].toString());
                propietariosLista.put("nombrePropietario", nombre);
                tblPropietarios.getItems().add(propietariosLista);
            }

            txtDirecPropiedad.setText(direccion);
            cmbAcreditacion.getSelectionModel().select(acreditacion);
            cmbGravamen.getSelectionModel().select(gravamen);





        }else {
            tabPropiedad.setDisable(true);

        }
    }

    @FXML
    public void guardarModificaciones() throws ParseException {

        JSONArray datosAval = new JSONArray();
        JSONArray datosPropietario = new JSONArray();

        String datosAvalEnviar = "";
        String datosPropietarioEnviar = "";

        if (tblAvales.getItems().size() != 0) {
            for (int i = 0; i < tblAvales.getItems().size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("idAval", colIdAvales.getCellData(i));
                obj.put("numero_socio", colNumSocio.getCellData(i));
                obj.put("nombre", colNombre.getCellData(i));
                obj.put("parentesco", colParentesco.getCellData(i));
                obj.put("identificacion", colIdentificacion.getCellData(i));
                datosAval.add(obj);
            }
        }

        if (tblPropietarios.getItems().size() != 0){
            for (int i = 0; i < tblPropietarios.getItems().size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("idPropietario", colIdPropietarios.getCellData(i));
                obj.put("nombre", colPropietario.getCellData(i));
                datosPropietario.add(obj);
            }
        }

        datosAvalEnviar = datosAval.toJSONString();
        datosPropietarioEnviar = datosPropietario.toJSONString();
        double tasaFormateada = parsePorcentaje( cmbTasa.getSelectionModel().getSelectedItem().toString());
        String res = servicio.modificarSolicitud(idCreditoRecibido,txtAval.getText(),
                Integer.parseInt(cmbPlazo.getSelectionModel().getSelectedItem().toString()),
                BigDecimal.valueOf(tasaFormateada), cmbBonif.getSelectionModel().getSelectedItem().toString(),datosAvalEnviar,
                datosPropietarioEnviar, txtDirecPropiedad.getText(), "");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert = new Alert(res.equals("CORRECTO")
                ? Alert.AlertType.INFORMATION
                : Alert.AlertType.ERROR);
        alert.setTitle(res.equals("CORRECTO") ? "GUARDADO CORRECTO" : "ERROR EN EL GUARDADO");
        alert.setHeaderText(res.equals("CORRECTO")
                ? "SOLICITUD GUARDADA"
                : "ERROR AL GUARDAR LA SOLICITUD");
        alert.setContentText(res.equals("CORRECTO")
                ? "SOLICITUD MODIFICADA CORRECTAMENTE."
                : res.toUpperCase());
        alert.showAndWait();
        Stage ventanaActual = (Stage) tblAvales.getScene().getWindow();
        ventanaActual.close();
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

    private void bloquearTxts() {


        txtMontoSoli.setEditable(false);
        txtTipo.setEditable(false);
        txtGrad.setEditable(false);
        txtMora.setEditable(false);
        txtIva.setEditable(false);
        txtEmpresa2.setEditable(false);
        txtAval.setEditable(false);
    }
}
