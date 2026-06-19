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

// Controlador da tela principal. Gerencia o menu de navegação e a área central
// que exibe as sub-telas (livros, usuários, empréstimos, devoluções).
// Não abre novas janelas — o conteúdo central muda dinamicamente dentro da mesma janela.
public class MainController {
    @FXML private Label usuarioLogadoLabel;  // mostra o nome do usuário logado no topo
    @FXML private StackPane conteudoPane;    // área central onde as sub-telas são exibidas

    // Botões visíveis apenas para admin — conectados via fx:id no FXML
    @FXML private Button btnLivros;
    @FXML private Button btnUsuarios;

    private Usuario usuarioLogado;

    // Recebe o usuário logado vindo do LoginController.
    // Exibe o nome e esconde os menus exclusivos do admin para usuários comuns.
    public void setUsuarioLogado(Usuario u) {
        this.usuarioLogado = u;
        usuarioLogadoLabel.setText("Logado como: " + u.getNome());

        // setVisible(false) torna invisível; setManaged(false) remove o espaço ocupado
        boolean admin = u.isAdmin();
        btnLivros.setVisible(admin);
        btnLivros.setManaged(admin);
        btnUsuarios.setVisible(admin);
        btnUsuarios.setManaged(admin);
    }

    // Cada botão do menu chama o método correspondente, que carrega a tela correta
    @FXML private void irParaEmprestimos() { carregarTela("emprestimo-view.fxml"); }
    @FXML private void irParaDevolucoes()  { carregarTela("devolucao-view.fxml"); }
    @FXML private void irParaLivros()      { carregarTela("livro-view.fxml"); }
    @FXML private void irParaUsuarios()    { carregarTela("usuario-view.fxml"); }

    // Carrega o arquivo FXML indicado e exibe seu conteúdo no StackPane central.
    // Também repassa o usuário logado para o controller da tela carregada.
    private void carregarTela(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/biblioteca/biblioteca/view/" + fxml));
            Pane pane = loader.load();

            // Identifica qual controller foi carregado e repassa o usuário logado
            // instanceof com pattern matching (Java 16+): verifica o tipo e declara a variável ao mesmo tempo
            Object ctrl = loader.getController();
            if (ctrl instanceof EmprestimoController ec) ec.setUsuarioLogado(usuarioLogado);
            if (ctrl instanceof DevolucaoController dc)  dc.setUsuarioLogado(usuarioLogado);
            if (ctrl instanceof LivroController lc)      lc.setUsuarioLogado(usuarioLogado);
            if (ctrl instanceof UsuarioController uc)    uc.setUsuarioLogado(usuarioLogado);

            // setAll() substitui todo o conteúdo atual do StackPane pela nova tela
            conteudoPane.getChildren().setAll(pane);
            LogUtil.registrarAcao("Navegou para " + fxml, usuarioLogado.getEmail());
        } catch (IOException e) {
            LogUtil.registrarExcecao("Navegação", usuarioLogado.getEmail(), e);
        }
    }

    // Faz logout: volta para a tela de login e cria uma nova Scene
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
