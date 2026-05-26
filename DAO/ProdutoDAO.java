package DAO;

import Model.Produto;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO extends ArquivoDAO<Produto> {
    private final BPlusTreeIndex orderedIndex;

    public ProdutoDAO(String path) throws IOException {
        super(path, Produto::new);
        this.orderedIndex = new BPlusTreeIndex(path + ".ordem_id.bplus.db");
        rebuildOrderedIndex();
    }

    @Override
    public synchronized Produto create(Produto produto) throws IOException {
        Produto created = super.create(produto);
        orderedIndex.put(created.getId(), created.getId());
        return created;
    }

    @Override
    public synchronized boolean delete(int id) throws IOException {
        boolean deleted = super.delete(id);
        if (deleted) {
            rebuildOrderedIndex();
        }
        return deleted;
    }

    public synchronized List<Produto> listOrderedById() throws IOException {
        List<Produto> produtos = new ArrayList<>();
        for (Long id : orderedIndex.valuesInOrder()) {
            Produto produto = read(id.intValue());
            if (produto != null) {
                produtos.add(produto);
            }
        }
        return produtos;
    }

    private void rebuildOrderedIndex() throws IOException {
        orderedIndex.clear();
        for (Produto produto : listActive()) {
            orderedIndex.put(produto.getId(), produto.getId());
        }
    }
}
