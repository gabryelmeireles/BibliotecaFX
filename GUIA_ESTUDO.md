# Guia de Estudo — BibliotecaFX

> **Como usar este guia:** leia tudo antes da apresentação. Na seção de perguntas do professor, treine respondendo em voz alta — falar é muito diferente de só ler.

---

## 1. O Que o Sistema Faz (visão do usuário)

O BibliotecaFX é um sistema desktop de gerenciamento de biblioteca. Ele permite:

- **Login:** entrar com e-mail e senha
- **Empréstimos:** selecionar um livro disponível e registrar o empréstimo por 7 dias *(disponível para todos os usuários)*
- **Devoluções:** selecionar um empréstimo ativo e registrar a devolução *(somente admin)*
- **Livros:** cadastrar, listar e excluir livros *(somente admin)*
- **Usuários:** cadastrar, listar e excluir usuários *(somente admin)*
- **Logs:** todas as ações e erros são gravados automaticamente em arquivos de texto

**Distinção de perfis:**
| Função | Usuário comum | Admin (`admin@email.com`) |
|--------|:---:|:---:|
| Fazer empréstimo | ✅ | ✅ |
| Devolver os próprios livros | ✅ | ✅ |
| Devolver livros de qualquer usuário | ❌ | ✅ |
| Gerenciar livros | ❌ | ✅ |
| Gerenciar usuários | ❌ | ✅ |

O menu lateral esconde "Livros" e "Usuários" para usuários comuns. A tela de "Devoluções" é visível para todos, mas filtra o conteúdo: usuário comum vê só os próprios empréstimos; admin vê os de todos.

---

## 2. A Arquitetura em Camadas (a estrutura do projeto)

O sistema é dividido em camadas, onde cada camada tem uma responsabilidade específica:

```
[TELA - FXML]  ←→  [CONTROLLER]  ←→  [BO - regras]  ←→  [DAO - banco]  ←→  [PostgreSQL]
```

| Camada | Pasta | O que faz |
|--------|-------|-----------|
| **Entity** | `model/entity` | Define os dados: Livro, Usuario, Emprestimo |
| **DAO** | `model/dao` | Faz as operações no banco de dados (SQL) |
| **BO** | `model/bo` | Aplica as regras de negócio e validações |
| **Controller** | `controller` | Controla a tela e reage aos cliques do usuário |
| **Util** | `util` | Ferramentas auxiliares: conexão com banco e log |
| **View** | `resources/.../view` | Arquivos FXML — definem o layout das telas |

**Por que essa separação?**
Se precisar mudar o banco de dados (ex: de PostgreSQL para MySQL), só muda os DAOs. Se mudar uma regra (ex: prazo de 7 para 14 dias), só muda o BO. Se mudar o visual, só muda o FXML. Cada parte pode mudar sem afetar as outras.

---

## 3. Arquivo por Arquivo

---

### `Launcher.java`

**O que é:** a classe que você executa para iniciar o programa. É a porta de entrada.

**Por que existe além do MainApp?**
Quando o Java usa o sistema de módulos (`module-info.java`, disponível desde o Java 9), ele bloqueia a inicialização direta de classes que estendem `Application`. O `Launcher` não tem essa restrição e simplesmente repassa o controle para o `MainApp`.

**Se eu tirar:** o programa não inicia — vai dar erro de módulo.

**Linha a linha:**
```java
Application.launch(MainApp.class, args);
```
→ Diz ao JavaFX: "inicie o MainApp com esses argumentos".

---

### `MainApp.java`

**O que é:** a classe principal do JavaFX. Define como a janela é configurada e qual tela abre primeiro.

**Linha a linha:**

```java
public class MainApp extends Application
```
→ Toda aplicação JavaFX precisa estender `Application`. Isso registra a classe no ciclo de vida do JavaFX.

```java
public void start(Stage stage) throws Exception
```
→ O JavaFX chama este método automaticamente. `Stage` é a janela do sistema operacional.

```java
FXMLLoader loader = new FXMLLoader(getClass().getResource("...login-view.fxml"))
```
→ Carrega o arquivo de layout da tela de login. Pense no FXML como o "HTML" do JavaFX — define onde ficam os botões e campos.

