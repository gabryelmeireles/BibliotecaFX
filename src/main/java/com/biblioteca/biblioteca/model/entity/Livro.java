package com.biblioteca.biblioteca.model.entity;

// Representa um livro no sistema. Esta classe é apenas um "molde" de dados —
// sem lógica de banco ou regras de negócio. Só guarda e expõe informações.
public class Livro {
    private int id;           // identificador único, gerado automaticamente pelo banco
    private String titulo;
    private String autor;
    private String isbn;      // código internacional de identificação do livro (opcional)
    private boolean disponivel; // false quando o livro está emprestado a alguém

    // Construtor vazio: usado pelo DAO quando monta o objeto com dados lidos do banco
    public Livro() {}

    // Construtor com dados: usado no cadastro de livro novo.
    // disponivel começa como true porque um livro recém-cadastrado está disponível.
    public Livro(String titulo, String autor, String isbn) {
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.disponivel = true;
    }

    // Getters e Setters: métodos para ler e alterar os atributos privados.
    // São necessários porque os atributos são private (encapsulamento).
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }

    // Usado pelo ComboBox do JavaFX para exibir o livro no formato "Título - Autor".
    // Sem este método, o ComboBox mostraria algo como "Livro@3a1f932".
    @Override
    public String toString() { return titulo + " - " + autor; }
}
