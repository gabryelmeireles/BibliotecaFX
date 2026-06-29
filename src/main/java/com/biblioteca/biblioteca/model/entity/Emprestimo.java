package com.biblioteca.biblioteca.model.entity;

import java.time.LocalDateTime;

// Representa um registro de empréstimo, ligando um usuário a um livro
public class Emprestimo {
    private int id;
    private Usuario usuario;
    private Livro livro;
    private LocalDateTime dataEmprestimo;
    private LocalDateTime dataPrevistaDevolucao;
    private LocalDateTime dataDevolucao;
    private boolean devolvido;

    // Construtor vazio usado ao montar o objeto a partir do banco
    public Emprestimo() {}

    // Construtor usado ao criar um novo empréstimo — já define o prazo de 7 dias automaticamente
    public Emprestimo(Usuario usuario, Livro livro) {
        this.usuario = usuario;
        this.livro = livro;
        this.dataEmprestimo = LocalDateTime.now();
        this.dataPrevistaDevolucao = LocalDateTime.now().plusDays(7);
        this.devolvido = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }
    public LocalDateTime getDataEmprestimo() { return dataEmprestimo; }
    public void setDataEmprestimo(LocalDateTime dataEmprestimo) { this.dataEmprestimo = dataEmprestimo; }
    public LocalDateTime getDataPrevistaDevolucao() { return dataPrevistaDevolucao; }
    public void setDataPrevistaDevolucao(LocalDateTime dataPrevistaDevolucao) { this.dataPrevistaDevolucao = dataPrevistaDevolucao; }
    public LocalDateTime getDataDevolucao() { return dataDevolucao; }
    public void setDataDevolucao(LocalDateTime dataDevolucao) { this.dataDevolucao = dataDevolucao; }
    public boolean isDevolvido() { return devolvido; }
    public void setDevolvido(boolean devolvido) { this.devolvido = devolvido; }

    // Verifica em tempo real se o prazo já passou e o livro ainda não foi devolvido
    public boolean isAtrasado() {
        return !devolvido && LocalDateTime.now().isAfter(dataPrevistaDevolucao);
    }
}