```java
Scene scene = new Scene(loader.load())
```
→ `Scene` é o "conteúdo" que aparece dentro da janela.

```java
stage.setMinWidth(900); stage.setMinHeight(560);
```
→ Impede que a janela fique pequena demais e quebre o layout.

```java
stage.setMaximized(true)
```
→ Abre a janela já em tela cheia.

```java
launch(args)
```
→ Método herdado de `Application` que inicializa todo o ciclo do JavaFX.

---

### `model/entity/Livro.java`

**O que é:** uma "caixinha" de dados que representa um livro. Não tem lógica — só armazena informações.

**Atributos:**
- `id` → gerado automaticamente pelo banco (auto-increment). Nunca defina manualmente.
- `titulo`, `autor` → dados obrigatórios
- `isbn` → código internacional do livro (opcional)
- `disponivel` → `true` = livre para empréstimo, `false` = emprestado

**Construtores:**
- `Livro()` sem parâmetros → usado pelo DAO quando lê dados do banco
- `Livro(titulo, autor, isbn)` com parâmetros → usado no cadastro. Começa com `disponivel = true` porque um livro recém-cadastrado está disponível.

**Getters e Setters:** métodos para ler e alterar os atributos. São necessários porque os atributos são `private` (encapsulamento — proteção de dados).

**`toString()`:**
```java
return titulo + " - " + autor;
```
→ O JavaFX chama esse método automaticamente para exibir o livro no ComboBox. Sem ele, apareceria algo como `Livro@3a1f932`.

**Se eu tirar o `toString()`:** o ComboBox mostraria um código interno incompreensível.

---

### `model/entity/Usuario.java`

**O que é:** representa um usuário do sistema. Mesma estrutura do `Livro.java`.

**Diferença importante:** não existe campo "é admin" no banco. O sistema identifica o admin pelo e-mail `admin@email.com`. Isso é encapsulado no método `isAdmin()`:

```java
public boolean isAdmin() { return "admin@email.com".equals(email); }
```
→ Centraliza a verificação em um único lugar. Se amanhã a regra mudar (ex: verificar um campo no banco), só muda aqui — sem alterar Controllers.

**Se eu tirar a senha da entidade:** o sistema não conseguiria fazer login.

---

### `model/entity/Emprestimo.java`

**O que é:** representa um empréstimo — a ligação entre um usuário e um livro com as datas envolvidas.

**Atributos importantes:**
- `dataDevolucao` → fica `null` enquanto o livro não foi devolvido
- `devolvido` → campo que marca se o livro foi devolvido

**Construtor principal:**
```java
this.dataEmprestimo = LocalDateTime.now();
this.dataPrevistaDevolucao = LocalDateTime.now().plusDays(7);
```
→ Define automaticamente a data de hoje e o prazo de 7 dias. Para mudar o prazo, basta trocar o número aqui.

**Método `isAtrasado()`:**
```java
return !devolvido && LocalDateTime.now().isAfter(dataPrevistaDevolucao);
```
→ Retorna `true` se o livro **não foi devolvido** E a **data prevista já passou**. Usado para pintar a linha de vermelho na tela de empréstimos.

**Se eu tirar `isAtrasado()`:** as linhas nunca ficam vermelhas e a coluna "Status" quebraria.

---

### `model/exception/BibliotecaException.java`

**O que é:** uma exceção criada especialmente para este sistema, para carregar mensagens amigáveis ao usuário.

**Por que não usar `Exception` normal?**
Com `Exception` normal, a mensagem chegaria ao usuário como `NullPointerException` ou `SQLException`, que não dizem nada. Com `BibliotecaException`, controlamos a mensagem: "Título obrigatório.", "Livro não disponível.", etc.

**Por que estende `RuntimeException` e não `Exception`?**
Exceções de `Exception` (checked) obrigam todos os métodos que podem lançá-la a declarar `throws BibliotecaException` na assinatura. Como essas exceções são tratadas nos Controllers, declarar em todos os métodos do BO e DAO seria ruído desnecessário. `RuntimeException` (unchecked) dispensa isso.

