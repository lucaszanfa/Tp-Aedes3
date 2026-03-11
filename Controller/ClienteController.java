package Controller;

import DAO.ClienteDAO;
import Model.Cliente;
import java.io.IOException;
import java.util.List;

public class ClienteController {

    private final ClienteDAO dao;

    public ClienteController(ClienteDAO dao) {
        this.dao = dao;
    }

    public Cliente cadastrar(String nome, String email, String telefone) throws IOException {
        validarTexto(nome, "Nome");
        validarTexto(email, "Email");
        validarTexto(telefone, "Telefone");
        return dao.create(new Cliente(0, nome.trim(), email.trim(), telefone.trim()));
    }

    public Cliente consultarPorId(int id) throws IOException {
        return dao.read(id);
    }

    public List<Cliente> listarAtivos() throws IOException {
        return dao.listActive();
    }

    public boolean excluir(int id) throws IOException {
        return dao.delete(id);
    }

    public boolean atualizar(Cliente cliente) throws IOException {
        validarTexto(cliente.getNome(), "Nome");
        validarTexto(cliente.getEmail(), "Email");
        validarTexto(cliente.getTelefone(), "Telefone");
        return dao.update(cliente);
    }

    private void validarTexto(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " obrigatorio.");
        }
    }
}
