package Model;

import Util.BinaryStringIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Cupom implements Registro {

    private int id;
    private String codigo;
    private double percentualDesconto;
    private boolean ativo;

    public Cupom() {
    }

    public Cupom(int id, String codigo, double percentualDesconto, boolean ativo) {
        this.id = id;
        this.codigo = codigo;
        this.percentualDesconto = percentualDesconto;
        this.ativo = ativo;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setPercentualDesconto(double percentualDesconto) {
        this.percentualDesconto = percentualDesconto;
    }

    public double getPercentualDesconto() {
        return percentualDesconto;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean getAtivo() {
        return ativo;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        BinaryStringIO.writeStringBlock(dos, codigo);
        dos.writeDouble(percentualDesconto);
        dos.writeBoolean(ativo);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        id = dis.readInt();
        String[] values = BinaryStringIO.readStringBlock(dis);
        codigo = values.length > 0 ? values[0] : "";
        percentualDesconto = dis.readDouble();
        ativo = dis.readBoolean();
    }

    @Override
    public String toString() {
        return "Cupom [id=" + id +
            ", codigo=" + codigo +
            ", percentualDesconto=" + percentualDesconto +
            ", ativo=" + ativo + "]";
    }
}