**Se eu tirar:** o sistema ainda funciona, mas as mensagens de erro para o usuário seriam técnicas e confusas.

---

### `util/DatabaseConnection.java`

**O que é:** a ponte entre o Java e o PostgreSQL. Toda vez que o sistema precisa falar com o banco, usa esta classe.

**Por que as credenciais estão em `database.properties`?**
Se ficassem no código-fonte, qualquer pessoa com acesso ao GitHub veria a senha. Com o arquivo `.properties` no `.gitignore`, ele nunca vai ao GitHub. Quem for rodar usa o `database.properties.example` como modelo.

**O bloco `static {}`:**
```java
static {
    // código aqui
}
```
→ Executado **uma única vez** quando a classe é carregada pelo Java. Lê o arquivo de configuração e extrai URL, usuário e senha. Se o arquivo não existir, o sistema para imediatamente com uma mensagem clara.

**`getConnection()`:**
→ Abre e retorna uma nova conexão. Os DAOs são responsáveis por fechar usando `try-with-resources`.

**Se eu tirar:** o sistema não consegue conectar ao banco — nada funciona.

---

### `util/LogUtil.java`

**O que é:** grava registros de ações e erros em arquivos de texto — requisito do edital.

**Dois arquivos de log:**
- `logs/uso.log` → ações normais (login, cadastro, empréstimo...)
- `logs/excecoes.log` → erros que aconteceram

**Exemplo de linha gerada:**
```
[17/06/2026 14:30:22] ACAO='LOGIN_REALIZADO' USUARIO='admin@email.com'
```

**`FileWriter(arquivo, true)`:** o `true` significa "modo append" — adiciona ao final sem apagar o que já estava no arquivo.

**O bloco `static {}`:** cria a pasta `logs/` na primeira vez que a classe é usada.

**Se eu tirar:** o sistema funciona normalmente, mas não há rastreabilidade — não é possível saber o que aconteceu.

---

### `model/dao/LivroDAO.java`

**O que é:** faz todas as operações de banco de dados relacionadas a livros.

**`PreparedStatement` com `?`:**
```java
String sql = "INSERT INTO livro (titulo, autor, isbn) VALUES (?, ?, ?)";
ps.setString(1, livro.getTitulo()); // substitui o 1º "?"
```
→ Os `?` são marcadores. O JDBC substitui por valores reais de forma segura, evitando **SQL Injection** (ataque onde alguém digita SQL nos campos do formulário).

**`try-with-resources`:**
```java
try (Connection conn = ...; PreparedStatement ps = ...) {
    // código
}
```
→ Fecha automaticamente a conexão, PreparedStatement e ResultSet ao sair do bloco, mesmo se der exceção. Evita "conexões abertas" que consomem recursos do banco.

**Transação em `excluir()`:**
```java
conn.setAutoCommit(false); // inicia a transação
// DELETE empréstimos...
// DELETE livro...
conn.commit();   // confirma tudo
// se falhar:
conn.rollback(); // desfaz tudo
```
→ As duas operações precisam acontecer juntas. Se deletar os empréstimos mas falhar no livro, o `rollback()` desfaz tudo, evitando dados inconsistentes no banco.

---

### `model/dao/UsuarioDAO.java`

Estrutura idêntica ao `LivroDAO.java`. Diferenças:

**`buscarPorEmailESenha()`:** retorna `null` se não encontrar. O `UsuarioBO` trata esse null com a mensagem "E-mail ou senha inválidos."

**Na listagem, a senha não é carregada:** boa prática — não trafegamos dados sensíveis sem necessidade.

---

### `model/dao/EmprestimoDAO.java`

**Diferença: usa JOIN nas queries**
```sql
SELECT e.id, u.nome, l.titulo, ...
FROM emprestimo e
JOIN usuario u ON e.id_usuario = u.id
JOIN livro l ON e.id_livro = l.id
WHERE e.devolvido = false
```
→ Em vez de fazer 3 consultas separadas (uma para empréstimo, outra para usuário, outra para livro), o JOIN traz tudo em uma consulta só. Mais eficiente.

