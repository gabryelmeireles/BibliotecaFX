package com.biblioteca.biblioteca.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Responsável por gravar registros das ações e erros do sistema em arquivos de texto.
// Requisito do edital: "rastreabilidade das ações" e "log de exceções".
public class LogUtil {

    private static final String PASTA_LOGS   = "logs";
    private static final String LOG_ACOES    = PASTA_LOGS + "/uso.log";       // ações normais
    private static final String LOG_EXCECOES = PASTA_LOGS + "/excecoes.log";  // erros
    private static final DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Cria a pasta "logs/" na primeira vez que a classe for carregada.
    // Sem isso, a gravação dos arquivos falharia se a pasta não existisse.
    static {
        try {
            Files.createDirectories(Paths.get(PASTA_LOGS));
        } catch (IOException e) {
            System.err.println("Erro ao criar pasta de logs: " + e.getMessage());
        }
    }

    // Registra uma ação bem-sucedida (ex: login, cadastro de livro, empréstimo).
    // Formato gerado: [17/06/2026 14:30:22] ACAO='LOGIN_REALIZADO' USUARIO='admin@email.com'
    public static void registrarAcao(String acao, String emailUsuario) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(LocalDateTime.now().format(fmt)).append("] ");
        sb.append("ACAO='").append(acao.toUpperCase().replace(" ", "_")).append("' ");
        sb.append("USUARIO='").append(
                emailUsuario != null ? emailUsuario : "nao_autenticado").append("'");
        escreverArquivo(LOG_ACOES, sb.toString());
    }

    // Registra um erro que aconteceu durante uma operação.
    // Formato: [data] ACAO='...' USUARIO='...' ERRO='mensagem do erro'
    public static void registrarExcecao(String acao, String emailUsuario, Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(LocalDateTime.now().format(fmt)).append("] ");
        sb.append("ACAO='").append(acao.toUpperCase().replace(" ", "_")).append("' ");
        sb.append("USUARIO='").append(
                emailUsuario != null ? emailUsuario : "nao_autenticado").append("' ");
        sb.append("ERRO='").append(e.getMessage()).append("'");
        escreverArquivo(LOG_EXCECOES, sb.toString());
    }

    // Abre o arquivo em modo "append" (true = não apaga o conteúdo anterior)
    // e escreve a linha de log ao final do arquivo.
    private static void escreverArquivo(String arquivo, String linha) {
        try (FileWriter fw = new FileWriter(arquivo, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(linha);
        } catch (IOException e) {
            System.err.println("Erro ao escrever log: " + e.getMessage());
        }
    }
}
