package org.monarchinitiative.svart;

import org.monarchinitiative.svart.impl.DefaultBreakendVariant;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface BreakendVariant extends Variant {

    String eventId();

    default String mateId() {
        return right().id();
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
        return withCoordinateSystem(CoordinateSystem.LEFT_OPEN);
    }

    @Override
    default BreakendVariant toOneBased() {
        return withCoordinateSystem(CoordinateSystem.FULLY_CLOSED);
    }

    static BreakendVariant of(String eventId, Breakend left, Breakend right, String ref, String alt) {
        return DefaultBreakendVariant.of(eventId, left, right, ref, alt);
    }
}
