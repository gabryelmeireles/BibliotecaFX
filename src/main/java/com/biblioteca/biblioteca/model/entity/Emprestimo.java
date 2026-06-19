package com.biblioteca.biblioteca.model.entity;

import java.time.LocalDateTime;

// Representa um empréstimo: a ligação entre um usuário e um livro durante um período.
// Quando um livro é emprestado, cria-se um Emprestimo. Quando devolvido, marca-se devolvido = true.
public class Emprestimo {
    private int id;
    private Usuario usuario;               // quem pegou o livro emprestado
    private Livro livro;                   // qual livro foi emprestado
    private LocalDateTime dataEmprestimo;
    private LocalDateTime dataPrevistaDevolucao;
    private LocalDateTime dataDevolucao;   // fica null até o livro ser devolvido
    private boolean devolvido;

    // Construtor vazio: usado pelo DAO ao montar objetos com dados vindos do banco
    public Emprestimo() {}

    // Construtor principal: cria o empréstimo definindo as datas automaticamente.
    // O prazo de devolução é definido como 7 dias a partir de hoje.
    public Emprestimo(Usuario usuario, Livro livro) {
        this.usuario = usuario;
        this.livro = livro;
        this.dataEmprestimo = LocalDateTime.now();
        this.dataPrevistaDevolucao = LocalDateTime.now().plusDays(7); // prazo: 7 dias
        this.devolvido = false;
    }

    // Getters e Setters
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

    // Verifica se o empréstimo está atrasado:
    // só está atrasado se NÃO foi devolvido E a data prevista já passou.
    // Usado na tela de empréstimos para pintar a linha de vermelho.
    public boolean isAtrasado() {
        return !devolvido && LocalDateTime.now().isAfter(dataPrevistaDevolucao);
    }
}
