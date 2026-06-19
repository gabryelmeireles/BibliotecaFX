package com.biblioteca.biblioteca.model.bo;

import com.biblioteca.biblioteca.model.dao.UsuarioDAO;
import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.model.exception.BibliotecaException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

// BO responsável pelas regras de negócio de usuários:
// autenticação no login, validação de cadastro e exclusão segura.
public class UsuarioBO {
    private UsuarioDAO dao = new UsuarioDAO();

    // Expressão regular para validar e-mail no formato nome@dominio.extensao.
    // Compilada uma vez como constante para reutilização eficiente.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // Autentica o usuário: valida se os campos foram preenchidos e consulta o banco.
    // Lança BibliotecaException com mensagem genérica para não revelar se o e-mail existe.
    public Usuario autenticar(String email, String senha) throws SQLException {
        if (email == null || email.isBlank())
            throw new BibliotecaException("E-mail obrigatório.");
        if (senha == null || senha.isBlank())
            throw new BibliotecaException("Senha obrigatória.");
        Usuario u = dao.buscarPorEmailESenha(email, senha);
        if (u == null)
            throw new BibliotecaException("E-mail ou senha inválidos.");
        return u;
    }

    // Valida os dados do formulário e cadastra um novo usuário
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

    // Retorna todos os usuários cadastrados
    public List<Usuario> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    // Verifica a regra de negócio antes de excluir:
    // não podemos excluir um usuário que ainda tem livros emprestados
    public void excluir(int idUsuario) throws SQLException {
        if (dao.possuiEmprestimosAtivos(idUsuario))
            throw new BibliotecaException("Usuário possui empréstimos ativos e não pode ser excluído.");
        dao.excluir(idUsuario);
    }
}
