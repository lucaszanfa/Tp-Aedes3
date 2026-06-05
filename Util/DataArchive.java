package Util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class DataArchive {

    private static final byte[] MAGIC = new byte[] { 'T', 'P', 'D', 'B', '1' };

    private DataArchive() {
    }

    public static byte[] create(File dataDirectory) throws IOException {
        List<File> files = listDataFiles(dataDirectory);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(buffer)) {
            out.write(MAGIC);
            out.writeInt(files.size());
            for (File file : files) {
                String relativePath = dataDirectory.toPath().relativize(file.toPath()).toString().replace('\\', '/');
                byte[] name = relativePath.getBytes(StandardCharsets.UTF_8);
                byte[] data = readAllBytes(file);
                out.writeInt(name.length);
                out.write(name);
                out.writeLong(data.length);
                out.write(data);
            }
        }
        return buffer.toByteArray();
    }

    public static long sumDataFileSizes(File dataDirectory) throws IOException {
        long total = 0;
        for (File file : listDataFiles(dataDirectory)) {
            total += file.length();
        }
        return total;
    }

    public static int countFiles(File dataDirectory) throws IOException {
        return listDataFiles(dataDirectory).size();
    }

    public static boolean isValidArchive(byte[] archive) throws IOException {
        try (DataInputStream in = new DataInputStream(new java.io.ByteArrayInputStream(archive))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(MAGIC, magic)) {
                return false;
            }
            int count = in.readInt();
            if (count < 0) {
                return false;
            }
            for (int i = 0; i < count; i++) {
                int nameSize = in.readInt();
                if (nameSize <= 0 || nameSize > 4096) {
                    return false;
                }
                byte[] name = new byte[nameSize];
                in.readFully(name);
                long size = in.readLong();
                if (size < 0 || size > Integer.MAX_VALUE) {
                    return false;
                }
                skipFully(in, size);
            }
            return in.read() == -1;
        } catch (EOFException e) {
            return false;
        }
    }

    private static List<File> listDataFiles(File dataDirectory) throws IOException {
        if (!dataDirectory.exists() || !dataDirectory.isDirectory()) {
            throw new IOException("Diretorio de dados nao encontrado: " + dataDirectory.getPath());
        }
        List<File> files = new ArrayList<>();
        collect(dataDirectory, files);
        files.sort(Comparator.comparing(File::getPath));
        return files;
    }

    private static void collect(File directory, List<File> files) {
        File[] children = directory.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                collect(child, files);
            } else if (child.isFile() && child.getName().endsWith(".db")) {
                files.add(child);
            }
        }
    }

    private static byte[] readAllBytes(File file) throws IOException {
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("Arquivo grande demais para backup em memoria: " + file.getPath());
        }
        byte[] data = new byte[(int) length];
        try (FileInputStream in = new FileInputStream(file)) {
            int offset = 0;
            while (offset < data.length) {
                int read = in.read(data, offset, data.length - offset);
                if (read == -1) {
                    break;
                }
                offset += read;
            }
            if (offset != data.length) {
                throw new EOFException("Leitura incompleta de " + file.getPath());
            }
        }
        return data;
    }

    private static void skipFully(DataInputStream in, long size) throws IOException {
        long remaining = size;
        while (remaining > 0) {
            long skipped = in.skip(remaining);
            if (skipped <= 0) {
                if (in.read() == -1) {
                    throw new EOFException();
                }
                skipped = 1;
            }
            remaining -= skipped;
        }
    }
}
