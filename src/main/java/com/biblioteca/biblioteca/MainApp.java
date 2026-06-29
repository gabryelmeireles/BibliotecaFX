package com.biblioteca.biblioteca;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Ponto de entrada real do JavaFX — inicializa a janela e carrega a tela de login
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Carrega o FXML da tela de login como ponto inicial da aplicação
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/com/biblioteca/biblioteca/view/login-view.fxml"));

        Scene scene = new Scene(loader.load());

        stage.setTitle("Sistema de Biblioteca");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(560);
        stage.setMaximized(true); // abre maximizado para melhor visualização da tabela
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
