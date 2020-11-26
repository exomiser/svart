
package org.monarchinitiative.variant.api;

/**
 * Contains the 4 strand types from GFF3. This is a superset of BED which only defines [+, -, .]
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public enum Strand {
    UNSTRANDED("."), UNKNOWN("?"), POSITIVE("+"), NEGATIVE("-");

    private final String symbol;

    Strand(String symbol) {
        this.symbol = symbol;
    }

    public static Strand parseStrand(String value) {
        switch (value) {
            case "+":
                return POSITIVE;
            case "-":
                return NEGATIVE;
            case ".":
                return UNSTRANDED;
            case "?":
            default:
                return UNKNOWN;
        }
    }

    static int compare(Strand x, Strand y) {
        return x.compareTo(y);
    }

    public boolean isPositive() {
        return this == POSITIVE;
    }

    public boolean isNegative() {
        return this == NEGATIVE;
    }

    public boolean hasComplement() {
        switch (this) {
            case NEGATIVE:
            case POSITIVE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Find out if conversion from current strand to <code>target</code> strand is legal, as specified in
     * {@link Stranded#withStrand(Strand)}.
     *
     * @param target target strand
     * @return <code>true</code> if conversion from the current strand to <code>target</code> strand is legal
     */
    public boolean isConvertibleTo(Strand target) {
        if (this == target || this == UNKNOWN) {
            return false;
        }
        return hasComplement() || (this == UNSTRANDED && target == UNKNOWN);
    }

    public Strand opposite() {
        switch (this) {
            case POSITIVE:
                return NEGATIVE;
            case NEGATIVE:
                return POSITIVE;
            default:
                return this;
        }
    }

    @Override
    public String toString() {
        return symbol;
    }
}
