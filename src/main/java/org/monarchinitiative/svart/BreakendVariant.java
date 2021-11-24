package org.monarchinitiative.svart;

import org.monarchinitiative.svart.impl.DefaultBreakendVariant;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface BreakendVariant extends Variant {

    String eventId();

    default String mateId() {
        return right().mateId();
    }

    Breakend left();

    default Breakend right() {
        return Breakend.unresolved(coordinateSystem());
    }

    @Override
    BreakendVariant withStrand(Strand other);

    @Override
    BreakendVariant toOppositeStrand();

    @Override
    BreakendVariant withCoordinateSystem(CoordinateSystem coordinateSystem);

    @Override
    default BreakendVariant toZeroBased() {
        return withCoordinateSystem(CoordinateSystem.ZERO_BASED);
    }

    @Override
    default BreakendVariant toOneBased() {
        return withCoordinateSystem(CoordinateSystem.ONE_BASED);
    }

    static BreakendVariant of(String eventId, Breakend left, Breakend right, String ref, String alt) {
        return DefaultBreakendVariant.of(eventId, left, right, ref, alt);
    }
}
