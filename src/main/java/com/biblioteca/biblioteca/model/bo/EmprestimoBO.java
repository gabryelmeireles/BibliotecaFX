package com.biblioteca.biblioteca.model.bo;

import com.biblioteca.biblioteca.model.dao.EmprestimoDAO;
import com.biblioteca.biblioteca.model.dao.LivroDAO;
import com.biblioteca.biblioteca.model.entity.Emprestimo;
import com.biblioteca.biblioteca.model.entity.Livro;
import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.model.exception.BibliotecaException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// Camada de regras de negócio para empréstimos — fica entre o controller e o banco
public class EmprestimoBO {
    private EmprestimoDAO emprestimoDAO = new EmprestimoDAO();
    private LivroDAO livroDAO = new LivroDAO();

    public void realizarEmprestimo(Usuario usuario, Livro livro) throws SQLException {
        // Verifica disponibilidade antes de registrar; o prazo de 7 dias é definido no construtor de Emprestimo
        if (!livro.isDisponivel())
            throw new BibliotecaException("Livro não disponível para empréstimo.");
        emprestimoDAO.salvar(new Emprestimo(usuario, livro));
        // Marca o livro como indisponível no banco após registrar o empréstimo
        livroDAO.atualizarDisponibilidade(livro.getId(), false);
    }

    public void realizarDevolucao(Emprestimo emprestimo) throws SQLException {
        // Registra a data de devolução e libera o livro para outros usuários
        emprestimoDAO.registrarDevolucao(emprestimo.getId());
        livroDAO.atualizarDisponibilidade(emprestimo.getLivro().getId(), true);
    }

    // Retorna apenas os empréstimos ativos do usuário informado
    public List<Emprestimo> listarAtivos(int idUsuario) throws SQLException {
        return emprestimoDAO.listarAtivos(idUsuario);
    }

    // Versão para admin: retorna todos os empréstimos ativos, ordenados pelo prazo mais próximo
    public List<Emprestimo> listarTodosAtivos() throws SQLException {
        return emprestimoDAO.listarTodosAtivos().stream()
                .sorted(Comparator.comparing(Emprestimo::getDataPrevistaDevolucao))
                .collect(Collectors.toList());
    }
}
