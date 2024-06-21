/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clienteescritoriosmartcupon;

import clienteescritoriosmartcupon.modelo.pojo.RespuestaLogin;
import clienteescritoriosmartcupon.modelo.pojo.Usuario;
import clienteescritoriosmartcupon.modelo.pojo.dao.InicioDeSesionDAO;
import clienteescritoriosmartcupon.utils.Utilidades;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author mateo
 */
public class FXMLLoginController implements Initializable {

    @FXML
    private TextField tfUsuario;
    @FXML
    private Label lbErrorUsuario;
    @FXML
    private PasswordField tfPassword;
    @FXML
    private Label lbErrorPassword;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lbErrorPassword.setVisible(false);
        lbErrorUsuario.setVisible(false);
    }

    @FXML
    private void btLogin(ActionEvent event) {

        String username = tfUsuario.getText();
        String password = tfPassword.getText();
        boolean isValido = false;

        if (username.isEmpty()) {
            lbErrorUsuario.setVisible(true);
            lbErrorUsuario.setText("El usuario es requerido *");
        } else {
            lbErrorUsuario.setVisible(false);
            isValido = true;
        }

        if (password.isEmpty()) {
            lbErrorPassword.setVisible(true);
            lbErrorPassword.setText("La contraseña es requerida *");
        } else {
            lbErrorPassword.setVisible(false);
            isValido = true;
        }

        if (isValido) {
            verificarSesion(username, password);
        }

    }

    private void verificarSesion(String username, String password) {

        RespuestaLogin respuestaValidacionLogin = InicioDeSesionDAO.validarLogin(username, password);
        if (!respuestaValidacionLogin.getError()) {
            Utilidades.mostrarAlertaSimple("Credenciales correctas",
                    respuestaValidacionLogin.getContenido(), Alert.AlertType.INFORMATION);
            irPantallaPrincipal(respuestaValidacionLogin.getUsuarioSesion());
        } else {

            Utilidades.mostrarAlertaSimple("Credenciales incorrectas",
                    respuestaValidacionLogin.getContenido(), Alert.AlertType.INFORMATION);
        }
    }

    private void irPantallaPrincipal(Usuario usuarioSesion) {

        try {
            Stage stagePrincipal = (Stage) tfUsuario.getScene().getWindow();

            FXMLLoader loadVista = new FXMLLoader(getClass().getResource("FXMLHome.fxml"));
            Parent vista = loadVista.load();

            FXMLHomeController controladorHome = loadVista.getController();
            controladorHome.inicializarHome(usuarioSesion);
            
            Scene scene = new Scene(vista);
            stagePrincipal.setTitle("Inicio");
            stagePrincipal.setScene(scene);
            stagePrincipal.show();

        } catch (IOException ex) {
            Logger.getLogger(FXMLLoginController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
