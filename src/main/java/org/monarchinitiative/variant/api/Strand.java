
package org.monarchinitiative.variant.api;

/**
 * Contains the 4 strand types from GFF3. This is a superset of BED which only defines [+, -, .]
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public enum Strand {

    POSITIVE("+"), NEGATIVE("-");

    private final String symbol;

    Strand(String symbol) {
        this.symbol = symbol;
    }

    public static Strand parseStrand(String value) {
        switch (value) {
            case "-":
                return NEGATIVE;
            case "+":
            default:
                return POSITIVE;
        }
    }

    public boolean isPositive() {
        return this == POSITIVE;
    }

    public boolean isNegative() {
        return this == NEGATIVE;
    }

    public Strand opposite() {
        return this == POSITIVE ? NEGATIVE : POSITIVE;
    }

    static int compare(Strand x, Strand y) {
        return x.compareTo(y);
    }

    @Override
    public String toString() {
        return symbol;
    }
}
