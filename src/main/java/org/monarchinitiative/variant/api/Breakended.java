package org.monarchinitiative.variant.api;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface Breakended {

    Breakend left();

    default Breakend right() {
        return Breakend.unresolved();
    }

    default String mateId() {
        return right().id();
    }

    default String eventId() {
        return "";
    }
}
