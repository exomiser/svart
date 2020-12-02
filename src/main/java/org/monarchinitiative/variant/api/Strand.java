
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

    /**
     * Find out if conversion from current strand to <code>target</code> strand is legal, as specified in
     * {@link Stranded#withStrand(Strand)}.
     *
     * @param target target strand
     * @return <code>true</code> if conversion from the current strand to <code>target</code> strand is legal
     */
    public boolean conversionIsLegal(Strand target) {
        // TODO - Has this has been adequately tested where used in the BreakendVariant.withStrand
        //  logic in combination with Strand.needsConversion below?
        if (this == POSITIVE || this == NEGATIVE) {
            return true;
        }
        if (this == UNSTRANDED && (target == UNSTRANDED || target == UNKNOWN)){
            return true;
        }
        return this == UNKNOWN && target == UNKNOWN;
    }

    /**
     * Find out if coordinate change is required in order to convert strand from <code>this</code> to
     * <code>target</code> strand.
     * <p>
     * The difference between {@link #conversionIsLegal(Strand)} and this method is that when <code>this==target</code>
     * {@link #conversionIsLegal(Strand)}  returns <code>true</code> while this method returns <code>false</code>.
     *
     * @param target strand to convert to
     * @return <code>true</code> if coordinate change is required and the conversion is legal, as specified in {@link Stranded#withStrand(Strand)}
     */
    public boolean needsConversion(Strand target) {
        if (this == target) {
            return false;
        }
        if (this == POSITIVE || this == NEGATIVE) {
            return true;
        }
        return this == UNSTRANDED && target == UNKNOWN;
    }

    static int compare(Strand x, Strand y) {
        return x.compareTo(y);
    }

    @Override
    public String toString() {
        return symbol;
    }
}
