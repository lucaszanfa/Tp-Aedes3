package Controller;

import DAO.ProdutoDAO;
import Model.Produto;
import Util.PatternMatcher;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PesquisaController {

    private final ProdutoDAO produtoDAO;

    public PesquisaController(ProdutoDAO produtoDAO) {
        this.produtoDAO = produtoDAO;
    }

    public List<Produto> pesquisarProdutosPorNome(String algoritmo, String padrao) throws IOException {
        validarPadrao(padrao);
        String normalizedAlgorithm = normalizeAlgorithm(algoritmo);
        String normalizedPattern = padrao.trim().toLowerCase();
        List<Produto> encontrados = new ArrayList<>();

        for (Produto produto : produtoDAO.listOrderedById()) {
            String nome = produto.getNome() == null ? "" : produto.getNome().toLowerCase();
            boolean match = "BM".equals(normalizedAlgorithm)
                ? PatternMatcher.containsBoyerMoore(nome, normalizedPattern)
                : PatternMatcher.containsKmp(nome, normalizedPattern);
            if (match) {
                encontrados.add(produto);
            }
        }
        return encontrados;
    }

    public String normalizeAlgorithm(String algoritmo) {
        if (algoritmo != null && algoritmo.trim().equalsIgnoreCase("BM")) {
            return "BM";
        }
        return "KMP";
    }

    private void validarPadrao(String padrao) {
        if (padrao == null || padrao.trim().isEmpty()) {
            throw new IllegalArgumentException("Padrao de pesquisa obrigatorio.");
        }
    }
}
