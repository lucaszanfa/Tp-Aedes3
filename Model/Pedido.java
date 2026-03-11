package Model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Pedido implements Registro {

    private int id;
    private int idCliente;
    private int idCupom;
    private double valorTotal;
    private int[] idsProdutos;
    private int[] quantidades;

    public Pedido() {
        this.idCupom = -1;
        this.idsProdutos = new int[0];
        this.quantidades = new int[0];
    }

    public Pedido(int id, int idCliente, int[] idsProdutos, int[] quantidades, int idCupom, double valorTotal) {
        this.id = id;
        this.idCliente = idCliente;
        this.idsProdutos = idsProdutos == null ? new int[0] : idsProdutos;
        this.quantidades = quantidades == null ? new int[0] : quantidades;
        this.idCupom = idCupom;
        this.valorTotal = valorTotal;
    }

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
        this.idsProdutos = idsProdutos == null ? new int[0] : idsProdutos;
    }

    public int[] getQuantidades() {
        return quantidades;
    }

    public void setQuantidades(int[] quantidades) {
        this.quantidades = quantidades == null ? new int[0] : quantidades;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        dos.writeInt(idCliente);
        dos.writeInt(idCupom);
        dos.writeDouble(valorTotal);

        int n = idsProdutos == null ? 0 : idsProdutos.length;
        dos.writeInt(n);
        for (int i = 0; i < n; i++) {
            dos.writeInt(idsProdutos[i]);
            dos.writeInt(quantidades[i]);
        }

        return baos.toByteArray();
    }

    @Override
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
        StringBuilder sb = new StringBuilder();
        sb.append("Pedido [id=").append(id)
            .append(", idCliente=").append(idCliente)
            .append(", idCupom=").append(idCupom)
            .append(", valorTotal=").append(valorTotal)
            .append(", itens=");

        for (int i = 0; i < idsProdutos.length; i++) {
            sb.append("(").append(idsProdutos[i]).append(" x").append(quantidades[i]).append(")");
            if (i < idsProdutos.length - 1) {
                sb.append(", ");
            }
        }

        sb.append("]");
        return sb.toString();
    }
}
