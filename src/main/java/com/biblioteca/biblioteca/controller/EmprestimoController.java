package com.biblioteca.biblioteca.controller;

import com.biblioteca.biblioteca.model.bo.EmprestimoBO;
import com.biblioteca.biblioteca.model.bo.LivroBO;
import com.biblioteca.biblioteca.model.entity.Emprestimo;
import com.biblioteca.biblioteca.model.entity.Livro;
import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.util.LogUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TableCell;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Controlador da tela de empréstimos.
// O usuário seleciona um livro disponível no ComboBox e registra o empréstimo.
// A tabela abaixo lista os empréstimos ativos do próprio usuário.
public class EmprestimoController {
    @FXML private ComboBox<Livro> livroCombo;  // lista suspensa com livros disponíveis
    @FXML private Label mensagemLabel;
    @FXML private TableView<Emprestimo> emprestimosTable;
    @FXML private TableColumn<Emprestimo, String> colLivro;
    @FXML private TableColumn<Emprestimo, String> colData;
    @FXML private TableColumn<Emprestimo, String> colPrazo;
    @FXML private TableColumn<Emprestimo, String> colStatus;

    private EmprestimoBO emprestimoBO = new EmprestimoBO();
    private LivroBO livroBO = new LivroBO();
    private Usuario usuarioLogado;
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setUsuarioLogado(Usuario u) {
        this.usuarioLogado = u;
        carregarDados();
    }

    // Preenche o ComboBox com livros disponíveis e a tabela com empréstimos do usuário
    private void carregarDados() {
        try {
            // O ComboBox usa o toString() do Livro para exibir "Título - Autor"
            livroCombo.setItems(FXCollections.observableArrayList(
                    livroBO.listarDisponiveis()));

            // Lista apenas os empréstimos ativos deste usuário (não de outros)
            List<Emprestimo> ativos = emprestimoBO.listarAtivos(usuarioLogado.getId());

            colLivro.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue().getLivro().getTitulo()));
            colData.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue().getDataEmprestimo().format(fmt)));
            colPrazo.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue().getDataPrevistaDevolucao().format(fmt)));
            colStatus.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue().isAtrasado() ? "ATRASADO" : "No prazo"));

            // Pinta a linha de vermelho claro se o empréstimo estiver atrasado.
            // updateItem() é chamado pelo JavaFX para cada linha sempre que a tabela é desenhada.
            emprestimosTable.setRowFactory(tv -> new TableRow<Emprestimo>() {
                @Override
                protected void updateItem(Emprestimo emp, boolean empty) {
                    super.updateItem(emp, empty);
                    if (emp != null && emp.isAtrasado()) {
                        setStyle("-fx-background-color: #fadbd8;"); // vermelho claro
                    } else {
                        setStyle(""); // sem cor especial (volta ao padrão)
                    }
                }
            });

            emprestimosTable.setItems(FXCollections.observableArrayList(ativos));
        } catch (Exception e) {
            LogUtil.registrarExcecao("Carregar emprestimos",
                    usuarioLogado != null ? usuarioLogado.getEmail() : null, e);
            mensagemLabel.setText("Erro ao carregar dados.");
        }
    }

    // Chamado quando o usuário clica em "Realizar Empréstimo"
    @FXML
    private void handleEmprestimo() {
        try {
            Livro livro = livroCombo.getValue(); // livro selecionado no ComboBox
            if (livro == null) throw new Exception("Selecione um livro.");
            emprestimoBO.realizarEmprestimo(usuarioLogado, livro);
            LogUtil.registrarAcao("EMPRESTIMO_REALIZADO: " + livro.getTitulo(),
                    usuarioLogado.getEmail());
            mensagemLabel.setStyle("-fx-text-fill: green;");
            mensagemLabel.setText("Empréstimo registrado! Devolução prevista em 7 dias.");
            carregarDados(); // recarrega para atualizar o ComboBox e a tabela
        } catch (Exception e) {
            LogUtil.registrarExcecao("Realizar emprestimo",
                    usuarioLogado != null ? usuarioLogado.getEmail() : null, e);
            mensagemLabel.setStyle("-fx-text-fill: red;");
            mensagemLabel.setText(e.getMessage());
        }
    }
}
