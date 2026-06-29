package com.biblioteca.biblioteca.model.bo;

import com.biblioteca.biblioteca.model.dao.UsuarioDAO;
import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.model.exception.BibliotecaException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

// Camada de regras de negócio para usuários
public class UsuarioBO {
    private UsuarioDAO dao = new UsuarioDAO();

    // Regex para validar formato de e-mail (nome@dominio.extensao)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    public Usuario autenticar(String email, String senha) throws SQLException {
        if (email == null || email.isBlank())
            throw new BibliotecaException("E-mail obrigatório.");
        if (senha == null || senha.isBlank())
            throw new BibliotecaException("Senha obrigatória.");
        Usuario u = dao.buscarPorEmailESenha(email, senha);
        if (u == null)
            throw new BibliotecaException("E-mail ou senha inválidos."); // mensagem genérica para não revelar se o e-mail existe
        return u;
    }

    public void cadastrar(String nome, String email, String senha) throws SQLException {
        if (nome == null || nome.isBlank())
            throw new BibliotecaException("Nome obrigatório.");
        if (email == null || email.isBlank())
            throw new BibliotecaException("E-mail obrigatório.");
        if (!EMAIL_PATTERN.matcher(email).matches())
            throw new BibliotecaException("E-mail inválido. Use o formato: nome@dominio.com");
        if (senha == null || senha.length() < 4)
            throw new BibliotecaException("Senha deve ter ao menos 4 caracteres.");
        dao.salvar(new Usuario(nome, email, senha));
    }

    public List<Usuario> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public void excluir(int idUsuario) throws SQLException {
        // Não permite excluir usuário que ainda tem livros em mãos
        if (dao.possuiEmprestimosAtivos(idUsuario))
            throw new BibliotecaException("Usuário possui empréstimos ativos e não pode ser excluído.");
        dao.excluir(idUsuario);
    }
}
