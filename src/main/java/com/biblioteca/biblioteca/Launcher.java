package com.biblioteca.biblioteca;

import javafx.application.Application;

// Existe por uma limitação técnica do Java com módulos (module-info.java):
// quando o projeto usa o sistema de módulos do Java 9+, o JavaFX não consegue
// iniciar direto de uma classe que estende Application.
// Esta classe serve como "porta de entrada" sem essa restrição e delega para o MainApp.
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args); // repassa o controle para o MainApp
    }
}
