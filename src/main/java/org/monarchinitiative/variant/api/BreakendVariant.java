package org.monarchinitiative.variant.api;

import org.monarchinitiative.variant.api.impl.DefaultBreakendVariant;

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
        return Breakend.unresolved();
    }

    @Override
    BreakendVariant withStrand(Strand other);

    @Override
    BreakendVariant toOppositeStrand();

    @Override
    BreakendVariant withCoordinateSystem(CoordinateSystem coordinateSystem);

    static BreakendVariant of(String eventId, Breakend left, Breakend right, String ref, String alt) {
        return DefaultBreakendVariant.of(eventId, left, right, ref, alt);
    }
}