**`mapearEmprestimo()`:** método privado que transforma uma linha do banco em um objeto Java completo. Foi criado para não repetir o mesmo código em `listarAtivos()` e `listarTodosAtivos()`.

**`listarAtivos()` vs `listarTodosAtivos()`:**
- `listarAtivos(idUsuario)` → só os empréstimos de um usuário (tela de Empréstimos)
- `listarTodosAtivos()` → empréstimos de todos (tela de Devoluções — só o admin usa)

---

### `model/bo/LivroBO.java`

**O que é:** camada de regras de negócio para livros. Filtra o que pode ou não chegar ao banco.

**Validações em `cadastrar()`:**
- `titulo.isBlank()` → retorna `true` para texto vazio ou só espaços
- ISBN é opcional; se informado, precisa ter o formato correto

**A regex do ISBN:**
```java
Pattern.compile("^(?:\\d{9}[\\dX]|\\d{13})$")
```
→ Aceita ISBN-10 (9 dígitos + dígito ou X) ou ISBN-13 (13 dígitos). `\\d` significa "qualquer dígito de 0 a 9". Se o professor perguntar: *"Pesquisei o padrão de validação de ISBN e usei expressão regular por ser a forma mais concisa e precisa de validar esse formato."*

**`listarDisponiveis()` com Stream:**
```java
dao.listarTodos().stream()
    .filter(Livro::isDisponivel)
    .collect(Collectors.toList())
```
→ `stream()` cria um fluxo de livros; `.filter()` mantém só os disponíveis; `.collect()` junta de volta numa lista. `Livro::isDisponivel` é uma referência de método — equivale a `l -> l.isDisponivel()`.

---

### `model/bo/UsuarioBO.java`

**A regex do e-mail:**
```java
Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
```
→ Valida o formato `nome@dominio.extensao`. Se o professor perguntar: *"Pesquisei o padrão padrão de validação de e-mail com expressão regular — é a forma mais robusta de verificar o formato sem depender de bibliotecas externas."*

**`autenticar()`:** mensagem genérica "E-mail ou senha inválidos" — proposital. Se dissesse "senha incorreta", revelaríamos que o e-mail existe.

---

### `model/bo/EmprestimoBO.java`

**`realizarEmprestimo()`:** duas operações em sequência:
1. Salva o empréstimo no banco
2. Marca o livro como indisponível

**`realizarDevolucao()`:** o inverso:
1. Marca o empréstimo como devolvido
2. Marca o livro como disponível novamente

**`listarTodosAtivos()` com Stream:**
```java
.stream()
.sorted(Comparator.comparing(Emprestimo::getDataPrevistaDevolucao))
.collect(Collectors.toList())
```
→ Ordena os empréstimos do prazo mais próximo para o mais distante. O admin vê primeiro o que está prestes a atrasar.

---

### `controller/LoginController.java`

**O que é:** controla o formulário de login (campos de e-mail, senha e botão "Entrar").

**`@FXML`:** esta anotação conecta a variável Java ao componente com o mesmo `fx:id` no arquivo FXML. Se o `fx:id` no FXML for `emailField`, a variável Java deve ter o mesmo nome.

**`handleLogin()`:**
1. Chama `bo.autenticar()` → se as credenciais forem inválidas, lança exceção
2. Carrega o FXML da tela principal
3. Passa o objeto `Usuario` para o `MainController` via `mc.setUsuarioLogado(u)`
4. Troca o conteúdo da janela

**Como passar dados entre telas:**
```java
MainController mc = loader.getController();
mc.setUsuarioLogado(u);
```
→ Após carregar o FXML, `getController()` retorna o controller daquela tela. Chamamos `setUsuarioLogado()` para repassar quem fez login.

---

### `controller/MainController.java`

**O que é:** a tela principal com o menu lateral. Não tem formulários — só gerencia a navegação.

**Como a navegação funciona:**
```java
conteudoPane.getChildren().setAll(pane);
```
→ O `conteudoPane` é um `StackPane` — um painel que empilha conteúdo. `setAll()` troca o conteúdo atual pela nova tela. **Só uma janela é usada — o conteúdo central muda dinamicamente.** Isso evita abrir várias janelas ao mesmo tempo.

