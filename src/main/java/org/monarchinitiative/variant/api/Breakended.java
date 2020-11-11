package org.monarchinitiative.variant.api;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface Breakended {

    default String getMateId() {
        return getRight().getId();
    }

    default String getEventId() {
        return "";
    }

    Breakend getLeft();

    default Breakend getRight() {
        return Breakend.unresolved();
    }
}
