
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

    public boolean isPositive() {
        return this == POSITIVE;
    }

    public boolean isNegative() {
        return this == NEGATIVE;
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

    public boolean hasComplement() {
        switch (this) {
            case NEGATIVE:
            case POSITIVE:
                return true;
            default:
                return false;
        }
    }

    public boolean isComplementOf(Strand other) {
        if (this == other) {
            return false;
        }
        return this == POSITIVE && other == NEGATIVE || this == NEGATIVE && other == POSITIVE;
    }

    public boolean notComplementOf(Strand other) {
        return !isComplementOf(other);
    }

    static int compare(Strand x, Strand y) {
        return x.compareTo(y);
    }

    @Override
    public String toString() {
        return symbol;
    }
}
