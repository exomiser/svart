package org.monarchinitiative.svart.impl;

import java.util.Arrays;

/**
 * Encoder/Decoder for the 2-bit base encoding scheme where A=b00, C=b01, G=b10, T=b11. This implementation
 * is specialised for encoding/decoding sequences up to a maximum of 64 bits (long) == 32 bases used to represent short,
 * compact sequence variants.
 */
public final class TwoBitBaseCodec {

    private static final byte[] BASE_BYTES = {'A', 'C', 'G', 'T'};

    // Optimize base encoding with ASCII (7-bit) lookup table.
    // INVALID_BASE = 127 == DEL command. SAM specs allow ASCII 33-126 inclusive (printable characters).
    private static final byte INVALID_BASE = Byte.MAX_VALUE;
    private static final int BASE_ENCODING_TABLE_SIZE = 128;
    private static final byte[] BASE_ENCODING_TABLE = new byte[BASE_ENCODING_TABLE_SIZE];

    static {
        Arrays.fill(BASE_ENCODING_TABLE, INVALID_BASE);
        // Set only valid values
        BASE_ENCODING_TABLE['A'] = BASE_ENCODING_TABLE['a'] = 0b00;
        BASE_ENCODING_TABLE['C'] = BASE_ENCODING_TABLE['c'] = 0b01;
        BASE_ENCODING_TABLE['G'] = BASE_ENCODING_TABLE['g'] = 0b10;
        BASE_ENCODING_TABLE['T'] = BASE_ENCODING_TABLE['t'] = 0b11;
    }

    private TwoBitBaseCodec() {
    }

    /**
     * Checks that an input allele is compatible with a 2-bit base encoding. Will check that the string is either
     * empty or only contains the bases A, C, G or T.
     *
     * @param allele The input allele to be checked for compatibility with 2-bit base encoding.
     * @return true if the input string is empty or only contains the characters 'A', 'C','G' or 'T'.
     */
    public static boolean isJustACGT(String allele) {
        final int length = allele.length();
        if (length == 0) {
            // empty alleles are allowed for indels
            return true;
        }
        if (length == 1) {
            char c = allele.charAt(0);
            return isValidBaseChar(c);
        }
        for (int i = 0; i < length; i++) {
            char c = allele.charAt(i);
            if (!isValidBaseChar(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidBaseChar(char c) {
        return c < BASE_ENCODING_TABLE_SIZE && BASE_ENCODING_TABLE[c] != INVALID_BASE;
    }

    public static long encodeAllele(String allele) {
        int alleleLength = allele.length();
        // For very short alleles (common case), use unrolled loop for better performance
        return switch (alleleLength) {
            case 1 -> encodeBase(allele.charAt(0));
            case 2 -> ((long) encodeBase(allele.charAt(0)) << 2) |
                      (long) encodeBase(allele.charAt(1));
            case 3 -> ((long) encodeBase(allele.charAt(0)) << 4) |
                      ((long) encodeBase(allele.charAt(1)) << 2) |
                      encodeBase(allele.charAt(2));
            case 4 -> ((long) encodeBase(allele.charAt(0)) << 6) |
                      ((long) encodeBase(allele.charAt(1)) << 4) |
                      ((long) encodeBase(allele.charAt(2)) << 2) |
                      encodeBase(allele.charAt(3));
            default -> {
                long encoded = 0L;
                // Start shift: (length-1) * 2
                int shift = (alleleLength - 1) << 1;
                for (int i = 0; i < alleleLength; i++) {
                    byte enc = encodeBase(allele.charAt(i));
                    encoded |= ((long) enc << shift);
                    shift -= 2; // Decrease shift by 2 bits for each position
                }
                yield encoded;
            }
        };
    }

    private static byte encodeBase(char c) {
        // this is safe to use directly without bounds checking as it is only used in the toBits method after the allele
        // has already been checked to fit within the lookup table
        return BASE_ENCODING_TABLE[c];
    }

    private static final String[] CACHED_ALLELES_1 = {
            "A", "C", "G", "T"
    };
    private static final String[] CACHED_ALLELES_2 = {
            "AA", "AC", "AG", "AT",
            "CA", "CC", "CG", "CT",
            "GA", "GC", "GG", "GT",
            "TA", "TC", "TG", "TT"
    };
    private static final String[] CACHED_ALLELES_3 = {
            "AAA", "AAC", "AAG", "AAT", "ACA", "ACC", "ACG", "ACT", "AGA", "AGC", "AGG", "AGT", "ATA", "ATC", "ATG", "ATT",
            "CAA", "CAC", "CAG", "CAT", "CCA", "CCC", "CCG", "CCT", "CGA", "CGC", "CGG", "CGT", "CTA", "CTC", "CTG", "CTT",
            "GAA", "GAC", "GAG", "GAT", "GCA", "GCC", "GCG", "GCT", "GGA", "GGC", "GGG", "GGT", "GTA", "GTC", "GTG", "GTT",
            "TAA", "TAC", "TAG", "TAT", "TCA", "TCC", "TCG", "TCT", "TGA", "TGC", "TGG", "TGT", "TTA", "TTC", "TTG", "TTT"
    };

    private static final long BASE_MASK = 0b11L;

    public static String decodeAllele(int alleleLength, int offset, final long bits) {
        // optimal path - no string allocation, otherwise do the whole thing and allocate a new string :'(
        // A = 0 (0b00), C = 1 (0b01), G = 2 (0b10), T = 3 (0b11)
        //Allele size distribution (pfeiffer exome sample):
        //1: 71388, 2: 2192, 3: 555, 4: 398, 5: 259, 6: 90, 7: 73, 8: 35, 9: 32, 10: 30
        return switch (alleleLength) {
            case 0 -> "";
            case 1 -> CACHED_ALLELES_1[(int) ((bits >> offset) & BASE_MASK)];
            case 2 -> CACHED_ALLELES_2[(int) (((bits >> offset) & BASE_MASK) * 4) +
                                       (int) ((bits >> (offset - 2)) & BASE_MASK)];
            case 3 -> CACHED_ALLELES_3[(int) (((bits >> offset) & BASE_MASK) * 16) +
                                       (int) (((bits >> (offset - 2)) & BASE_MASK) * 4) +
                                       (int) ((bits >> (offset - 4)) & BASE_MASK)];
//             benchmarks showed there was no obvious performance to be gained from using cached alleles above 3 bases
            default -> newAlleleString(alleleLength, offset, bits);
        };
    }

    private static String newAlleleString(int alleleLength, int offset, final long bits) {
        byte[] bases = new byte[alleleLength];
        int shift = offset;
        for (int i = 0; i < alleleLength; i++) {
            bases[i] = decodeBase((byte) ((bits >> shift) & BASE_MASK));
            shift -= 2; // Each base takes 2 bits, so we decrement by 2 each time
        }
        return new String(bases, 0, alleleLength);
    }

    private static byte decodeBase(byte base) {
        return BASE_BYTES[base];
    }

    static long reverseComplementAllele(int alleleLength, long offset, final long bits) {
        if (alleleLength == 1) {
            // For single base, just complement (no reversal needed)
            return (~bits >> offset) & BASE_MASK;
        }
        long complementBits = ~bits;
        long revComp = 0L;
        final int maxShift = (alleleLength - 1) << 1;
        for (long i = 0, complementIndex = maxShift; i < alleleLength; i++, complementIndex -= 2) {
            // Extract complement base at position i
            long complementBase = (complementBits >> (offset + (i << 1))) & BASE_MASK;
            // Place it at reversed position (complementIndex)
            revComp |= complementBase << complementIndex;
        }
        return revComp;
    }
}
