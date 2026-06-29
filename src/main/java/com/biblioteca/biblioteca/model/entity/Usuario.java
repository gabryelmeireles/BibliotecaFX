package com.biblioteca.biblioteca.model.entity;

// Representa um usuário do sistema (pode ser comum ou administrador)
public class Usuario {
    private int id;
    private String nome;
    private String email;
    private String senha;

    // Construtor vazio usado ao montar o objeto a partir do banco
    public Usuario() {}

    // Construtor usado ao cadastrar um novo usuário
    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    // Identifica o admin pelo e-mail fixo — não há campo "perfil" no banco
    public boolean isAdmin() { return "admin@email.com".equals(email); }
}
