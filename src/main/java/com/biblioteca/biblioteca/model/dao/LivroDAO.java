package com.biblioteca.biblioteca.model.dao;

import com.biblioteca.biblioteca.model.entity.Livro;
import com.biblioteca.biblioteca.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO = Data Access Object. Responsável exclusivamente pelas operações de banco de dados
// relacionadas a livros. Não contém regras de negócio — isso fica no LivroBO.
public class LivroDAO {

    // Retorna todos os livros cadastrados no banco, sem nenhum filtro
    public List<Livro> listarTodos() throws SQLException {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM livro";
        // try-with-resources: fecha automaticamente Connection, PreparedStatement e ResultSet
        // ao sair do bloco, mesmo que ocorra uma exceção. Evita vazamento de recursos.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            // rs.next() avança linha por linha no resultado retornado pelo banco
            while (rs.next()) {
                Livro l = new Livro();
                l.setId(rs.getInt("id"));
                l.setTitulo(rs.getString("titulo"));
                l.setAutor(rs.getString("autor"));
                l.setIsbn(rs.getString("isbn"));
                l.setDisponivel(rs.getBoolean("disponivel"));
                livros.add(l);
            }
        }
        return livros;
    }

    // Busca diretamente no banco apenas os livros com disponivel = true
    public List<Livro> listarDisponiveis() throws SQLException {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM livro WHERE disponivel = true";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Livro l = new Livro();
                l.setId(rs.getInt("id"));
                l.setTitulo(rs.getString("titulo"));
                l.setAutor(rs.getString("autor"));
                l.setIsbn(rs.getString("isbn"));
                l.setDisponivel(rs.getBoolean("disponivel"));
                livros.add(l);
            }
        }
        return livros;
    }

    // Insere um novo livro no banco.
    // PreparedStatement com "?" evita SQL Injection: os valores são tratados como dados
    // puros, nunca como parte do comando SQL.
    public void salvar(Livro livro) throws SQLException {
        String sql = "INSERT INTO livro (titulo, autor, isbn) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, livro.getTitulo()); // substitui o 1º "?"
            ps.setString(2, livro.getAutor());  // substitui o 2º "?"
            ps.setString(3, livro.getIsbn());   // substitui o 3º "?"
            ps.executeUpdate();                  // executa o INSERT
        }
    }

    // Atualiza apenas o campo "disponivel" do livro especificado pelo id
    public void atualizarDisponibilidade(int id, boolean disponivel) throws SQLException {
        String sql = "UPDATE livro SET disponivel = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, disponivel);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // Verifica se existe pelo menos um empréstimo ativo para este livro.
    // Usado antes de excluir: não podemos remover um livro que está emprestado.
    public boolean possuiEmprestimoAtivo(int idLivro) throws SQLException {
        String sql = "SELECT COUNT(*) FROM emprestimo WHERE id_livro = ? AND devolvido = false";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLivro);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0; // COUNT(*) > 0 significa que tem empréstimo ativo
        }
        return false;
    }

    // Exclui o livro e todos os seus registros de empréstimo usando TRANSAÇÃO.
    // Transação garante que as duas operações aconteçam juntas ou nenhuma aconteça:
    // se deletar os empréstimos mas falhar no livro, o rollback() desfaz tudo.
    public void excluir(int idLivro) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // inicia a transação — desliga o commit automático
            try {
                // Primeiro apaga os empréstimos do livro (integridade referencial do banco)
                try (PreparedStatement ps1 = conn.prepareStatement(
                        "DELETE FROM emprestimo WHERE id_livro = ?")) {
                    ps1.setInt(1, idLivro);
                    ps1.executeUpdate();
                }
                // Depois apaga o próprio livro
                try (PreparedStatement ps2 = conn.prepareStatement(
                        "DELETE FROM livro WHERE id = ?")) {
                    ps2.setInt(1, idLivro);
                    ps2.executeUpdate();
                }
                conn.commit(); // confirma as duas operações no banco
            } catch (SQLException e) {
                conn.rollback(); // desfaz tudo se algo der errado
                throw e;
            } finally {
                conn.setAutoCommit(true); // volta ao modo padrão independente do resultado
            }
        }
    }
}
