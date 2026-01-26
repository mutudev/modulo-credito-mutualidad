package com.mutualidad.modulo_credito.Controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class AsesorController implements Initializable {

    public String usuario;
    public int rolUsuario;
    public boolean apertura = false;
    public String turno = "";

    @FXML
    private Label lblHora, lblFecha, lblUsuario;

    @FXML
    private StackPane contentArea;

    @Autowired
    private ApplicationContext context;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        iniciarReloj();
        try {
            inicio();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LocalDateTime fecha = LocalDateTime.now();
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


}
