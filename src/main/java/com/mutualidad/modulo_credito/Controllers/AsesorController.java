package com.mutualidad.modulo_credito.Controllers;

import com.mutualidad.modulo_credito.Models.ModelUsuario;
import com.mutualidad.modulo_credito.Services.Servicio;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.synedra.validatorfx.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class AsesorController implements Initializable {

    public String usuario;
    public int rolUsuario;

    private Map<String, Label> labelMap = new HashMap<>();

    @FXML
    private Label lblHora, lblFecha, lblUsuario;

    @FXML
    private Label lblInicio, lblNuvSolicitud, lblRegistros, lblGradualidad, lblCreAut, lblProyeccion, lblEstadoCuenta,
            lblHistorialPagos, lblCarteras, lblContratos;

    @FXML private GridPane gridOpciones;

    @FXML
    private StackPane contentArea;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Servicio servicio;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        iniciarReloj();
        try {
            inicio();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LocalDate fecha = servicio.traerFechaHoy();
        lblFecha.setText(fecha.getDayOfMonth() + "/" + fecha.getMonthValue() + "/" + fecha.getYear());
        Platform.runLater(() -> {
            Stage stage = (Stage) lblHora.getScene().getWindow();
            stage.setOnCloseRequest(event -> cierreDeVentana(event));
        });
    }

    public void cierreDeVentana(Event event) {
        event.consume();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("CIERRE DE APLICATIVO");
        alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR EL APLICATIVO?");
        alert.setContentText(
                "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage ventanaActual = (Stage) lblHora.getScene().getWindow();
            ventanaActual.close();
        }
    }

    public void iniciarReloj() {
        Thread hiloReloj =
                new Thread(
                        () -> {
                            SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
                            while (true) {
                                try {
                                    String horaActual = formatoHora.format(new Date());
                                    Platform.runLater(() -> lblHora.setText(horaActual));
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
        hiloReloj.setDaemon(true);
        hiloReloj.start();
    }

    public void setUsuario(String usuario, int Rol) {
        this.usuario = usuario;
        this.rolUsuario = Rol;
        lblUsuario.setText(usuario);
        generarModulos();
    }

    @FXML
    public void inicio() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/java/fx/inicio.fxml")
        );

        loader.setControllerFactory(context::getBean);

        Parent fxml = loader.load();
        contentArea.getChildren().setAll(fxml);
    }

    @FXML
    public void solicitudes() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/java/fx/solicitudes.fxml")
        );

        loader.setControllerFactory(context::getBean);

        Parent fxml = loader.load();
        contentArea.getChildren().setAll(fxml);
    }

    @FXML
    public void registrosPendientes() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/java/fx/listadoSolicitudes.fxml")
        );

        loader.setControllerFactory(context::getBean);

        Parent fxml = loader.load();
        contentArea.getChildren().setAll(fxml);
    }

    @FXML
    public void reimpresionContratos() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/java/fx/reimpresionContratos.fxml")
        );

        loader.setControllerFactory(context::getBean);

        Parent fxml = loader.load();
        contentArea.getChildren().setAll(fxml);
    }

    @FXML
    public void calculoGradualidad() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/java/fx/gradualidad.fxml")
        );

        loader.setControllerFactory(context::getBean);

        Parent fxml = loader.load();
        contentArea.getChildren().setAll(fxml);
    }

    @FXML
    public void calcularProyeccion() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/java/fx/proyecciones.fxml")
        );

        loader.setControllerFactory(context::getBean);

        Parent fxml = loader.load();
        contentArea.getChildren().setAll(fxml);
    }

    @FXML
    public void carterasCredito() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/java/fx/carteras.fxml")
        );

        loader.setControllerFactory(context::getBean);

        Parent fxml = loader.load();
        contentArea.getChildren().setAll(fxml);
    }

    @FXML
    public void creditosAutorizados() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/java/fx/creditosAutorizados.fxml")
        );

        loader.setControllerFactory(context::getBean);

        Parent fxml = loader.load();
        contentArea.getChildren().setAll(fxml);
    }

    @FXML
    public void estadoCuenta() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/java/fx/estadoCuenta.fxml")
        );

        loader.setControllerFactory(context::getBean);

        Parent fxml = loader.load();
        contentArea.getChildren().setAll(fxml);
    }

    @FXML
    public void historialPagos() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/java/fx/historialPagos.fxml")
        );

        loader.setControllerFactory(context::getBean);

        Parent fxml = loader.load();
        contentArea.getChildren().setAll(fxml);
    }

    public void generarModulos() {
        System.out.println("me ejecuté");

        ModelUsuario usuario = servicio.traerUsuario(LoginController.usuarioLoggeado);

        List<Object[]> result = servicio.traerModulos(usuario.getId());
        List<String> modulos = new ArrayList<>();

        for (Object[] row : result) {
            String descripcion = (String) row[2];
            modulos.add(descripcion);
        }

        for (int i = 0; i < modulos.size(); i++) {
            switch (modulos.get(i)) {
                case "INICIO":
                    labelMap.put(modulos.get(i), lblInicio);
                    break;
                case "NUEVA SOLICITUD":
                    labelMap.put(modulos.get(i), lblNuvSolicitud);
                    break;
                case "SOLICITUDES PENDIENTES":
                    labelMap.put(modulos.get(i), lblRegistros);
                    break;
                case "GRADUALIDAD":
                    labelMap.put(modulos.get(i), lblGradualidad);
                    break;
                case "CRÉDITOS AUTORIZADOS":
                    labelMap.put(modulos.get(i), lblCreAut);
                    break;
                case "PROYECCIONES":
                    labelMap.put(modulos.get(i), lblProyeccion);
                    break;
                case "ESTADO DE CUENTA":
                    labelMap.put(modulos.get(i), lblEstadoCuenta);
                    break;
                case "HISTORIAL DE PAGOS":
                    labelMap.put(modulos.get(i), lblHistorialPagos);
                    break;
                case "CARTERAS":
                    labelMap.put(modulos.get(i), lblCarteras);
                    break;
                case "CONTRATOS":
                    labelMap.put(modulos.get(i), lblContratos);
                    break;
            }
        }

        ObservableList<String> moduloXrol = FXCollections.observableArrayList();
        moduloXrol.addAll(modulos);

        gridOpciones.getChildren().clear();

        int fila = 0;
        for (String modulo : moduloXrol) {

            if (moduloXrol.isEmpty()) {
                break;
            }

            if (labelMap.containsKey(modulo)) {
                Label label = labelMap.get(modulo);
                gridOpciones.add(label, 0, fila);
                label.setVisible(true);
                label.setDisable(false);
                label.setCursor(Cursor.HAND);
                fila++;
            }
        }
    }




}
