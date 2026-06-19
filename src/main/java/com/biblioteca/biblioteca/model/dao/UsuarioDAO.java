package com.biblioteca.biblioteca.model.dao;

import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO responsável pelas operações de banco de dados relacionadas a usuários
public class UsuarioDAO {

    // Busca um usuário pelo e-mail e senha para autenticação no login.
    // Retorna null se não encontrar nenhum usuário com essas credenciais.
    public Usuario buscarPorEmailESenha(String email, String senha) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE email = ? AND senha = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, senha);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { // se encontrou algum registro com esse e-mail e senha
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                u.setSenha(rs.getString("senha"));
                return u;
            }
        }
        return null; // retorna null quando as credenciais não correspondem a nenhum usuário
    }

    // Insere um novo usuário no banco de dados
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

    // Retorna todos os usuários cadastrados.
    // A senha não é carregada por questão de segurança: não precisamos dela na listagem.
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

    // Verifica se o usuário tem algum empréstimo ainda não devolvido.
    // Usado antes de excluir: não podemos remover usuário com livro na mão.
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

    // Exclui o usuário e seus registros de empréstimo usando transação,
    // garantindo que as duas operações aconteçam juntas ou nenhuma aconteça.
    public void excluir(int idUsuario) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // inicia a transação
            try {
                // Primeiro apaga os empréstimos do usuário (integridade referencial)
                try (PreparedStatement ps1 = conn.prepareStatement(
                        "DELETE FROM emprestimo WHERE id_usuario = ?")) {
                    ps1.setInt(1, idUsuario);
                    ps1.executeUpdate();
                }
                // Depois apaga o próprio usuário
                try (PreparedStatement ps2 = conn.prepareStatement(
                        "DELETE FROM usuario WHERE id = ?")) {
                    ps2.setInt(1, idUsuario);
                    ps2.executeUpdate();
                }
                conn.commit(); // confirma as duas operações
            } catch (SQLException e) {
                conn.rollback(); // desfaz tudo se algo der errado
                throw e;
            } finally {
                conn.setAutoCommit(true); // volta ao modo padrão
            }
        }
    }
}
