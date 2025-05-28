package org.monarchinitiative.svart.variantkey;

import org.monarchinitiative.svart.variant.TwoBitBaseCodec;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.monarchinitiative.svart.variant.TwoBitBaseCodec.encodeAllele;
import static org.monarchinitiative.svart.variant.TwoBitBaseCodec.isJustACGT;


/**
 * Implementation of the <a href="https://github.com/Genomicsplc/variantkey">VariantKeys</a> algorithm -
 * "<a href="https://doi.org/10.1101/473744">A Reversible Numerical Representation of Human Genetic Variants</a>"
 * <p>
 * This class provides methods to encode and decode <b>human</b> genetic variants into a compact 64-bit numeric
 * representation. The human limitation comes from the limitation of encoding the chromosome as an integer value in the
 * range of 0-25 and having an upper limit on the position of 268,435,456.
 * The encoding supports both reversible (for simple ACGT variants) and non-reversible (hashed, for complex variants)
 * formats.
 */
public final class VariantKeys {

    private static final long VKMASK_CHROM = 0xF800000000000000L; // VariantKeys binary mask for CHROM  0xF800_0000_0000_0000   [ 11111000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 ]
    private static final long VKMASK_POS = 0x07FFFFFF80000000L;   // VariantKeys binary mask for POS    0x07FF_FFFF_8000_0000   [ 00000111 11111111 11111111 11111111 10000000 00000000 00000000 00000000 ]
    private static final long VKSHIFT_CHROM = 59; // CHROM LSB position from the VariantKeys LSB
    private static final long VKSHIFT_POS = 31; //  POS LSB position from the VariantKeys LSB
    private static final long ALLELE_LEN_MASK = 0b1111;

    /**
     * Maximum allowed position value for a variant. 28 bits = 2^28 = 268,435,456. This is enough to contain the maximum
     * position on the largest human chromosome (GRCh37 NC_000001.10 chr1 = 249,250,621).
     */
    public static final int MAX_POS = 0x10000000;

    private VariantKeys() {
    }

    /**
     * Encodes variant information into a 64-bit numeric key.
     *
     * @param chrom     Chromosome name (e.g. "1", "X", "MT")
     * @param pos       Position on the chromosome (0-based)
     * @param reference Reference allele sequence
     * @param alternate Alternate allele sequence
     * @return 64-bit encoded variant key
     * @throws IllegalArgumentException if position exceeds maximum allowed value
     */
    public static long encodeVariantKey(String chrom, int pos, String reference, String alternate) {
        int chr = (int) encodeChrom(chrom);
        return encodeVariantKey(chr, pos, reference, alternate);
    }

    /**
     * Encodes variant information into a 64-bit numeric key using numeric chromosome representation.
     *
     * @param chrom     Chromosome number (1-25 for chr1-22, X, Y, MT)
     * @param pos       Position on the chromosome (0-based)
     * @param reference Reference allele sequence
     * @param alternate Alternate allele sequence
     * @return 64-bit encoded variant key
     * @throws IllegalArgumentException if chromosome number or position is invalid
     */
    public static long encodeVariantKey(int chrom, int pos, String reference, String alternate) {
        if (chrom < 0 || chrom > 25) {
            throw new IllegalArgumentException("Invalid chrom: " + chrom + " must be between 0 and 25");
        }
        if (pos > MAX_POS) {
            throw new IllegalArgumentException("Pos overflow! " + pos + " > " + MAX_POS);
        }
        return (long) chrom << VKSHIFT_CHROM | (long) pos << VKSHIFT_POS | encodeRefAlt(reference, alternate);
    }

    /**
     * Decodes a variant key back into its component parts.
     *
     * @param key an encoded variantkey
     * @return a {@link VariantKey} containing the decoded chromosome, position, reference, and alternate alleles.
     * For non-reversible keys, reference and alternate alleles will be empty strings.
     */
    public static VariantKey decodeVariantKey(long key) {
        String chrom = decodeChrom(key);
        int pos = (int) decodePos(key);
        if (isReversible(key)) {
            String ref = decodeRef(key);
            String alt = decodeAlt(key);
            return new VariantKey(key, chrom, pos, ref, alt);
        }
        // non-reversible encoding - the ref and alt cannot be decoded
        return new VariantKey(key, chrom, pos, "", "");
    }

    /**
     * Checks if a variant key uses reversible encoding.
     *
     * @param key the variant key to check
     * @return true if the key uses reversible encoding (last bit is 0)
     */
    public static boolean isReversible(long key) {
        return (key & 0x1) == 0;
    }

