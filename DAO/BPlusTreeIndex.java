package DAO;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Arvore B+ persistente de chave long. As folhas sao encadeadas para
 * percorrer chaves em ordem sem ordenar os resultados em memoria.
 */
public class BPlusTreeIndex {

    private static final int MAGIC = 0x42505431;
    private static final int VERSION = 1;
    private static final int MAX_KEYS = 4;
    private static final int HEADER_SIZE = 32;
    private static final int NODE_SIZE = 117;

    private final String path;
    private long root;
    private long firstLeaf;
    private long nextAddress;

    public BPlusTreeIndex(String path) throws IOException {
        this.path = path;
        initialize();
    }

    public synchronized void clear() throws IOException {
        File file = new File(path);
        ensureParent(file);
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.setLength(0);
            root = HEADER_SIZE;
            firstLeaf = root;
            nextAddress = HEADER_SIZE + NODE_SIZE;
            writeHeader(raf);
            writeNode(raf, root, new Node(true));
        }
    }

    public synchronized void put(long key, long value) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
            Split split = insert(raf, root, key, value);
            if (split != null) {
                Node newRoot = new Node(false);
                newRoot.count = 1;
                newRoot.keys[0] = split.key;
                newRoot.children[0] = root;
                newRoot.children[1] = split.rightAddress;
                root = allocate(raf, newRoot);
            }
            writeHeader(raf);
        }
    }

    public synchronized Long get(long key) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            Node leaf = findLeaf(raf, key);
            for (int i = 0; i < leaf.count; i++) {
                if (leaf.keys[i] == key) {
                    return leaf.values[i];
                }
            }
            return null;
        }
    }

    public synchronized List<Long> valuesInOrder() throws IOException {
        return range(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public synchronized List<Long> range(long fromInclusive, long toInclusive) throws IOException {
        List<Long> values = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            long address = fromInclusive == Long.MIN_VALUE ? firstLeaf : findLeafAddress(raf, fromInclusive);
            while (address != -1L) {
                Node leaf = readNode(raf, address);
                for (int i = 0; i < leaf.count; i++) {
                    if (leaf.keys[i] > toInclusive) {
                        return values;
                    }
                    if (leaf.keys[i] >= fromInclusive) {
                        values.add(leaf.values[i]);
                    }
                }
                address = leaf.nextLeaf;
            }
        }
        return values;
    }

    private Split insert(RandomAccessFile raf, long address, long key, long value) throws IOException {
        Node node = readNode(raf, address);
        if (node.leaf) {
            int position = 0;
            while (position < node.count && node.keys[position] < key) {
                position++;
            }
            if (position < node.count && node.keys[position] == key) {
                node.values[position] = value;
                writeNode(raf, address, node);
                return null;
            }
            insertLeafEntry(node, position, key, value);
            if (node.count <= MAX_KEYS) {
                writeNode(raf, address, node);
                return null;
            }
            return splitLeaf(raf, address, node);
        }

        int child = 0;
        while (child < node.count && key >= node.keys[child]) {
            child++;
        }
        Split childSplit = insert(raf, node.children[child], key, value);
        if (childSplit == null) {
            return null;
        }
        insertInternalEntry(node, child, childSplit.key, childSplit.rightAddress);
        if (node.count <= MAX_KEYS) {
            writeNode(raf, address, node);
            return null;
        }
        return splitInternal(raf, address, node);
    }

    private Split splitLeaf(RandomAccessFile raf, long address, Node node) throws IOException {
        int leftCount = (node.count + 1) / 2;
        Node right = new Node(true);
        right.count = node.count - leftCount;
        for (int i = 0; i < right.count; i++) {
            right.keys[i] = node.keys[leftCount + i];
            right.values[i] = node.values[leftCount + i];
        }
        node.count = leftCount;
        right.nextLeaf = node.nextLeaf;
        long rightAddress = allocate(raf, right);
        node.nextLeaf = rightAddress;
        writeNode(raf, address, node);
        return new Split(right.keys[0], rightAddress);
    }

    private Split splitInternal(RandomAccessFile raf, long address, Node node) throws IOException {
        int middle = node.count / 2;
        long promoted = node.keys[middle];
        Node right = new Node(false);
        right.count = node.count - middle - 1;
        for (int i = 0; i < right.count; i++) {
            right.keys[i] = node.keys[middle + 1 + i];
        }
        for (int i = 0; i <= right.count; i++) {
            right.children[i] = node.children[middle + 1 + i];
        }
        node.count = middle;
        long rightAddress = allocate(raf, right);
        writeNode(raf, address, node);
        return new Split(promoted, rightAddress);
    }

    private void insertLeafEntry(Node node, int position, long key, long value) {
        for (int i = node.count; i > position; i--) {
            node.keys[i] = node.keys[i - 1];
            node.values[i] = node.values[i - 1];
        }
        node.keys[position] = key;
        node.values[position] = value;
        node.count++;
    }

    private void insertInternalEntry(Node node, int position, long key, long childAddress) {
        for (int i = node.count; i > position; i--) {
            node.keys[i] = node.keys[i - 1];
        }
        for (int i = node.count + 1; i > position + 1; i--) {
            node.children[i] = node.children[i - 1];
        }
        node.keys[position] = key;
        node.children[position + 1] = childAddress;
        node.count++;
    }

    private Node findLeaf(RandomAccessFile raf, long key) throws IOException {
        return readNode(raf, findLeafAddress(raf, key));
    }

    private long findLeafAddress(RandomAccessFile raf, long key) throws IOException {
        long address = root;
        Node node = readNode(raf, address);
        while (!node.leaf) {
            int child = 0;
            while (child < node.count && key >= node.keys[child]) {
                child++;
            }
            address = node.children[child];
            node = readNode(raf, address);
        }
        return address;
    }

    private long allocate(RandomAccessFile raf, Node node) throws IOException {
        long address = nextAddress;
        nextAddress += NODE_SIZE;
        writeNode(raf, address, node);
        return address;
    }

    private void initialize() throws IOException {
        File file = new File(path);
        ensureParent(file);
        if (!file.exists() || file.length() < HEADER_SIZE + NODE_SIZE) {
            clear();
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            if (raf.readInt() != MAGIC || raf.readInt() != VERSION) {
                clear();
                return;
            }
            root = raf.readLong();
            firstLeaf = raf.readLong();
            nextAddress = raf.readLong();
        }
    }

    private void writeHeader(RandomAccessFile raf) throws IOException {
        raf.seek(0);
        raf.writeInt(MAGIC);
        raf.writeInt(VERSION);
        raf.writeLong(root);
        raf.writeLong(firstLeaf);
        raf.writeLong(nextAddress);
    }

    private Node readNode(RandomAccessFile raf, long address) throws IOException {
        raf.seek(address);
        Node node = new Node(raf.readBoolean());
        node.count = raf.readInt();
        node.nextLeaf = raf.readLong();
        for (int i = 0; i < MAX_KEYS; i++) {
            node.keys[i] = raf.readLong();
        }
        for (int i = 0; i < MAX_KEYS; i++) {
            node.values[i] = raf.readLong();
        }
        for (int i = 0; i <= MAX_KEYS; i++) {
            node.children[i] = raf.readLong();
        }
        return node;
    }

    private void writeNode(RandomAccessFile raf, long address, Node node) throws IOException {
        raf.seek(address);
        raf.writeBoolean(node.leaf);
        raf.writeInt(node.count);
        raf.writeLong(node.nextLeaf);
        for (int i = 0; i < MAX_KEYS; i++) {
            raf.writeLong(i < node.count ? node.keys[i] : 0L);
        }
        for (int i = 0; i < MAX_KEYS; i++) {
            raf.writeLong(node.leaf && i < node.count ? node.values[i] : 0L);
        }
        for (int i = 0; i <= MAX_KEYS; i++) {
            raf.writeLong(!node.leaf && i <= node.count ? node.children[i] : 0L);
        }
    }

    private void ensureParent(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    private static final class Node {
        private final boolean leaf;
        private int count;
        private long nextLeaf = -1L;
        private final long[] keys = new long[MAX_KEYS + 1];
        private final long[] values = new long[MAX_KEYS + 1];
        private final long[] children = new long[MAX_KEYS + 2];

        private Node(boolean leaf) {
            this.leaf = leaf;
        }
    }

    private static final class Split {
        private final long key;
        private final long rightAddress;

        private Split(long key, long rightAddress) {
            this.key = key;
            this.rightAddress = rightAddress;
        }
    }
}
