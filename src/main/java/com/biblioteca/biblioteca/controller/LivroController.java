package com.biblioteca.biblioteca.controller;

import com.biblioteca.biblioteca.model.bo.LivroBO;
import com.biblioteca.biblioteca.model.entity.Livro;
import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.util.LogUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

// Controlador da tela de gerenciamento de livros.
// Permite cadastrar, listar e (somente admin) excluir livros.
public class LivroController {
    @FXML private TextField tituloField;
    @FXML private TextField autorField;
    @FXML private TextField isbnField;
    @FXML private Label mensagemLabel;
    @FXML private TableView<Livro> livrosTable;
    @FXML private TableColumn<Livro, String> colTitulo;
    @FXML private TableColumn<Livro, String> colAutor;
    @FXML private TableColumn<Livro, String> colIsbn;
    @FXML private TableColumn<Livro, String> colDisponivel;
    @FXML private Button btnExcluir;

    private LivroBO bo = new LivroBO();
    private Usuario usuarioLogado;

    // Recebe o usuário logado vindo do MainController.
    // Esconde o botão "Excluir" para quem não for admin.
    public void setUsuarioLogado(Usuario u) {
        this.usuarioLogado = u;
        btnExcluir.setVisible(u.getEmail().equals("admin@email.com"));
        carregarDados();
    }

    // Busca os livros no banco e preenche a tabela na tela
    private void carregarDados() {
        try {
            // setCellValueFactory define de onde cada coluna tira o valor para exibir.
            // d.getValue() retorna o objeto Livro daquela linha da tabela.
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

            // FXCollections.observableArrayList() converte a List comum para o formato do JavaFX
            livrosTable.setItems(
                    FXCollections.observableArrayList(bo.listarTodos()));
        } catch (Exception e) {
            LogUtil.registrarExcecao("Carregar livros",
                    usuarioLogado != null ? usuarioLogado.getEmail() : null, e);
        }
    }

    // Chamado quando o usuário clica no botão "Cadastrar"
    @FXML
    private void handleCadastrar() {
        try {
            bo.cadastrar(tituloField.getText(), autorField.getText(), isbnField.getText());
            LogUtil.registrarAcao("Livro cadastrado: " + tituloField.getText(),
                    usuarioLogado.getEmail());
            mensagemLabel.setStyle("-fx-text-fill: green;");
            mensagemLabel.setText("Livro cadastrado com sucesso!");
            tituloField.clear(); autorField.clear(); isbnField.clear(); // limpa os campos
            carregarDados(); // atualiza a tabela com o novo livro
        } catch (Exception e) {
            LogUtil.registrarExcecao("Cadastrar livro",
                    usuarioLogado != null ? usuarioLogado.getEmail() : null, e);
            mensagemLabel.setStyle("-fx-text-fill: red;");
            mensagemLabel.setText(e.getMessage()); // exibe a mensagem de validação
        }
    }

    // Chamado quando o admin clica em "Excluir" — botão invisível para usuários comuns
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
