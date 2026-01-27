package com.mutualidad.modulo_credito.Controllers;

import com.mutualidad.modulo_credito.Main;
import com.mutualidad.modulo_credito.Models.ModelCredito;
import com.mutualidad.modulo_credito.Models.ModelSocio;
import com.mutualidad.modulo_credito.Services.Servicio;
import com.tenpisoft.n2w.MoneyConverters;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class GradualidadController implements Initializable {

    @FXML
    private TextField txtNumSocio, txtNomSocio, txtGrad, txtVeces;

    @FXML
    private Label lblAviso;

    @FXML
    private Button btnCalcular, btnLimpiar, btnImprimir;

    @FXML
    private ImageView imgBusqueda;

    @FXML
    private TableView<ModelCredito> tblCreditos;

    @FXML
    private TableColumn<ModelCredito, String> colFolio;

    @FXML
    private TableColumn<ModelCredito, String> colMonto;

    @FXML
    private TableColumn<ModelCredito, String> colTasa;

    @FXML
    private TableColumn<ModelCredito, String> colEstado;

    @FXML
    private TableColumn<ModelCredito, String> colVencido;

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

        colTasa.setCellValueFactory(data ->
                new SimpleStringProperty(
                        formatoPorcentaje.format(data.getValue().getTasa())
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

        colVencido.setCellValueFactory(data -> {
            int credito_id = data.getValue().getId();
            String texto ="";
            List<Object[]> creditos = servicio.traerCreditosParaGradualidades(credito_id);
            for (Object[] fila : creditos) {

                if(Integer.parseInt(fila[7].toString()) > 0){
                    texto = "Sí";
                }else {
                    texto = "No";
                }
                break;
            }
            return new SimpleStringProperty(texto);
        });

        txtNumSocio.setTextFormatter(
                new TextFormatter<>(change -> {
                    // Permite solo dígitos
                    if (change.getText().matches("[0-9]*")) {
                        return change;
                    }
                    return null;
                })
        );
    }

    @FXML
    public void cargarConTecla() {
        cargarCreditos();
    }

    public void cargarSocioBuscado(String numSocio) {
        txtNumSocio.setText(numSocio);
        cargarCreditos();
    }


    @FXML
    public void generarReporte() {

        if(txtNomSocio.getText().trim().equals("")){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("ERROR AL INTENTAR IMPRIMIR EL REPORTE");
            alert.setContentText(
                    "POR FAVOR, CARGUE A UN SOCIO PRIMERO");
            alert.showAndWait();
            return;
        }

        LocalDateTime fecha = LocalDateTime.now();
        String fechaForm = fecha.getDayOfMonth() + "/" + fecha.getMonthValue() + "/" + fecha.getYear();
        Map pars = new HashMap<>();
        pars.put("nomSocio", txtNomSocio.getText());
        pars.put("numSocio",Integer.parseInt(txtNumSocio.getText()));
        pars.put("fecha", fechaForm);
        pars.put("gradualidad", txtGrad.getText());

        try {
            InputStream isRepo = getClass().getResourceAsStream("/Reports/gradualidad.jasper");
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
    public void buscarSocioPorNombre() {
        try {
            Stage nuevaVentana = new Stage();
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/busquedaSocio.fxml"));
            fxml.setControllerFactory(Main.context::getBean);
            Scene nuevaEscena = new Scene(fxml.load());
            BusquedaController controlador = fxml.getController();
            controlador.setGradualidadController(this);
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
    public void limpiar() {
        txtNomSocio.setText("");
        txtNumSocio.setText("");
        btnCalcular.setDisable(false);
        txtNumSocio.setEditable(true);
        imgBusqueda.setVisible(true);
        txtGrad.setText("");
        txtVeces.setText("");
        lblAviso.setText("");
        tblCreditos.getItems().clear();
    }

    @FXML
    public void mostrarGradualidad() {
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
        int creditoId = primeraFila.getId();
        List<Object[]> creditos = servicio.traerCreditosParaGradualidades(creditoId);
        for (Object[] fila : creditos) {
            txtVeces.setText(String.valueOf(Integer.parseInt(fila[7].toString())));
            break;
        }

    }

    public void settearGradualidad() {
        int numSocio = Integer.parseInt(txtNumSocio.getText());
        List<Object[]> creditos = servicio.traerCreditosParaGradualidadesPorNumSocio(numSocio);

        if (creditos.size() == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("AVISO");
            alert.setHeaderText("CÁLCULO DE GRADUALIDAD");
            alert.setContentText(
                    "EL SOCIO NO TIENE CRÉDITOS, NO CORRESPONDE GRADUALIDAD");
            alert.showAndWait();
            limpiar();
            return;
        }

        List<Object[]> gradsMutu = servicio.traerGradualidadesMut();
        List<Object[]> gradsNgu = servicio.traerGradualidadesNgu();
        List<Object> mutuIdx1 = new ArrayList<>();
        List<Object> nguIdx1  = new ArrayList<>();

        for (Object[] fila : gradsMutu) {
            mutuIdx1.add(fila[1].toString());
        }

        for (Object[] fila : gradsNgu) {
            nguIdx1.add(fila[1].toString());
        }

        int contadorGrads = 0;
        for (Object[] fila : creditos) {
            if(fila[10].toString().equals("1.30%")){
                if (Integer.parseInt(fila[7].toString()) >= 5) {
                    contadorGrads = 0;
                } else {
                    if (numSocio <= 8542 && contadorGrads < 2) {
                        contadorGrads++;
                    } else if (numSocio >= 8543 && contadorGrads < 3) {
                        contadorGrads++;
                    }
                }
            }
        }

        String tasaReinicio = "";
        if (numSocio <= 8542) {
            if (contadorGrads == 0) {

                List<Object[]> creditoUltimo = servicio.chequeoGradualidadesReinicio(numSocio);
                for (Object[] fila : creditoUltimo) {
                    tasaReinicio = fila[10].toString();
                }

                if (tasaReinicio.equalsIgnoreCase("1.30%")) {
                    txtGrad.setText("N/A");
                    lblAviso.setText("(EL SOCIO AÚN NO HA REINICIADO SU GRADUALIDAD)");
                    lblAviso.setVisible(true);
                } else {
                    txtGrad.setText("2.0");
                    lblAviso.setText("(EL SOCIO YA PUEDE PRESTAR LA GRADUALIDAD MINIMA SEGÚN NÚMERO DE SOCIO)");
                    lblAviso.setVisible(true);
                }

            } else {
                txtGrad.setText(mutuIdx1.get(contadorGrads - 1).toString());
            }

        } else {
            if (contadorGrads == 0) {

                List<Object[]> creditoUltimo = servicio.chequeoGradualidadesReinicio(numSocio);
                for (Object[] fila : creditoUltimo) {
                    tasaReinicio = fila[10].toString();
                }

                if (tasaReinicio.equalsIgnoreCase("1.30%")) {
                    txtGrad.setText("N/A");
                    lblAviso.setText("(EL SOCIO AÚN NO HA REINICIADO SU GRADUALIDAD)");
                    lblAviso.setVisible(true);
                } else {
                    txtGrad.setText("1.7");
                    lblAviso.setText("(EL SOCIO YA PUEDE PRESTAR LA GRADUALIDAD MINIMA SEGÚN NÚMERO DE SOCIO)");
                    lblAviso.setVisible(true);
                }
            } else {
                txtGrad.setText(nguIdx1.get(contadorGrads - 1).toString());
            }
        }


    }

    @FXML
    public void cargarCreditos() {

        if (txtNumSocio.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("ERROR AL INTENTAR CARGAR LA GRADUALIDAD");
            alert.setContentText(
                    "POR FAVOR, RELLENE TODOS LOS CAMPOS");
            alert.showAndWait();
            return;
        }

        int numSocio = Integer.parseInt(txtNumSocio.getText().trim());
        tblCreditos.getItems().clear();
        txtNumSocio.setEditable(false);
        imgBusqueda.setVisible(false);
        btnCalcular.setDisable(true);
        ModelSocio socio = servicio.traerSocioXNumero(numSocio);
        txtNomSocio.setText(socio.getPrimerNom() + " " + socio.getSegundoNom() + " " + socio.getApellidoP() + " " + socio.getApellidoM());
        List<ModelCredito> creditos = servicio.encontrarCreditosConSocio(numSocio);
        ObservableList<ModelCredito> data = FXCollections.observableArrayList(creditos);
        tblCreditos.setItems(data);
        settearGradualidad();
    }



}
