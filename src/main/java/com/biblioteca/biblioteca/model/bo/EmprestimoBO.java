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

// BO responsável pelas regras de negócio de empréstimo e devolução de livros.
// Coordena as operações entre EmprestimoDAO e LivroDAO.
public class EmprestimoBO {
    private EmprestimoDAO emprestimoDAO = new EmprestimoDAO();
    private LivroDAO livroDAO = new LivroDAO();

    // Realiza um empréstimo: valida se o livro está disponível,
    // salva o registro no banco e marca o livro como indisponível.
    public void realizarEmprestimo(Usuario usuario, Livro livro) throws SQLException {
        if (!livro.isDisponivel())
            throw new BibliotecaException("Livro não disponível para empréstimo.");
        emprestimoDAO.salvar(new Emprestimo(usuario, livro));
        livroDAO.atualizarDisponibilidade(livro.getId(), false); // livro agora está emprestado
    }

    // Registra a devolução: marca o empréstimo como devolvido
    // e libera o livro para novos empréstimos.
    public void realizarDevolucao(Emprestimo emprestimo) throws SQLException {
        emprestimoDAO.registrarDevolucao(emprestimo.getId());
        livroDAO.atualizarDisponibilidade(emprestimo.getLivro().getId(), true); // livro disponível novamente
    }

    // Retorna os empréstimos ativos de um usuário específico (tela de Empréstimos)
    public List<Emprestimo> listarAtivos(int idUsuario) throws SQLException {
        return emprestimoDAO.listarAtivos(idUsuario);
    }

    // Retorna TODOS os empréstimos ativos ordenados por prazo de devolução.
    // Os mais urgentes (prazo mais próximo) aparecem primeiro na lista.
    // Usa Stream API (Aula 07): .stream().sorted(Comparator.comparing(...))
    public List<Emprestimo> listarTodosAtivos() throws SQLException {
        return emprestimoDAO.listarTodosAtivos().stream()
                .sorted(Comparator.comparing(Emprestimo::getDataPrevistaDevolucao))
                .collect(Collectors.toList());
    }
}
