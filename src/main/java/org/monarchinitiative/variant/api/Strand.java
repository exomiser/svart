
package org.monarchinitiative.variant.api;

/**
 * Contains the 4 strand types from GFF3.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public enum Strand {
    UNSTRANDED, UNKNOWN, POSITIVE, NEGATIVE;

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
}
