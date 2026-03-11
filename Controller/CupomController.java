package Controller;

import DAO.CupomDAO;
import Model.Cupom;
import java.io.IOException;
import java.util.List;

public class CupomController {

    private final CupomDAO dao;

    public CupomController(CupomDAO dao) {
        this.dao = dao;
    }

    public Cupom cadastrar(String codigo, double percentualDesconto, boolean ativo) throws IOException {
        validarTexto(codigo, "Codigo");
        validarPercentual(percentualDesconto);
        return dao.create(new Cupom(0, codigo.trim(), percentualDesconto, ativo));
    }

    public Cupom consultarPorId(int id) throws IOException {
        return dao.read(id);
    }

    public Cupom consultarPorCodigo(String codigo) throws IOException {
        return dao.findByCodigo(codigo);
    }

    public List<Cupom> listarAtivos() throws IOException {
        return dao.listActive();
    }

    public boolean excluir(int id) throws IOException {
        return dao.delete(id);
    }

    public boolean atualizar(Cupom cupom) throws IOException {
        validarTexto(cupom.getCodigo(), "Codigo");
        validarPercentual(cupom.getPercentualDesconto());
        return dao.update(cupom);
    }

    private void validarTexto(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " obrigatorio.");
        }
    }

    private void validarPercentual(double percentualDesconto) {
        if (percentualDesconto < 0 || percentualDesconto > 100) {
            throw new IllegalArgumentException("Percentual deve estar entre 0 e 100.");
        }
    }
}
