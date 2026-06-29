package com.biblioteca.biblioteca.model.exception;

// Exceção personalizada para erros de regra de negócio do sistema
// Estende RuntimeException para não obrigar o controller a declarar throws em todo lugar
public class BibliotecaException extends RuntimeException {

    public BibliotecaException(String mensagem) {
        super(mensagem);
    }

    // Segunda forma permite encadear a causa original, útil para log de erros
    public BibliotecaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
