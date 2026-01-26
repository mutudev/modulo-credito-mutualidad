package com.mutualidad.modulo_credito.Controllers;

import com.mutualidad.modulo_credito.Main;
import com.mutualidad.modulo_credito.Models.ModelSocio;
import com.mutualidad.modulo_credito.Services.Servicio;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jfree.data.json.impl.JSONArray;
import org.jfree.data.json.impl.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

@Component
public class SolicitudController  implements Initializable {

    @FXML
    private Label lblNumero, lblNombre, lblAhorros, lblPSmut, lblPSngu, lblTotal, lblGradualidad, lblSugerido,
            lblEmpresa, lblTasa, lblGrad2, lblPlazo, lblMora, lblIva, lblEmpresaSal, lblBonif, lblError, lblError2,
            lblError3, lblAvalAplica;

    @FXML
    private Button btnCargar, btnLimpiar, btnContinuar1, btnCargar2, btnLimpiar2, btnCargarAval, btnAddAval,
            btnLimpiar3, btnQuitar, btnQuitar2;

    @FXML
    private TextField txtNumSocio, txtNombre, txtAhorros, txtPSmut, txtPSngu, txtTotalAhorros,
            txtGradualidad, txtEmpresa, txtMontoSoli, txtMora, txtIva, txtNumAval, txtNomAval, txtEmpresa2,
             txtDirecPropiedad, txtPropietario, txtNumSocio3, txtNomSocio3,
            txtAhorrosConf, txtMontoConf, txtRiesgoConf, txtEmisorConf, txtGradConf,
            txtTasaOrdConf, txtMoraConf, txtTasaIvaConf, txtBonifConf, txtTipoConf,
            txtAvalConf, txtTipoRiesgoConf, txtPlazoConf, txtDireccionAval;

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
    private TableColumn<Map<String, String>, String> colDireccion;

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


    NumberFormat formatoMXN = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    DecimalFormat formatoPorcentaje = new DecimalFormat("#0.00'%'");

    @FXML
    public void cargarDatos() {
        //Consultar los datos del socio

        if(txtNumSocio.getText().isEmpty()){
            lblError.setVisible(true);
            return;
        }


        int numSocio = Integer.parseInt(txtNumSocio.getText());
        numeroSocio = numSocio;
        Map<String, Object> result =
                servicio.buscarSocioSolicitud(numSocio, "", 0,
                        BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0),
                        BigDecimal.valueOf(0), BigDecimal.valueOf(0), "", "");

