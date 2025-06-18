
package org.monarchinitiative.svart;

/**
 * Class representing the VCF confidence interval:
 * <p>
 * ##INFO=<ID=CIPOS,Number=2,Type=Integer,Description="Confidence interval around POS for imprecise variants">
 * ##INFO=<ID=CIEND,Number=2,Type=Integer,Description="Confidence interval around END for imprecise variants">
 * <p>
 * Although I can't find a formal definition stating this, the examples always show first integer as negative or zero and
 * the second positive or zero.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public record ConfidenceInterval(int lowerBound, int upperBound) implements Comparable<ConfidenceInterval> {

    private static final ConfidenceInterval PRECISE = new ConfidenceInterval(0, 0);

    public ConfidenceInterval {
        if (lowerBound > 0 || upperBound < 0) {
            throw new IllegalArgumentException("'" + lowerBound + ", " + upperBound + "' ConfidenceInterval must have negative lowerBound and positive upperBound");
        }
    }

    public static ConfidenceInterval of(int lowerBound, int upperBound) {
        if (lowerBound == 0 && upperBound == 0) {
            return PRECISE;
        }
        return new ConfidenceInterval(lowerBound, upperBound);
    }

    public static ConfidenceInterval precise() {
        return PRECISE;
    }

    public int minPos(int pos) {
        return pos + lowerBound;
    }

    public int maxPos(int pos) {
        return pos + upperBound;
    }

    public boolean isPrecise() {
        return this.equals(PRECISE);
    }

    /**
     * @return length of the confidence interval, precise CI has length <code>0</code>
     */
    public int length() {
        return Math.abs(lowerBound) + upperBound;
    }

    public ConfidenceInterval invert() {
        return isPrecise() ? PRECISE : new ConfidenceInterval(Math.negateExact(upperBound), Math.abs(lowerBound));
    }

    /**
     * Shorter confidence interval is better.
     *
     * @param o confidence interval to compare with
     * @return comparison result as specified in {@link Comparable}
     */
    @Override
    public int compareTo(ConfidenceInterval o) {
        return compare(this, o);
    }

    public static int compare(ConfidenceInterval x, ConfidenceInterval y) {
        return Integer.compare(x.length(), y.length());
    }

    @Override
    public String toString() {
        return isPrecise() ? "" : "(-" + -lowerBound + ", +" + upperBound + ')';
    }
}
