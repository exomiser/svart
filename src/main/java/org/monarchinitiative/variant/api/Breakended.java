package org.monarchinitiative.variant.api;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface Breakended {

    default String mateId() {
        return right().id();
    }

    default String eventId() {
        return "";
    }

    Breakend left();

    default Breakend right() {
        return Breakend.unresolved();
    }
}
