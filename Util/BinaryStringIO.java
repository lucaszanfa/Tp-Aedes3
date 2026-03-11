package Util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class BinaryStringIO {

    private BinaryStringIO() {
    }

    public static void writeStringBlock(DataOutput out, String... values) throws IOException {
        if (values == null) {
            out.writeShort(0);
            return;
        }

        if (values.length > Short.MAX_VALUE) {
            throw new IOException("Quantidade de strings excede o limite de 2 bytes.");
        }

        out.writeShort(values.length);
        for (String value : values) {
            byte[] bytes = (value == null ? "" : value).getBytes(StandardCharsets.UTF_8);
            out.writeInt(bytes.length);
            out.write(bytes);
        }
    }

    public static String[] readStringBlock(DataInput in) throws IOException {
        int count = in.readUnsignedShort();
        String[] values = new String[count];

        for (int i = 0; i < count; i++) {
            int size = in.readInt();
            if (size < 0) {
                throw new IOException("Tamanho de string invalido: " + size);
            }
            byte[] bytes = new byte[size];
            in.readFully(bytes);
            values[i] = new String(bytes, StandardCharsets.UTF_8);
        }

        return values;
    }
}