**Controle de acesso por perfil:**
```java
boolean admin = u.isAdmin();
btnDevolucoes.setVisible(admin);
btnDevolucoes.setManaged(admin);
btnLivros.setVisible(admin);
btnLivros.setManaged(admin);
btnUsuarios.setVisible(admin);
btnUsuarios.setManaged(admin);
```
→ `setVisible(false)` torna o botão invisível. `setManaged(false)` faz o layout ignorar o espaço que ele ocuparia — sem ele, os botões invisíveis deixariam lacunas no menu. Com os dois juntos, o botão some completamente.

→ Usuário comum vê só "Empréstimos" e "Sair". Admin vê tudo.

**`instanceof` com pattern matching (Java 16+):**
```java
if (ctrl instanceof EmprestimoController ec) ec.setUsuarioLogado(usuarioLogado);
```
→ Em uma linha: verifica se é do tipo `EmprestimoController` E já cria a variável `ec`. Equivale ao código mais longo: `if (ctrl instanceof EmprestimoController) { EmprestimoController ec = (EmprestimoController) ctrl; ec.setUsuarioLogado(...); }`

---

### `controller/LivroController.java`

**`setCellValueFactory()`:** define de onde cada coluna da tabela tira o valor.
```java
colTitulo.setCellValueFactory(d ->
    new SimpleStringProperty(d.getValue().getTitulo()));
```
→ Para cada linha, `d.getValue()` retorna o objeto `Livro` daquela linha. `.getTitulo()` pega o título.

**Botão Excluir escondido para não-admin:**
```java
btnExcluir.setVisible(u.getEmail().equals("admin@email.com"));
```
→ `setVisible(false)` torna o botão invisível. O admin vê, os demais não.

**`FXCollections.observableArrayList()`:** o JavaFX precisa de uma lista "observável" para atualizar a tabela automaticamente quando os dados mudam.

---

### `controller/UsuarioController.java`

Estrutura igual ao `LivroController.java`. Proteção extra no `handleExcluir()`:
```java
if (selecionado.getEmail().equals("admin@email.com"))
    throw new Exception("O usuário admin não pode ser excluído.");
```
→ Mesmo o admin logado não pode excluir a si mesmo. Garante que sempre exista um administrador no sistema.

---

### `controller/EmprestimoController.java`

**ComboBox de livros:** exibe apenas livros com `disponivel = true`. Após o empréstimo, a lista é recarregada e o livro emprestado some automaticamente.

**Linha vermelha para atraso:**
```java
emprestimosTable.setRowFactory(tv -> new TableRow<Emprestimo>() {
    @Override
    protected void updateItem(Emprestimo emp, boolean empty) {
        super.updateItem(emp, empty);
        if (emp != null && emp.isAtrasado()) {
            setStyle("-fx-background-color: #fadbd8;");
        } else {
            setStyle("");
        }
    }
});
```
→ O JavaFX chama `updateItem()` para cada linha ao desenhar a tabela. Se o empréstimo estiver atrasado (verificado por `isAtrasado()`), pinta de vermelho claro. Senão, remove a cor.

---

### `controller/DevolucaoController.java`

**Filtragem por perfil:**
```java
List<Emprestimo> lista = usuarioLogado.isAdmin()
    ? bo.listarTodosAtivos()
    : bo.listarAtivos(usuarioLogado.getId());
```
→ Admin vê todos os empréstimos ativos do sistema (modelo de bibliotecário: o usuário devolve o livro ao balcão e o admin registra). Usuário comum vê só os próprios empréstimos e pode devolvê-los diretamente.

**Após registrar a devolução:** `carregarDados()` é chamado e o empréstimo some da tabela automaticamente (porque já está marcado como `devolvido = true` no banco).

---

## 4. Perguntas Prováveis do Professor

### Sobre a Arquitetura

**"Por que você separou em DAO, BO e Controller?"**
> Cada camada tem uma responsabilidade específica. O DAO cuida só do banco, o BO cuida das regras de negócio e o Controller cuida da tela. Se eu precisar mudar o banco de dados, só mexo nos DAOs. Se mudar uma regra de negócio, só mexo no BO. Isso facilita manutenção e evita que tudo fique misturado no mesmo arquivo.

