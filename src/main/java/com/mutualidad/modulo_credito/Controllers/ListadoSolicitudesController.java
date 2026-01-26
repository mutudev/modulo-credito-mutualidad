package com.mutualidad.modulo_credito.Controllers;

import com.mutualidad.modulo_credito.Main;
import com.mutualidad.modulo_credito.Models.ModelSocio;
import com.mutualidad.modulo_credito.Models.ModelSolicitud;
import com.mutualidad.modulo_credito.Services.Servicio;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class ListadoSolicitudesController implements Initializable {

    @FXML
    private TableView<ModelSolicitud> tblSolicitudes;

    @FXML
    private TableColumn<ModelSolicitud, String> colFolio;

    @FXML
    private TableColumn<ModelSolicitud, String> colNumSocio;

    @FXML
    private TableColumn<ModelSolicitud, String> colNombre;

    @FXML
    private TableColumn<ModelSolicitud, String> colMonto;

    @FXML
    private TableColumn<ModelSolicitud, Void> colAcciones;

    @Autowired
    private Servicio servicio;

    NumberFormat formatoMXN = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // 🔹 Vincular columnas de texto
        colFolio.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getId().toString()));

        colNumSocio.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNumSocio().toString()));

        colNombre.setCellValueFactory(data -> {

            int numSocio = data.getValue().getNumSocio();

            if (numSocio == 0) {
                return new SimpleStringProperty("");
            }


            return new SimpleStringProperty(servicio.obtenerSocioConNumero(numSocio));
        });


        colMonto.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(formatoMXN.format(data.getValue().getMonto())));

        colAcciones.setCellFactory(param -> new TableCell<>() {

            Button btnEditar = new Button();
            Button btnConfirmar = new Button();
            Button btnInfo = new Button();
            Button btnEliminar = new Button();

            ImageView iconEditar = new ImageView(
                    new Image(getClass().getResourceAsStream("/assets/images/icono-editar.png"))
            );
            ImageView iconVer = new ImageView(
                    new Image(getClass().getResourceAsStream("/assets/images/icono-confirmar.png"))
            );
            ImageView iconInfo = new ImageView(
                    new Image(getClass().getResourceAsStream("/assets/images/icono-info.png"))
            );
            ImageView iconEliminar = new ImageView(
                    new Image(getClass().getResourceAsStream("/assets/images/icono-eliminar.png"))
            );

            private final HBox hbox = new HBox(8, btnInfo, btnEditar,btnEliminar ,btnConfirmar );

            {
                // Centrar botones
                hbox.setAlignment(Pos.CENTER);

                iconEditar.setFitWidth(16);
                iconEditar.setFitHeight(16);

                iconVer.setFitWidth(16);
                iconVer.setFitHeight(16);

                iconInfo.setFitWidth(16);
                iconInfo.setFitHeight(16);

                iconEliminar.setFitWidth(16);
                iconEliminar.setFitHeight(16);

                btnEditar.setGraphic(iconEditar);
                btnConfirmar.setGraphic(iconVer);
                btnInfo.setGraphic(iconInfo);
                btnEliminar.setGraphic(iconEliminar);

                btnEditar.setCursor(Cursor.HAND);
                btnConfirmar.setCursor(Cursor.HAND);
                btnInfo.setCursor(Cursor.HAND);
                btnEliminar.setCursor(Cursor.HAND);

                btnEditar.setStyle("-fx-background-color: #f1c40f;");
                btnConfirmar.setStyle("-fx-background-color: #2ecc71;");
                btnInfo.setStyle("-fx-background-color: #3498db;"); // Azul
                btnEliminar.setStyle("-fx-background-color: #e74c3c;");

                btnInfo.setOnAction(e -> {

                    try {
                        ModelSolicitud solicitud =
                                getTableView().getItems().get(getIndex());
                        int idSolicitud = solicitud.getId();
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/com/java/fx/verSolicitud.fxml")
                        );
                        loader.setControllerFactory(Main.context::getBean);
                        Scene scene = new Scene(loader.load());
                        VerSolicitudController controller =
                                loader.getController();
                        controller.settearDatos(idSolicitud);
                        Stage stage = new Stage();
                        stage.setTitle("Detalle de Solicitud");
                        stage.setScene(scene);
                        stage.setResizable(false);
                        stage.show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });



                btnEditar.setOnAction(e -> {


                    try {
                        ModelSolicitud solicitud =
                                getTableView().getItems().get(getIndex());
                        int idSolicitud = solicitud.getId();
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/com/java/fx/modificarSolicitudes.fxml")
                        );
                        loader.setControllerFactory(Main.context::getBean);
                        Scene scene = new Scene(loader.load());
                         ModificarSolicitudController controller =
                                loader.getController();
                        controller.settearDatos(idSolicitud);
                        Stage stage = new Stage();
                        stage.setTitle("Modificación de Solicitud");
                        stage.setScene(scene);
                        stage.setResizable(false);
                        stage.show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                });

                btnEliminar.setOnAction(e -> {
                    ModelSolicitud solicitud = getTableView().getItems().get(getIndex());

                    int idSolicitud = solicitud.getId();

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("CANCELACIÓN DE SOLICITUD");
                    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CANCELAR LA SOLICITUD?");
                    alert.setContentText(
                            "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        //SE EJECUTA EL UPDATE
                        String res = servicio.cancelarSolicitud(idSolicitud, "");

                        if (!res.equals("CORRECTO")) {
                            alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("ERROR AL INTENTAR CANCELAR");
                            alert.setHeaderText("ERROR AL INTENTAR CANCELAR LA SOLICITUD DE CRÉDITO.");
                            alert.setContentText(res.toUpperCase());
                            alert.showAndWait();
                        } else {
                            alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle(res.toUpperCase());
                            alert.setHeaderText("SOLICITUD CANCELADA CON ÉXITO");
                            alert.setContentText("SOLICITUD DE CRÉDITO CANCELADA CON ÉXITO");
                            alert.showAndWait();
                        }

                        tblSolicitudes.setItems(null);
                        cargarSolicitudes();

                    }
                });

                btnConfirmar.setOnAction(e -> {
                    try {
                        ModelSolicitud solicitud =
                                getTableView().getItems().get(getIndex());
                        int idSolicitud = solicitud.getId();
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/com/java/fx/confirmarSolicitud.fxml")
                        );
                        loader.setControllerFactory(Main.context::getBean);
                        Scene scene = new Scene(loader.load());
                        ConfirmarSolicitudController controller =
                                loader.getController();

                        controller.settearDatos(idSolicitud);
                        Stage stage = new Stage();
                        stage.setTitle("Confirmar Solicitud");
                        stage.setScene(scene);
                        stage.setResizable(false);
                        stage.show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
                setAlignment(Pos.CENTER); // centra la celda
            }
        });

        // 🔹 Cargar datos (ejemplo)
        cargarSolicitudes();
    }

    private void cargarSolicitudes() {
        List<ModelSolicitud> solicitudes = servicio.obtenerSolicitudesPendientes(2);
        ObservableList<ModelSolicitud> data =
                FXCollections.observableArrayList(solicitudes);

        tblSolicitudes.setItems(data);
    }
}
