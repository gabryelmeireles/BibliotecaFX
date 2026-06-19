package com.biblioteca.biblioteca;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Classe principal do JavaFX. Toda aplicação JavaFX precisa estender Application.
// O JavaFX chama o método start() automaticamente quando o programa é iniciado.
public class MainApp extends Application {

    // O JavaFX chama este método automaticamente após inicializar.
    // Stage representa a janela principal do sistema operacional.
    @Override
    public void start(Stage stage) throws Exception {
        // Carrega o arquivo de layout da tela de login (FXML = formato XML do JavaFX)
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/biblioteca/biblioteca/view/login-view.fxml"));

        // Scene é o "conteúdo" visível dentro da janela
        Scene scene = new Scene(loader.load());

        stage.setTitle("Sistema de Biblioteca");
        stage.setScene(scene);
        stage.setMinWidth(900);    // impede que a janela fique pequena demais e quebre o layout
        stage.setMinHeight(560);
        stage.setMaximized(true);  // abre a janela já em tela cheia
        stage.show();              // exibe a janela
    }

    public static void main(String[] args) {
        launch(args); // método herdado de Application que inicializa o JavaFX
    }
}
