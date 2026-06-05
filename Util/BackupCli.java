package Util;

public class BackupCli {

    public static void main(String[] args) throws Exception {
        String dataPath = args.length > 0 ? args[0] : "data";
        String huffmanPath = args.length > 1 ? args[1] : "backups/fase4_huffman.huff";
        String lzwPath = args.length > 2 ? args[2] : "backups/fase4_lzw.lzw";

        CompressionResult huffman = BackupService.createHuffmanBackup(dataPath, huffmanPath);
        CompressionResult lzw = BackupService.createLzwBackup(dataPath, lzwPath);

        print(huffman);
        print(lzw);
    }

    private static void print(CompressionResult result) {
        System.out.println(result.getAlgorithm());
        System.out.println("Arquivo: " + result.getOutputPath());
        System.out.println("Original: " + result.getOriginalSize() + " bytes");
        System.out.println("Comprimido: " + result.getCompressedSize() + " bytes");
        System.out.println("Taxa: " + String.format("%.2f%%", result.getCompressionRate()));
        System.out.println("Verificado: " + result.isVerified());
        System.out.println(result.getInterpretation());
        System.out.println();
    }
}