        if (result.get("Resultado").toString().equals("CORRECTO")) {
            settearDatos(result);
        } else if(result.get("Resultado").toString().equals("INCORRECTO")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL INTENTAR BUSCAR AL SOCIO");
            alert.setHeaderText(result.get("NombreFormateado").toString().toUpperCase());
            alert.setContentText(
                    "ERROR EN LA LÍNEA: " + result.get("NumSocioEncontrado") + " DEL PROCEDIMIENTO ALMACENADO.");
            alert.showAndWait();

        }



    }

    public void settearDatos(Map<String, Object> result) {


        lblError.setVisible(false);
        btnCargar.setDisable(true);
        txtNumSocio.setDisable(true);
        lblNombre.setVisible(true);
        lblAhorros.setVisible(true);
        lblPSmut.setVisible(true);
        lblPSngu.setVisible(true);
        lblTotal.setVisible(true);
        lblGradualidad.setVisible(true);
        lblSugerido.setVisible(true);
        lblEmpresa.setVisible(true);
        imgBusqueda1.setVisible(false);
        //Poner txt en false y values en null
        txtNombre.setText(result.get("NombreFormateado").toString());
        nombre = result.get("NombreFormateado").toString();
        txtNombre.setVisible(true);
        txtAhorros.setText(
                formatoMXN.format((BigDecimal) result.get("AhorrosAntesPs"))
        );
        txtAhorros.setVisible(true);

        txtPSmut.setText(
                formatoMXN.format((BigDecimal) result.get("psMut"))
        );
        txtPSmut.setVisible(true);

        txtPSngu.setText(
                formatoMXN.format((BigDecimal) result.get("psNgu"))
        );
        txtPSngu.setVisible(true);

        txtTotalAhorros.setText(
                formatoMXN.format((BigDecimal) result.get("AhorrosTotal"))
        );
        txtTotalAhorros.setVisible(true);

        BigDecimal gradualidad = (BigDecimal) result.get("Gradualidad");

        txtGradualidad.setText(
                gradualidad.setScale(1, RoundingMode.HALF_UP).toString()
        );

        txtGradualidad.setVisible(true);
        txtEmpresa.setText(result.get("Empresa").toString());
        empresaPertenece = result.get("Empresa").toString();
        txtEmpresa.setVisible(true);
        //Poner boton no visible
        btnContinuar1.setVisible(true);
        ahorrosAntesPs = ((BigDecimal) result.get("AhorrosAntesPs"))
                .setScale(2, RoundingMode.HALF_UP);
        psMut = ((BigDecimal) result.get("psMut"))
                .setScale(2, RoundingMode.HALF_UP);
        psNgu = ((BigDecimal) result.get("psNgu"))
                .setScale(2, RoundingMode.HALF_UP);
        ahorrosTotales = ((BigDecimal) result.get("AhorrosTotal"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @FXML
    private void continuarDatosCredito() {
        tabPane.getSelectionModel().clearAndSelect(1);
    }

    @FXML
    private void regresarDatosGenerales() {
        tabPane.getSelectionModel().clearAndSelect(0);
    }

    @FXML
    private void continuarAgregadoAvales() {
        if (!isRiesgo) {
            tabPane.getSelectionModel().clearAndSelect(4);
        } else {
            tabPane.getSelectionModel().clearAndSelect(2);
        }
    }

    @FXML
    private void regresarDatosCredito() {
        tabPane.getSelectionModel().clearAndSelect(1);
    }

    @FXML
    private void continuarConfirmacion() {
        tabPane.getSelectionModel().clearAndSelect(3);
    }

    @FXML
    private void regresarAgregadoAvales() {
        if (!isRiesgo) {
            tabPane.getSelectionModel().clearAndSelect(1);
        } else {
            tabPane.getSelectionModel().clearAndSelect(2);

        }
    }

    private boolean cambiandoTab = false;

    @FXML
    private void chequeoDatosSocio() {
        if (cambiandoTab) {
            return;
        }

        try {
            cambiandoTab = true;
            if (txtNumSocio.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR AL INTENTAR AVANZAR");
                alert.setHeaderText("CARGUE CORRECTAMENTE AL SOCIO");
                alert.setContentText("POR FAVOR, CARGUE LOS DATOS ANTES DE AVANZAR.");
                alert.showAndWait();
                tabPane.getSelectionModel().select(0);
                return;
            }
            if (txtMontoSoli.getText().isEmpty()
                    && (tabAvales.isSelected()
                    || tabPropiedad.isSelected()
                    || tabConfirmar.isSelected())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR AL INTENTAR AVANZAR");
                alert.setHeaderText("INGRESE UN MONTO VÁLIDO");
                alert.setContentText("POR FAVOR, INGRESE UN MONTO VÁLIDO ANTES DE CONTINUAR.");
                alert.showAndWait();
                tabPane.getSelectionModel().select(1);
                return;
            }
            if (!isRiesgo && (tabAvales.isSelected() || tabPropiedad.isSelected())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR AL INTENTAR AVANZAR");
                alert.setHeaderText("NO SE PERMITE AGREGAR AVALES");
                alert.setContentText("ESTE ES UN CRÉDITO DE AHORROS, NO SE PERMITEN AVALES.");
                alert.showAndWait();
                tabPane.getSelectionModel().select(0);
                return;
            }

            if (isRiesgo && cmbAvalAplica.getSelectionModel().getSelectedIndex() != 2 && tabPropiedad.isSelected()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR AL INTENTAR AVANZAR");
                alert.setHeaderText("NO SE PERMITE AGREGAR AVALES CON PROPIEDAD");
                alert.setContentText("EL RIESGO DE ESTE CRÉDITO ES MENOR A LOS $10,000.00.");
                alert.showAndWait();
                tabPane.getSelectionModel().select(1);
                return;
            }
            if (tabAvales.isSelected()) {
                tabPane.getSelectionModel().select(2);
                return;
            }
            if (tabConfirmar.isSelected()) {
                if(isRiesgo){
                    if(cmbAvalAplica.getSelectionModel().getSelectedItem().toString() == "AVAL CON PROPIEDAD > $10,000.00" ){
                        if(tblAvales.getItems().size() != 0 && tblPropietarios.getItems().size() != 0 &&
                                !txtDirecPropiedad.getText().isEmpty()){
                            settearConfirmacion();
                        }else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("ERROR AL INTENTAR AVANZAR");
                            alert.setHeaderText("ERROR AL INTENTAR AVANZAR");
                            alert.setContentText(
                                    "POR FAVOR, COMPLETE LA SOLICITUD");
                            alert.showAndWait();
                        }
                    }else{

                        if(tblAvales.getItems().size() != 0 ){
                            settearConfirmacion();
                        }else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("ERROR AL INTENTAR AVANZAR");
                            alert.setHeaderText("ERROR AL INTENTAR AVANZAR");
                            alert.setContentText(
                                    "POR FAVOR, COMPLETE LA SOLICITUD");
                            alert.showAndWait();
                        }

                    }

                }else{
                    if(cmbPlazo.isVisible()){
                        settearConfirmacion();
                    }else{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("ERROR AL INTENTAR AVANZAR");
                        alert.setHeaderText("ERROR AL INTENTAR AVANZAR");
                        alert.setContentText(
                                "POR FAVOR, COMPLETE LA SOLICITUD");
                        alert.showAndWait();
                    }
                }
                tabPane.getSelectionModel().select(4);

            }
        } finally {
            cambiandoTab = false;
        }
    }

    private void settearConfirmacion(){
        txtNumSocio3.setText(txtNumSocio.getText());
        txtNomSocio3.setText(txtNombre.getText());
        txtNumSocio3.setText(txtNumSocio.getText());
        txtAhorrosConf.setText(txtAhorros.getText());
        txtMontoConf.setText(txtMontoSoli.getText());

        if(isRiesgo){
            txtRiesgoConf.setText(formatoMXN.format(
                    montoSolicitado.doubleValue() - ahorrosTotales.doubleValue()));

        }else{
            txtRiesgoConf.setText(formatoMXN.format(0));
        }
        //txtRiesgoConf.setText(txtMontoSoli.getText());

        txtEmisorConf.setText(txtEmpresa2.getText());
        txtGradConf.setText(cmbGradualidad.getSelectionModel().getSelectedItem().toString());
        txtTasaOrdConf.setText(cmbTasa.getSelectionModel().getSelectedItem().toString());
        txtMoraConf.setText(txtMora.getText());
        txtTasaIvaConf.setText(txtIva.getText());
        txtBonifConf.setText(cmbBonif.getSelectionModel().getSelectedItem().toString());
        txtTipoConf.setText(cmbTipo.getSelectionModel().getSelectedItem().toString());
        if(tblAvales.getItems().size() ==0){
            txtAvalConf.setText("N/A");
        }else{
            txtAvalConf.setText("Sí, " + String.valueOf(tblAvales.getItems().size()) );
        }
        if(isRiesgo){
            txtTipoRiesgoConf.setText(cmbAvalAplica.getSelectionModel().getSelectedItem().toString());
        }else {
            txtTipoRiesgoConf.setText("N/A");
        }
        txtPlazoConf.setText(cmbPlazo.getSelectionModel().getSelectedItem().toString());
    }

    public void cargarSocioPorNombre(String nombre, String numero, int ventana) {
        if (ventana == 0) {
            txtNumSocio.setText(numero);
            cargarDatos();
        } else {
            txtNumAval.setText(numero);
            cargarDatosAval();
        }

    }

    @FXML
    public void buscarSocioPorNombre() {
        try {
            Stage nuevaVentana = new Stage();
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/busquedaSocio.fxml"));
            fxml.setControllerFactory(Main.context::getBean);
            Scene nuevaEscena = new Scene(fxml.load());
            BusquedaController controlador = fxml.getController();
            controlador.setCajeroController(this, tabPane.getSelectionModel().getSelectedIndex());
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
    public void limpiarDatos() {
        //Poner labels en false
        btnCargar.setDisable(false);
        txtNumSocio.setDisable(false);
        lblNombre.setVisible(false);
        lblAhorros.setVisible(false);
        lblPSmut.setVisible(false);
        lblPSngu.setVisible(false);
        lblTotal.setVisible(false);
        lblGradualidad.setVisible(false);
        lblSugerido.setVisible(false);
        lblEmpresa.setVisible(false);
        //Poner txt en false y values en null
        txtNumSocio.setText("");
        txtNumSocio.setVisible(true);
        txtNombre.setText(null);
        txtNombre.setVisible(false);
        imgBusqueda1.setVisible(true);
        txtAhorros.setText(null);
        txtAhorros.setVisible(false);
        txtPSmut.setText(null);
        txtPSmut.setVisible(false);
        txtPSngu.setText(null);
        txtPSngu.setVisible(false);
        txtTotalAhorros.setText(null);
        txtTotalAhorros.setVisible(false);
        txtGradualidad.setText(null);
        txtGradualidad.setVisible(false);
        txtEmpresa.setText(null);
        txtEmpresa.setVisible(false);
        //Poner boton no visible
        btnContinuar1.setVisible(false);
        lblError.setVisible(false);
        limpiarDatosCredito();
        limpiarDatosAval();
    }

    @FXML
    private void cargarDatosCredito() {



        if(txtMontoSoli.getText().isEmpty()){
            lblError2.setVisible(true);
            return;
        }else {
            lblError2.setVisible(false);
        }
        montoSolicitado = BigDecimal.valueOf(Long.parseLong(txtMontoSoli.getText()));


        if (montoSolicitado.doubleValue() > ahorrosTotales.doubleValue()) {
            isRiesgo = true;
        }




        settearDatosCredito();




    }

    @FXML
    private void ocultarAvalAplica() {

        int index = cmbTasa.getSelectionModel().getSelectedIndex();
        boolean ocultar = index == 0 || index == 2 || index == 3;

        cmbAvalAplica.setVisible(!ocultar);
        lblAvalAplica.setVisible(!ocultar);
    }

    @FXML
    private void ocultarAvalSocio() {

        int index = cmbOpcionAval.getSelectionModel().getSelectedIndex();
        if( index == 1 ){
            txtNumAval.setEditable(false);
            btnCargarAval.setDisable(true);
            txtNumAval.setText("");
            imgBusqueda2.setVisible(false);
        }else{
            imgBusqueda2.setVisible(true);
            txtNumAval.setEditable(true);
            btnCargarAval.setDisable(false);

        }
    }

    private void settearDatosCredito() {

        txtMontoSoli.setTextFormatter(null);
        double monto = Double.parseDouble(txtMontoSoli.getText());
        String montoFor = formatoMXN.format(monto);
        txtMontoSoli.setText(montoFor);
        txtMontoSoli.setEditable(false);

        List<Object[]> tasas;
        tasas = servicio.traerTasas();
        cmbTasa.getItems().clear();
        for (Object[] fila : tasas) {
            double tasa = ((Number) fila[1]).doubleValue();
            String tasaFormateada = formatoPorcentaje.format(tasa);
            cmbTasa.getItems().add(tasaFormateada);
        }

        if (isRiesgo) {
            cmbTipo.getSelectionModel().select(1);
            cmbTasa.getSelectionModel().select(1);
            lblAvalAplica.setVisible(true);
            cmbAvalAplica.setVisible(true);
        } else {
            cmbTipo.getSelectionModel().select(0);
            cmbTasa.getSelectionModel().select(0);
            lblAvalAplica.setVisible(false);
            cmbAvalAplica.setVisible(false);
        }


        List<Object[]> plazos;
        plazos = servicio.traerPlazos();
        cmbPlazo.getItems().clear();
        for (Object[] fila : plazos) {
            String plazo = fila[1].toString();
            cmbPlazo.getItems().add(plazo);
        }



        cmbPlazo.getSelectionModel().select(1);

        List<Object[]> tiposRiesgo;
        tiposRiesgo = servicio.traerTiposRiesgo();
        cmbAvalAplica.getItems().clear();
        for (Object[] fila : tiposRiesgo) {
            String tipoRiesg = fila[1].toString();
            cmbAvalAplica.getItems().add(tipoRiesg);
        }
        cmbAvalAplica.getSelectionModel().select(1);


        List<Object[]> gradualidades;

        if (numeroSocio <= 8542) {
            gradualidades = servicio.traerGradualidadesMut();
        } else {
            gradualidades = servicio.traerGradualidadesNgu();
        }

        cmbGradualidad.getItems().clear();
        for (Object[] fila : gradualidades) {
            String gradualidad = fila[1].toString();
            cmbGradualidad.getItems().add(gradualidad);
        }

        cmbGradualidad.getSelectionModel().selectFirst();

        List<Object[]> idsAval;
        if(isRiesgo){
             idsAval = servicio.traerIdsAvales() ;
            cmbIdAval.getItems().clear();
            for (Object[] fila : idsAval) {
                String id = fila[1].toString();
                cmbIdAval.getItems().add(id);
            }
            cmbIdAval.getSelectionModel().selectFirst();
        }

        List<Object[]> acreditacionesPropiedad;
        if(isRiesgo){
            acreditacionesPropiedad = servicio.traerAcreditaciones() ;
            cmbAcreditacion.getItems().clear();
            for (Object[] fila : acreditacionesPropiedad) {
                String acreditacion = fila[1].toString();
                cmbAcreditacion.getItems().add(acreditacion);
            }
            cmbAcreditacion.getSelectionModel().selectFirst();
        }

        List<Object[]> parentescos;
        if(isRiesgo){
            parentescos = servicio.traerParentescos() ;
            cmbParentescoAval.getItems().clear();
            for (Object[] fila : parentescos) {
                String parentesco = fila[1].toString();
                cmbParentescoAval.getItems().add(parentesco);
            }
            cmbParentescoAval.getSelectionModel().selectFirst();
        }


        if (numeroSocio <= 8542 && isRiesgo) {
            txtEmpresa2.setText(servicio.traerEmpresa("0002").getNombre());
        } else if (numeroSocio <= 8542 && !isRiesgo) {
            txtEmpresa2.setText(servicio.traerEmpresa("0001").getNombre());
        } else {
            txtEmpresa2.setText(servicio.traerEmpresa("0002").getNombre());
        }

        txtMora.setText(formatoPorcentaje.format(2.0));

        boolean ivaValido = servicio.traerTipoCredito(cmbTipo.getSelectionModel().getSelectedItem().toString()).isIva();
        if (ivaValido) {
            txtIva.setText(formatoPorcentaje.format(16));
        } else {
            txtIva.setText(formatoPorcentaje.format(0));
        }


        lblTasa.setVisible(true);

        lblPlazo.setVisible(true);
        lblMora.setVisible(true);
        lblIva.setVisible(true);
        lblEmpresaSal.setVisible(true);
        lblBonif.setVisible(true);
        txtMontoSoli.setVisible(true);
        txtMora.setVisible(true);
        txtIva.setVisible(true);
        txtEmpresa2.setVisible(true);
        cmbTipo.setVisible(true);
        cmbTasa.setVisible(true);

        if (isRiesgo) {
            cmbGradualidad.setVisible(true);
            lblGrad2.setVisible(true);
        } else {
            cmbGradualidad.setVisible(true);
            lblGrad2.setVisible(true);
            cmbGradualidad.getItems().add(0);
            cmbGradualidad.getSelectionModel().selectLast();
            cmbGradualidad.setDisable(true);
        }


        cmbPlazo.setVisible(true);
        cmbBonif.setVisible(true);
    }

    @FXML
    private void limpiarDatosCredito() {

        txtMontoSoli.setTextFormatter(
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


        isRiesgo = false;
        txtMontoSoli.setText("");
        txtMontoSoli.setEditable(true);
        lblTasa.setVisible(false);
        lblGrad2.setVisible(false);
        lblPlazo.setVisible(false);
        lblMora.setVisible(false);
        lblIva.setVisible(false);
        lblEmpresaSal.setVisible(false);
        lblBonif.setVisible(false);
        txtMora.setVisible(false);
        txtIva.setVisible(false);
        txtEmpresa2.setVisible(false);
        cmbTasa.setVisible(false);
        cmbGradualidad.setVisible(false);
        cmbPlazo.setVisible(false);
        cmbBonif.setVisible(false);
        lblError2.setVisible(false);
        lblAvalAplica.setVisible(false);
        cmbAvalAplica.setVisible(false);

    }

    @FXML
    private void agregarAval() {
        String nombreAval = "";
        String numeroAval = "";
        String ahorro ="";
        String direccionAval = "";



        if(cmbOpcionAval.getSelectionModel().getSelectedIndex() == 1){

            if(txtNumSocio.getText().isEmpty() || txtDireccionAval.getText().isEmpty() ){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR AL INTENTAR AGREGAR AL AVAL");
                alert.setHeaderText("ERROR AL INTENTAR AGREGAR AL AVAL");
                alert.setContentText(
                        "POR FAVOR, LLENE TODOS LOS CAMPOS");
                alert.showAndWait();
                return;
            }
            nombreAval = txtNomAval.getText().toString().toUpperCase();
            numeroAval = "N/A";
            ahorro = "N/A";
            direccionAval = txtDireccionAval.getText().trim();
        }else{
            if(txtNumSocio.getText().isEmpty() ||
                    txtNumAval.getText().isEmpty() ){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR AL INTENTAR AGREGAR AL AVAL");
                alert.setHeaderText("ERROR AL INTENTAR AGREGAR AL AVAL");
                alert.setContentText(
                        "CARGUE AL SOCIO AVAL CORRECTAMENTE");
                alert.showAndWait();
                return;
            }
            int numSocio = Integer.parseInt(txtNumAval.getText());

            Map<String, Object> result =
                    servicio.buscarSocioSolicitud(numSocio, "", 0,
                            BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0), BigDecimal.valueOf(0), "", "");
            ahorro = formatoMXN.format((BigDecimal) result.get("AhorrosAntesPs"));
            nombreAval = result.get("NombreFormateado").toString().toUpperCase();
            numeroAval = txtNumAval.getText().toString();
            direccionAval = txtDireccionAval.getText().trim();
        }
        String parentesco  = cmbParentescoAval.getSelectionModel().getSelectedItem().toString().toUpperCase();
        String identificacion = cmbIdAval.getValue().toString();
        Map<String, String> fila = new HashMap<>();
        fila.put("numSocio", numeroAval);
        fila.put("nombre", nombreAval);
        fila.put("ahorro", ahorro);
        fila.put("parentesco", parentesco);
        fila.put("identificacion", identificacion);
        fila.put("direccionAval", direccionAval);


        tblAvales.getItems().add(fila);
        txtNumAval.setText("");
        txtNomAval.setText("");
        txtDireccionAval.setText("");
        lblError3.setVisible(false);
        txtNomAval.setEditable(true);
        txtNumAval.setEditable(true);
        txtDireccionAval.setEditable(true);
        cmbOpcionAval.setDisable(false);
        imgBusqueda2.setVisible(true);



    }

    @FXML
    public void CargarAvalConBoton() {
        cargarDatosAval();
    }

    @FXML
    private void agregarPropietario() {
        String nomPropietario = txtPropietario.getText().toUpperCase();

        Map<String, String> fila = new HashMap<>();
        fila.put("nombrePropietario", nomPropietario);
        tblPropietarios.getItems().add(fila);


        txtPropietario.setText("");


    }


    @FXML
    private void limpiarDatosAval() {
        txtNumAval.setText("");
        txtNomAval.setText("");
        cmbOpcionAval.setDisable(false);
        lblError3.setVisible(false);
        txtNomAval.setEditable(true);
        txtNumAval.setEditable(true);
        txtDireccionAval.setText("");
        tblAvales.getItems().clear();

    }

    @FXML
    private void quitarAval() {
        int index = tblAvales.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            tblAvales.getItems().remove(index);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL INTENTAR ELIMINAR AL AVAL");
            alert.setHeaderText("ERROR AL INTENTAR ELIMINAR AL AVAL");
            alert.setContentText(
                    "POR FAVOR, SELECCIONE UN AVAL PRIMERO.");
            alert.showAndWait();
        }
    }

    @FXML
    private void quitarPropietario() {
        int index = tblPropietarios.getSelectionModel().getSelectedIndex();

        if (index >= 0) {
            tblPropietarios.getItems().remove(index);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL INTENTAR ELIMINAR AL PROPIETARIO");
            alert.setHeaderText("ERROR AL INTENTAR ELIMINAR AL PROPIETARIO");
            alert.setContentText(
                    "POR FAVOR, SELECCIONE UN PROPIETARIO PRIMERO.");
            alert.showAndWait();
        }
    }

    @FXML
    private void cargarDatosAval() {
        if (txtNumAval.getText().isEmpty()) {
            lblError3.setVisible(true);
            return;
        }

        if(txtNumSocio.getText().matches(txtNumAval.getText().toString())){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL INTENTAR BUSCAR AL SOCIO");
            alert.setHeaderText("ERROR AL INTENTAR BUSCAR AL SOCIO");
            alert.setContentText(
                    "EL SOCIO NO PUEDE SER SU PROPIO AVAL");
            alert.showAndWait();
            limpiarDatosAval();
            return;
        }

        lblError3.setVisible(false);
        int numSocio = Integer.parseInt(txtNumAval.getText());
        numeroSocio = numSocio;
        Map<String, Object> result =
                servicio.buscarSocioSolicitud(numSocio, "", 0,
                        BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0),
                        BigDecimal.valueOf(0), BigDecimal.valueOf(0), "", "");

        if (result.get("Resultado").toString().equals("CORRECTO")) {
            List<Object[]> socio = servicio.traerDetalleSocio(numSocio);
            for (Object[] fila : socio) {
                txtDireccionAval.setText("CALLE: " + fila[5].toString().toUpperCase());
            }
            txtDireccionAval.setEditable(false);
            imgBusqueda2.setVisible(false);
            cmbOpcionAval.setDisable(true);
            settearDatosAval(result);

        } else if (result.get("Resultado").toString().equals("INCORRECTO")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL INTENTAR BUSCAR AL SOCIO");
            alert.setHeaderText(result.get("NombreFormateado").toString().toUpperCase());
            alert.setContentText(
                    "ERROR EN LA LÍNEA: "
                            + result.get("NumSocioEncontrado")
                            + " DEL PROCEDIMIENTO ALMACENADO.");
            alert.showAndWait();
        }

    }

    public void settearDatosAval(Map<String, Object> result) {

        txtNomAval.setEditable(false);

        txtNumAval.setEditable(false);
        txtNomAval.setText(result.get("NombreFormateado").toString());

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        cmbBonif.getItems().addAll(opciones);
        cmbOpcionAval.getItems().addAll(opciones);
        cmbGravamen.getItems().addAll(opciones);



        cmbAvalAplica.getSelectionModel().selectFirst();
        cmbBonif.getSelectionModel().selectFirst();
        cmbOpcionAval.getSelectionModel().selectFirst();
        cmbGravamen.getSelectionModel().selectFirst();



        txtDirecPropiedad.setTextFormatter(
                new TextFormatter<>(
                        change -> {
                            change.setText(change.getText().toUpperCase());
                            return change;
                        }));

        txtDireccionAval.setTextFormatter(
                new TextFormatter<>(
                        change -> {
                            change.setText(change.getText().toUpperCase());
                            return change;
                        }));

        txtPropietario.setTextFormatter(
                new TextFormatter<>(
                        change -> {
                            change.setText(change.getText().toUpperCase());
                            if (change.getText().matches("[0-9]")) {
                                change.setText("");
                            }
                            return change;
                        }));



        txtNomAval.setTextFormatter(
                new TextFormatter<>(
                        change -> {
                            change.setText(change.getText().toUpperCase());
                            if (change.getText().matches("[0-9]")) {
                                change.setText("");
                            }
                            return change;
                        }));


        txtNumSocio.setTextFormatter(
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


        txtNumAval.setTextFormatter(
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


        txtMontoSoli.setTextFormatter(
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

        List<Object[]> tiposCredito;
        tiposCredito = servicio.traerTiposCreditoCaso1();

        cmbTipo.getItems().clear();
        for (Object[] fila : tiposCredito) {
            String nombreCredito = fila[2].toString();
            cmbTipo.getItems().add(nombreCredito);
        }
        if (!tiposCredito.isEmpty()) {
            cmbTipo.getSelectionModel().selectFirst();
        }


        colNumSocio.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("numSocio"))
        );

        colNombre.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("nombre"))
        );

        colAhorro.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("ahorro"))
        );

        colParentesco.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("parentesco"))
        );

        colIdentificacion.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("identificacion"))
        );

        colDireccion.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get("direccionAval"))
        );


        colPropietario.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().get("nombrePropietario")
                )
        );

    }

    @FXML
    public void cargarConTecla(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            cargarDatos();
        }else if(event.getCode().equals(KeyCode.ESCAPE)){
            limpiarDatos();
        }
    }

    @FXML
    public void cargarConTeclaDatosCredito(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            cargarDatosCredito();
        }else if(event.getCode().equals(KeyCode.ESCAPE)){
            limpiarDatosCredito();
        }
    }

    @FXML
    private void guardarSolicitud() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("GUARDADO DE SOLICITUD");
        alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA PROCESAR ESTA SOLICITUD?");
        alert.setContentText("EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() == ButtonType.CANCEL) {
            return;
        }

        JSONArray datosAval = new JSONArray();
        JSONArray datosPropietario = new JSONArray();

        String datosAvalEnviar = "";
        String datosPropietarioEnviar = "";

        int esDeRiesgo = isRiesgo ? 1 : 0;

        String asesor = LoginController.usuarioLoggeado;
        int numSocio = Integer.parseInt(txtNumSocio.getText());
        String tipoCred = cmbTipo.getSelectionModel().getSelectedItem().toString();
        String empresa = txtEmpresa2.getText();
        int plazo = Integer.parseInt(cmbPlazo.getSelectionModel().getSelectedItem().toString());

        BigDecimal ahorro = BigDecimal.valueOf(parseMoneda(txtTotalAhorros.getText()));
        BigDecimal tasa = BigDecimal.valueOf(parsePorcentaje(cmbTasa.getSelectionModel().getSelectedItem().toString()));
        BigDecimal mora = BigDecimal.valueOf(parsePorcentaje(txtMora.getText()));
        BigDecimal iva = BigDecimal.valueOf(parsePorcentaje(txtIva.getText()));
        BigDecimal gradualidad = BigDecimal.valueOf(0);

        if (isRiesgo) {
            gradualidad = BigDecimal.valueOf(
                    Double.parseDouble(cmbGradualidad.getSelectionModel().getSelectedItem().toString()));
        }


        BigDecimal montoRiesgo = BigDecimal.valueOf(parseMoneda(txtRiesgoConf.getText()));

        String bonifAplica = cmbBonif.getSelectionModel().getSelectedItem().toString();
        String tipoRiesgo = isRiesgo ? cmbAvalAplica.getSelectionModel().getSelectedItem().toString() : "";

        String direc = txtDirecPropiedad.getText();
        boolean gravamen = "Sí".equals(cmbGravamen.getSelectionModel().getSelectedItem().toString());

        String acreditacion = isRiesgo
                ? cmbAcreditacion.getSelectionModel().getSelectedItem().toString()
                : "";

        if (isRiesgo) {

            for (int i = 0; i < tblAvales.getItems().size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("numero_socio", colNumSocio.getCellData(i));
                obj.put("nombre", colNombre.getCellData(i));
                obj.put("parentesco", colParentesco.getCellData(i));
                obj.put("identificacion", colIdentificacion.getCellData(i));
                obj.put("direccionAval", colDireccion.getCellData(i));
                datosAval.add(obj);
            }

            for (int i = 0; i < tblPropietarios.getItems().size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("nombre", colPropietario.getCellData(i));
                datosPropietario.add(obj);
            }

            datosAvalEnviar = datosAval.toJSONString();
            datosPropietarioEnviar = datosPropietario.toJSONString();
        } else {
            datosAvalEnviar = datosAval.toJSONString();
            datosPropietarioEnviar = datosPropietario.toJSONString();
        }




        String res = servicio.guardarSolicitud(
                asesor,
                numSocio,
                tipoCred,
                empresa,
                montoSolicitado,
                plazo,
                ahorro,
                tasa,
                mora,
                iva,
                esDeRiesgo,
                gradualidad,
                montoRiesgo,
                bonifAplica,
                tipoRiesgo,
                datosAvalEnviar,
                datosPropietarioEnviar,
                direc,
                gravamen,
                acreditacion,
                ""
        );

        alert = new Alert(res.equals("CORRECTO")
                ? Alert.AlertType.INFORMATION
                : Alert.AlertType.ERROR);

        alert.setTitle(res.equals("CORRECTO") ? "GUARDADO CORRECTO" : "ERROR EN EL GUARDADO");
        alert.setHeaderText(res.equals("CORRECTO")
                ? "SOLICITUD GUARDADA"
                : "ERROR AL GUARDAR LA SOLICITUD");
        alert.setContentText(res.equals("CORRECTO")
                ? "SOLICITUD DEL SOCIO: " + numSocio + " GUARDADA CORRECTAMENTE."
                : res.toUpperCase());

        alert.showAndWait();

        tabPane.getSelectionModel().clearAndSelect(0);
        limpiarDatosAval();
        limpiarDatos();
        limpiarDatosCredito();
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

    private double parsePorcentaje(String porcentaje) {
        try {
            Number numero = formatoPorcentaje.parse(porcentaje);
            return numero.doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }



}
