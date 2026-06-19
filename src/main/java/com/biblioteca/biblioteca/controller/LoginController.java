package com.biblioteca.biblioteca.controller;

import com.biblioteca.biblioteca.model.bo.UsuarioBO;
import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.util.LogUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

// Controlador da tela de login. Gerencia o formulário e a lógica de autenticação.
public class LoginController {
    // @FXML conecta a variável Java ao componente com o mesmo fx:id no arquivo login-view.fxml
    @FXML private TextField emailField;
    @FXML private PasswordField senhaField;  // PasswordField oculta os caracteres digitados
    @FXML private Label mensagemLabel;       // exibe mensagens de erro para o usuário

    private UsuarioBO bo = new UsuarioBO();

    // Chamado automaticamente quando o usuário clica no botão "Entrar"
    @FXML
    private void handleLogin() {
        try {
            // BO valida os campos e consulta o banco; lança exceção se as credenciais forem erradas
            Usuario u = bo.autenticar(emailField.getText(), senhaField.getText());
            LogUtil.registrarAcao("Login realizado", u.getEmail());

            // Carrega o arquivo FXML da tela principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/biblioteca/biblioteca/view/main-view.fxml"));
            Parent root = loader.load();

            // Obtém o controller da tela que acabou de ser carregada
            // e passa o usuário logado para ele
            MainController mc = loader.getController();
            mc.setUsuarioLogado(u);

            // Substitui o conteúdo da janela atual pela tela principal
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            // Em caso de erro (senha errada, banco offline), exibe a mensagem na tela
            LogUtil.registrarExcecao("Login", emailField.getText(), e);
            mensagemLabel.setText(e.getMessage());
        }
    }
}