    /**
     * Converts a chromosome string representation to its numeric code.
     * Returns a numerical value in the range of 1-25 to encode chromosomes 1-22, X, Y and MT.
     * Unplaced contigs, scaffolds and patches are assigned the value 0.
     *
     * @param chrom The string name of the chromosome (e.g. "1", "chr1", "X", "chrX", "MT", "chrM")
     * @return numeric code (0-25) representing the chromosome
     */
    static long encodeChrom(String chrom) {
        String stripped = chrom.startsWith("chr") || chrom.startsWith("CHR") ? chrom.substring(3) : chrom;
        return switch (stripped) {
            case "1" -> 1;
            case "2" -> 2;
            case "3" -> 3;
            case "4" -> 4;
            case "5" -> 5;
            case "6" -> 6;
            case "7" -> 7;
            case "8" -> 8;
            case "9" -> 9;
            case "10" -> 10;
            case "11" -> 11;
            case "12" -> 12;
            case "13" -> 13;
            case "14" -> 14;
            case "15" -> 15;
            case "16" -> 16;
            case "17" -> 17;
            case "18" -> 18;
            case "19" -> 19;
            case "20" -> 20;
            case "21" -> 21;
            case "22" -> 22;
            case "X" -> 23;
            case "Y" -> 24;
            case "M", "MT" -> 25;
            // default is 0 in C implementation
            default -> throw new IllegalArgumentException("Invalid chrom: " + chrom + " Must be (chr)?[1-22,X,Y,M,MT]");
        };
    }

    /**
     * Encodes reference and alternate alleles into a numeric representation.
     * Uses reversible encoding for simple ACGT variants up to 11 bases combined length,
     * and non-reversible (hashed) encoding for more complex variants.
     *
     * @param ref Reference allele. String containing a sequence of nucleotide letters.
     *            The value in the pos field refers to the position of the first nucleotide in the String.
     *            Characters must be A-Z, a-z or *
     * @param alt Alternate non-reference allele string.
     *            Characters must be A-Z, a-z or *
     * @return encoded representation of reference and alternate alleles
     */
    static long encodeRefAlt(String ref, String alt) {
        if ((ref.length() + alt.length()) <= 11 && isJustACGT(ref) && isJustACGT(alt)) {
            // use reversible encoding
            return encodeRefAltReversible(ref, alt);
        }
        return encodeRefAltHashed(ref.getBytes(StandardCharsets.UTF_8), alt.getBytes(StandardCharsets.UTF_8));
    }

    private static long encodeRefAltReversible(String reference, String alternate) {
        long bits = 0L;

        long refLength = reference.length();
        bits |= refLength << 27;

        long altLength = alternate.length();
        bits |= altLength << 23;

        long refEncoded = encodeAllele(reference);
        bits |= refEncoded << (23 - (refLength << 1));

        long altEncoded = encodeAllele(alternate);
        bits |= altEncoded << (23 - ((refLength + altLength) << 1));

        return bits;
    }

    private static long encodeRefAltHashed(byte[] ref, byte[] alt) {
        // 0x3 is the separator character between REF and ALT [00000000 00000000 00000000 00000011]
        long h = muxHash(hash32(alt), muxHash(0x3, hash32(ref)));
        // MurmurHash3 finalization mix - force all bits of a hash block to avalanche
        // Ensure h is treated as unsigned 32-bit integer
        h ^= h >>> 16;
        h = (h * 0x85ebca6bL) & 0xFFFFFFFFL;
        h ^= h >>> 13;
        h = (h * 0xc2b2ae35L) & 0xFFFFFFFFL;
        h ^= h >>> 16;
        return (h >>> 1) | 0x1; // 0x1 is the set bit to indicate HASH mode [00000000 00000000 00000000 00000001]
    }

    // Mix two 32 bit hash numbers using a MurmurHash3-like algorithm
    private static long muxHash(long k, long h) {
        // Ensure k and h are treated as unsigned 32-bit integers
        k &= 0xFFFFFFFFL;
        h &= 0xFFFFFFFFL;

        k = (k * 0xcc9e2d51L) & 0xFFFFFFFFL;
        k = ((k >>> 17) | (k << 15)) & 0xFFFFFFFFL;
        k = (k * 0x1b873593L) & 0xFFFFFFFFL;
        h ^= k;
        h = ((h >>> 19) | (h << 13)) & 0xFFFFFFFFL;
        return ((h * 5L) & 0xFFFFFFFFL) + 0xe6546b64L & 0xFFFFFFFFL;
    }

