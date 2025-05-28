package org.monarchinitiative.svart.variantkey;

/**
 * Record representing a genetic variant with both its 64-bit encoded key and decoded components.
 */
public record VariantKey(long key, String chrom, int pos, String ref, String alt) implements Comparable<VariantKey> {

    /**
     * Creates a new VariantKey from string chromosome representation and variant details.
     *
     * @param chrom Chromosome name (e.g. "1", "X", "MT")
     * @param pos   Position on the chromosome (0-based)
     * @param ref   Reference allele
     * @param alt   Alternate allele
     * @return A new VariantKey instance
     */
    public static VariantKey of(String chrom, int pos, String ref, String alt) {
        long key = VariantKeys.encodeVariantKey(chrom, pos, ref, alt);
        return new VariantKey(key, chrom, pos, ref, alt);
    }

    /**
     * Creates a new VariantKey from numeric chromosome representation and variant details.
     *
     * @param chrom Chromosome number (1-25 for chr1-22, X, Y, MT)
     * @param pos   Position on the chromosome (0-based)
     * @param ref   Reference allele
     * @param alt   Alternate allele
     * @return A new VariantKey instance
     */
    public static VariantKey of(int chrom, int pos, String ref, String alt) {
        long key = VariantKeys.encodeVariantKey(chrom, pos, ref, alt);
        return new VariantKey(key, VariantKeys.decodeChrom(key), pos, ref, alt);
    }

    /**
     * Checks if this variant key uses reversible encoding.
     *
     * @return true if the variant key can be fully decoded back to its original values
     */
    public boolean isReversible() {
        return VariantKeys.isReversible(key);
    }

    /**
     * Note that the original C implementation uses unsigned longs and these need to be used for correct comparisons,
     * otherwise variants on chromosomes >= 16 will have negative values.
     * @param o the object to be compared.
     */
    @Override
    public int compareTo(VariantKey o) {
        return Long.compareUnsigned(key, o.key);
    }

    @Override
    public String toString() {
        return "VariantKey{" +
               "key=" + key +
               ", chrom='" + chrom + '\'' +
               ", pos=" + pos +
               ", ref='" + ref + '\'' +
               ", alt='" + alt + '\'' +
               '}';
    }
}
