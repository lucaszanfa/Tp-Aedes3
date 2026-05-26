package Model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Registro associativo do relacionamento N:N entre Pedido e Produto.
 * A chave primaria e composta por idPedido e idProduto.
 */
public class PedidoProduto {

    private int idPedido;
    private int idProduto;
    private int quantidade;

    public PedidoProduto() {
    }

    public PedidoProduto(int idPedido, int idProduto, int quantidade) {
        this.idPedido = idPedido;
        this.idProduto = idProduto;
        this.quantidade = quantidade;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public int getIdProduto() {
        return idProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public long getChaveComposta() {
        return chave(idPedido, idProduto);
    }

    public static long chave(int idPedido, int idProduto) {
        return (((long) idPedido) << 32) | (idProduto & 0xffffffffL);
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(idPedido);
        dos.writeInt(idProduto);
        dos.writeInt(quantidade);
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] data) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        idPedido = dis.readInt();
        idProduto = dis.readInt();
        quantidade = dis.readInt();
    }

    @Override
    public String toString() {
        return "PedidoProduto [idPedido=" + idPedido
            + ", idProduto=" + idProduto
            + ", quantidade=" + quantidade + "]";
    }
}
