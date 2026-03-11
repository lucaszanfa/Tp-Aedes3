package Model;

import Util.BinaryStringIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Cliente implements Registro {

    private int id;
    private String nome;
    private String email;
    private String telefone;

    public Cliente() {
    }

    public Cliente(int id, String nome, String email, String telefone) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }

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

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        BinaryStringIO.writeStringBlock(dos, nome, email, telefone);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        id = dis.readInt();
        String[] values = BinaryStringIO.readStringBlock(dis);
        nome = values.length > 0 ? values[0] : "";
        email = values.length > 1 ? values[1] : "";
        telefone = values.length > 2 ? values[2] : "";
    }

    @Override
    public String toString() {
        return "Cliente [id=" + id +
            ", nome=" + nome +
            ", email=" + email +
            ", telefone=" + telefone + "]";
    }
}
