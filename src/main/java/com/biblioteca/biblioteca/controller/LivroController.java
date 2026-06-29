package com.biblioteca.biblioteca.controller;

import com.biblioteca.biblioteca.model.bo.LivroBO;
import com.biblioteca.biblioteca.model.entity.Livro;
import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.util.LogUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LivroController {

    // Campos de entrada do formulário de cadastro
    @FXML private TextField tituloField;
    @FXML private TextField autorField;
    @FXML private TextField isbnField;
    @FXML private Label mensagemLabel;

    // Tabela que lista todos os livros cadastrados
    @FXML private TableView<Livro> livrosTable;
    @FXML private TableColumn<Livro, String> colTitulo;
    @FXML private TableColumn<Livro, String> colAutor;
    @FXML private TableColumn<Livro, String> colIsbn;
    @FXML private TableColumn<Livro, String> colDisponivel;
    @FXML private Button btnExcluir;

    private LivroBO bo = new LivroBO();
    private Usuario usuarioLogado;

    // Recebe o usuário logado e já carrega os dados da tela
    public void setUsuarioLogado(Usuario u) {
        this.usuarioLogado = u;
        // Botão de excluir só aparece para o administrador
        btnExcluir.setVisible(u.getEmail().equals("admin@email.com"));
        carregarDados();
    }

    private void carregarDados() {
        try {
            livrosTable.setMaxWidth(colTitulo.getPrefWidth() + colAutor.getPrefWidth() + colIsbn.getPrefWidth() + colDisponivel.getPrefWidth() + 18);

            // Cada coluna lê o atributo correspondente do objeto Livro
            colTitulo.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue().getTitulo()));
            colAutor.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue().getAutor()));
            colIsbn.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue().getIsbn()));
            colDisponivel.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue().isDisponivel() ? "Disponível" : "Indisponível"));

            livrosTable.setItems(
                    FXCollections.observableArrayList(bo.listarTodos()));
        } catch (Exception e) {
            LogUtil.registrarExcecao("Carregar livros",
                    usuarioLogado != null ? usuarioLogado.getEmail() : null, e);
        }
    }

    // Acionado pelo botão "Cadastrar" — valida e salva o livro via BO
    @FXML
    private void handleCadastrar() {
        try {
            bo.cadastrar(tituloField.getText(), autorField.getText(), isbnField.getText());
            LogUtil.registrarAcao("Livro cadastrado: " + tituloField.getText(),
                    usuarioLogado.getEmail());
            mensagemLabel.setStyle("-fx-text-fill: green;");
            mensagemLabel.setText("Livro cadastrado com sucesso!");
            tituloField.clear(); autorField.clear(); isbnField.clear();
            carregarDados();
        } catch (Exception e) {
            LogUtil.registrarExcecao("Cadastrar livro",
                    usuarioLogado != null ? usuarioLogado.getEmail() : null, e);
            mensagemLabel.setStyle("-fx-text-fill: red;");
            mensagemLabel.setText(e.getMessage());
        }
    }

    // Acionado pelo botão "Excluir" — só aparece para o admin
    @FXML
    private void handleExcluir() {
        try {
            Livro selecionado = livrosTable.getSelectionModel().getSelectedItem();
            if (selecionado == null) throw new Exception("Selecione um livro.");
            bo.excluir(selecionado.getId());
            LogUtil.registrarAcao("Livro excluido: " + selecionado.getTitulo(),
                    usuarioLogado.getEmail());
            mensagemLabel.setStyle("-fx-text-fill: green;");
            mensagemLabel.setText("Livro excluído com sucesso!");
            carregarDados();
        } catch (Exception e) {
            LogUtil.registrarExcecao("Excluir livro",
                    usuarioLogado != null ? usuarioLogado.getEmail() : null, e);
            mensagemLabel.setStyle("-fx-text-fill: red;");
            mensagemLabel.setText(e.getMessage());
        }
    }
}
