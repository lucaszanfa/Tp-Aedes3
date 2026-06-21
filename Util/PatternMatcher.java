package Util;

import java.util.Arrays;

public final class PatternMatcher {

    private PatternMatcher() {
    }

    public static boolean containsKmp(String text, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return true;
        }
        if (text == null || text.length() < pattern.length()) {
            return false;
        }

        int[] lps = buildLongestPrefixSuffix(pattern);
        int i = 0;
        int j = 0;
        while (i < text.length()) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == pattern.length()) {
                    return true;
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return false;
    }

    public static boolean containsBoyerMoore(String text, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return true;
        }
        if (text == null || text.length() < pattern.length()) {
            return false;
        }

        int[] badChar = buildBadCharacterTable(pattern);
        int shift = 0;
        while (shift <= text.length() - pattern.length()) {
            int j = pattern.length() - 1;
            while (j >= 0 && pattern.charAt(j) == text.charAt(shift + j)) {
                j--;
            }
            if (j < 0) {
                return true;
            }
            shift += Math.max(1, j - badChar[text.charAt(shift + j)]);
        }
        return false;
    }

    private static int[] buildLongestPrefixSuffix(String pattern) {
        int[] lps = new int[pattern.length()];
        int length = 0;
        int i = 1;
        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(length)) {
                lps[i] = ++length;
                i++;
            } else if (length > 0) {
                length = lps[length - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    private static int[] buildBadCharacterTable(String pattern) {
        int[] table = new int[Character.MAX_VALUE + 1];
        Arrays.fill(table, -1);
        for (int i = 0; i < pattern.length(); i++) {
            table[pattern.charAt(i)] = i;
        }
        return table;
    }
}
