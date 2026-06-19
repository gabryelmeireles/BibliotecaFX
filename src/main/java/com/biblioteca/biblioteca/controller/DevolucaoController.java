package com.biblioteca.biblioteca.controller;

import com.biblioteca.biblioteca.model.bo.EmprestimoBO;
import com.biblioteca.biblioteca.model.entity.Emprestimo;
import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.util.LogUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Controlador da tela de devoluções.
// Lista TODOS os empréstimos ativos do sistema (não só do usuário logado),
// para que o admin possa registrar a devolução de qualquer livro.
public class DevolucaoController {
    @FXML private TableView<Emprestimo> emprestimosTable;
    @FXML private TableColumn<Emprestimo, String> colLivro;
    @FXML private TableColumn<Emprestimo, String> colUsuario; // nome de quem pegou o livro
    @FXML private TableColumn<Emprestimo, String> colData;
    @FXML private Label mensagemLabel;

    private EmprestimoBO bo = new EmprestimoBO();
    private Usuario usuarioLogado;
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setUsuarioLogado(Usuario u) {
        this.usuarioLogado = u;
        carregarDados();
    }

    // Admin vê todos os empréstimos ativos; usuário comum vê só os seus próprios
    private void carregarDados() {
        try {
            colLivro.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                    d.getValue().getLivro().getTitulo()));
            colUsuario.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                    d.getValue().getUsuario().getNome()));
            colData.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                    d.getValue().getDataPrevistaDevolucao().format(fmt)));

            List<Emprestimo> lista = usuarioLogado.isAdmin()
                    ? bo.listarTodosAtivos()
                    : bo.listarAtivos(usuarioLogado.getId());

            emprestimosTable.setItems(FXCollections.observableArrayList(lista));
        } catch (Exception e) {
            LogUtil.registrarExcecao("Carregar devoluções",
                    usuarioLogado != null ? usuarioLogado.getEmail() : null, e);
            mensagemLabel.setText("Erro ao carregar empréstimos ativos.");
        }
    }

    // Chamado quando o admin clica em "Registrar Devolução"
    @FXML
    private void handleDevolucao() {
        try {
            Emprestimo selecionado = emprestimosTable.getSelectionModel().getSelectedItem();
            if (selecionado == null) throw new Exception("Selecione um empréstimo.");
            bo.realizarDevolucao(selecionado); // marca como devolvido e libera o livro
            LogUtil.registrarAcao("Devolução: " + selecionado.getLivro().getTitulo(),
                    usuarioLogado.getEmail());
            mensagemLabel.setStyle("-fx-text-fill: green;");
            mensagemLabel.setText("Devolução registrada com sucesso!");
            carregarDados(); // atualiza a tabela — empréstimo some da lista
        } catch (Exception e) {
            LogUtil.registrarExcecao("Realizar devolução",
                    usuarioLogado != null ? usuarioLogado.getEmail() : null, e);
            mensagemLabel.setStyle("-fx-text-fill: red;");
            mensagemLabel.setText(e.getMessage());
        }
    }
}
