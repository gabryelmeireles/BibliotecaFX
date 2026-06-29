package com.biblioteca.biblioteca.model.dao;

import com.biblioteca.biblioteca.model.entity.Emprestimo;
import com.biblioteca.biblioteca.model.entity.Livro;
import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Responsável por toda comunicação com a tabela "emprestimo" no banco de dados
public class EmprestimoDAO {

    // Insere um novo empréstimo no banco
    public void salvar(Emprestimo emprestimo) throws SQLException {
        String sql = "INSERT INTO emprestimo (id_usuario, id_livro, data_prevista_devolucao) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, emprestimo.getUsuario().getId());
            ps.setInt(2, emprestimo.getLivro().getId());
            ps.setTimestamp(3, Timestamp.valueOf(emprestimo.getDataPrevistaDevolucao()));
            ps.executeUpdate();
        }
    }

    // Marca o empréstimo como devolvido e registra a data da devolução
    public void registrarDevolucao(int idEmprestimo) throws SQLException {
        String sql = "UPDATE emprestimo SET devolvido = true, data_devolucao = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEmprestimo);
            ps.executeUpdate();
        }
    }

    // Busca os empréstimos ainda ativos de um usuário específico
    public List<Emprestimo> listarAtivos(int idUsuario) throws SQLException {
        List<Emprestimo> lista = new ArrayList<>();
        String sql = "SELECT e.id, u.id as uid, u.nome, u.email, " +
                "l.id as lid, l.titulo, l.autor, " +
                "e.data_emprestimo, e.data_prevista_devolucao " +
                "FROM emprestimo e " +
                "JOIN usuario u ON e.id_usuario = u.id " +
                "JOIN livro l ON e.id_livro = l.id " +
                "WHERE e.devolvido = false AND e.id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearEmprestimo(rs));
            }
        }
        return lista;
    }

    // Versão sem filtro de usuário — usada pelo admin para ver todos os empréstimos ativos
    public List<Emprestimo> listarTodosAtivos() throws SQLException {
        List<Emprestimo> lista = new ArrayList<>();
        String sql = "SELECT e.id, u.id as uid, u.nome, u.email, " +
                "l.id as lid, l.titulo, l.autor, " +
                "e.data_emprestimo, e.data_prevista_devolucao " +
                "FROM emprestimo e " +
                "JOIN usuario u ON e.id_usuario = u.id " +
                "JOIN livro l ON e.id_livro = l.id " +
                "WHERE e.devolvido = false";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearEmprestimo(rs));
            }
        }
        return lista;
    }

    // Converte uma linha do ResultSet em um objeto Emprestimo com usuário e livro preenchidos
    private Emprestimo mapearEmprestimo(ResultSet rs) throws SQLException {
        Emprestimo emp = new Emprestimo();
        emp.setId(rs.getInt("id"));

        Usuario u = new Usuario();
        u.setId(rs.getInt("uid"));
        u.setNome(rs.getString("nome"));
        u.setEmail(rs.getString("email"));
        emp.setUsuario(u);

        Livro l = new Livro();
        l.setId(rs.getInt("lid"));
        l.setTitulo(rs.getString("titulo"));
        l.setAutor(rs.getString("autor"));
        emp.setLivro(l);

        emp.setDataEmprestimo(rs.getTimestamp("data_emprestimo").toLocalDateTime());
        emp.setDataPrevistaDevolucao(rs.getTimestamp("data_prevista_devolucao").toLocalDateTime());
        return emp;
    }
}
