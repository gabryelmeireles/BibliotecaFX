package com.biblioteca.biblioteca.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// Responsável por fornecer conexões com o banco de dados PostgreSQL.
// As credenciais ficam em um arquivo externo (database.properties) que está no
// .gitignore — assim a senha nunca vai para o repositório no GitHub.
public class DatabaseConnection {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    // Bloco estático: executado uma única vez quando a classe é carregada pelo Java.
    // Lê o arquivo database.properties do classpath e extrai a URL, usuário e senha.
    static {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (in == null) {
                // Sem o arquivo de configuração, o sistema não tem como funcionar
                throw new ExceptionInInitializerError(
                    "Arquivo database.properties não encontrado no classpath.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
        URL      = props.getProperty("db.url");
        USER     = props.getProperty("db.user");
        PASSWORD = props.getProperty("db.password");
    }

    // Abre e retorna uma nova conexão com o banco de dados.
    // Cada chamada abre uma conexão nova. Os DAOs são responsáveis por fechá-la
    // usando try-with-resources (fechamento automático ao fim do bloco).
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
