package org.monarchinitiative.svart;

import org.monarchinitiative.svart.variant.DefaultGenomicBreakend;

/**
 * A broken end of a {@link Contig} used as a component of a {@link GenomicBreakendVariant}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public interface GenomicBreakend extends GenomicRegion {

    static GenomicBreakend unresolved(CoordinateSystem coordinateSystem) {
        return UnresolvedGenomicBreakend.of(coordinateSystem);
    }

    static GenomicBreakend unresolved(CoordinateSystem coordinateSystem, String id) {
        return UnresolvedGenomicBreakend.of(coordinateSystem, id);
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
    GenomicBreakend withStrand(Strand other);

    @Override
    GenomicBreakend withCoordinateSystem(CoordinateSystem coordinateSystem);

    /**
     * Convert the breakend to opposite strand.
     */
    @Override
    default GenomicBreakend toOppositeStrand() {
        return withStrand(strand().opposite());
    }

    /**
     * @return <code>true</code> if the breakend is unresolved
     */
    default boolean isUnresolved() {
        return false;
    }

    static GenomicBreakend of(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, int start, int end) {
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, end);
        return DefaultGenomicBreakend.of(contig, id, strand, coordinates);
    }

    static GenomicBreakend of(Contig contig, String id, Strand strand, Coordinates coordinates) {
        return DefaultGenomicBreakend.of(contig, id, strand, coordinates);
    }
}
