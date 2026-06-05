package Main;

import Controller.ClienteController;
import Controller.CupomController;
import Controller.PedidoController;
import Controller.ProdutoController;
import DAO.ClienteDAO;
import DAO.CupomDAO;
import DAO.PedidoDAO;
import DAO.PedidoProdutoDAO;
import DAO.ProdutoDAO;
import Model.Cliente;
import Model.Cupom;
import Model.Pedido;
import Model.PedidoProduto;
import Model.Produto;
import Util.BackupService;
import Util.CompressionResult;
import View.HtmlView;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {

    private static final String DATA_PATH = "data";
    private static final String HUFFMAN_BACKUP_PATH = "backups/fase4_huffman.huff";
    private static final String LZW_BACKUP_PATH = "backups/fase4_lzw.lzw";

    public static void main(String[] args) throws Exception {
        ClienteDAO clienteDAO = new ClienteDAO(DATA_PATH + "/clientes.db");
        ProdutoDAO produtoDAO = new ProdutoDAO(DATA_PATH + "/produtos.db");
        CupomDAO cupomDAO = new CupomDAO(DATA_PATH + "/cupons.db");
        PedidoDAO pedidoDAO = new PedidoDAO(DATA_PATH + "/pedidos.db");
        PedidoProdutoDAO pedidoProdutoDAO = new PedidoProdutoDAO(DATA_PATH + "/pedido_produto.db");

        ClienteController clienteController = new ClienteController(clienteDAO);
        ProdutoController produtoController = new ProdutoController(produtoDAO, pedidoProdutoDAO);
        CupomController cupomController = new CupomController(cupomDAO);
        PedidoController pedidoController = new PedidoController(pedidoDAO, clienteDAO, produtoDAO, cupomDAO, pedidoProdutoDAO);

        HttpServer server = HttpServer.create(new InetSocketAddress(18080), 0);
        server.createContext("/", ex -> {
            if (!"GET".equals(ex.getRequestMethod())) {
                sendText(ex, 405, "Metodo nao permitido");
                return;
            }
            sendHtml(ex, HtmlView.page("Inicio", HtmlView.nav()
                + "<section class='hero'>"
                + "<span class='eyebrow'>Fase III - MVC + DAO</span>"
                + "<h1>Loja Online</h1>"
                + "<p>Sistema web para uma loja online com relacionamento N:N Pedido-Produto, persistencia binaria com lapide e recuperacao ordenada por Arvore B+.</p>"
                + "<div class='stats'>"
                + "<div class='stat'><strong>N:N</strong><span>itens persistidos na tabela associativa PedidoProduto.</span></div>"
                + "<div class='stat'><strong>MVC</strong><span>arquitetura separando interface, regras de negocio e persistencia.</span></div>"
                + "<div class='stat'><strong>B+</strong><span>catalogo recuperado em ordem diretamente pelo indice.</span></div>"
                + "</div>"
                + "</section>"
                + "<div class='grid'>"
                + "<div class='card'>"
                + "<h2>Fluxo do sistema</h2>"
                + "<p class='lede'>O administrador organiza o catalogo, mantem os clientes atualizados e acompanha os pedidos ativos. O cliente realiza compras com multiplos itens e pode receber desconto via cupom.</p>"
                + "</div>"
                + "<div class='card'>"
                + "<h2>Documentacao</h2>"
                + "<p class='lede'>Os artefatos pedidos pelo professor foram organizados na pasta <code>docs</code>, incluindo descricao do problema, DCU, DER e arquitetura proposta com diagrama.</p>"
                + "</div>"
                + "</div>"
                + "<p class='footer-note'>Abra cada modulo pelo menu para executar o CRUD completo e consultar os registros ativos.</p>"));
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
            sendHtml(ex, HtmlView.page("Clientes", HtmlView.nav() + msgBox(msg)
                + "<section class='hero'>"
                + "<span class='eyebrow'>Relacionamento</span>"
                + "<h1>Clientes da Loja Online</h1>"
                + "<p>Cadastre consumidores da plataforma, mantenha os dados atualizados e consulte os registros ativos para uso nos pedidos.</p>"
                + "</section>"
                + "<h2 class='section-title'>Gestao de clientes</h2>"
                + "<div class='grid'>"
                + "<div class='card'>"
                + "<h2>Cadastrar</h2>"
                + "<p class='lede'>Inclui um novo cliente na base binaria para uso posterior nos pedidos.</p>"
                + "<form method='post' action='/clientes/create'>"
                + "<label>Nome</label><input name='nome' required>"
                + "<label>Email</label><input name='email' required>"
                + "<label>Telefones (csv)</label><input name='telefones' placeholder='11999999999, 11888888888' required>"
                + "<button type='submit'>Salvar</button>"
                + "</form>"
                + "</div>"
                + "<div class='card'>"
                + "<h2>Atualizar</h2>"
                + "<p class='lede'>Atualize, exclua logicamente ou consulte um cliente especifico pelo identificador.</p>"
                + "<form method='post' action='/clientes/update'>"
                + "<label>ID</label><input name='id' required>"
                + "<label>Nome</label><input name='nome' required>"
                + "<label>Email</label><input name='email' required>"
                + "<label>Telefones (csv)</label><input name='telefones' placeholder='11999999999, 11888888888' required>"
                + "<button type='submit'>Atualizar</button>"
                + "</form>"
                + "<form method='post' action='/clientes/delete'>"
                + "<label>ID para excluir</label><input name='id' required>"
                + "<button type='submit'>Excluir (Lapide)</button>"
                + "</form>"
                + "<form method='post' action='/clientes/find'>"
                + "<label>ID para consultar</label><input name='id' required>"
                + "<button type='submit'>Consultar por ID</button>"
                + "</form>"
                + "</div>"
                + "</div>"
                + "<div class='card'>"
                + "<h2>Clientes ativos</h2>"
                + "<table><tr><th>ID</th><th>Nome</th><th>Email</th><th>Telefone</th></tr>"
                + rows + "</table></div>"));
        });

        server.createContext("/clientes/create", ex -> handlePost(ex, "/clientes", data -> {
            clienteController.cadastrar(data.get("nome"), data.get("email"), data.get("telefones"));
            return "Cliente cadastrado.";
        }));
        server.createContext("/clientes/update", ex -> handlePost(ex, "/clientes", data -> {
            int id = parseInt(data.get("id"), "ID do cliente");
            Cliente c = new Cliente(id, data.get("nome"), data.get("email"), parseCsvText(data.get("telefones"), "Telefones"));
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
            List<Produto> produtos = produtoController.listarOrdenadosPorId();
            StringBuilder rows = new StringBuilder();
            for (Produto p : produtos) {
                rows.append("<tr><td>").append(p.getId()).append("</td><td>")
                    .append(escape(p.getNome())).append("</td><td>")
                    .append(p.getPreco()).append("</td><td>")
                    .append(p.getEstoque()).append("</td></tr>");
            }
            sendHtml(ex, HtmlView.page("Produtos", HtmlView.nav() + msgBox(msg)
                + "<section class='hero'>"
                + "<span class='eyebrow'>Catalogo</span>"
                + "<h1>Produtos da Loja Online</h1>"
                + "<p>Gerencie os itens do ecommerce. A tabela abaixo e percorrida em ordem de ID pela Arvore B+ persistente, sem ordenacao em memoria.</p>"
                + "</section>"
                + "<h2 class='section-title'>Gestao de catalogo</h2>"
                + "<div class='grid'>"
                + "<div class='card'>"
                + "<h2>Cadastrar</h2>"
                + "<p class='lede'>Adicione novos produtos ao catalogo virtual e defina estoque inicial.</p>"
                + "<form method='post' action='/produtos/create'>"
                + "<label>Nome</label><input name='nome' required>"
                + "<label>Preco</label><input name='preco' required>"
                + "<label>Estoque</label><input name='estoque' required>"
                + "<button type='submit'>Salvar</button>"
                + "</form>"
                + "</div>"
                + "<div class='card'>"
                + "<h2>Atualizar</h2>"
                + "<p class='lede'>Use o ID para editar, excluir logicamente ou consultar um item especifico.</p>"
                + "<form method='post' action='/produtos/update'>"
                + "<label>ID</label><input name='id' required>"
                + "<label>Nome</label><input name='nome' required>"
                + "<label>Preco</label><input name='preco' required>"
                + "<label>Estoque</label><input name='estoque' required>"
                + "<button type='submit'>Atualizar</button>"
                + "</form>"
                + "<form method='post' action='/produtos/delete'>"
                + "<label>ID para excluir</label><input name='id' required>"
                + "<button type='submit'>Excluir (Lapide)</button>"
                + "</form>"
                + "<form method='post' action='/produtos/find'>"
                + "<label>ID para consultar</label><input name='id' required>"
                + "<button type='submit'>Consultar por ID</button>"
                + "</form>"
                + "</div>"
                + "</div>"
                + "<div class='card'>"
                + "<h2>Produtos ativos em ordem (B+)</h2>"
                + "<table><tr><th>ID</th><th>Nome</th><th>Preco</th><th>Estoque</th></tr>"
                + rows + "</table></div>"));
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
            sendHtml(ex, HtmlView.page("Cupons", HtmlView.nav() + msgBox(msg)
                + "<section class='hero'>"
                + "<span class='eyebrow'>Promocoes</span>"
                + "<h1>Cupons da Loja Online</h1>"
                + "<p>Controle campanhas promocionais para aplicar descontos nos pedidos ja registrados no sistema.</p>"
                + "</section>"
                + "<h2 class='section-title'>Gestao de cupons</h2>"
                + "<div class='grid'>"
                + "<div class='card'>"
                + "<h2>Cadastrar</h2>"
                + "<p class='lede'>Cadastre codigos promocionais e determine se estao liberados para uso.</p>"
                + "<form method='post' action='/cupons/create'>"
                + "<label>Codigo</label><input name='codigo' required>"
                + "<label>Percentual de desconto</label><input name='percentualDesconto' required>"
                + "<label>Ativo (true/false)</label><input name='ativo' required>"
                + "<button type='submit'>Salvar</button>"
                + "</form>"
                + "</div>"
                + "<div class='card'>"
                + "<h2>Atualizar</h2>"
                + "<p class='lede'>Edite o desconto, altere o status ou consulte um cupom por ID.</p>"
                + "<form method='post' action='/cupons/update'>"
                + "<label>ID</label><input name='id' required>"
                + "<label>Codigo</label><input name='codigo' required>"
                + "<label>Percentual de desconto</label><input name='percentualDesconto' required>"
                + "<label>Ativo (true/false)</label><input name='ativo' required>"
                + "<button type='submit'>Atualizar</button>"
                + "</form>"
                + "<form method='post' action='/cupons/delete'>"
                + "<label>ID para excluir</label><input name='id' required>"
                + "<button type='submit'>Excluir (Lapide)</button>"
                + "</form>"
                + "<form method='post' action='/cupons/find'>"
                + "<label>ID para consultar</label><input name='id' required>"
                + "<button type='submit'>Consultar por ID</button>"
                + "</form>"
                + "</div>"
                + "</div>"
                + "<div class='card'>"
                + "<h2>Cupons ativos</h2>"
                + "<table><tr><th>ID</th><th>Codigo</th><th>Desconto %</th><th>Ativo</th></tr>"
                + rows + "</table></div>"));
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
            String pedidosCliente = query(ex).getOrDefault("pedidosCliente", "");
            String itensPedido = query(ex).getOrDefault("itensPedido", "");
            String pedidosProduto = query(ex).getOrDefault("pedidosProduto", "");
            List<Pedido> pedidos = pedidoController.listarAtivos();
            StringBuilder rows = new StringBuilder();
            for (Pedido p : pedidos) {
                rows.append("<tr><td>").append(p.getId()).append("</td><td>")
                    .append(p.getIdCliente()).append("</td><td>")
                    .append(p.getIdCupom()).append("</td><td>")
                    .append(escape(p.getDataPedido())).append("</td><td>")
                    .append(p.getValorTotal()).append("</td><td>")
                    .append(itensToString(pedidoController.listarItensDoPedido(p.getId()))).append("</td></tr>");
            }

            sendHtml(ex, HtmlView.page("Pedidos", HtmlView.nav() + msgBox(msg)
                + "<section class='hero'>"
                + "<span class='eyebrow'>Operacao</span>"
                + "<h1>Pedidos da Loja Online</h1>"
                + "<p>Monte compras com multiplos produtos. Cada item e armazenado na tabela associativa PedidoProduto, cuja chave primaria e (idPedido, idProduto).</p>"
                + "</section>"
                + "<h2 class='section-title'>Gestao de pedidos</h2>"
                + "<div class='grid'>"
                + "<div class='card'>"
                + "<h2>Criar pedido</h2>"
                + "<p class='lede'>Informe o cliente e os itens do carrinho usando listas CSV de produtos e quantidades.</p>"
                + "<form method='post' action='/pedidos/create'>"
                + "<label>ID Cliente</label><input name='idCliente' required>"
                + "<label>IDs dos produtos (csv)</label><input name='idsProdutos' placeholder='1,2,3' required>"
                + "<label>Quantidades (csv)</label><input name='quantidades' placeholder='2,1,4' required>"
                + "<button type='submit'>Criar</button>"
                + "</form>"
                + "</div>"
                + "<div class='card'>"
                + "<h2>Atualizar pedido</h2>"
                + "<p class='lede'>Edite cliente, itens e cupom de um pedido existente com recalcule de estoque e total.</p>"
                + "<form method='post' action='/pedidos/update'>"
                + "<label>ID Pedido</label><input name='id' required>"
                + "<label>ID Cliente</label><input name='idCliente' required>"
                + "<label>IDs dos produtos (csv)</label><input name='idsProdutos' placeholder='1,2,3' required>"
                + "<label>Quantidades (csv)</label><input name='quantidades' placeholder='2,1,4' required>"
                + "<label>ID Cupom (-1 para nenhum)</label><input name='idCupom' value='-1' required>"
                + "<button type='submit'>Atualizar</button>"
                + "</form>"
                + "</div>"
                + "<div class='card'>"
                + "<h2>Associar cupom</h2>"
                + "<p class='lede'>Vincule um cupom ativo a um pedido existente ou realize operacoes de consulta e exclusao logica.</p>"
                + "<form method='post' action='/pedidos/associar-cupom'>"
                + "<label>ID Pedido</label><input name='idPedido' required>"
                + "<label>ID Cupom</label><input name='idCupom' required>"
                + "<button type='submit'>Associar</button>"
                + "</form>"
                + "<form method='post' action='/pedidos/delete'>"
                + "<label>ID para excluir</label><input name='id' required>"
                + "<button type='submit'>Excluir (Lapide)</button>"
                + "</form>"
                + "<form method='post' action='/pedidos/find'>"
                + "<label>ID para consultar</label><input name='id' required>"
                + "<button type='submit'>Consultar por ID</button>"
                + "</form>"
                + "</div>"
                + "<div class='card'>"
                + "<h2>Pedidos por cliente</h2>"
                + "<p class='lede'>Navegue no relacionamento 1:N usando o hash extensivel do cliente para encontrar os pedidos vinculados.</p>"
                + "<form method='post' action='/pedidos/by-cliente'>"
                + "<label>ID Cliente</label><input name='idCliente' required>"
                + "<button type='submit'>Listar pedidos do cliente</button>"
                + "</form>"
                + (!pedidosCliente.isEmpty() ? "<div class='result-block'><strong>Relacionamento 1:N:</strong><br>" + escape(pedidosCliente) + "</div>" : "")
                + "</div>"
                + "<div class='card'>"
                + "<h2>Produtos de um pedido</h2>"
                + "<p class='lede'>Navegue de Pedido para Produto atraves da chave composta da tabela associativa.</p>"
                + "<form method='post' action='/pedidos/itens'>"
                + "<label>ID Pedido</label><input name='idPedido' required>"
                + "<button type='submit'>Listar produtos do pedido</button>"
                + "</form>"
                + (!itensPedido.isEmpty() ? "<div class='result-block'><strong>Pedido -> Produtos:</strong><br>" + escape(itensPedido) + "</div>" : "")
                + "</div>"
                + "<div class='card'>"
                + "<h2>Pedidos de um produto</h2>"
                + "<p class='lede'>Navegue de Produto para Pedido usando o indice secundario B+ da associacao N:N.</p>"
                + "<form method='post' action='/pedidos/by-produto'>"
                + "<label>ID Produto</label><input name='idProduto' required>"
                + "<button type='submit'>Listar pedidos do produto</button>"
                + "</form>"
                + (!pedidosProduto.isEmpty() ? "<div class='result-block'><strong>Produto -> Pedidos:</strong><br>" + escape(pedidosProduto) + "</div>" : "")
                + "</div>"
                + "</div>"
                + "<div class='card'>"
                + "<h2>Pedidos ativos</h2>"
                + "<table><tr><th>ID</th><th>Cliente</th><th>Cupom</th><th>Data</th><th>Total</th><th>Itens</th></tr>"
                + rows + "</table></div>"));
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

        server.createContext("/pedidos/update", ex -> handlePost(ex, "/pedidos", data -> {
            pedidoController.atualizarPedido(
                parseInt(data.get("id"), "ID do pedido"),
                parseInt(data.get("idCliente"), "ID do cliente"),
                parseCsvInt(data.get("idsProdutos"), "IDs dos produtos"),
                parseCsvInt(data.get("quantidades"), "Quantidades"),
                parseInt(data.get("idCupom"), "ID do cupom")
            );
            return "Pedido atualizado.";
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
            return p.toString() + " | Itens N:N: " + itensToString(pedidoController.listarItensDoPedido(p.getId()));
        }));
        server.createContext("/pedidos/by-cliente", ex -> handlePost(ex, "/pedidos", data -> {
            int idCliente = parseInt(data.get("idCliente"), "ID do cliente");
            List<Pedido> pedidosDoCliente = pedidoController.listarPorCliente(idCliente);
            String descricao = pedidosDoCliente.isEmpty()
                ? "Nenhum pedido ativo encontrado para o cliente " + idCliente + "."
                : pedidosDoCliente.stream().map(Pedido::toString).reduce((a, b) -> a + " | " + b).orElse("");
            return "__REDIRECT__pedidosCliente=" + encode(descricao);
        }));
        server.createContext("/pedidos/itens", ex -> handlePost(ex, "/pedidos", data -> {
            int idPedido = parseInt(data.get("idPedido"), "ID do pedido");
            Pedido pedido = pedidoController.consultarPorId(idPedido);
            if (pedido == null) {
                throw new IllegalArgumentException("Pedido nao encontrado.");
            }
            List<PedidoProduto> itens = pedidoController.listarItensDoPedido(idPedido);
            String descricao = itens.isEmpty()
                ? "Nenhum produto associado ao pedido " + idPedido + "."
                : itensToString(itens);
            return "__REDIRECT__itensPedido=" + encode(descricao);
        }));
        server.createContext("/pedidos/by-produto", ex -> handlePost(ex, "/pedidos", data -> {
            int idProduto = parseInt(data.get("idProduto"), "ID do produto");
            List<Pedido> pedidosDoProduto = pedidoController.listarPorProduto(idProduto);
            String descricao = pedidosDoProduto.isEmpty()
                ? "Nenhum pedido ativo contem o produto " + idProduto + "."
                : pedidosDoProduto.stream().map(p -> "Pedido " + p.getId())
                    .reduce((a, b) -> a + ", " + b).orElse("");
            return "__REDIRECT__pedidosProduto=" + encode(descricao);
        }));

        server.createContext("/compressao", ex -> {
            if (!"GET".equals(ex.getRequestMethod())) {
                sendText(ex, 405, "Metodo nao permitido");
                return;
            }
            String msg = query(ex).getOrDefault("msg", "");
            String result = query(ex).getOrDefault("result", "");
            long rawSize = BackupService.rawDataSize(DATA_PATH);
            int fileCount = BackupService.dataFileCount(DATA_PATH);
            sendHtml(ex, HtmlView.page("Compressao", HtmlView.nav() + msgBox(msg)
                + "<section class='hero'>"
                + "<span class='eyebrow'>Fase IV</span>"
                + "<h1>Backup compactado dos dados</h1>"
                + "<p>Gere um unico arquivo compactado contendo os arquivos binarios usados pelo aplicativo, incluindo bases principais e indices persistentes.</p>"
                + "<div class='stats'>"
                + "<div class='stat'><strong>" + fileCount + "</strong><span>arquivos .db incluidos no backup completo.</span></div>"
                + "<div class='stat'><strong>" + formatBytes(rawSize) + "</strong><span>soma dos arquivos de dados antes do empacotamento.</span></div>"
                + "</div>"
                + "</section>"
                + "<div class='grid'>"
                + "<div class='card'>"
                + "<h2>Huffman</h2>"
                + "<p class='lede'>Cria o pacote unico dos arquivos de dados e aplica codificacao de Huffman sobre o pacote completo.</p>"
                + "<form method='post' action='/compressao/huffman'>"
                + "<button type='submit'>Gerar backup Huffman</button>"
                + "</form>"
                + "</div>"
                + "<div class='card'>"
                + "<h2>LZW</h2>"
                + "<p class='lede'>Cria o mesmo pacote unico dos arquivos de dados e aplica LZW sobre o pacote completo.</p>"
                + "<form method='post' action='/compressao/lzw'>"
                + "<button type='submit'>Gerar backup LZW</button>"
                + "</form>"
                + "</div>"
                + "</div>"
                + (!result.isEmpty() ? "<div class='card'><h2>Resultado</h2>" + result + "</div>" : "")));
        });

        server.createContext("/compressao/huffman", ex -> handlePost(ex, "/compressao", data -> {
            CompressionResult result = BackupService.createHuffmanBackup(DATA_PATH, HUFFMAN_BACKUP_PATH);
            return "__REDIRECT__result=" + encode(formatCompressionResult(result));
        }));

        server.createContext("/compressao/lzw", ex -> handlePost(ex, "/compressao", data -> {
            CompressionResult result = BackupService.createLzwBackup(DATA_PATH, LZW_BACKUP_PATH);
            return "__REDIRECT__result=" + encode(formatCompressionResult(result));
        }));

        server.start();
        System.out.println("Server rodando em: http://localhost:18080");
    }

    private static String itensToString(List<PedidoProduto> itens) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < itens.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            PedidoProduto item = itens.get(i);
            sb.append(item.getIdProduto()).append("x").append(item.getQuantidade());
        }
        return sb.length() == 0 ? "Sem itens" : sb.toString();
    }

    private static void handlePost(HttpExchange ex, String redirectPath, PostAction action) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            sendText(ex, 405, "Metodo nao permitido");
            return;
        }

        try {
            Map<String, String> data = body(ex);
            String msg = action.run(data);
            if (msg.startsWith("__REDIRECT__")) {
                redirect(ex, redirectPath + "?" + msg.substring("__REDIRECT__".length()));
                return;
            }
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

    private static String[] parseCsvText(String csv, String field) {
        if (csv == null || csv.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " obrigatorio.");
        }
        return java.util.Arrays.stream(csv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);
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

    private static String formatCompressionResult(CompressionResult result) {
        return "<table>"
            + "<tr><th>Algoritmo</th><td>" + escape(result.getAlgorithm()) + "</td></tr>"
            + "<tr><th>Arquivo gerado</th><td>" + escape(result.getOutputPath()) + "</td></tr>"
            + "<tr><th>Tamanho original</th><td>" + result.getOriginalSize() + " bytes (" + formatBytes(result.getOriginalSize()) + ")</td></tr>"
            + "<tr><th>Tamanho comprimido</th><td>" + result.getCompressedSize() + " bytes (" + formatBytes(result.getCompressedSize()) + ")</td></tr>"
            + "<tr><th>Calculo da taxa</th><td>1 - (" + result.getCompressedSize() + " / " + result.getOriginalSize() + ") = "
            + String.format("%.2f%%", result.getCompressionRate()) + "</td></tr>"
            + "<tr><th>Interpretacao</th><td>" + escape(result.getInterpretation()) + "</td></tr>"
            + "<tr><th>Integridade</th><td>" + (result.isVerified() ? "Backup descompactado e conferido byte a byte." : "Falha na verificacao.") + "</td></tr>"
            + "</table>";
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        return String.format("%.2f KB", bytes / 1024.0);
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
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[1024];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            String raw = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
            return parseForm(raw);
        }
    }

    private static Map<String, String> parseForm(String form) {
        Map<String, String> out = new HashMap<>();
        if (form == null || form.trim().isEmpty()) {
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
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao codificar URL.", e);
        }
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao decodificar URL.", e);
        }
    }

    private interface PostAction {
        String run(Map<String, String> data) throws Exception;
    }
}
