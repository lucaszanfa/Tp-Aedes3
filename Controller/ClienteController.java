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

    public Cliente cadastrar(String nome, String email, String telefonesCsv) throws IOException {
        validarTexto(nome, "Nome");
        validarTexto(email, "Email");
        String[] telefones = parseTelefones(telefonesCsv);
        return dao.create(new Cliente(0, nome.trim(), email.trim(), telefones));
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
        if (cliente.getTelefones().length == 0) {
            throw new IllegalArgumentException("Telefone obrigatorio.");
        }
        return dao.update(cliente);
    }

    private String[] parseTelefones(String telefonesCsv) {
        validarTexto(telefonesCsv, "Telefone(s)");
        return java.util.Arrays.stream(telefonesCsv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);
    }

    private void validarTexto(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " obrigatorio.");
        }
    }
}
