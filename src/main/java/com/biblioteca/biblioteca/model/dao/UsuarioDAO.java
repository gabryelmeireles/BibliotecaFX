package com.biblioteca.biblioteca.model.dao;

import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Responsável por toda comunicação com a tabela "usuario" no banco de dados
public class UsuarioDAO {

    // Busca o usuário pelo e-mail e senha para autenticação no login
    public Usuario buscarPorEmailESenha(String email, String senha) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE email = ? AND senha = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, senha);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                u.setSenha(rs.getString("senha"));
                return u;
            }
        }
        return null; // retorna null se não encontrar; o BO trata esse caso
    }

    // Insere um novo usuário no banco
    public void salvar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nome, email, senha) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getSenha());
            ps.executeUpdate();
        }
    }

    // Retorna todos os usuários cadastrados (sem a senha, por segurança)
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                lista.add(u);
            }
        }
        return lista;
    }

    // Verifica se o usuário ainda tem empréstimos em aberto antes de permitir exclusão
    public boolean possuiEmprestimosAtivos(int idUsuario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM emprestimo WHERE id_usuario = ? AND devolvido = false";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    // Usa transação para deletar os empréstimos do usuário antes de deletar o próprio usuário
    public void excluir(int idUsuario) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = conn.prepareStatement(
                        "DELETE FROM emprestimo WHERE id_usuario = ?")) {
                    ps1.setInt(1, idUsuario);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement(
                        "DELETE FROM usuario WHERE id = ?")) {
                    ps2.setInt(1, idUsuario);
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
