package com.biblioteca.biblioteca.model.bo;

import com.biblioteca.biblioteca.model.dao.LivroDAO;
import com.biblioteca.biblioteca.model.entity.Livro;
import com.biblioteca.biblioteca.model.exception.BibliotecaException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Camada de regras de negócio para livros
public class LivroBO {
    private LivroDAO dao = new LivroDAO();

    // Padrão que aceita ISBN-10 (9 dígitos + dígito/X) ou ISBN-13 (13 dígitos)
    private static final Pattern ISBN_PATTERN = Pattern.compile(
            "^(?:\\d{9}[\\dX]|\\d{13})$"
    );

    public List<Livro> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    // Filtra somente os livros disponíveis para exibir no ComboBox de empréstimo
    public List<Livro> listarDisponiveis() throws SQLException {
        return dao.listarTodos().stream()
                .filter(Livro::isDisponivel)
                .collect(Collectors.toList());
    }

    public void cadastrar(String titulo, String autor, String isbn) throws SQLException {
        // Validações básicas de campos obrigatórios
        if (titulo == null || titulo.isBlank())
            throw new BibliotecaException("Título obrigatório.");
        if (autor == null || autor.isBlank())
            throw new BibliotecaException("Autor obrigatório.");

        // ISBN é opcional, mas se informado precisa estar no formato correto
        if (isbn != null && !isbn.isBlank()) {
            String isbnLimpo = isbn.replaceAll("[\\s-]", ""); // remove espaços e hifens antes de validar
            if (!ISBN_PATTERN.matcher(isbnLimpo).matches())
                throw new BibliotecaException("ISBN inválido. Use formato ISBN-10 ou ISBN-13.");
        }

        dao.salvar(new Livro(titulo, autor, isbn));
    }

    public void excluir(int idLivro) throws SQLException {
        // Não permite excluir livro que ainda está emprestado
        if (dao.possuiEmprestimoAtivo(idLivro))
            throw new BibliotecaException("Livro possui empréstimo ativo e não pode ser excluído.");
        dao.excluir(idLivro);
    }
}
