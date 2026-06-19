package com.biblioteca.biblioteca.model.entity;

// Representa um usuário do sistema (tanto admin quanto usuário comum).
// A distinção entre admin e usuário comum é feita pelo e-mail:
// "admin@email.com" é tratado como administrador nos Controllers.
public class Usuario {
    private int id;
    private String nome;
    private String email;
    private String senha;

    // Construtor vazio: usado pelo DAO quando monta o objeto com dados do banco
    public Usuario() {}

    // Construtor com dados: usado no cadastro de um usuário novo
    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    // Getters e Setters para acessar os atributos privados (encapsulamento)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    // Retorna true se este usuário é o administrador do sistema
    public boolean isAdmin() { return "admin@email.com".equals(email); }
}
