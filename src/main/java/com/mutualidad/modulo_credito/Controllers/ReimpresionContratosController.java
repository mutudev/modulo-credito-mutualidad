package com.mutualidad.modulo_credito.Controllers;

import com.mutualidad.modulo_credito.Main;
import com.mutualidad.modulo_credito.Models.ModelCredito;
import com.mutualidad.modulo_credito.Models.ModelEmpresa;
import com.mutualidad.modulo_credito.Models.ModelSolicitud;
import com.mutualidad.modulo_credito.Services.Servicio;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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
            creditos.add(creditoSimple);
        }
        data =
                FXCollections.observableArrayList(creditos);
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
            cmbEmpresa.getSelectionModel().selectFirst();
        } else if (event.getCode().equals(KeyCode.F3) && lblBusquedaFolio.getText().equals("[F3] Buscar Por Número de Socio")) {
            imgBusqueda.setVisible(true);
            cmbEmpresa.setDisable(false);
            lblNumYFolio.setText("Número de Socio:");
            lblBusquedaFolio.setText("[F3] Buscar Por Folio");
            txtNumSocio.setText("");
        }
    }

    @FXML
    public void mostrarPagare() {

    }

    @FXML
    public void mostrarTablaAmortizacion() {

    }

    @FXML
    public void mostrarContrato() {

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
