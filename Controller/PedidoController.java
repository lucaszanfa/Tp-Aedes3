package Controller;

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
import java.util.ArrayList;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PedidoController {

    private final PedidoDAO pedidoDAO;
    private final ClienteDAO clienteDAO;
    private final ProdutoDAO produtoDAO;
    private final CupomDAO cupomDAO;
    private final PedidoProdutoDAO pedidoProdutoDAO;

    public PedidoController(PedidoDAO pedidoDAO, ClienteDAO clienteDAO, ProdutoDAO produtoDAO, CupomDAO cupomDAO,
                            PedidoProdutoDAO pedidoProdutoDAO) throws IOException {
        this.pedidoDAO = pedidoDAO;
        this.clienteDAO = clienteDAO;
        this.produtoDAO = produtoDAO;
        this.cupomDAO = cupomDAO;
        this.pedidoProdutoDAO = pedidoProdutoDAO;
        this.pedidoProdutoDAO.migrateLegacyItems(pedidoDAO.listActive());
    }

    public Pedido criarPedido(int idCliente, int[] idsProdutos, int[] quantidades) throws IOException {
        validarCliente(idCliente);
        Map<Integer, Integer> itens = normalizarItens(idsProdutos, quantidades);
        double total = aplicarEstoqueParaNovoPedido(itens);
        Pedido pedido = new Pedido(0, idCliente, new int[0], new int[0], -1, LocalDate.now().toString(), total);
        Pedido created = pedidoDAO.create(pedido);
        pedidoProdutoDAO.replaceForPedido(created.getId(), criarItens(created.getId(), itens));
        return created;
    }

    public boolean associarCupom(int idPedido, int idCupom) throws IOException {
        Pedido pedido = pedidoDAO.read(idPedido);
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido nao encontrado.");
        }
        if (pedido.getIdCupom() != -1) {
            throw new IllegalArgumentException("Pedido ja possui cupom associado.");
        }

        Cupom cupom = cupomDAO.read(idCupom);
        if (cupom == null || !cupom.getAtivo()) {
            throw new IllegalArgumentException("Cupom invalido ou inativo.");
        }

        double desconto = pedido.getValorTotal() * (cupom.getPercentualDesconto() / 100.0);
        pedido.setValorTotal(Math.max(0, pedido.getValorTotal() - desconto));
        pedido.setIdCupom(cupom.getId());
        return pedidoDAO.update(pedido);
    }

    public Pedido consultarPorId(int id) throws IOException {
        return pedidoDAO.read(id);
    }

    public List<Pedido> listarAtivos() throws IOException {
        return pedidoDAO.listActive();
    }

    public List<Pedido> listarPorCliente(int idCliente) throws IOException {
        validarCliente(idCliente);
        return pedidoDAO.listByCliente(idCliente);
    }

    public List<PedidoProduto> listarItensDoPedido(int idPedido) throws IOException {
        return pedidoProdutoDAO.listByPedido(idPedido);
    }

    public List<Pedido> listarPorProduto(int idProduto) throws IOException {
        if (produtoDAO.read(idProduto) == null) {
            throw new IllegalArgumentException("Produto nao encontrado.");
        }
        List<Pedido> pedidos = new ArrayList<>();
        for (PedidoProduto item : pedidoProdutoDAO.listByProduto(idProduto)) {
            Pedido pedido = pedidoDAO.read(item.getIdPedido());
            if (pedido != null) {
                pedidos.add(pedido);
            }
        }
        return pedidos;
    }

    public boolean excluir(int id) throws IOException {
        Pedido pedido = pedidoDAO.read(id);
        if (pedido == null) {
            return false;
        }
        for (PedidoProduto item : pedidoProdutoDAO.listByPedido(id)) {
            Produto produto = produtoDAO.read(item.getIdProduto());
            if (produto != null) {
                produto.setEstoque(produto.getEstoque() + item.getQuantidade());
                produtoDAO.update(produto);
            }
        }
        pedidoProdutoDAO.deleteByPedido(id);
        return pedidoDAO.delete(id);
    }

    public boolean atualizarPedido(int idPedido, int idCliente, int[] idsProdutos, int[] quantidades, int idCupom) throws IOException {
        Pedido atual = pedidoDAO.read(idPedido);
        if (atual == null) {
            throw new IllegalArgumentException("Pedido nao encontrado.");
        }

        validarCliente(idCliente);
        Map<Integer, Integer> novosItens = normalizarItens(idsProdutos, quantidades);
        Map<Integer, Integer> itensAtuais = mapItensPersistidos(pedidoProdutoDAO.listByPedido(idPedido));

        double total = recalcularEstoqueEValor(itensAtuais, novosItens);
        if (idCupom != -1) {
            Cupom cupom = cupomDAO.read(idCupom);
            if (cupom == null || !cupom.getAtivo()) {
                throw new IllegalArgumentException("Cupom invalido ou inativo.");
            }
            total -= total * (cupom.getPercentualDesconto() / 100.0);
        }

        Pedido atualizado = new Pedido(
            idPedido,
            idCliente,
            new int[0],
            new int[0],
            idCupom,
            atual.getDataPedido(),
            Math.max(0.0, total)
        );
        boolean updated = pedidoDAO.update(atualizado);
        if (updated) {
            pedidoProdutoDAO.replaceForPedido(idPedido, criarItens(idPedido, novosItens));
        }
        return updated;
    }

    private void validarCliente(int idCliente) throws IOException {
        if (clienteDAO.read(idCliente) == null) {
            throw new IllegalArgumentException("Cliente nao encontrado.");
        }
    }

    private Map<Integer, Integer> normalizarItens(int[] idsProdutos, int[] quantidades) {
        if (idsProdutos == null || quantidades == null || idsProdutos.length == 0 || idsProdutos.length != quantidades.length) {
            throw new IllegalArgumentException("Pedido deve possuir produtos e quantidades validas.");
        }
        Map<Integer, Integer> itens = new LinkedHashMap<>();
        for (int i = 0; i < idsProdutos.length; i++) {
            int idProduto = idsProdutos[i];
            int quantidade = quantidades[i];
            if (quantidade <= 0) {
                throw new IllegalArgumentException("Quantidade invalida para produto " + idProduto + ".");
            }
            itens.merge(idProduto, quantidade, Integer::sum);
        }
        return itens;
    }

    private double aplicarEstoqueParaNovoPedido(Map<Integer, Integer> itens) throws IOException {
        double total = 0.0;
        for (Map.Entry<Integer, Integer> entry : itens.entrySet()) {
            Produto produto = produtoDAO.read(entry.getKey());
            if (produto == null) {
                throw new IllegalArgumentException("Produto " + entry.getKey() + " nao encontrado.");
            }
            if (produto.getEstoque() < entry.getValue()) {
                throw new IllegalArgumentException("Estoque insuficiente para produto " + produto.getNome() + ".");
            }
        }
        for (Map.Entry<Integer, Integer> entry : itens.entrySet()) {
            Produto produto = produtoDAO.read(entry.getKey());
            produto.setEstoque(produto.getEstoque() - entry.getValue());
            produtoDAO.update(produto);
            total += produto.getPreco() * entry.getValue();
        }
        return total;
    }

    private double recalcularEstoqueEValor(Map<Integer, Integer> itensAtuais, Map<Integer, Integer> novosItens) throws IOException {
        Map<Integer, Boolean> chaves = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : itensAtuais.entrySet()) {
            chaves.put(entry.getKey(), Boolean.TRUE);
        }
        for (Map.Entry<Integer, Integer> entry : novosItens.entrySet()) {
            chaves.put(entry.getKey(), Boolean.TRUE);
        }

        for (Integer idProduto : chaves.keySet()) {
            Produto produto = produtoDAO.read(idProduto);
            if (produto == null) {
                throw new IllegalArgumentException("Produto " + idProduto + " nao encontrado.");
            }
            int quantidadeAtual = itensAtuais.getOrDefault(idProduto, 0);
            int novaQuantidade = novosItens.getOrDefault(idProduto, 0);
            int disponivel = produto.getEstoque() + quantidadeAtual;
            if (disponivel < novaQuantidade) {
                throw new IllegalArgumentException("Estoque insuficiente para produto " + produto.getNome() + ".");
            }
        }

        double total = 0.0;
        for (Integer idProduto : chaves.keySet()) {
            Produto produto = produtoDAO.read(idProduto);
            int quantidadeAtual = itensAtuais.getOrDefault(idProduto, 0);
            int novaQuantidade = novosItens.getOrDefault(idProduto, 0);
            int disponivel = produto.getEstoque() + quantidadeAtual;
            produto.setEstoque(disponivel - novaQuantidade);
            produtoDAO.update(produto);
            total += produto.getPreco() * novaQuantidade;
        }
        return total;
    }

    private int[] mapKeys(Map<Integer, Integer> itens) {
        return itens.keySet().stream().mapToInt(Integer::intValue).toArray();
    }

    private int[] mapValues(Map<Integer, Integer> itens) {
        return itens.values().stream().mapToInt(Integer::intValue).toArray();
    }

    private List<PedidoProduto> criarItens(int idPedido, Map<Integer, Integer> itens) {
        List<PedidoProduto> registros = new ArrayList<>();
        for (Map.Entry<Integer, Integer> item : itens.entrySet()) {
            registros.add(new PedidoProduto(idPedido, item.getKey(), item.getValue()));
        }
        return registros;
    }

    private Map<Integer, Integer> mapItensPersistidos(List<PedidoProduto> registros) {
        Map<Integer, Integer> itens = new LinkedHashMap<>();
        for (PedidoProduto item : registros) {
            itens.put(item.getIdProduto(), item.getQuantidade());
        }
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("Pedido nao possui itens associados.");
        }
        return itens;
    }
}
