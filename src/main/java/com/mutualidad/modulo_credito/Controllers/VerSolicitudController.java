package com.mutualidad.modulo_credito.Controllers;

import com.mutualidad.modulo_credito.Services.Servicio;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;





@Component
public class VerSolicitudController implements Initializable {



    @FXML
    private Button btnCerrar;
    @FXML
    private Label lblAvalAplica;

    @FXML
    private TextField txtNumSocio, txtNombre, txtAhorros, txtTotalAhorros,
            txtEmpresa, txtMontoSoli, txtMora, txtIva, txtEmpresa2,
            txtTipo, txtTasa, txtGrad, txtPlazo, txtAval, txtBonif,
            txtDirecPropiedad,txtNumSocio3, txtNomSocio3,
            txtAhorrosConf, txtMontoConf, txtRiesgoConf, txtEmisorConf, txtGradConf,
            txtTasaOrdConf, txtMoraConf, txtTasaIvaConf, txtBonifConf, txtTipoConf,
            txtAvalConf, txtTipoRiesgoConf, txtPlazoConf, txtAsesor, txtId, txtAcred, txtGrav;


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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

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
    }

    public void settearDatos(int idCredito) {

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

        if (Integer.parseInt(numSocio) <= 8542 ) {
            txtEmpresa.setText(servicio.traerEmpresa("0001").getNombre());
        } else {
            txtEmpresa.setText(servicio.traerEmpresa("0002").getNombre());
        }


        txtId.setText(id);
        txtNumSocio.setText(numSocio);
        txtAsesor.setText(asesor);
        txtNombre.setText(nombreSocio);
        txtTotalAhorros.setText(ahorrosMomento);
        txtAsesor.setText(asesor);
        txtMontoSoli.setText(monto);
        txtTipo.setText(nombreCredito);
        txtTasa.setText(tasa);
        txtGrad.setText(gradualidad);
        txtPlazo.setText(plazo);
        txtMora.setText(mora);
        txtIva.setText(iva);
        txtEmpresa2.setText(nomEmpresa);

        if(bonifAplica.equals("true")){
            txtBonif.setText("Sí");
        } else {
            txtBonif.setText("No");
        }

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

                propietariosLista.put("nombrePropietario", nombre);
                tblPropietarios.getItems().add(propietariosLista);
            }

            txtDirecPropiedad.setText(direccion);
            txtAcred.setText(acreditacion);
            txtGrav.setText(gravamen);





        }else {
            tabPropiedad.setDisable(true);

        }








    }


    private void bloquearTxts() {
        txtId.setEditable(false);
        txtNumSocio.setEditable(false);
        txtAsesor.setEditable(false);
        txtNombre.setEditable(false);
        txtTotalAhorros.setEditable(false);
        txtMontoSoli.setEditable(false);
        txtTipo.setEditable(false);
        txtTasa.setEditable(false);
        txtGrad.setEditable(false);
        txtPlazo.setEditable(false);
        txtMora.setEditable(false);
        txtIva.setEditable(false);
        txtEmpresa.setEditable(false);
        txtEmpresa2.setEditable(false);
        txtBonif.setEditable(false);
        txtAval.setEditable(false);
    }

}
