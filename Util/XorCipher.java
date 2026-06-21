package Util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class XorCipher {

    private static final String PREFIX = "XOR1:";
    private static final byte[] KEY = "tp-loja-online-chave-xor".getBytes(StandardCharsets.UTF_8);

    private XorCipher() {
    }

    public static String encrypt(String plainText) {
        byte[] input = (plainText == null ? "" : plainText).getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = xor(input);
        return PREFIX + Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decryptIfEncrypted(String value) {
        if (value == null || !value.startsWith(PREFIX)) {
            return value;
        }
        byte[] encrypted = Base64.getDecoder().decode(value.substring(PREFIX.length()));
        return new String(xor(encrypted), StandardCharsets.UTF_8);
    }

    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    private static byte[] xor(byte[] input) {
        byte[] output = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = (byte) (input[i] ^ KEY[i % KEY.length]);
        }
        return output;
    }
}
