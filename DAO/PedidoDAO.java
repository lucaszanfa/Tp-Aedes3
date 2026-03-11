package DAO;

import Model.Pedido;
import java.io.IOException;

public class PedidoDAO extends ArquivoDAO<Pedido> {
    public PedidoDAO(String path) throws IOException {
        super(path, Pedido::new);
    }
}
