package com.mutualidad.modulo_credito.Controllers;


import com.mutualidad.modulo_credito.Main;
import com.mutualidad.modulo_credito.Services.Servicio;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import net.synedra.validatorfx.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class LoginController implements Initializable {

    @FXML
    private Button btnIngresar;

    @FXML private Button btnCancelar, btnEjemplo;

    @FXML private TextField txtUsuario;

    @FXML private ListView listUsuarios;

    @FXML private PasswordField txtPass;

    @FXML private Label lblHora;

    @FXML private Label lblFecha, lblValUser;

    public int rol = 0;

    private final Validator validator = new Validator();

    public static String usuarioLoggeado = "";
    public static String turno = "";

    @Autowired
    private Servicio usuarioService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        validator
                .createCheck()
                .dependsOn("input", txtUsuario.textProperty())
                .withMethod(
                        c -> {
                            String texto = c.get("input");
                            if (texto == null || texto.isEmpty()) {
                                c.error("El campo no puede estar vacío");
                                lblValUser.setText("El campo no puede estar vacío");
                            } else if (texto.matches(".*\\d.*")) {
                                c.error("No se permiten números en este campo");
                                lblValUser.setText("No se permiten números en este campo");
                            } else if (texto.length() < 3) {
                                c.error("El texto debe tener al menos 3 caracteres");
                                lblValUser.setText("El texto debe tener al menos 3 caracteres");
                            } else {
                                lblValUser.setText("");
                            }
                        })
                .decorates(txtUsuario)
                .immediate();

        // Formateo a mayúsculas
        txtUsuario.setTextFormatter(
                new TextFormatter<>(
                        change -> {
                            change.setText(change.getText().toUpperCase());
                            return change;
                        }));

        iniciarReloj();

        LocalDateTime fecha = LocalDateTime.now();
        lblFecha.setText(
                "FECHA: " + fecha.getDayOfMonth() + "/" + fecha.getMonthValue() + "/" + fecha.getYear());

        Platform.runLater(
                () -> {
                    Stage stage = (Stage) btnCancelar.getScene().getWindow();
                    stage.setOnCloseRequest(event -> cierreDeVentana(event));
                });
    }

    @FXML
    public void prueba() {
        Map pars = new HashMap<>();
        String html =
                "<html>" +
                        "<body style='font-size:11pt; font-family:Serif;'>" +

                        "<b>CONTRATO</b> QUE CELEBRAN POR UNA PARTE EL ING. CARLOS ALBERTO VILLANUEVA RUIZ COMO " +
                        "APODERADO DE <b>“NUEVA GENERACION DE UMAN ASOCIACION CIVIL”</b> y sus correspondientes " +
                        "abreviaturas <b>“NUEVA GENERACION DE UMAN A.C.”</b> Y POR LA OTRA MARIA JUSTINA CHE IUIT " +
                        "Y MARIA ANGELICA CHE IUIT, AL TENOR DE LAS SIGUIENTES CLAUSULAS." +

                        "<br/><br/>" +

                        "<div style='text-align:center;'><b>DECLARACIONES</b></div>" +

                        "<br/>" +

                        "<b>1.</b> CARLOS ALBERTO VILLANUEVA RUIZ, como Apoderado Legal de " +
                        "<b>“NUEVA GENERACION DE UMAN ASOCIACION CIVIL”</b>, también conocida como y sus correspondientes " +
                        "abreviaturas <b>“NUEVA GENERACION DE UMAN A.C.”</b>, manifiesta ser Ingeniero Civil, " +
                        "mayor de edad legal, continúa declarando tener como domicilio el predio marcado con el " +
                        "número ciento ochenta y tres, de la calle nueve, en la ciudad de Umán, Yucatán, México." +

                        "<br/><br/>" +

                        "<b>Prestamista</b>" +

                        "<br/><br/>" +

                        "<b>2.</b> MARIA JUSTINA CHE IUIT, manifiesta ser <b>SOLTERO(A)</b>, CON OFICIO " +
                        "<b>VENTA DE PERIODICOS</b>, continúa declarando tener como domicilio el predio ubicado " +
                        "en la calle C-20 B N°93-B X 7 Y 9, COL. SAN FRANCISCO, de la Col. SAN FRANCISCO, " +
                        "en la ciudad de UMAN, YUCATAN, México." +

                        "<br/><br/>" +

                        "<b>Aval</b>" +

                        "<br/><br/>" +

                        "<b>3.</b> MARIA ANGELICA CHE IUIT, manifiesta ser <b>SOLTERO(A)</b>, CON OFICIO " +
                        "<b>OBRERA</b>, continúa declarando tener como domicilio el predio ubicado en la calle " +
                        "C-20 B N°93-B X 7 Y 9, de la Col. SAN FRANCISCO, en la ciudad de UMAN, YUCATAN, México." +

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


        String firmas =
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


        String aviso =
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


    public void cierreDeVentana(Event event) {
        event.consume();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("CIERRE DE APLICATIVO");
        alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR EL APLICATIVO?");
        alert.setContentText(
                "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
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
                                    Platform.runLater(() -> lblHora.setText("HORA: " + horaActual));
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
        hiloReloj.setDaemon(true);
        hiloReloj.start();
    }

    @FXML
    public void cerrarConBoton() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("CIERRE DE APLICATIVO");
        alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR EL APLICATIVO?");
        alert.setContentText(
                "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
            ventanaActual.close();
        }
    }

    @FXML
    public void EntrarConBoton(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            validarLogin();
        }
    }


    @FXML
    public void validarLogin() {
        Map<String, Object> result =
                usuarioService.validarLogin(txtUsuario.getText().trim(), txtPass.getText().trim(), "", 0);
        if (validator.validate() && result.get("Resultado").toString().equals("CORRECTO")) {
            try {

                LocalDateTime fecha = LocalDateTime.now();

                if (fecha.getHour() >= 7 && fecha.getHour() <= 12) {
                    turno = "MAT";
                } else if (fecha.getHour() >= 14 && fecha.getHour() <= 20) {
                    turno = "VESP";
                }

                rol = Integer.parseInt(result.get("Rol").toString());
                usuarioLoggeado = txtUsuario.getText().trim();
                Stage ventanaActual = (Stage) btnIngresar.getScene().getWindow();
                Stage nuevaVentana = new Stage();
                FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/mainAsesor.fxml"));
                fxml.setControllerFactory(Main.context::getBean);
                Scene nuevaEscena = new Scene(fxml.load());
                AsesorController controlador = fxml.getController();
                controlador.setUsuario(txtUsuario.getText().trim(), rol);
                nuevaEscena
                        .getStylesheets()
                        .add(getClass().getResource("/assets/css/estilos.css").toExternalForm());
                nuevaVentana.setTitle("INICIO - ASESOR");
                Image icon = new Image(getClass().getResourceAsStream("/assets/images/logo.png"));
                nuevaVentana.getIcons().add(icon);
                nuevaVentana.setScene(nuevaEscena);
                nuevaVentana.setResizable(false);
                nuevaVentana.centerOnScreen();
                nuevaVentana.show();
                ventanaActual.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR AL INTENTAR INICIAR SESIÓN");
            alert.setHeaderText(result.get("Resultado").toString().toUpperCase());
            alert.setContentText("ERROR CON SEVERIDAD: " + result.get("Rol"));
            alert.showAndWait();
        }
    }

}