    // Return a 32 bit hash of a nucleotide string
    private static long hash32(byte[] str) {
        long h = 0;
        int len = 6;
        while (str.length >= len) {
            h = muxHash(packChars(str), h);
            str = Arrays.copyOfRange(str, len, str.length);
        }
        if (str.length > 0) {
            h = muxHash(packCharsTail(str), h);
        }
        return h;
    }

    private static int packChars(byte[] str) {
        return ((encodePackchar(str[5]) << 1)
                ^ (encodePackchar(str[4]) << (1 + (5)))
                ^ (encodePackchar(str[3]) << (1 + (5 * 2)))
                ^ (encodePackchar(str[2]) << (1 + (5 * 3)))
                ^ (encodePackchar(str[1]) << (1 + (5 * 4)))
                ^ (encodePackchar(str[0]) << (1 + (5 * 5))));
    }

    // pack blocks of 6 characters in 32 bit (6 x 5 bit + 2 spare bit) [ 01111122 22233333 44444555 55666660 ]
    private static int packCharsTail(byte[] str) {
        int h = 0;
        if (str.length >= 5) {
            h ^= encodePackchar(str[4]) << (1 + (5));
        }
        if (str.length >= 4) {
            h ^= encodePackchar(str[3]) << (1 + (5 * 2));
        }
        if (str.length >= 3) {
            h ^= encodePackchar(str[2]) << (1 + (5 * 3));
        }
        if (str.length >= 2) {
            h ^= encodePackchar(str[1]) << (1 + (5 * 4));
        }
        if (str.length >= 1) {
            h ^= encodePackchar(str[0]) << (1 + (5 * 5));
        }
        return h;
    }

    private static int encodePackchar(byte c) {
        if (c < 'A') {
            return 27;
        }
        if (c >= 'a') {
            return (c - 'a' + 1);
        }
        return (c - 'A' + 1);
    }

    private static final String[] CHROMS = {"NA",
            "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15",
            "16", "17", "18", "19", "20",
            "21", "22", "X", "Y", "MT"};

    /**
     * Decodes the chromosome from a variant key to its string representation.
     *
     * @param key the variant key to decode
     * @return string representation of the chromosome (e.g. "1", "X", "MT", "NA" for unknown)
     */
    public static String decodeChrom(long key) {
        return CHROMS[decodeChromToInt(key)];
    }

    /**
     * Decodes the chromosome from a variant key to its numeric representation. Non-representable keys will return 0.
     *
     * @param key the variant key to decode
     * @return numeric chromosome code (0-25)
     */
    public static int decodeChromToInt(long key) {
        long chrBits = ((key & VKMASK_CHROM) >>> VKSHIFT_CHROM);
        return (int) ((chrBits >= 1 && chrBits <= 25) ? chrBits : 0L);
    }

    /**
     * Extracts the position from a variant key.
     *
     * @param variantKey the variant key to decode
     * @return the position on the chromosome
     */
    public static long decodePos(long variantKey) {
        return (variantKey & VKMASK_POS) >>> VKSHIFT_POS;
    }

    /**
     * Gets the length of the reference allele from a variant key.
     *
     * @param variantKey the variant key to analyze
     * @return length of the reference allele
     */
    public static int refLength(long variantKey) {
        return (int) (variantKey >>> 27 & ALLELE_LEN_MASK);
    }

    /**
     * Gets the length of the alternate allele from a variant key.
     *
     * @param variantKey the variant key to analyze
     * @return length of the alternate allele
     */
    public static int altLength(long variantKey) {
        return (int) (variantKey >>> 23 & ALLELE_LEN_MASK);
    }

    /**
     * Decodes the reference allele from a variant key.
     *
     * @param variantKey the variant key to decode
     * @return the reference allele sequence, or empty string if non-reversible encoding
     */
    public static String decodeRef(long variantKey) {
        return decodeAllele(refLength(variantKey), 21, variantKey);
    }

    /**
     * Decodes the alternate allele from a variant key.
     *
     * @param variantKey the variant key to decode
     * @return the alternate allele sequence, or empty string if non-reversible encoding
     */
    public static String decodeAlt(long variantKey) {
        return decodeAllele(altLength(variantKey), 21 - (refLength(variantKey) << 1), variantKey);
    }

    private static String decodeAllele(int alleleLength, int alleleOffset, long variantKey) {
        return isReversible(variantKey) ? TwoBitBaseCodec.decodeAllele(alleleLength, alleleOffset, variantKey) : "";
    }

}


