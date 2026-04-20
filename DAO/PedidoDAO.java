package DAO;

import Model.Pedido;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO extends ArquivoDAO<Pedido> {

    private final PedidoClienteIndexDAO clientePedidoIndex;

    public PedidoDAO(String path) throws IOException {
        super(path, Pedido::new);
        this.clientePedidoIndex = new PedidoClienteIndexDAO(path + ".cliente_pedidos");
        rebuildClientePedidoIndex();
    }

    @Override
    public synchronized Pedido create(Pedido pedido) throws IOException {
        Pedido created = super.create(pedido);
        clientePedidoIndex.add(created.getIdCliente(), created.getId());
        return created;
    }

    @Override
    public synchronized boolean update(Pedido pedido) throws IOException {
        boolean updated = super.update(pedido);
        if (updated) {
            rebuildClientePedidoIndex();
        }
        return updated;
    }

    @Override
    public synchronized boolean delete(int id) throws IOException {
        boolean deleted = super.delete(id);
        if (deleted) {
            rebuildClientePedidoIndex();
        }
        return deleted;
    }

    public synchronized List<Pedido> listByCliente(int idCliente) throws IOException {
        List<Pedido> pedidos = new ArrayList<>();
        for (Integer idPedido : clientePedidoIndex.listPedidoIds(idCliente)) {
            Pedido pedido = read(idPedido);
            if (pedido != null) {
                pedidos.add(pedido);
            }
        }
        return pedidos;
    }

    private void rebuildClientePedidoIndex() throws IOException {
        clientePedidoIndex.rebuild(listActive());
    }
}