**"O que é o padrão DAO?"**
> DAO significa "Data Access Object". É um padrão de projeto que isola toda a lógica de acesso ao banco em uma classe separada. O resto do sistema não sabe se os dados vêm de um banco SQL, de um arquivo ou de qualquer outro lugar.

---

### Sobre Banco de Dados

**"O que é PreparedStatement? Por que não usou Statement?"**
> PreparedStatement usa `?` como marcadores. O JDBC substitui esses marcadores com os valores reais de forma segura, tratando-os como dados puros — nunca como parte do SQL. Com `Statement` comum, se eu concatenar strings diretamente na SQL, um usuário mal-intencionado poderia digitar um trecho de SQL no campo e executar comandos no banco (isso se chama SQL Injection). PreparedStatement impede isso.

**"O que é try-with-resources?"**
> É uma sintaxe do Java que garante que recursos como conexões de banco e arquivos sejam fechados automaticamente ao sair do bloco, mesmo que ocorra uma exceção. Antes dessa sintaxe, precisávamos de um bloco `finally` para fechar na mão — era fácil esquecer e deixar conexões abertas, o que consumia recursos do banco sem necessidade.

**"O que é uma transação? Por que usou no excluir?"**
> Transação é um conjunto de operações que devem acontecer todas juntas ou nenhuma. No caso de excluir um livro, preciso primeiro deletar seus empréstimos e depois deletar o livro. Se a primeira operação funcionar mas a segunda falhar, o banco ficaria com empréstimos "órfãos" apontando para um livro que não existe mais. Com a transação, se qualquer etapa falhar, o `rollback()` desfaz tudo automaticamente.

---

### Sobre Java e JavaFX

**"O que é FXML?"**
> É um formato baseado em XML usado pelo JavaFX para definir o layout da interface. É como o HTML para uma página web — define onde ficam os botões, campos de texto e tabelas. A separação entre layout (FXML) e lógica (Java) deixa o código mais organizado: posso mudar a aparência sem mexer no código Java.

**"O que é a anotação `@FXML`?"**
> Ela conecta uma variável Java a um componente definido no arquivo FXML. O JavaFX usa reflexão para injetar o componente cujo `fx:id` corresponde ao nome da variável. Sem ela, a variável ficaria `null` e daria `NullPointerException` ao tentar usar.

**"Por que existe `Launcher.java` além de `MainApp.java`?"**
> Por uma limitação técnica do Java com módulos. Quando o projeto usa `module-info.java`, o JavaFX não consegue iniciar diretamente de uma classe que estende `Application`. O `Launcher` contorna isso sendo a classe de entrada sem essa restrição e delega para o `MainApp`.

**"O que é `instanceof` com pattern matching?"**
> É uma funcionalidade do Java 16 que combina verificação de tipo com declaração de variável em uma linha só. Em vez de escrever `if (ctrl instanceof EmprestimoController) { EmprestimoController ec = (EmprestimoController) ctrl; ec.setUsuarioLogado(u); }`, escrevo apenas `if (ctrl instanceof EmprestimoController ec) { ec.setUsuarioLogado(u); }`. Mais conciso e sem necessidade de cast explícito.

---

### Sobre Controle de Acesso (Admin vs Usuário Comum)

**"O que o admin pode fazer que o usuário comum não pode?"**
> O admin tem acesso a quatro funcionalidades: gerenciar livros (cadastrar e excluir), gerenciar usuários (cadastrar e excluir), registrar devoluções de qualquer empréstimo do sistema, e fazer empréstimos. O usuário comum só tem acesso à tela de empréstimos — pode pegar livros emprestados, mas não gerencia o acervo nem os outros usuários.

**"Como o sistema controla quem pode ver o quê?"**
> Quando o usuário faz login, o `LoginController` passa o objeto `Usuario` para o `MainController` via `setUsuarioLogado()`. Nesse método, verifico se é admin com `u.isAdmin()` e uso `setVisible()` e `setManaged()` para mostrar ou esconder os botões do menu lateral. O usuário comum não vê os botões de "Livros" e "Usuários" — eles somem do menu. A tela de "Devoluções" é visível para todos, mas o `DevolucaoController` filtra os dados: admin chama `listarTodosAtivos()`, usuário comum chama `listarAtivos(id)` e vê só os próprios empréstimos.

