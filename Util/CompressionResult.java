package Util;

public class CompressionResult {

    private final String algorithm;
    private final String outputPath;
    private final long originalSize;
    private final long compressedSize;
    private final boolean verified;

    public CompressionResult(String algorithm, String outputPath, long originalSize, long compressedSize, boolean verified) {
        this.algorithm = algorithm;
        this.outputPath = outputPath;
        this.originalSize = originalSize;
        this.compressedSize = compressedSize;
        this.verified = verified;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public long getOriginalSize() {
        return originalSize;
    }

    public long getCompressedSize() {
        return compressedSize;
    }

    public boolean isVerified() {
        return verified;
    }

    public double getCompressionRate() {
        if (originalSize == 0) {
            return 0.0;
        }
        return 100.0 * (1.0 - ((double) compressedSize / (double) originalSize));
    }

    public String getInterpretation() {
        double rate = getCompressionRate();
        if (rate > 0) {
            return String.format("Reducao de %.2f%% em relacao ao pacote original.", rate);
        }
        if (rate == 0) {
            return "O arquivo comprimido ficou com o mesmo tamanho do pacote original.";
        }
        return String.format("Aumento de %.2f%% por causa do overhead do algoritmo e do cabecalho.", Math.abs(rate));
    }
}
