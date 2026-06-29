package com.biblioteca.biblioteca.controller;

import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.util.LogUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {
    @FXML private Label usuarioLogadoLabel;
    @FXML private StackPane conteudoPane; // painel central onde as telas são carregadas

    // Botões visíveis apenas para administradores
    @FXML private Button btnLivros;
    @FXML private Button btnUsuarios;
    @FXML private Button btnLogs;

    private Usuario usuarioLogado;

    // Recebe o usuário da sessão e oculta os botões admin caso não seja administrador
    public void setUsuarioLogado(Usuario u) {
        this.usuarioLogado = u;
        usuarioLogadoLabel.setText("Logado como: " + u.getNome());

        boolean admin = u.isAdmin();
        btnLivros.setVisible(admin);
        btnLivros.setManaged(admin);       // setManaged(false) remove o espaço do botão quando oculto
        btnUsuarios.setVisible(admin);
        btnUsuarios.setManaged(admin);
        btnLogs.setVisible(admin);
        btnLogs.setManaged(admin);
    }

    @FXML private void irParaEmprestimos() { carregarTela("emprestimo-view.fxml"); }
    @FXML private void irParaDevolucoes()  { carregarTela("devolucao-view.fxml"); }
    @FXML private void irParaLivros()      { carregarTela("livro-view.fxml"); }
    @FXML private void irParaUsuarios()    { carregarTela("usuario-view.fxml"); }
    @FXML private void irParaLogs()        { carregarTela("log-view.fxml"); }

    // Carrega o FXML no painel central e repassa o usuário logado para o novo controller
    private void carregarTela(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/biblioteca/biblioteca/view/" + fxml));
            Pane pane = loader.load();

            Object ctrl = loader.getController();
            if (ctrl instanceof EmprestimoController ec) ec.setUsuarioLogado(usuarioLogado);
            if (ctrl instanceof DevolucaoController dc)  dc.setUsuarioLogado(usuarioLogado);
            if (ctrl instanceof LivroController lc)      lc.setUsuarioLogado(usuarioLogado);
            if (ctrl instanceof UsuarioController uc)    uc.setUsuarioLogado(usuarioLogado);
            if (ctrl instanceof LogController lgc)       lgc.setUsuarioLogado(usuarioLogado);

            conteudoPane.getChildren().setAll(pane);
            LogUtil.registrarAcao("Navegou para " + fxml, usuarioLogado.getEmail());
        } catch (IOException e) {
            LogUtil.registrarExcecao("Navegação", usuarioLogado.getEmail(), e);
        }
    }

    // Registra o logout e volta para a tela de login
    @FXML private void handleSair() {
        try {
            LogUtil.registrarAcao("Logout", usuarioLogado.getEmail());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/biblioteca/biblioteca/view/login-view.fxml"));
            Stage stage = (Stage) usuarioLogadoLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            LogUtil.registrarExcecao("Logout", usuarioLogado.getEmail(), e);
        }
    }
}