**"Como você identifica quem é admin?"**
> O método `isAdmin()` na classe `Usuario` compara o e-mail com `"admin@email.com"`. Não existe um campo "tipo de usuário" no banco — a distinção é feita pelo e-mail. Centralizei essa verificação em um método para não repetir a comparação em vários arquivos: se a regra mudar, altero só em um lugar.

**"Por que usar `setManaged(false)` além de `setVisible(false)`?"**
> `setVisible(false)` torna o componente invisível, mas o JavaFX ainda reserva o espaço que ele ocupa no layout. Com `setManaged(false)`, o layout ignora completamente o botão — ele não ocupa espaço, não deixa lacuna no menu. Os dois juntos são necessários para que o botão suma de verdade visualmente.

---

### Sobre Regras de Negócio

**"Por que você criou a BibliotecaException?"**
> Para que as mensagens de erro mostradas ao usuário sejam claras e amigáveis. Quando valido que o título é obrigatório, não quero que apareça `NullPointerException` para o usuário — quero "Título obrigatório." A `BibliotecaException` carrega essa mensagem controlada, e o Controller simplesmente a exibe na tela.

**"Por que `BibliotecaException` estende `RuntimeException`?"**
> Exceções que estendem `Exception` (checked) obrigam todos os métodos que podem lançá-la a declarar isso na assinatura com `throws`. Como essas exceções são tratadas nos Controllers, declarar em todos os métodos dos BOs e DAOs seria muito ruído sem benefício real. `RuntimeException` (unchecked) dispensa essa declaração obrigatória.

**"O que são Streams? Por que usou?"**
> Streams são uma forma funcional de processar coleções, introduzida no Java 8. Permitem filtrar, ordenar e transformar listas de forma concisa e legível. Usei em `LivroBO.listarDisponiveis()` para filtrar livros disponíveis e em `EmprestimoBO.listarTodosAtivos()` para ordenar empréstimos por prazo de devolução.

**"Por que validou ISBN com expressão regular?"**
> O ISBN tem um formato muito específico: 10 ou 13 dígitos com regras precisas. Expressão regular é a forma mais concisa e exata de validar esse padrão em uma linha, sem precisar de vários `if`s aninhados. A expressão `^(?:\d{9}[\dX]|\d{13})$` significa: "começa, tem 9 dígitos + um dígito ou X OU tem 13 dígitos, termina".

---

### Sobre Segurança e Boas Práticas

**"Por que as credenciais do banco estão em um arquivo separado?"**
> Se ficassem no código-fonte, qualquer pessoa com acesso ao repositório no GitHub veria a senha do banco de dados. Com o arquivo `database.properties` listado no `.gitignore`, ele nunca é enviado ao GitHub. Quem for rodar o projeto usa o `database.properties.example` como modelo para criar o próprio arquivo de configuração.

**"Por que o admin não pode ser excluído?"**
> Para garantir que sempre exista pelo menos um administrador no sistema. Se o admin fosse excluído, ninguém teria mais acesso às funções de gerenciamento (excluir usuários, registrar devoluções). É uma proteção de integridade do sistema.

**"O que são os logs gerados pelo sistema?"**
> O sistema grava dois arquivos na pasta `logs/`: `uso.log` registra ações normais (login, cadastro, empréstimo...) e `excecoes.log` registra erros. Isso é rastreabilidade — permite saber o que aconteceu no sistema e quando, útil para auditoria. É um requisito explícito do edital.

**"Por que a listagem de usuários não carrega a senha?"**
> Boa prática de segurança: não trafegamos dados sensíveis que não são necessários. A lista de usuários é só para visualização — a senha não precisa estar nela.

---

## 5. Como Fazer Mudanças Rápidas (se o professor pedir na hora)

### Mudar o prazo de devolução de 7 para 14 dias
**Arquivo:** `model/entity/Emprestimo.java`
```java
// Antes:
this.dataPrevistaDevolucao = LocalDateTime.now().plusDays(7);
// Depois:
this.dataPrevistaDevolucao = LocalDateTime.now().plusDays(14);
```

