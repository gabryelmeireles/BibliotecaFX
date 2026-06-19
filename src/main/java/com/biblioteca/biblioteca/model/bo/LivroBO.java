package com.biblioteca.biblioteca.model.bo;

import com.biblioteca.biblioteca.model.dao.LivroDAO;
import com.biblioteca.biblioteca.model.entity.Livro;
import com.biblioteca.biblioteca.model.exception.BibliotecaException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// BO = Business Object. Contém as regras de negócio para livros.
// Fica entre o Controller (tela) e o DAO (banco), garantindo que
// dados inválidos nunca cheguem ao banco de dados.
public class LivroBO {
    private LivroDAO dao = new LivroDAO();

    // Expressão regular que valida ISBN-10 (9 dígitos + dígito ou X) ou ISBN-13 (13 dígitos).
    // Compilada uma vez como constante para evitar recriar o padrão a cada chamada.
    private static final Pattern ISBN_PATTERN = Pattern.compile(
            "^(?:\\d{9}[\\dX]|\\d{13})$"
    );

    // Retorna todos os livros — delega direto ao DAO sem regra adicional
    public List<Livro> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    // Retorna apenas os livros disponíveis usando a Stream API (Aula 07 — Streams).
    // dao.listarTodos() traz todos; .filter() seleciona só os disponíveis.
    // Livro::isDisponivel é uma referência de método — equivale a: l -> l.isDisponivel()
    public List<Livro> listarDisponiveis() throws SQLException {
        return dao.listarTodos().stream()
                .filter(Livro::isDisponivel)
                .collect(Collectors.toList());
    }

    // Valida os dados do formulário e cadastra um novo livro
    public void cadastrar(String titulo, String autor, String isbn) throws SQLException {
        // titulo e autor são obrigatórios; isBlank() retorna true para vazio ou só espaços
        if (titulo == null || titulo.isBlank())
            throw new BibliotecaException("Título obrigatório.");
        if (autor == null || autor.isBlank())
            throw new BibliotecaException("Autor obrigatório.");

        // ISBN é opcional, mas se informado precisa seguir o formato correto
        if (isbn != null && !isbn.isBlank()) {
            String isbnLimpo = isbn.replaceAll("[\\s-]", ""); // remove espaços e hifens
            if (!ISBN_PATTERN.matcher(isbnLimpo).matches())
                throw new BibliotecaException("ISBN inválido. Use formato ISBN-10 ou ISBN-13.");
        }

        dao.salvar(new Livro(titulo, autor, isbn));
    }

    // Verifica a regra de negócio antes de excluir:
    // livro com empréstimo ativo não pode ser removido do sistema
    public void excluir(int idLivro) throws SQLException {
        if (dao.possuiEmprestimoAtivo(idLivro))
            throw new BibliotecaException("Livro possui empréstimo ativo e não pode ser excluído.");
        dao.excluir(idLivro);
    }
}
