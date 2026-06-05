package Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.PriorityQueue;

public final class HuffmanCompressor {

    private static final byte[] MAGIC = new byte[] { 'H', 'U', 'F', '1' };

    private HuffmanCompressor() {
    }

    public static byte[] compress(byte[] input) throws IOException {
        long[] frequencies = new long[256];
        for (byte b : input) {
            frequencies[b & 0xFF]++;
        }
        Node root = buildTree(frequencies);
        String[] codes = new String[256];
        if (root != null) {
            fillCodes(root, "", codes);
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(buffer)) {
            out.write(MAGIC);
            out.writeLong(input.length);
            int unique = 0;
            for (long frequency : frequencies) {
                if (frequency > 0) {
                    unique++;
                }
            }
            out.writeInt(unique);
            for (int i = 0; i < frequencies.length; i++) {
                if (frequencies[i] > 0) {
                    out.writeByte(i);
                    out.writeLong(frequencies[i]);
                }
            }
            BitWriter writer = new BitWriter(out);
            for (byte b : input) {
                writer.writeBits(codes[b & 0xFF]);
            }
            writer.flush();
        }
        return buffer.toByteArray();
    }

    public static byte[] decompress(byte[] compressed) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(compressed))) {
            for (byte expected : MAGIC) {
                if (in.readByte() != expected) {
                    throw new IOException("Arquivo Huffman invalido.");
                }
            }
            long originalSize = in.readLong();
            int unique = in.readInt();
            long[] frequencies = new long[256];
            for (int i = 0; i < unique; i++) {
                int value = in.readUnsignedByte();
                long frequency = in.readLong();
                frequencies[value] = frequency;
            }
            Node root = buildTree(frequencies);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (root == null) {
                return out.toByteArray();
            }
            if (root.isLeaf()) {
                for (long i = 0; i < originalSize; i++) {
                    out.write(root.value);
                }
                return out.toByteArray();
            }
            BitReader reader = new BitReader(in);
            Node current = root;
            for (long i = 0; i < originalSize; ) {
                int bit = reader.readBit();
                if (bit < 0) {
                    throw new EOFException("Fim inesperado do fluxo Huffman.");
                }
                current = bit == 0 ? current.left : current.right;
                if (current.isLeaf()) {
                    out.write(current.value);
                    current = root;
                    i++;
                }
            }
            return out.toByteArray();
        }
    }

    private static Node buildTree(long[] frequencies) {
        PriorityQueue<Node> queue = new PriorityQueue<>();
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                queue.add(new Node(i, frequencies[i], null, null));
            }
        }
        if (queue.isEmpty()) {
            return null;
        }
        while (queue.size() > 1) {
            Node left = queue.poll();
            Node right = queue.poll();
            queue.add(new Node(-1, left.frequency + right.frequency, left, right));
        }
        return queue.poll();
    }

    private static void fillCodes(Node node, String prefix, String[] codes) {
        if (node.isLeaf()) {
            codes[node.value] = prefix.isEmpty() ? "0" : prefix;
            return;
        }
        fillCodes(node.left, prefix + "0", codes);
        fillCodes(node.right, prefix + "1", codes);
    }

    private static class Node implements Comparable<Node> {
        private final int value;
        private final long frequency;
        private final Node left;
        private final Node right;

        private Node(int value, long frequency, Node left, Node right) {
            this.value = value;
            this.frequency = frequency;
            this.left = left;
            this.right = right;
        }

        private boolean isLeaf() {
            return left == null && right == null;
        }

        @Override
        public int compareTo(Node other) {
            int byFrequency = Long.compare(this.frequency, other.frequency);
            return byFrequency != 0 ? byFrequency : Integer.compare(this.value, other.value);
        }
    }

    private static class BitWriter {
        private final DataOutputStream out;
        private int current;
        private int count;

        private BitWriter(DataOutputStream out) {
            this.out = out;
        }

        private void writeBits(String bits) throws IOException {
            for (int i = 0; i < bits.length(); i++) {
                current = (current << 1) | (bits.charAt(i) == '1' ? 1 : 0);
                count++;
                if (count == 8) {
                    out.writeByte(current);
                    current = 0;
                    count = 0;
                }
            }
        }

        private void flush() throws IOException {
            if (count > 0) {
                out.writeByte(current << (8 - count));
            }
        }
    }

    private static class BitReader {
        private final DataInputStream in;
        private int current;
        private int remaining;

        private BitReader(DataInputStream in) {
            this.in = in;
        }

        private int readBit() throws IOException {
            if (remaining == 0) {
                current = in.read();
                if (current == -1) {
                    return -1;
                }
                remaining = 8;
            }
            remaining--;
            return (current >> remaining) & 1;
        }
    }
}
