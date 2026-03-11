package DAO;

import Model.Produto;
import java.io.IOException;

public class ProdutoDAO extends ArquivoDAO<Produto> {
    public ProdutoDAO(String path) throws IOException {
        super(path, Produto::new);
    }
}
