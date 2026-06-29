package com.biblioteca.biblioteca.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// Gerencia a conexão com o banco de dados PostgreSQL
// As credenciais ficam em database.properties (ignorado pelo .gitignore) para não subir ao GitHub
public class DatabaseConnection {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    // Bloco estático: roda uma vez quando a classe é carregada e lê as credenciais do arquivo
    static {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (in == null) {
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

    // Abre e retorna uma nova conexão — cada DAO fecha a sua própria conexão via try-with-resources
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
