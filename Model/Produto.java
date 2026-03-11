package Model;

import Util.BinaryStringIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Produto implements Registro {

    private int id;
    private String nome;
    private double preco;
    private int estoque;

    public Produto() {
    }

    public Produto(int id, String nome, double preco, int estoque) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public double getPreco() {
        return preco;
    }

    public void setEstoque(int estoque) {
        this.estoque = estoque;
    }

    public int getEstoque() {
        return estoque;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        BinaryStringIO.writeStringBlock(dos, nome);
        dos.writeDouble(preco);
        dos.writeInt(estoque);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        id = dis.readInt();
        String[] values = BinaryStringIO.readStringBlock(dis);
        nome = values.length > 0 ? values[0] : "";
        preco = dis.readDouble();
        estoque = dis.readInt();
    }

    @Override
    public String toString() {
        return "Produto [id=" + id +
            ", nome=" + nome +
            ", preco=" + preco +
            ", estoque=" + estoque + "]";
    }
}
