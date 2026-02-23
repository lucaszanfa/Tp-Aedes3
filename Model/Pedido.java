package Model;

import java.io.*;

public class Pedido {

    private int id;
    private int idCliente;
    private int idCupom;      // -1 = sem cupom
    private double valorTotal;

    private int[] idsProdutos;
    private int[] quantidades;

    public Pedido() {
        idCupom = -1;
        idsProdutos = new int[0];
        quantidades = new int[0];
    }

    public Pedido(int id, int idCliente, int[] idsProdutos, int[] quantidades, int idCupom, double valorTotal) {
        this.id = id;
        this.idCliente = idCliente;
        this.idsProdutos = idsProdutos;
        this.quantidades = quantidades;
        this.idCupom = idCupom;
        this.valorTotal = valorTotal;
    }

    // GETTERS E SETTERS

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdCupom() {
        return idCupom;
    }

    public void setIdCupom(int idCupom) {
        this.idCupom = idCupom;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public int[] getIdsProdutos() {
        return idsProdutos;
    }

    public void setIdsProdutos(int[] idsProdutos) {
        this.idsProdutos = idsProdutos;
    }

    public int[] getQuantidades() {
        return quantidades;
    }

    public void setQuantidades(int[] quantidades) {
        this.quantidades = quantidades;
    }

    // SERIALIZAÇÃO (objeto -> bytes)

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        dos.writeInt(idCliente);
        dos.writeInt(idCupom);
        dos.writeDouble(valorTotal);

        // quantidade de itens no pedido
        int n = (idsProdutos == null) ? 0 : idsProdutos.length;
        dos.writeInt(n);

        // salva pares (idProduto, quantidade)
        for (int i = 0; i < n; i++) {
            dos.writeInt(idsProdutos[i]);
            dos.writeInt(quantidades[i]);
        }

        return baos.toByteArray();
    }

    // DESSERIALIZAÇÃO (bytes -> objeto)

    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        id = dis.readInt();
        idCliente = dis.readInt();
        idCupom = dis.readInt();
        valorTotal = dis.readDouble();

        int n = dis.readInt();
        idsProdutos = new int[n];
        quantidades = new int[n];

        for (int i = 0; i < n; i++) {
            idsProdutos[i] = dis.readInt();
            quantidades[i] = dis.readInt();
        }
    }

    @Override
    public String toString() {
        String s = "Pedido [id=" + id +
                   ", idCliente=" + idCliente +
                   ", idCupom=" + idCupom +
                   ", valorTotal=" + valorTotal +
                   ", itens=";

        for (int i = 0; i < idsProdutos.length; i++) {
            s += "(" + idsProdutos[i] + " x" + quantidades[i] + ")";
            if (i < idsProdutos.length - 1) s += ", ";
        }

        s += "]";
        return s;
    }
}
