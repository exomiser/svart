package org.monarchinitiative.svart;

import org.monarchinitiative.svart.impl.DefaultGenomicBreakendVariant;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenomicBreakendVariant extends GenomicVariant {

    String eventId();

    default String mateId() {
        return right().id();
    }

    GenomicBreakend left();

    default GenomicBreakend right() {
        return GenomicBreakend.unresolved(coordinateSystem());
    }

    @Override
    GenomicBreakendVariant withStrand(Strand other);

    @Override
    GenomicBreakendVariant toOppositeStrand();

    @Override
    GenomicBreakendVariant withCoordinateSystem(CoordinateSystem coordinateSystem);

    @Override
    default GenomicBreakendVariant toZeroBased() {
        return withCoordinateSystem(CoordinateSystem.ZERO_BASED);
    }

    @Override
    default GenomicBreakendVariant toOneBased() {
        return withCoordinateSystem(CoordinateSystem.ONE_BASED);
    }

    static GenomicBreakendVariant of(String eventId, GenomicBreakend left, GenomicBreakend right, String ref, String alt) {
        return DefaultGenomicBreakendVariant.of(eventId, left, right, ref, alt);
    }
}
