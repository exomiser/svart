package org.monarchinitiative.svart;

/**
 * Represents interval start/end type.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public enum Bound {

    /**
     * Open endpoint does <em>not</em> include the coordinate.
     */
    OPEN,

    /**
     * Closed endpoint <em>includes</em> the coordinate.
     */
    CLOSED;

    public boolean isOpen() {
        return this == OPEN;
    }

    public boolean isClosed() {
        return this == CLOSED;
    }

}
