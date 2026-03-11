package DAO;

import Model.Cliente;
import java.io.IOException;

public class ClienteDAO extends ArquivoDAO<Cliente> {
    public ClienteDAO(String path) throws IOException {
        super(path, Cliente::new);
    }
}
