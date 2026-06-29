package com.biblioteca.biblioteca.controller;

import com.biblioteca.biblioteca.model.entity.Usuario;
import com.biblioteca.biblioteca.util.LogUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Tela de visualização dos logs do sistema — acessível apenas pelo admin
public class LogController {

    // Componentes vinculados ao FXML
    @FXML private TableView<EntradaLog> tabelaLogs;
    @FXML private TableColumn<EntradaLog, String> colDataHora;
    @FXML private TableColumn<EntradaLog, String> colTipo;
    @FXML private TableColumn<EntradaLog, String> colAcao;
    @FXML private TableColumn<EntradaLog, String> colUsuario;
    @FXML private TableColumn<EntradaLog, String> colDetalhe;
    @FXML private Label labelTotal;

    private Usuario usuarioLogado;

    // Caminhos dos arquivos de log definidos no LogUtil
    private static final String LOG_ACOES    = LogUtil.LOG_ACOES;
    private static final String LOG_EXCECOES = LogUtil.LOG_EXCECOES;

    // Regex para extrair os campos de cada linha do arquivo de log
    // Formato esperado: [dd/MM/yyyy HH:mm:ss] ACAO='...' USUARIO='...' ERRO='...' (ERRO é opcional)
    private static final Pattern PADRAO_LOG = Pattern.compile(
            "\\[(.+?)] ACAO='(.+?)' USUARIO='(.+?)'(?: ERRO='(.+?)')?");
    private static final DateTimeFormatter FMT_LOG =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML
    public void initialize() {
        // Ajusta a largura da tabela para caber exatamente nas colunas definidas
        tabelaLogs.setMaxWidth(colDataHora.getPrefWidth() + colTipo.getPrefWidth() + colAcao.getPrefWidth() + colUsuario.getPrefWidth() + colDetalhe.getPrefWidth() + 18);

        // Colunas simples lidas via PropertyValueFactory, que busca o getter correspondente na EntradaLog
        colDataHora.setCellValueFactory(new PropertyValueFactory<>("dataHora"));
        colAcao.setCellValueFactory(new PropertyValueFactory<>("acao"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colDetalhe.setCellValueFactory(new PropertyValueFactory<>("detalhe"));

        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        // Coluna Tipo com cor diferente: vermelho para erros, verde para ações normais
        colTipo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String tipo, boolean empty) {
                super.updateItem(tipo, empty);
                if (empty || tipo == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(tipo);
                    setStyle("ERRO".equals(tipo)
                            ? "-fx-text-fill: #c0392b; -fx-font-weight: bold;"
                            : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            }
        });

        carregarLogs();
    }

    // Recebe o usuário da sessão e registra que ele acessou os logs
    public void setUsuarioLogado(Usuario u) {
        this.usuarioLogado = u;
        LogUtil.registrarAcao("Visualizou logs do sistema", u.getEmail());
    }

    // Acionado pelo botão "Atualizar" — relê os arquivos de log do disco
    @FXML
    private void handleAtualizar() {
        carregarLogs();
    }

    private void carregarLogs() {
        List<EntradaLog> entradas = new ArrayList<>();
        // Lê os dois arquivos de log e combina em uma única lista
        entradas.addAll(lerArquivo(LOG_ACOES, "AÇÃO"));
        entradas.addAll(lerArquivo(LOG_EXCECOES, "ERRO"));

        // Ordena do mais recente para o mais antigo
        entradas.sort(Comparator.comparing(EntradaLog::getTimestamp).reversed());

        ObservableList<EntradaLog> lista = FXCollections.observableArrayList(entradas);
        tabelaLogs.setItems(lista);
        labelTotal.setText("Total de registros: " + lista.size() +
                " (" + contarTipo(entradas, "AÇÃO") + " ações, " +
                contarTipo(entradas, "ERRO") + " erros)");
    }

    private long contarTipo(List<EntradaLog> lista, String tipo) {
        return lista.stream().filter(e -> tipo.equals(e.getTipo())).count();
    }

    // Lê um arquivo de log linha por linha e extrai os campos via regex
    private List<EntradaLog> lerArquivo(String caminho, String tipo) {
        List<EntradaLog> resultado = new ArrayList<>();
        java.nio.file.Path path = Paths.get(caminho).toAbsolutePath();
        try {
            if (!Files.exists(path)) return resultado; // arquivo ainda não foi criado
            List<String> linhas = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String linha : linhas) {
                if (linha.isBlank()) continue;
                Matcher m = PADRAO_LOG.matcher(linha);
                if (m.find()) {
                    String dataHoraStr = m.group(1);
                    String detalhe    = m.group(4) != null ? m.group(4) : "-"; // ERRO é opcional
                    LocalDateTime ts;
                    try {
                        ts = LocalDateTime.parse(dataHoraStr, FMT_LOG);
                    } catch (Exception ex) {
                        ts = LocalDateTime.MIN; // data inválida vai para o final da lista
                    }
                    resultado.add(new EntradaLog(dataHoraStr, tipo, m.group(2), m.group(3), detalhe, ts));
                }
            }
        } catch (Exception e) {
            System.err.println("[LogController] Erro ao ler log: " + e.getMessage());
        }
        return resultado;
    }

    // Classe interna que representa uma linha do log já parseada para exibição na tabela
    public static class EntradaLog {
        private final SimpleStringProperty dataHora;
        private final SimpleStringProperty tipo;
        private final SimpleStringProperty acao;
        private final SimpleStringProperty usuario;
        private final SimpleStringProperty detalhe;
        private final LocalDateTime timestamp; // usado apenas para ordenação, não exibido diretamente

        public EntradaLog(String dataHora, String tipo, String acao, String usuario, String detalhe, LocalDateTime timestamp) {
            this.dataHora  = new SimpleStringProperty(dataHora);
            this.tipo      = new SimpleStringProperty(tipo);
            this.acao      = new SimpleStringProperty(acao);
            this.usuario   = new SimpleStringProperty(usuario);
            this.detalhe   = new SimpleStringProperty(detalhe);
            this.timestamp = timestamp;
        }

        public String        getDataHora()  { return dataHora.get(); }
        public String        getTipo()      { return tipo.get(); }
        public String        getAcao()      { return acao.get(); }
        public String        getUsuario()   { return usuario.get(); }
        public String        getDetalhe()   { return detalhe.get(); }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
