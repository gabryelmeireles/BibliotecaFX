package com.biblioteca.biblioteca.model.exception;

// Exceção customizada criada especificamente para este sistema.
// Usada para sinalizar erros de regra de negócio com mensagens amigáveis ao usuário,
// como "Título obrigatório." ou "Livro não disponível para empréstimo."
//
// Estende RuntimeException (exceção não verificada) para que os métodos que a lançam
// não precisem declarar "throws BibliotecaException" na assinatura.
// Isso deixa o código mais limpo, pois essas situações são tratadas nos Controllers.
public class BibliotecaException extends RuntimeException {

    // Construtor com apenas mensagem: o mais usado nas validações dos BOs
    public BibliotecaException(String mensagem) {
        super(mensagem);
    }

    // Construtor com mensagem + causa: usado quando queremos encapsular outra exceção
    // e ainda manter a mensagem amigável para o usuário
    public BibliotecaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
