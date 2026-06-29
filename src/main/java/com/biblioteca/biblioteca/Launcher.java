package com.biblioteca.biblioteca;

import javafx.application.Application;

// o JavaFX não consegue iniciar direto de uma classe que estende Application quando se usa module-info
// então essa classe serve de ponto de entrada e delega para o MainApp
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
