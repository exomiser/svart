package org.monarchinitiative.variant.api;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public interface Breakend extends GenomicPosition {

    static Breakend unresolved() {
        return UnresolvedBreakend.instance();
    }

    /**
     * @return id corresponding to id of the record (e.g. VCF) this breakend was created from
     */
    String id();

    @Override
    CoordinateSystem coordinateSystem();

    @Override
    Breakend withCoordinateSystem(CoordinateSystem coordinateSystem);

    // override Stranded<T> methods from ChromosomalRegion in order to return the more specific Breakend type
    @Override
    Breakend withStrand(Strand strand);

    /**
     * Convert the breakend to opposite strand.
     */
    @Override
    default Breakend toOppositeStrand() {
        return withStrand(strand().opposite());
    }

    /**
     * @return <code>true</code> if the breakend is unresolved
     */
    default boolean isUnresolved() {
        return this.equals(unresolved());
    }
}
