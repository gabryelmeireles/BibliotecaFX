package com.biblioteca.biblioteca.controller;

import com.biblioteca.biblioteca.model.bo.UsuarioBO;
import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.util.LogUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UsuarioController {

    // Campos do formulário de cadastro
    @FXML private TextField nomeField;
    @FXML private TextField emailField;
    @FXML private PasswordField senhaField;
    @FXML private Label mensagemLabel;

    // Tabela com todos os usuários cadastrados
    @FXML private TableView<Usuario> usuariosTable;
    @FXML private TableColumn<Usuario, String> colNome;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private Button btnExcluir;

    private UsuarioBO bo = new UsuarioBO();
    private Usuario usuarioLogado;

    // Recebe o usuário da sessão e já carrega a lista
    public void setUsuarioLogado(Usuario u) {
        this.usuarioLogado = u;
        // Botão de excluir só aparece para o administrador
        btnExcluir.setVisible(u.getEmail().equals("admin@email.com"));
        carregarDados();
    }

    private void carregarDados() {
        try {
            usuariosTable.setMaxWidth(colNome.getPrefWidth() + colEmail.getPrefWidth() + 18);

            // Cada coluna exibe um atributo do objeto Usuario
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

    // Acionado pelo botão "Cadastrar"
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

    // Acionado pelo botão "Excluir" — só visível para o admin
    @FXML
    private void handleExcluir() {
        try {
            Usuario selecionado = usuariosTable.getSelectionModel().getSelectedItem();
            if (selecionado == null) throw new Exception("Selecione um usuário.");
            // Proteção para não deixar o admin ser removido
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