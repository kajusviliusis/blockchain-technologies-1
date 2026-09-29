package hashing.experiment.support;

import java.util.HexFormat;

/** Calculates bit and hexadecimal differences between two hash outputs. */
public final class HashDifferenceCalculator {

    private static final HexFormat HEX_FORMAT = HexFormat.of();

    private HashDifferenceCalculator() {
    }

    public static int countDifferingBits(String firstHash, String secondHash) {
        byte[] firstBytes = HEX_FORMAT.parseHex(firstHash);
        byte[] secondBytes = HEX_FORMAT.parseHex(secondHash);

        if (firstBytes.length != secondBytes.length) {
            throw new IllegalArgumentException("Hashes must have equal lengths.");
        }

        int differingBits = 0;
        for (int index = 0; index < firstBytes.length; index++) {
            differingBits += Integer.bitCount((firstBytes[index] ^ secondBytes[index]) & 0xff);
        }

        return differingBits;
    }

    public static int countDifferingHexCharacters(String firstHash, String secondHash) {
        if (firstHash.length() != secondHash.length()) {
            throw new IllegalArgumentException("Hashes must have equal lengths.");
        }

        int differingCharacters = 0;
        for (int index = 0; index < firstHash.length(); index++) {
            if (firstHash.charAt(index) != secondHash.charAt(index)) {
                differingCharacters++;
            }
        }

        return differingCharacters;
    }
}
