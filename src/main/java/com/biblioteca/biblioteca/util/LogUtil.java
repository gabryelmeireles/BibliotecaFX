package com.biblioteca.biblioteca.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtil {

    private static final String PASTA_LOGS   = "logs";
    public  static final String LOG_ACOES    = PASTA_LOGS + "/uso.log";      // ações dos usuários
    public  static final String LOG_EXCECOES = PASTA_LOGS + "/excecoes.log"; // erros capturados
    private static final DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Cria a pasta de logs ao carregar a classe, caso ainda não exista
    static {
        try {
            Files.createDirectories(Paths.get(PASTA_LOGS));
        } catch (IOException e) {
            System.err.println("Erro ao criar pasta de logs: " + e.getMessage());
        }
    }

    // Grava uma ação do usuário no uso.log com data, hora e e-mail
    public static void registrarAcao(String acao, String emailUsuario) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(LocalDateTime.now().format(fmt)).append("] ");
        sb.append("ACAO='").append(acao.toUpperCase().replace(" ", "_")).append("' ");
        sb.append("USUARIO='").append(
                emailUsuario != null ? emailUsuario : "nao_autenticado").append("'");
        escreverArquivo(LOG_ACOES, sb.toString());
    }

    // Grava uma exceção no excecoes.log com a mensagem do erro
    public static void registrarExcecao(String acao, String emailUsuario, Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(LocalDateTime.now().format(fmt)).append("] ");
        sb.append("ACAO='").append(acao.toUpperCase().replace(" ", "_")).append("' ");
        sb.append("USUARIO='").append(
                emailUsuario != null ? emailUsuario : "nao_autenticado").append("' ");
        sb.append("ERRO='").append(e.getMessage()).append("'");
        escreverArquivo(LOG_EXCECOES, sb.toString());
    }

    // Abre o arquivo em modo append e escreve a linha de log
    private static void escreverArquivo(String arquivo, String linha) {
        try (FileWriter fw = new FileWriter(arquivo, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(linha);
        } catch (IOException e) {
            System.err.println("Erro ao escrever log: " + e.getMessage());
        }
    }
}
