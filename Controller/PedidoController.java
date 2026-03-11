package Controller;

import DAO.ClienteDAO;
import DAO.CupomDAO;
import DAO.PedidoDAO;
import DAO.ProdutoDAO;
import Model.Cliente;
import Model.Cupom;
import Model.Pedido;
import Model.Produto;
import java.io.IOException;
import java.util.List;

public class PedidoController {

    private final PedidoDAO pedidoDAO;
    private final ClienteDAO clienteDAO;
    private final ProdutoDAO produtoDAO;
    private final CupomDAO cupomDAO;

    public PedidoController(PedidoDAO pedidoDAO, ClienteDAO clienteDAO, ProdutoDAO produtoDAO, CupomDAO cupomDAO) {
        this.pedidoDAO = pedidoDAO;
        this.clienteDAO = clienteDAO;
        this.produtoDAO = produtoDAO;
        this.cupomDAO = cupomDAO;
    }

    public Pedido criarPedido(int idCliente, int[] idsProdutos, int[] quantidades) throws IOException {
        validarPedido(idsProdutos, quantidades);

        Cliente cliente = clienteDAO.read(idCliente);
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente nao encontrado.");
        }

        double total = 0.0;
        for (int i = 0; i < idsProdutos.length; i++) {
            Produto produto = produtoDAO.read(idsProdutos[i]);
            if (produto == null) {
                throw new IllegalArgumentException("Produto " + idsProdutos[i] + " nao encontrado.");
            }
            if (quantidades[i] <= 0) {
                throw new IllegalArgumentException("Quantidade invalida para produto " + idsProdutos[i] + ".");
            }
            if (produto.getEstoque() < quantidades[i]) {
                throw new IllegalArgumentException("Estoque insuficiente para produto " + produto.getNome() + ".");
            }

            produto.setEstoque(produto.getEstoque() - quantidades[i]);
            produtoDAO.update(produto);
            total += produto.getPreco() * quantidades[i];
        }

        Pedido pedido = new Pedido(0, idCliente, idsProdutos, quantidades, -1, total);
        return pedidoDAO.create(pedido);
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

    public boolean excluir(int id) throws IOException {
        return pedidoDAO.delete(id);
    }

    public boolean atualizar(Pedido pedido) throws IOException {
        validarPedido(pedido.getIdsProdutos(), pedido.getQuantidades());
        if (clienteDAO.read(pedido.getIdCliente()) == null) {
            throw new IllegalArgumentException("Cliente do pedido nao existe.");
        }
        return pedidoDAO.update(pedido);
    }

    private void validarPedido(int[] idsProdutos, int[] quantidades) {
        if (idsProdutos == null || quantidades == null || idsProdutos.length == 0 || idsProdutos.length != quantidades.length) {
            throw new IllegalArgumentException("Pedido deve possuir produtos e quantidades validas.");
        }
    }
}
