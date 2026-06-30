# 📚 BibliotecaFX

Sistema desktop de gerenciamento de biblioteca desenvolvido em **Java 21** com **JavaFX 21**, seguindo a arquitetura em camadas **MVC** (Model-View-Controller). Projeto prático da disciplina de Programação III — Instituto Federal de Goiás, Campus Luziânia.

---

## 🛠️ Tecnologias utilizadas

- **Java 21**
- **JavaFX 21.0.6** — interface gráfica desktop
- **PostgreSQL** — persistência de dados (driver `postgresql 42.7.4`)
- **Maven** — gerenciamento de dependências e build
- **JDBC** com `PreparedStatement` — acesso ao banco de dados

---

## 🏗️ Arquitetura

O projeto segue o padrão **MVC em camadas**, com responsabilidades bem separadas:

```
View (FXML)  →  Controller  →  BO (Business Object)  →  DAO  →  PostgreSQL
```

| Camada | Responsabilidade |
|---|---|
| **View** (`view/*.fxml`) | Telas da aplicação, descritas em XML |
| **Controller** | Recebe eventos da tela e coordena o fluxo |
| **BO** (Business Object) | Regras de negócio e validações |
| **DAO** (Data Access Object) | Acesso ao banco via SQL/JDBC |
| **Entity** | Representação dos dados (`Livro`, `Usuario`, `Emprestimo`) |
| **Util** | Classes de suporte (conexão com banco, logs) |
| **Exception** | Exceção customizada de regras de negócio |

### Estrutura de pastas

```
src/main/java/com/biblioteca/biblioteca/
├── Launcher.java                # ponto de entrada (contorna restrição do module-info)
├── MainApp.java                 # inicializa o JavaFX
├── controller/
│   ├── LoginController.java
│   ├── MainController.java      # navegação principal (painel dinâmico)
│   ├── LivroController.java
│   ├── EmprestimoController.java
│   ├── DevolucaoController.java
│   ├── UsuarioController.java
│   └── LogController.java       # auditoria — somente admin
├── model/
│   ├── entity/                  # Livro, Usuario, Emprestimo
│   ├── bo/                      # LivroBO, UsuarioBO, EmprestimoBO
│   ├── dao/                     # LivroDAO, UsuarioDAO, EmprestimoDAO
│   └── exception/
│       └── BibliotecaException.java
└── util/
    ├── DatabaseConnection.java
    └── LogUtil.java
```

---

## ✨ Funcionalidades

### Autenticação
Login por e-mail e senha. Apenas usuários autenticados acessam a aplicação. Mensagens de erro são genéricas por segurança — o sistema nunca revela se o e-mail existe ou se foi a senha que errou.

### Gerenciamento de usuários *(admin)*
Cadastro, listagem e exclusão de usuários, com validação de e-mail via expressão regular e bloqueio de exclusão de usuários com empréstimos ativos.

### Gerenciamento de livros *(admin)*
Cadastro, listagem e exclusão de livros, com validação de ISBN-10/ISBN-13 via expressão regular e bloqueio de exclusão de livros com empréstimo ativo.

### 📖 Caso de uso — Realizar Empréstimo
Usuário seleciona um livro disponível e realiza o empréstimo, com prazo de devolução automático de **7 dias**. Empréstimos atrasados são destacados em vermelho na tabela.

### 🔄 Caso de uso — Registrar Devolução
Registra a devolução de um empréstimo ativo e libera o livro para novo empréstimo. Administradores visualizam todos os empréstimos do sistema; usuários comuns visualizam apenas os próprios.

### 🧾 Auditoria e Logs *(admin)*
Tela dedicada (`LogController`) que lê e exibe os arquivos de log do sistema em uma tabela, com data/hora, tipo (ação ou erro), usuário responsável e detalhes — ordenados do mais recente para o mais antigo.

---

## 🧭 Navegação — Painel Dinâmico

A navegação entre telas é feita por **painel dinâmico**: o cabeçalho e o menu lateral permanecem fixos, e apenas a área central (`StackPane`) é substituída a cada navegação:

```java
conteudoPane.getChildren().setAll(pane);
```

O `MainController` é responsável por carregar o FXML correspondente e repassar o usuário logado ao controller carregado. A única exceção é o logout, que realiza uma troca completa de `Scene`.

---

## 🔒 Segurança

- **Proteção contra SQL Injection** — todas as consultas usam `PreparedStatement` com parâmetros (`?`), nunca concatenação de strings.
- **Validação de entrada com Regex** — e-mail e ISBN são validados por expressão regular antes de chegar ao banco.
- **Credenciais fora do código** — usuário e senha do PostgreSQL ficam em `database.properties`, listado no `.gitignore`. Um arquivo `database.properties.example` serve de modelo para quem for configurar o projeto.
- **Mensagens de login genéricas** — o sistema nunca informa se o e-mail existe ou se a senha está incorreta.

---

## 🪵 Rastreabilidade e Auditoria

Toda ação relevante do sistema é registrada em arquivo, atendendo aos requisitos de rastreabilidade e log de exceções:

| Arquivo | Conteúdo |
|---|---|
| `logs/uso.log` | Ações executadas com sucesso (login, empréstimo, navegação...) |
| `logs/excecoes.log` | Erros capturados, com ação, usuário e descrição |

Formato das linhas de log:
```
[dd/MM/yyyy HH:mm:ss] ACAO='...' USUARIO='...' (ERRO='...')
```

Esses logs podem ser consultados diretamente na aplicação pela tela de **Logs**, disponível apenas para administradores.

---

## ⚙️ Como executar o projeto

### Pré-requisitos
- JDK 21 ou superior
- Maven 3.9+
- PostgreSQL instalado e em execução

### 1. Clone o repositório
```bash
git clone https://github.com/gabryelmeireles/BibliotecaFX.git
cd BibliotecaFX
```

### 2. Configure o banco de dados
Crie um banco PostgreSQL chamado `biblioteca` e crie as tabelas necessárias (`usuario`, `livro`, `emprestimo`).

### 3. Configure as credenciais
Copie o arquivo de exemplo e edite com suas credenciais locais:
```bash
cp src/main/resources/database.properties.example src/main/resources/database.properties
```
```properties
db.url=jdbc:postgresql://localhost:5432/biblioteca
db.user=postgres
db.password=SUA_SENHA_AQUI
```

### 4. Execute a aplicação
```bash
mvn clean javafx:run
```

---

## 👤 Perfis de acesso

| Funcionalidade | Usuário comum | Administrador |
|---|---|---|
| Realizar empréstimo | ✅ | ✅ |
| Ver e devolver próprios empréstimos | ✅ | ✅ |
| Ver e devolver empréstimos de todos | ❌ | ✅ |
| Cadastrar/excluir livros | ❌ | ✅ |
| Cadastrar/excluir usuários | ❌ | ✅ |
| Visualizar logs do sistema | ❌ | ✅ |

---

## 👨‍💻 Autor

Desenvolvido por **Gabryel Meireles** — Bacharelado em Sistemas de Informação, IFG Campus Luziânia.
