package com.biblioteca.biblioteca.controller;

import com.biblioteca.biblioteca.model.bo.EmprestimoBO;
import com.biblioteca.biblioteca.model.entity.Emprestimo;
import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.util.LogUtil;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DevolucaoController {

    // Componentes vinculados ao FXML
    @FXML private TableView<Emprestimo> emprestimosTable;
    @FXML private TableColumn<Emprestimo, String> colLivro;
    @FXML private TableColumn<Emprestimo, String> colUsuario;
    @FXML private TableColumn<Emprestimo, String> colData;
    @FXML private Label mensagemLabel;

    private EmprestimoBO bo = new EmprestimoBO();
    private Usuario usuarioLogado;
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Chamado pelo MainController ao abrir a tela, passando o usuário da sessão
    public void setUsuarioLogado(Usuario u) {
        this.usuarioLogado = u;
        carregarDados();
    }

    private void carregarDados() {
        // Ajusta a largura da tabela para caber exatamente nas colunas definidas
        emprestimosTable.setMaxWidth(colLivro.getPrefWidth() + colUsuario.getPrefWidth() + colData.getPrefWidth() + 18);

        // Define o que cada coluna exibe, buscando dados do objeto Emprestimo
        colLivro.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getLivro().getTitulo()));
        colUsuario.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getUsuario().getNome()));
        colData.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getDataPrevistaDevolucao().format(fmt)));

        // Usa Task para buscar os dados em segundo plano e não travar a tela durante o carregamento
        Task<List<Emprestimo>> task = new Task<>() {
            @Override
            protected List<Emprestimo> call() throws Exception {
                // Admin vê todos os empréstimos ativos; usuário comum vê só os seus
                return usuarioLogado.isAdmin()
                        ? bo.listarTodosAtivos()
                        : bo.listarAtivos(usuarioLogado.getId());
            }
        };

        // Quando a busca termina, atualiza a tabela na thread do JavaFX
        task.setOnSucceeded(e ->
                emprestimosTable.setItems(FXCollections.observableArrayList(task.getValue())));

        task.setOnFailed(e -> {
            LogUtil.registrarExcecao("Carregar devoluções",
                    usuarioLogado != null ? usuarioLogado.getEmail() : null,
                    new Exception(task.getException()));
            mensagemLabel.setText("Erro ao carregar empréstimos ativos.");
        });

        new Thread(task).start();
    }

    // Acionado quando o usuário clica em "Registrar Devolução"
    @FXML
    private void handleDevolucao() {
        Emprestimo selecionado = emprestimosTable.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mensagemLabel.setStyle("-fx-text-fill: red;");
            mensagemLabel.setText("Selecione um empréstimo.");
            return;
        }

        // Também usa Task para não travar a UI durante a operação no banco
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                bo.realizarDevolucao(selecionado);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            LogUtil.registrarAcao("Devolução: " + selecionado.getLivro().getTitulo(),
                    usuarioLogado.getEmail());
            mensagemLabel.setStyle("-fx-text-fill: green;");
            mensagemLabel.setText("Devolução registrada com sucesso!");
            carregarDados(); // recarrega a tabela para remover o item devolvido
        });

        task.setOnFailed(e -> {
            Exception ex = new Exception(task.getException());
            LogUtil.registrarExcecao("Realizar devolução",
                    usuarioLogado != null ? usuarioLogado.getEmail() : null, ex);
            mensagemLabel.setStyle("-fx-text-fill: red;");
            mensagemLabel.setText(task.getException().getMessage());
        });

        new Thread(task).start();
    }
}
