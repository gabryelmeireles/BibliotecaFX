package com.biblioteca.biblioteca.controller;

import com.biblioteca.biblioteca.model.bo.UsuarioBO;
import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.util.LogUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    // Campos do formulário de login
    @FXML private TextField emailField;
    @FXML private PasswordField senhaField;
    @FXML private Label mensagemLabel; // exibe erros de autenticação

    private UsuarioBO bo = new UsuarioBO();

    // Acionado ao clicar em "Entrar"
    @FXML
    private void handleLogin() {
        try {
            // BO valida os campos e consulta o banco; lança exceção se inválido
            Usuario u = bo.autenticar(emailField.getText(), senhaField.getText());
            LogUtil.registrarAcao("Login realizado", u.getEmail());

            // Carrega a tela principal e passa o usuário autenticado para ela
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/biblioteca/biblioteca/view/main-view.fxml"));
            Parent root = loader.load();

            MainController mc = loader.getController();
            mc.setUsuarioLogado(u);

            // Substitui o conteúdo da janela atual pela tela principal
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            LogUtil.registrarExcecao("Login", emailField.getText(), e);
            mensagemLabel.setText(e.getMessage()); // exibe o erro sem detalhes técnicos
        }
    }
}