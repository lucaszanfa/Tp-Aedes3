package DAO;

import Model.Cupom;
import java.io.IOException;
import java.util.List;

public class CupomDAO extends ArquivoDAO<Cupom> {
    public CupomDAO(String path) throws IOException {
        super(path, Cupom::new);
    }

    public Cupom findByCodigo(String codigo) throws IOException {
        List<Cupom> cupons = listActive();
        for (Cupom cupom : cupons) {
            if (cupom.getCodigo() != null && cupom.getCodigo().equalsIgnoreCase(codigo)) {
                return cupom;
            }
        }
        return null;
    }
}
