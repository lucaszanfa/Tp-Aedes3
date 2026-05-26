package DAO;

import Model.Pedido;
import Model.PedidoProduto;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class PedidoProdutoDAO {

    private static final int HEADER_SIZE = 4;
    private final String path;
    private final BPlusTreeIndex porPedido;
    private final BPlusTreeIndex porProduto;

    public PedidoProdutoDAO(String path) throws IOException {
        this.path = path;
        initializeFile();
        this.porPedido = new BPlusTreeIndex(path + ".pedido.bplus.db");
        this.porProduto = new BPlusTreeIndex(path + ".produto.bplus.db");
        rebuildIndexes();
    }

    public synchronized void create(PedidoProduto item) throws IOException {
        validar(item);
        if (read(item.getIdPedido(), item.getIdProduto()) != null) {
            throw new IllegalArgumentException("Produto ja associado ao pedido.");
        }
        try (RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
            raf.seek(0);
            int totalInsercoes = raf.readInt();
            byte[] data = item.toByteArray();
            raf.seek(0);
            raf.writeInt(totalInsercoes + 1);
            long position = raf.length();
            raf.seek(position);
            raf.writeBoolean(false);
            raf.writeInt(data.length);
            raf.write(data);
            index(item, position);
        }
    }

    public synchronized PedidoProduto read(int idPedido, int idProduto) throws IOException {
        Long position = porPedido.get(PedidoProduto.chave(idPedido, idProduto));
        if (position == null) {
            return null;
        }
        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            return readAt(raf, position);
        }
    }

    public synchronized List<PedidoProduto> listByPedido(int idPedido) throws IOException {
        return readPositions(porPedido.range(chaveInicial(idPedido), chaveFinal(idPedido)));
    }

    public synchronized List<PedidoProduto> listByProduto(int idProduto) throws IOException {
        return readPositions(porProduto.range(chaveInicial(idProduto), chaveFinal(idProduto)));
    }

    public synchronized void replaceForPedido(int idPedido, List<PedidoProduto> items) throws IOException {
        deleteByPedido(idPedido);
        for (PedidoProduto item : items) {
            create(item);
        }
    }

    public synchronized void deleteByPedido(int idPedido) throws IOException {
        boolean changed = false;
        for (PedidoProduto item : listByPedido(idPedido)) {
            changed |= markDeleted(item);
        }
        if (changed) {
            rebuildIndexes();
        }
    }

    public synchronized void migrateLegacyItems(List<Pedido> pedidos) throws IOException {
        for (Pedido pedido : pedidos) {
            if (!listByPedido(pedido.getId()).isEmpty()) {
                continue;
            }
            int[] produtos = pedido.getIdsProdutos();
            int[] quantidades = pedido.getQuantidades();
            for (int i = 0; i < produtos.length && i < quantidades.length; i++) {
                create(new PedidoProduto(pedido.getId(), produtos[i], quantidades[i]));
            }
        }
    }

    private List<PedidoProduto> listActive() throws IOException {
        List<PedidoProduto> list = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            raf.seek(HEADER_SIZE);
            while (raf.getFilePointer() < raf.length()) {
                boolean lapide = raf.readBoolean();
                int size = raf.readInt();
                byte[] data = new byte[size];
                raf.readFully(data);
                if (!lapide) {
                    PedidoProduto item = new PedidoProduto();
                    item.fromByteArray(data);
                    list.add(item);
                }
            }
        }
        return list;
    }

    private List<PedidoProduto> readPositions(List<Long> positions) throws IOException {
        List<PedidoProduto> list = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            for (Long position : positions) {
                PedidoProduto item = readAt(raf, position);
                if (item != null) {
                    list.add(item);
                }
            }
        }
        return list;
    }

    private PedidoProduto readAt(RandomAccessFile raf, long position) throws IOException {
        if (position < HEADER_SIZE || position >= raf.length()) {
            return null;
        }
        raf.seek(position);
        boolean lapide = raf.readBoolean();
        int size = raf.readInt();
        byte[] data = new byte[size];
        raf.readFully(data);
        if (lapide) {
            return null;
        }
        PedidoProduto item = new PedidoProduto();
        item.fromByteArray(data);
        return item;
    }

    private boolean markDeleted(PedidoProduto item) throws IOException {
        Long position = porPedido.get(item.getChaveComposta());
        if (position == null) {
            return false;
        }
        try (RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
            raf.seek(position);
            if (raf.readBoolean()) {
                return false;
            }
            raf.seek(position);
            raf.writeBoolean(true);
            return true;
        }
    }

    private void rebuildIndexes() throws IOException {
        porPedido.clear();
        porProduto.clear();
        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            raf.seek(HEADER_SIZE);
            while (raf.getFilePointer() < raf.length()) {
                long position = raf.getFilePointer();
                boolean lapide = raf.readBoolean();
                int size = raf.readInt();
                byte[] data = new byte[size];
                raf.readFully(data);
                if (!lapide) {
                    PedidoProduto item = new PedidoProduto();
                    item.fromByteArray(data);
                    index(item, position);
                }
            }
        }
    }

    private void index(PedidoProduto item, long position) throws IOException {
        porPedido.put(item.getChaveComposta(), position);
        porProduto.put(PedidoProduto.chave(item.getIdProduto(), item.getIdPedido()), position);
    }

    private long chaveInicial(int id) {
        return PedidoProduto.chave(id, 0);
    }

    private long chaveFinal(int id) {
        return PedidoProduto.chave(id, Integer.MAX_VALUE);
    }

    private void validar(PedidoProduto item) {
        if (item.getIdPedido() <= 0 || item.getIdProduto() <= 0 || item.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Item de pedido possui chave ou quantidade invalida.");
        }
    }

    private void initializeFile() throws IOException {
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        if (!file.exists() || file.length() < HEADER_SIZE) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.setLength(0);
                raf.writeInt(0);
            }
        }
    }
}