---

### Mudar a senha mínima de 4 para 6 caracteres
**Arquivo:** `model/bo/UsuarioBO.java`
```java
// Antes:
if (senha == null || senha.length() < 4)
    throw new BibliotecaException("Senha deve ter ao menos 4 caracteres.");
// Depois:
if (senha == null || senha.length() < 6)
    throw new BibliotecaException("Senha deve ter ao menos 6 caracteres.");
```

---

### Mudar o título da janela
**Arquivo:** `MainApp.java`
```java
stage.setTitle("Sistema de Biblioteca"); // mude o texto aqui
```

---

### Mostrar apenas livros disponíveis na tela de Livros
**Arquivo:** `controller/LivroController.java`, método `carregarDados()`
```java
// Antes:
livrosTable.setItems(FXCollections.observableArrayList(bo.listarTodos()));
// Depois:
livrosTable.setItems(FXCollections.observableArrayList(bo.listarDisponiveis()));
```

---

### Mudar a cor do destaque de atraso
**Arquivo:** `controller/EmprestimoController.java`
```java
// Atual (vermelho claro):
setStyle("-fx-background-color: #fadbd8;");
// Amarelo:
setStyle("-fx-background-color: #fef9c3;");
// Laranja claro:
setStyle("-fx-background-color: #fde8d0;");
```

---

### Adicionar número de telefone ao usuário

**Passo 1 — `model/entity/Usuario.java`:** adicionar o atributo e os getters/setters
```java
private String telefone;

public String getTelefone() { return telefone; }
public void setTelefone(String telefone) { this.telefone = telefone; }
```

**Passo 2 — `model/dao/UsuarioDAO.java`:** atualizar os SQLs
```java
// No salvar():
String sql = "INSERT INTO usuario (nome, email, senha, telefone) VALUES (?, ?, ?, ?)";
ps.setString(4, usuario.getTelefone());

// No listarTodos(), adicionar no while:
u.setTelefone(rs.getString("telefone"));
```

**Passo 3 — no banco de dados:**
```sql
ALTER TABLE usuario ADD COLUMN telefone VARCHAR(20);
```

**Passo 4 — no FXML `usuario-view.fxml`:** adicionar campo de telefone no formulário.

**Passo 5 — `controller/UsuarioController.java`:** adicionar a variável `@FXML TextField telefoneField` e passar o valor no cadastro.

---

## 6. Glossário para a Apresentação

| Termo | O que dizer se perguntarem |
|-------|---------------------------|
| **JavaFX** | Framework do Java para criar aplicações desktop com interface gráfica |
| **FXML** | Arquivo XML que define o layout das telas no JavaFX |
| **Maven** | Ferramenta que gerencia as dependências (bibliotecas externas) do projeto |
| **PostgreSQL** | Banco de dados relacional onde todos os dados são armazenados |
| **PreparedStatement** | Forma segura de executar SQL no Java, previne SQL Injection |
| **try-with-resources** | Sintaxe Java que fecha recursos (banco, arquivos) automaticamente |
| **Encapsulamento** | Atributos `private` + getters/setters — proteção e controle de acesso aos dados |
| **Transação** | Grupo de operações que acontecem todas juntas ou nenhuma |
| **Stream** | Forma funcional de processar listas no Java 8+: filtrar, ordenar, transformar |
| **Regex** | Expressão regular — padrão para validar formatos de texto (e-mail, ISBN) |
| **RuntimeException** | Exceção não verificada — não precisa ser declarada na assinatura do método |
| **DAO** | Padrão que isola toda a lógica de acesso ao banco em uma classe separada |
| **BO** | Camada que contém as regras de negócio e validações |
| **@FXML** | Anotação que conecta uma variável Java a um componente do arquivo FXML |
| **ObservableList** | Lista do JavaFX que notifica a tabela quando seus dados mudam |
| **JOIN** | Comando SQL que une dados de múltiplas tabelas em uma consulta só |
| **module-info.java** | Arquivo que define o módulo Java do projeto (sistema de módulos do Java 9+) |
