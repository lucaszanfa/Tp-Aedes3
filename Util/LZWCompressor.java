package Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LZWCompressor {

    private static final byte[] MAGIC = new byte[] { 'L', 'Z', 'W', '1' };
    private static final int MAX_DICTIONARY_SIZE = 65536;

    private LZWCompressor() {
    }

    public static byte[] compress(byte[] input) throws IOException {
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(String.valueOf((char) i), i);
        }

        List<Integer> codes = new ArrayList<>();
        String current = "";
        for (byte b : input) {
            String symbol = String.valueOf((char) (b & 0xFF));
            String candidate = current + symbol;
            if (dictionary.containsKey(candidate)) {
                current = candidate;
            } else {
                codes.add(dictionary.get(current));
                if (dictionary.size() < MAX_DICTIONARY_SIZE) {
                    dictionary.put(candidate, dictionary.size());
                }
                current = symbol;
            }
        }
        if (!current.isEmpty()) {
            codes.add(dictionary.get(current));
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(buffer)) {
            out.write(MAGIC);
            out.writeLong(input.length);
            for (Integer code : codes) {
                out.writeShort(code);
            }
        }
        return buffer.toByteArray();
    }

    public static byte[] decompress(byte[] compressed) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(compressed))) {
            for (byte expected : MAGIC) {
                if (in.readByte() != expected) {
                    throw new IOException("Arquivo LZW invalido.");
                }
            }
            long originalSize = in.readLong();
            List<String> dictionary = new ArrayList<>();
            for (int i = 0; i < 256; i++) {
                dictionary.add(String.valueOf((char) i));
            }

            int first;
            try {
                first = in.readUnsignedShort();
            } catch (EOFException e) {
                return new byte[0];
            }
            String previous = dictionary.get(first);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeLatin1(out, previous);

            while (true) {
                int code;
                try {
                    code = in.readUnsignedShort();
                } catch (EOFException e) {
                    break;
                }

                String entry;
                if (code < dictionary.size()) {
                    entry = dictionary.get(code);
                } else if (code == dictionary.size()) {
                    entry = previous + previous.charAt(0);
                } else {
                    throw new IOException("Codigo LZW invalido: " + code);
                }

                writeLatin1(out, entry);
                if (dictionary.size() < MAX_DICTIONARY_SIZE) {
                    dictionary.add(previous + entry.charAt(0));
                }
                previous = entry;
            }

            byte[] data = out.toByteArray();
            if (data.length != originalSize) {
                throw new IOException("Tamanho LZW restaurado invalido.");
            }
            return data;
        }
    }

    private static void writeLatin1(ByteArrayOutputStream out, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.ISO_8859_1);
        out.write(bytes, 0, bytes.length);
    }
}
