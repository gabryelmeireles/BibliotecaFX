package com.biblioteca.biblioteca.model.dao;

import com.biblioteca.biblioteca.model.entity.Emprestimo;
import com.biblioteca.biblioteca.model.entity.Livro;
import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO responsável pelas operações de banco de dados relacionadas a empréstimos
public class EmprestimoDAO {

    // Salva um novo empréstimo no banco.
    // A data_emprestimo é gerada automaticamente pelo banco (DEFAULT NOW()),
    // por isso só enviamos o prazo de devolução.
    public void salvar(Emprestimo emprestimo) throws SQLException {
        String sql = "INSERT INTO emprestimo (id_usuario, id_livro, data_prevista_devolucao) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, emprestimo.getUsuario().getId());
            ps.setInt(2, emprestimo.getLivro().getId());
            // Timestamp.valueOf() converte LocalDateTime para o formato que o JDBC entende
            ps.setTimestamp(3, Timestamp.valueOf(emprestimo.getDataPrevistaDevolucao()));
            ps.executeUpdate();
        }
    }

    // Marca um empréstimo como devolvido e registra a data exata da devolução
    public void registrarDevolucao(int idEmprestimo) throws SQLException {
        String sql = "UPDATE emprestimo SET devolvido = true, data_devolucao = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEmprestimo);
            ps.executeUpdate();
        }
    }

    // Lista os empréstimos ativos de um usuário específico.
    // Usa JOIN para trazer dados do usuário e do livro junto com o empréstimo —
    // evita fazer 3 consultas separadas ao banco.
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

    // Lista TODOS os empréstimos ativos, de qualquer usuário.
    // Usado na tela de Devolução, onde o admin precisa ver os empréstimos de todos.
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

    // Método auxiliar que transforma uma linha do ResultSet em um objeto Emprestimo completo.
    // Extraído para não repetir o mesmo código em listarAtivos() e listarTodosAtivos().
    private Emprestimo mapearEmprestimo(ResultSet rs) throws SQLException {
        Emprestimo emp = new Emprestimo();
        emp.setId(rs.getInt("id"));

        // Reconstrói o objeto Usuario com os dados trazidos pelo JOIN
        Usuario u = new Usuario();
        u.setId(rs.getInt("uid"));
        u.setNome(rs.getString("nome"));
        u.setEmail(rs.getString("email"));
        emp.setUsuario(u);

        // Reconstrói o objeto Livro com os dados trazidos pelo JOIN
        Livro l = new Livro();
        l.setId(rs.getInt("lid"));
        l.setTitulo(rs.getString("titulo"));
        l.setAutor(rs.getString("autor"));
        emp.setLivro(l);

        // toLocalDateTime() converte o Timestamp do banco para LocalDateTime do Java
        emp.setDataEmprestimo(rs.getTimestamp("data_emprestimo").toLocalDateTime());
        emp.setDataPrevistaDevolucao(rs.getTimestamp("data_prevista_devolucao").toLocalDateTime());
        return emp;
    }
}
