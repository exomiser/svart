package org.monarchinitiative.variant.api;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface Breakended {

    String getMateId();

    default String getEventId() {
        return "";
    }

    Breakend getLeft();

    Breakend getRight();
}
