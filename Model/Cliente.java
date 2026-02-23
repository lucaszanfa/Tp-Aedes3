package Model;

import java.io.*;

public class Cliente {

    private int id;
    private String nome;
    private String email;
    private String telefone;

    // Construtor vazio
    public Cliente() {
    }

    // Construtor completo
    public Cliente(int id, String nome, String email, String telefone) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }

    // GETTERS E SETTERS

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    // Método para transformar objeto em array de bytes
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        dos.writeUTF(nome);
        dos.writeUTF(email);
        dos.writeUTF(telefone);

        return baos.toByteArray();
    }

    // Método para reconstruir objeto a partir de bytes
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        id = dis.readInt();
        nome = dis.readUTF();
        email = dis.readUTF();
        telefone = dis.readUTF();
    }

    @Override
    public String toString() {
        return "Cliente [id=" + id +
                ", nome=" + nome +
                ", email=" + email +
                ", telefone=" + telefone + "]";
    }
}