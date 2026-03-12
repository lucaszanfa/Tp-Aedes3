package Model;

import Util.BinaryStringIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Cliente implements Registro {

    private int id;
    private String nome;
    private String email;
    private String[] telefones;

    public Cliente() {
        this.telefones = new String[0];
    }

    public Cliente(int id, String nome, String email, String telefone) {
        this(id, nome, email, new String[] { telefone });
    }

    public Cliente(int id, String nome, String email, String[] telefones) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefones = telefones == null ? new String[0] : telefones;
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
        return String.join(", ", telefones);
    }

    public void setTelefone(String telefone) {
        setTelefones(new String[] { telefone });
    }

    public String[] getTelefones() {
        return telefones;
    }

    public void setTelefones(String[] telefones) {
        this.telefones = telefones == null ? new String[0] : telefones;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        String[] values = new String[2 + telefones.length];
        values[0] = nome;
        values[1] = email;
        System.arraycopy(telefones, 0, values, 2, telefones.length);
        BinaryStringIO.writeStringBlock(dos, values);
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
        if (values.length > 2) {
            telefones = Arrays.copyOfRange(values, 2, values.length);
        } else {
            telefones = new String[0];
        }
    }

    @Override
    public String toString() {
        return "Cliente [id=" + id +
            ", nome=" + nome +
            ", email=" + email +
            ", telefones=" + Arrays.toString(telefones) + "]";
    }
}
