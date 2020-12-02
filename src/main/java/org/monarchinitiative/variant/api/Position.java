package org.monarchinitiative.variant.api;

/**
 * Position with a confidence level expressed using a {@link ConfidenceInterval}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public interface Position extends Comparable<Position> {

    /**
     * Creates a new {@link Position} using the pos argument with the same {@link ConfidenceInterval} around the point.
     *
     * @param pos numeric position for the new instance
     * @return a new Position instance with the same {@link ConfidenceInterval} as the instance it was derived from
     */
    Position withPos(int pos);

    /**
     * Creates a new {@link Position} using the delta argument to increment/decrement with pos with the same {@link ConfidenceInterval} around the resulting point.
     *
     * @param delta amount by which to shift the position. Positive inputs will increase the value of pos, negative decrease it.
     * @return a Position shifted by the provided delta
     */
    Position shift(int delta);

    /**
     * @return the numeric position
     */
    int pos();

    /**
     * @return confidence interval associated with the position
     */
    ConfidenceInterval confidenceInterval();

    int minPos();

    int maxPos();

    /**
     * @return true if this position is precise (CI = [0,0])
     */
    boolean isPrecise();

    Position toPrecise();

    /**
     * Inverts the current {@link Position} to the opposite end of the given {@link Contig} using the
     * {@link CoordinateSystem} provided.
     *
     * @param contig  {@link Contig} on which the position is located
     * @param coordinateSystem {@link CoordinateSystem} the Position is being used in
     * @return a new position at the opposite end of the {@link Contig} on which the current {@link Position} is located
     */
    Position invert(Contig contig, CoordinateSystem coordinateSystem);

    /**
     * Note: this class has a natural ordering that is inconsistent with equals.
     *
     * @param o other {@link Position} to compare with.
     * @return a natural ordering of positions.
     */
    @Override
    default int compareTo(Position o) {
        return compare(this, o);
    }

    /**
     * Note: this class has a natural ordering that is inconsistent with equals.
     *
     * @param x first {@link Position} to compare.
     * @param y second {@link Position} to compare.
     * @return a natural ordering of positions with more precise positions being ordered before less precise ones.
     */
    static int compare(Position x, Position y) {
        int result = Integer.compare(x.pos(), y.pos());
        if (result == 0) {
            result = ConfidenceInterval.compare(x.confidenceInterval(), y.confidenceInterval());
        }
        return result;
    }

    /**
     * Create precise position.
     *
     * @param pos position coordinate
     * @return precise position
     */
    static Position of(int pos) {
        return PrecisePosition.of(pos);
    }

    static Position of(int pos, int ciUpstream, int ciDownstream) {
        return of(pos, ConfidenceInterval.of(ciUpstream, ciDownstream));
    }

    /**
     * @param pos                The numeric position - this should be an unsigned integer.
     * @param confidenceInterval confidence interval around the given position
     * @return a {@link Position} with the specified confidenceInterval
     */
    static Position of(int pos, ConfidenceInterval confidenceInterval) {
        return confidenceInterval.isPrecise() ? PrecisePosition.of(pos) : ImprecisePosition.of(pos, confidenceInterval);
    }
}
