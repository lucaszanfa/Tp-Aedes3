package Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public final class BackupService {

    private BackupService() {
    }

    public static CompressionResult createHuffmanBackup(String dataPath, String outputPath) throws IOException {
        byte[] archive = DataArchive.create(new File(dataPath));
        byte[] compressed = HuffmanCompressor.compress(archive);
        byte[] restored = HuffmanCompressor.decompress(compressed);
        boolean verified = Arrays.equals(archive, restored) && DataArchive.isValidArchive(restored);
        write(outputPath, compressed);
        return new CompressionResult("Huffman", outputPath, archive.length, compressed.length, verified);
    }

    public static CompressionResult createLzwBackup(String dataPath, String outputPath) throws IOException {
        byte[] archive = DataArchive.create(new File(dataPath));
        byte[] compressed = LZWCompressor.compress(archive);
        byte[] restored = LZWCompressor.decompress(compressed);
        boolean verified = Arrays.equals(archive, restored) && DataArchive.isValidArchive(restored);
        write(outputPath, compressed);
        return new CompressionResult("LZW", outputPath, archive.length, compressed.length, verified);
    }

    public static long rawDataSize(String dataPath) throws IOException {
        return DataArchive.sumDataFileSizes(new File(dataPath));
    }

    public static int dataFileCount(String dataPath) throws IOException {
        return DataArchive.countFiles(new File(dataPath));
    }

    private static void write(String outputPath, byte[] data) throws IOException {
        File file = new File(outputPath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(data);
        }
    }
}
