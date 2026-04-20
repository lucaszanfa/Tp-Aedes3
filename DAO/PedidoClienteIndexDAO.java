package DAO;

import Model.Pedido;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class PedidoClienteIndexDAO {

    private static final long NULL_POINTER = -1L;
    private final ExtensibleHashIndex hashIndex;
    private final String listPath;

    public PedidoClienteIndexDAO(String basePath) throws IOException {
        this.hashIndex = new ExtensibleHashIndex(basePath + ".dir.db", basePath + ".buckets.db", 4);
        this.listPath = basePath + ".list.db";
        initializeListFile();
    }

    public synchronized void rebuild(List<Pedido> pedidos) throws IOException {
        hashIndex.clear();
        try (RandomAccessFile listFile = new RandomAccessFile(listPath, "rw")) {
            listFile.setLength(0);
        }
        for (Pedido pedido : pedidos) {
            add(pedido.getIdCliente(), pedido.getId());
        }
    }

    public synchronized void add(int idCliente, int idPedido) throws IOException {
        Long head = hashIndex.get(idCliente);
        long next = head == null ? NULL_POINTER : head;
        long newNode = appendNode(idPedido, next);
        hashIndex.put(idCliente, newNode);
    }

    public synchronized List<Integer> listPedidoIds(int idCliente) throws IOException {
        List<Integer> ids = new ArrayList<>();
        Long nodePointer = hashIndex.get(idCliente);
        if (nodePointer == null) {
            return ids;
        }

        try (RandomAccessFile listFile = new RandomAccessFile(listPath, "r")) {
            long current = nodePointer;
            while (current != NULL_POINTER) {
                listFile.seek(current);
                ids.add(listFile.readInt());
                current = listFile.readLong();
            }
        }
        return ids;
    }

    private long appendNode(int idPedido, long next) throws IOException {
        try (RandomAccessFile listFile = new RandomAccessFile(listPath, "rw")) {
            long position = listFile.length();
            listFile.seek(position);
            listFile.writeInt(idPedido);
            listFile.writeLong(next);
            return position;
        }
    }

    private void initializeListFile() throws IOException {
        File file = new File(listPath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        if (!file.exists()) {
            try (RandomAccessFile listFile = new RandomAccessFile(file, "rw")) {
                listFile.setLength(0);
            }
        }
    }
}
