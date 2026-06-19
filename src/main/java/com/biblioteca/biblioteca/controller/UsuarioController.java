package com.biblioteca.biblioteca.controller;

import com.biblioteca.biblioteca.model.bo.UsuarioBO;
import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.util.LogUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

// Controlador da tela de gerenciamento de usuários.
// Permite cadastrar, listar e (somente admin) excluir usuários.
public class UsuarioController {
    @FXML private TextField nomeField;
    @FXML private TextField emailField;
    @FXML private PasswordField senhaField;
    @FXML private Label mensagemLabel;
    @FXML private TableView<Usuario> usuariosTable;
    @FXML private TableColumn<Usuario, String> colNome;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private Button btnExcluir;

    private UsuarioBO bo = new UsuarioBO();
    private Usuario usuarioLogado;

    // Recebe o usuário logado; só admin pode excluir outros usuários
    public void setUsuarioLogado(Usuario u) {
        this.usuarioLogado = u;
        // Só admin pode excluir
        btnExcluir.setVisible(u.getEmail().equals("admin@email.com"));
        carregarDados();
    }

    // Preenche a tabela com todos os usuários cadastrados
    private void carregarDados() {
        try {
            colNome.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue().getNome()));
            colEmail.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue().getEmail()));
            usuariosTable.setItems(
                    FXCollections.observableArrayList(bo.listarTodos()));
        } catch (Exception e) {
            LogUtil.registrarExcecao("Listar usuarios",
                    usuarioLogado != null ? usuarioLogado.getEmail() : null, e);
        }
    }

    // Chamado quando o usuário clica em "Cadastrar"
    @FXML
    private void handleCadastrar() {
        try {
            bo.cadastrar(nomeField.getText(), emailField.getText(), senhaField.getText());
            LogUtil.registrarAcao("Usuario cadastrado: " + emailField.getText(),
                    usuarioLogado.getEmail());
            mensagemLabel.setStyle("-fx-text-fill: green;");
            mensagemLabel.setText("Usuário cadastrado com sucesso!");
            nomeField.clear(); emailField.clear(); senhaField.clear();
            carregarDados();
        } catch (Exception e) {
            LogUtil.registrarExcecao("Cadastrar usuario",
                    usuarioLogado != null ? usuarioLogado.getEmail() : null, e);
            mensagemLabel.setStyle("-fx-text-fill: red;");
            mensagemLabel.setText(e.getMessage());
        }
    }

    // Chamado quando o admin clica em "Excluir".
    // Protegido contra a exclusão do próprio admin (garantia de que sempre haverá um admin).
    @FXML
    private void handleExcluir() {
        try {
            Usuario selecionado = usuariosTable.getSelectionModel().getSelectedItem();
            if (selecionado == null) throw new Exception("Selecione um usuário.");
            if (selecionado.getEmail().equals("admin@email.com"))
                throw new Exception("O usuário admin não pode ser excluído.");
            bo.excluir(selecionado.getId());
            LogUtil.registrarAcao("Usuario excluido: " + selecionado.getEmail(),
                    usuarioLogado.getEmail());
            mensagemLabel.setStyle("-fx-text-fill: green;");
            mensagemLabel.setText("Usuário excluído com sucesso!");
            carregarDados();
        } catch (Exception e) {
            LogUtil.registrarExcecao("Excluir usuario",
                    usuarioLogado != null ? usuarioLogado.getEmail() : null, e);
            mensagemLabel.setStyle("-fx-text-fill: red;");
            mensagemLabel.setText(e.getMessage());
        }
    }
}
