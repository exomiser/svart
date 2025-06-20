package org.monarchinitiative.svart.variant;

/**
 * Package private utility class for cacheing single-base alleles.
 */
public final class AlleleCache {

    private static final String A = "A";
    private static final String T = "T";
    private static final String G = "G";
    private static final String C = "C";
    private static final String NO_CALL = ".";
    private static final String SPAN_DEL = "*";
    private static final String N = "N";

    private AlleleCache() {
    }

    /**
     * Returns a cached instance of an allele if possible, otherwise will return the original instance.
     *
     * @param allele an allele to attempt to cache
     * @return the cached or original instance of the input allele
     */
    public static String cacheAllele(String allele) {
        return allele.length() == 1 ? getCachedBase(allele) : allele;
    }

    private static String getCachedBase(String allele) {
        char base = allele.charAt(0);
        return switch (base) {
            case 'A' -> A;
            case 'T' -> T;
            case 'G' -> G;
            case 'C' -> C;
            case '.' -> NO_CALL;
            case '*' -> SPAN_DEL;
            case 'N' -> N;
            default -> allele;
        };
    }
}
