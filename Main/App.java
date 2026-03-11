package Main;

import Controller.ClienteController;
import Controller.CupomController;
import Controller.PedidoController;
import Controller.ProdutoController;
import DAO.ClienteDAO;
import DAO.CupomDAO;
import DAO.PedidoDAO;
import DAO.ProdutoDAO;
import Model.Cliente;
import Model.Cupom;
import Model.Pedido;
import Model.Produto;
import View.HtmlView;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {

    public static void main(String[] args) throws Exception {
        ClienteDAO clienteDAO = new ClienteDAO("data/clientes.db");
        ProdutoDAO produtoDAO = new ProdutoDAO("data/produtos.db");
        CupomDAO cupomDAO = new CupomDAO("data/cupons.db");
        PedidoDAO pedidoDAO = new PedidoDAO("data/pedidos.db");

        ClienteController clienteController = new ClienteController(clienteDAO);
        ProdutoController produtoController = new ProdutoController(produtoDAO);
        CupomController cupomController = new CupomController(cupomDAO);
        PedidoController pedidoController = new PedidoController(pedidoDAO, clienteDAO, produtoDAO, cupomDAO);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", ex -> {
            if (!"GET".equals(ex.getRequestMethod())) {
                sendText(ex, 405, "Metodo nao permitido");
                return;
            }
            sendHtml(ex, HtmlView.page("Inicio", HtmlView.nav() + """
                <div class='card'>
                    <h1>Sistema de Pedidos</h1>
                    <p>CRUD de Cliente, Produto, Pedido e Cupom com persistencia binaria, cabecalho e lapide.</p>
                </div>
                """));
        });

        server.createContext("/styles.css", ex -> {
            ex.getResponseHeaders().add("Content-Type", "text/css; charset=utf-8");
            byte[] data = HtmlView.css().getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, data.length);
            ex.getResponseBody().write(data);
            ex.close();
        });

        server.createContext("/clientes", ex -> {
            if (!"GET".equals(ex.getRequestMethod())) {
                sendText(ex, 405, "Metodo nao permitido");
                return;
            }
            String msg = query(ex).getOrDefault("msg", "");
            List<Cliente> clientes = clienteController.listarAtivos();
            StringBuilder rows = new StringBuilder();
            for (Cliente c : clientes) {
                rows.append("<tr><td>").append(c.getId()).append("</td><td>")
                    .append(escape(c.getNome())).append("</td><td>")
                    .append(escape(c.getEmail())).append("</td><td>")
                    .append(escape(c.getTelefone())).append("</td></tr>");
            }
            sendHtml(ex, HtmlView.page("Clientes", HtmlView.nav() + msgBox(msg) + """
                <h1>Clientes</h1>
                <div class='grid'>
                  <div class='card'>
                    <h2>Cadastrar</h2>
                    <form method='post' action='/clientes/create'>
                      <label>Nome</label><input name='nome' required>
                      <label>Email</label><input name='email' required>
                      <label>Telefone</label><input name='telefone' required>
                      <button type='submit'>Salvar</button>
                    </form>
                  </div>
                  <div class='card'>
                    <h2>Atualizar</h2>
                    <form method='post' action='/clientes/update'>
                      <label>ID</label><input name='id' required>
                      <label>Nome</label><input name='nome' required>
                      <label>Email</label><input name='email' required>
                      <label>Telefone</label><input name='telefone' required>
                      <button type='submit'>Atualizar</button>
                    </form>
                    <form method='post' action='/clientes/delete'>
                      <label>ID para excluir</label><input name='id' required>
                      <button type='submit'>Excluir (Lapide)</button>
                    </form>
                    <form method='post' action='/clientes/find'>
                      <label>ID para consultar</label><input name='id' required>
                      <button type='submit'>Consultar por ID</button>
                    </form>
                  </div>
                </div>
                <div class='card'>
                  <h2>Ativos</h2>
                  <table><tr><th>ID</th><th>Nome</th><th>Email</th><th>Telefone</th></tr>
                """ + rows + "</table></div>"));
        });

        server.createContext("/clientes/create", ex -> handlePost(ex, "/clientes", data -> {
            clienteController.cadastrar(data.get("nome"), data.get("email"), data.get("telefone"));
            return "Cliente cadastrado.";
        }));
        server.createContext("/clientes/update", ex -> handlePost(ex, "/clientes", data -> {
            int id = parseInt(data.get("id"), "ID do cliente");
            Cliente c = new Cliente(id, data.get("nome"), data.get("email"), data.get("telefone"));
            if (!clienteController.atualizar(c)) {
                throw new IllegalArgumentException("Cliente nao encontrado.");
            }
            return "Cliente atualizado.";
        }));
        server.createContext("/clientes/delete", ex -> handlePost(ex, "/clientes", data -> {
            int id = parseInt(data.get("id"), "ID do cliente");
            if (!clienteController.excluir(id)) {
                throw new IllegalArgumentException("Cliente nao encontrado.");
            }
            return "Cliente excluido logicamente.";
        }));
        server.createContext("/clientes/find", ex -> handlePost(ex, "/clientes", data -> {
            int id = parseInt(data.get("id"), "ID do cliente");
            Cliente c = clienteController.consultarPorId(id);
            if (c == null) {
                throw new IllegalArgumentException("Cliente nao encontrado.");
            }
            return c.toString();
        }));

        server.createContext("/produtos", ex -> {
            if (!"GET".equals(ex.getRequestMethod())) {
                sendText(ex, 405, "Metodo nao permitido");
                return;
            }
            String msg = query(ex).getOrDefault("msg", "");
            List<Produto> produtos = produtoController.listarAtivos();
            StringBuilder rows = new StringBuilder();
            for (Produto p : produtos) {
                rows.append("<tr><td>").append(p.getId()).append("</td><td>")
                    .append(escape(p.getNome())).append("</td><td>")
                    .append(p.getPreco()).append("</td><td>")
                    .append(p.getEstoque()).append("</td></tr>");
            }
            sendHtml(ex, HtmlView.page("Produtos", HtmlView.nav() + msgBox(msg) + """
                <h1>Produtos</h1>
                <div class='grid'>
                  <div class='card'>
                    <h2>Cadastrar</h2>
                    <form method='post' action='/produtos/create'>
                      <label>Nome</label><input name='nome' required>
                      <label>Preco</label><input name='preco' required>
                      <label>Estoque</label><input name='estoque' required>
                      <button type='submit'>Salvar</button>
                    </form>
                  </div>
                  <div class='card'>
                    <h2>Atualizar</h2>
                    <form method='post' action='/produtos/update'>
                      <label>ID</label><input name='id' required>
                      <label>Nome</label><input name='nome' required>
                      <label>Preco</label><input name='preco' required>
                      <label>Estoque</label><input name='estoque' required>
                      <button type='submit'>Atualizar</button>
                    </form>
                    <form method='post' action='/produtos/delete'>
                      <label>ID para excluir</label><input name='id' required>
                      <button type='submit'>Excluir (Lapide)</button>
                    </form>
                    <form method='post' action='/produtos/find'>
                      <label>ID para consultar</label><input name='id' required>
                      <button type='submit'>Consultar por ID</button>
                    </form>
                  </div>
                </div>
                <div class='card'>
                  <h2>Ativos</h2>
                  <table><tr><th>ID</th><th>Nome</th><th>Preco</th><th>Estoque</th></tr>
                """ + rows + "</table></div>"));
        });

        server.createContext("/produtos/create", ex -> handlePost(ex, "/produtos", data -> {
            produtoController.cadastrar(
                data.get("nome"),
                parseDouble(data.get("preco"), "Preco"),
                parseInt(data.get("estoque"), "Estoque")
            );
            return "Produto cadastrado.";
        }));
        server.createContext("/produtos/update", ex -> handlePost(ex, "/produtos", data -> {
            Produto p = new Produto(
                parseInt(data.get("id"), "ID do produto"),
                data.get("nome"),
                parseDouble(data.get("preco"), "Preco"),
                parseInt(data.get("estoque"), "Estoque")
            );
            if (!produtoController.atualizar(p)) {
                throw new IllegalArgumentException("Produto nao encontrado.");
            }
            return "Produto atualizado.";
        }));
        server.createContext("/produtos/delete", ex -> handlePost(ex, "/produtos", data -> {
            if (!produtoController.excluir(parseInt(data.get("id"), "ID do produto"))) {
                throw new IllegalArgumentException("Produto nao encontrado.");
            }
            return "Produto excluido logicamente.";
        }));
        server.createContext("/produtos/find", ex -> handlePost(ex, "/produtos", data -> {
            Produto p = produtoController.consultarPorId(parseInt(data.get("id"), "ID do produto"));
            if (p == null) {
                throw new IllegalArgumentException("Produto nao encontrado.");
            }
            return p.toString();
        }));

        server.createContext("/cupons", ex -> {
            if (!"GET".equals(ex.getRequestMethod())) {
                sendText(ex, 405, "Metodo nao permitido");
                return;
            }
            String msg = query(ex).getOrDefault("msg", "");
            List<Cupom> cupons = cupomController.listarAtivos();
            StringBuilder rows = new StringBuilder();
            for (Cupom c : cupons) {
                rows.append("<tr><td>").append(c.getId()).append("</td><td>")
                    .append(escape(c.getCodigo())).append("</td><td>")
                    .append(c.getPercentualDesconto()).append("</td><td>")
                    .append(c.getAtivo()).append("</td></tr>");
            }
            sendHtml(ex, HtmlView.page("Cupons", HtmlView.nav() + msgBox(msg) + """
                <h1>Cupons</h1>
                <div class='grid'>
                  <div class='card'>
                    <h2>Cadastrar</h2>
                    <form method='post' action='/cupons/create'>
                      <label>Codigo</label><input name='codigo' required>
                      <label>Percentual de desconto</label><input name='percentualDesconto' required>
                      <label>Ativo (true/false)</label><input name='ativo' required>
                      <button type='submit'>Salvar</button>
                    </form>
                  </div>
                  <div class='card'>
                    <h2>Atualizar</h2>
                    <form method='post' action='/cupons/update'>
                      <label>ID</label><input name='id' required>
                      <label>Codigo</label><input name='codigo' required>
                      <label>Percentual de desconto</label><input name='percentualDesconto' required>
                      <label>Ativo (true/false)</label><input name='ativo' required>
                      <button type='submit'>Atualizar</button>
                    </form>
                    <form method='post' action='/cupons/delete'>
                      <label>ID para excluir</label><input name='id' required>
                      <button type='submit'>Excluir (Lapide)</button>
                    </form>
                    <form method='post' action='/cupons/find'>
                      <label>ID para consultar</label><input name='id' required>
                      <button type='submit'>Consultar por ID</button>
                    </form>
                  </div>
                </div>
                <div class='card'>
                  <h2>Ativos</h2>
                  <table><tr><th>ID</th><th>Codigo</th><th>Desconto %</th><th>Ativo</th></tr>
                """ + rows + "</table></div>"));
        });

        server.createContext("/cupons/create", ex -> handlePost(ex, "/cupons", data -> {
            cupomController.cadastrar(
                data.get("codigo"),
                parseDouble(data.get("percentualDesconto"), "Percentual"),
                Boolean.parseBoolean(data.get("ativo"))
            );
            return "Cupom cadastrado.";
        }));
        server.createContext("/cupons/update", ex -> handlePost(ex, "/cupons", data -> {
            Cupom cupom = new Cupom(
                parseInt(data.get("id"), "ID do cupom"),
                data.get("codigo"),
                parseDouble(data.get("percentualDesconto"), "Percentual"),
                Boolean.parseBoolean(data.get("ativo"))
            );
            if (!cupomController.atualizar(cupom)) {
                throw new IllegalArgumentException("Cupom nao encontrado.");
            }
            return "Cupom atualizado.";
        }));
        server.createContext("/cupons/delete", ex -> handlePost(ex, "/cupons", data -> {
            if (!cupomController.excluir(parseInt(data.get("id"), "ID do cupom"))) {
                throw new IllegalArgumentException("Cupom nao encontrado.");
            }
            return "Cupom excluido logicamente.";
        }));
        server.createContext("/cupons/find", ex -> handlePost(ex, "/cupons", data -> {
            Cupom cupom = cupomController.consultarPorId(parseInt(data.get("id"), "ID do cupom"));
            if (cupom == null) {
                throw new IllegalArgumentException("Cupom nao encontrado.");
            }
            return cupom.toString();
        }));

        server.createContext("/pedidos", ex -> {
            if (!"GET".equals(ex.getRequestMethod())) {
                sendText(ex, 405, "Metodo nao permitido");
                return;
            }
            String msg = query(ex).getOrDefault("msg", "");
            List<Pedido> pedidos = pedidoController.listarAtivos();
            StringBuilder rows = new StringBuilder();
            for (Pedido p : pedidos) {
                rows.append("<tr><td>").append(p.getId()).append("</td><td>")
                    .append(p.getIdCliente()).append("</td><td>")
                    .append(p.getIdCupom()).append("</td><td>")
                    .append(p.getValorTotal()).append("</td><td>")
                    .append(itensToString(p)).append("</td></tr>");
            }

            sendHtml(ex, HtmlView.page("Pedidos", HtmlView.nav() + msgBox(msg) + """
                <h1>Pedidos</h1>
                <div class='grid'>
                  <div class='card'>
                    <h2>Criar pedido</h2>
                    <form method='post' action='/pedidos/create'>
                      <label>ID Cliente</label><input name='idCliente' required>
                      <label>IDs dos produtos (csv)</label><input name='idsProdutos' placeholder='1,2,3' required>
                      <label>Quantidades (csv)</label><input name='quantidades' placeholder='2,1,4' required>
                      <button type='submit'>Criar</button>
                    </form>
                  </div>
                  <div class='card'>
                    <h2>Associar cupom</h2>
                    <form method='post' action='/pedidos/associar-cupom'>
                      <label>ID Pedido</label><input name='idPedido' required>
                      <label>ID Cupom</label><input name='idCupom' required>
                      <button type='submit'>Associar</button>
                    </form>
                    <form method='post' action='/pedidos/delete'>
                      <label>ID para excluir</label><input name='id' required>
                      <button type='submit'>Excluir (Lapide)</button>
                    </form>
                    <form method='post' action='/pedidos/find'>
                      <label>ID para consultar</label><input name='id' required>
                      <button type='submit'>Consultar por ID</button>
                    </form>
                  </div>
                </div>
                <div class='card'>
                  <h2>Ativos</h2>
                  <table><tr><th>ID</th><th>Cliente</th><th>Cupom</th><th>Total</th><th>Itens</th></tr>
                """ + rows + "</table></div>"));
        });

        server.createContext("/pedidos/create", ex -> handlePost(ex, "/pedidos", data -> {
            pedidoController.criarPedido(
                parseInt(data.get("idCliente"), "ID do cliente"),
                parseCsvInt(data.get("idsProdutos"), "IDs dos produtos"),
                parseCsvInt(data.get("quantidades"), "Quantidades")
            );
            return "Pedido criado.";
        }));

        server.createContext("/pedidos/associar-cupom", ex -> handlePost(ex, "/pedidos", data -> {
            pedidoController.associarCupom(
                parseInt(data.get("idPedido"), "ID do pedido"),
                parseInt(data.get("idCupom"), "ID do cupom")
            );
            return "Cupom associado ao pedido.";
        }));

        server.createContext("/pedidos/delete", ex -> handlePost(ex, "/pedidos", data -> {
            if (!pedidoController.excluir(parseInt(data.get("id"), "ID do pedido"))) {
                throw new IllegalArgumentException("Pedido nao encontrado.");
            }
            return "Pedido excluido logicamente.";
        }));
        server.createContext("/pedidos/find", ex -> handlePost(ex, "/pedidos", data -> {
            Pedido p = pedidoController.consultarPorId(parseInt(data.get("id"), "ID do pedido"));
            if (p == null) {
                throw new IllegalArgumentException("Pedido nao encontrado.");
            }
            return p.toString();
        }));

        server.start();
        System.out.println("Server rodando em: http://localhost:8080");
    }

    private static String itensToString(Pedido p) {
        int[] ids = p.getIdsProdutos();
        int[] qtd = p.getQuantidades();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(ids[i]).append("x").append(qtd[i]);
        }
        return sb.toString();
    }

    private static void handlePost(HttpExchange ex, String redirectPath, PostAction action) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            sendText(ex, 405, "Metodo nao permitido");
            return;
        }

        try {
            Map<String, String> data = body(ex);
            String msg = action.run(data);
            redirect(ex, redirectPath + "?msg=" + encode(msg));
        } catch (Exception e) {
            redirect(ex, redirectPath + "?msg=" + encode("Erro: " + e.getMessage()));
        }
    }

    private static int[] parseCsvInt(String csv, String field) {
        if (csv == null || csv.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " obrigatorio.");
        }
        String[] parts = csv.split(",");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            out[i] = parseInt(parts[i].trim(), field);
        }
        return out;
    }

    private static int parseInt(String value, String field) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException(field + " invalido.");
        }
    }

    private static double parseDouble(String value, String field) {
        try {
            return Double.parseDouble(value.trim().replace(",", "."));
        } catch (Exception e) {
            throw new IllegalArgumentException(field + " invalido.");
        }
    }

    private static String msgBox(String msg) {
        return HtmlView.msgBox(msg, escape(msg));
    }

    private static void sendHtml(HttpExchange ex, String html) throws IOException {
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(200, body.length);
        ex.getResponseBody().write(body);
        ex.close();
    }

    private static void sendText(HttpExchange ex, int status, String text) throws IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(status, body.length);
        ex.getResponseBody().write(body);
        ex.close();
    }

    private static Map<String, String> query(HttpExchange ex) {
        String raw = ex.getRequestURI().getRawQuery();
        return parseForm(raw == null ? "" : raw);
    }

    private static Map<String, String> body(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            String raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return parseForm(raw);
        }
    }

    private static Map<String, String> parseForm(String form) {
        Map<String, String> out = new HashMap<>();
        if (form == null || form.isBlank()) {
            return out;
        }
        String[] pairs = form.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = decode(kv[0]);
            String value = kv.length > 1 ? decode(kv[1]) : "";
            out.put(key, value);
        }
        return out;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }

    private static void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().add("Location", location);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private interface PostAction {
        String run(Map<String, String> data) throws Exception;
    }
}
