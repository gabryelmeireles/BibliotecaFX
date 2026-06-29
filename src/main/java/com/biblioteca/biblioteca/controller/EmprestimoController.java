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

public class EmprestimoController {

    // Componentes da tela vinculados ao FXML
    @FXML private ComboBox<Livro> livroCombo;   // lista suspensa com os livros disponíveis
    @FXML private Label mensagemLabel;           // exibe mensagens de sucesso ou erro pro usuário
    @FXML private TableView<Emprestimo> emprestimosTable; // tabela com os empréstimos ativos
    @FXML private TableColumn<Emprestimo, String> colLivro;
    @FXML private TableColumn<Emprestimo, String> colData;
    @FXML private TableColumn<Emprestimo, String> colPrazo;
    @FXML private TableColumn<Emprestimo, String> colStatus;

    // Camada de negócio responsável pelas regras de empréstimo e livros
    private EmprestimoBO emprestimoBO = new EmprestimoBO();
    private LivroBO livroBO = new LivroBO();

    private Usuario usuarioLogado; // guarda o usuário que está na sessão

    // Formato de data usado na exibição da tabela
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Chamado pelo MainController assim que a tela é aberta, passando o usuário logado
    public void setUsuarioLogado(Usuario u) {
        this.usuarioLogado = u;
        carregarDados(); // já carrega os dados assim que sabe quem é o usuário
    }

    private void carregarDados() {
        try {
            // Ajusta a largura da tabela para não deixar espaço vazio no final
            emprestimosTable.setMaxWidth(colLivro.getPrefWidth() + colData.getPrefWidth() + colPrazo.getPrefWidth() + colStatus.getPrefWidth() + 18);

            // Preenche o ComboBox somente com livros que ainda estão disponíveis para empréstimo
            livroCombo.setItems(FXCollections.observableArrayList(
                    livroBO.listarDisponiveis()));

            // Busca os empréstimos que o usuário ainda não devolveu
            List<Emprestimo> ativos = emprestimoBO.listarAtivos(usuarioLogado.getId());

            // Define o que cada coluna vai exibir, buscando o dado direto do objeto Emprestimo
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

            // Colore a linha de vermelho claro se o empréstimo estiver atrasado
            emprestimosTable.setRowFactory(tv -> new TableRow<Emprestimo>() {
                @Override
                protected void updateItem(Emprestimo emp, boolean empty) {
                    super.updateItem(emp, empty);
                    if (emp != null && emp.isAtrasado()) {
                        setStyle("-fx-background-color: #fadbd8;");
                    } else {
                        setStyle("");
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

    // Acionado quando o usuário clica no botão "Realizar Empréstimo"
    @FXML
    private void handleEmprestimo() {
        try {
            Livro livro = livroCombo.getValue();
            if (livro == null) throw new Exception("Selecione um livro."); // validação básica

            // Delega a regra de negócio pro BO (prazo, disponibilidade, etc.)
            emprestimoBO.realizarEmprestimo(usuarioLogado, livro);

            LogUtil.registrarAcao("EMPRESTIMO_REALIZADO: " + livro.getTitulo(),
                    usuarioLogado.getEmail());

            mensagemLabel.setStyle("-fx-text-fill: green;");
            mensagemLabel.setText("Empréstimo registrado! Devolução prevista em 7 dias.");

            carregarDados(); // atualiza a tabela e o ComboBox após o empréstimo
        } catch (Exception e) {
            LogUtil.registrarExcecao("Realizar emprestimo",
                    usuarioLogado != null ? usuarioLogado.getEmail() : null, e);
            mensagemLabel.setStyle("-fx-text-fill: red;");
            mensagemLabel.setText(e.getMessage()); // exibe a mensagem de erro pro usuário
        }
    }
}
