package org.monarchinitiative.svart;

import org.monarchinitiative.svart.impl.DefaultBreakend;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public interface Breakend extends GenomicRegion {

    static Breakend unresolved(CoordinateSystem coordinateSystem) {
        return UnresolvedBreakend.instance(coordinateSystem);
    }

    /**
     * @return id corresponding to id of the record (e.g. VCF) this breakend was created from
     */
    String id();

    default String mateId() {
        return id();
    }

    // override Stranded<T> methods from ChromosomalRegion in order to return the more specific Breakend type
    @Override
    Breakend withStrand(Strand other);

    @Override
    Breakend withCoordinateSystem(CoordinateSystem coordinateSystem);

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
        return this.equals(unresolved(coordinateSystem()));
    }

    static Breakend of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position start, Position end) {
        return DefaultBreakend.of(contig, id, strand, coordinateSystem, start, end);
    }
}
