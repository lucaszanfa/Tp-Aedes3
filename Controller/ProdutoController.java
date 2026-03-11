package Controller;

import DAO.ProdutoDAO;
import Model.Produto;
import java.io.IOException;
import java.util.List;

public class ProdutoController {

    private final ProdutoDAO dao;

    public ProdutoController(ProdutoDAO dao) {
        this.dao = dao;
    }

    public Produto cadastrar(String nome, double preco, int estoque) throws IOException {
        validarTexto(nome, "Nome");
        if (preco < 0) {
            throw new IllegalArgumentException("Preco nao pode ser negativo.");
        }
        if (estoque < 0) {
            throw new IllegalArgumentException("Estoque nao pode ser negativo.");
        }
        return dao.create(new Produto(0, nome.trim(), preco, estoque));
    }

    public Produto consultarPorId(int id) throws IOException {
        return dao.read(id);
    }

    public List<Produto> listarAtivos() throws IOException {
        return dao.listActive();
    }

    public boolean excluir(int id) throws IOException {
        return dao.delete(id);
    }

    public boolean atualizar(Produto produto) throws IOException {
        validarTexto(produto.getNome(), "Nome");
        if (produto.getPreco() < 0 || produto.getEstoque() < 0) {
            throw new IllegalArgumentException("Preco e estoque devem ser nao negativos.");
        }
        return dao.update(produto);
    }

    private void validarTexto(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " obrigatorio.");
        }
    }
}
