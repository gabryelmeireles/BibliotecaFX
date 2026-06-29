package com.biblioteca.biblioteca.model.dao;

import com.biblioteca.biblioteca.model.entity.Livro;
import com.biblioteca.biblioteca.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Responsável por toda comunicação com a tabela "livro" no banco de dados
public class LivroDAO {

    // Retorna todos os livros cadastrados, independente de disponibilidade
    public List<Livro> listarTodos() throws SQLException {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM livro";
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

    // Retorna apenas os livros com disponivel = true (consulta direta no banco)
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

    // Insere um novo livro — disponivel começa como true por padrão no banco
    public void salvar(Livro livro) throws SQLException {
        String sql = "INSERT INTO livro (titulo, autor, isbn) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, livro.getTitulo());
            ps.setString(2, livro.getAutor());
            ps.setString(3, livro.getIsbn());
            ps.executeUpdate();
        }
    }

    // Atualiza o campo disponivel ao realizar ou devolver um empréstimo
    public void atualizarDisponibilidade(int id, boolean disponivel) throws SQLException {
        String sql = "UPDATE livro SET disponivel = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, disponivel);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // Verifica se o livro ainda está emprestado antes de permitir exclusão
    public boolean possuiEmprestimoAtivo(int idLivro) throws SQLException {
        String sql = "SELECT COUNT(*) FROM emprestimo WHERE id_livro = ? AND devolvido = false";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLivro);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    // Usa transação para garantir que os empréstimos e o livro sejam deletados juntos ou nenhum seja
    public void excluir(int idLivro) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = conn.prepareStatement(
                        "DELETE FROM emprestimo WHERE id_livro = ?")) {
                    ps1.setInt(1, idLivro);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement(
                        "DELETE FROM livro WHERE id = ?")) {
                    ps2.setInt(1, idLivro);
                    ps2.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback(); // se qualquer delete falhar, desfaz tudo
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
