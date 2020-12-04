package org.monarchinitiative.variant.api;

/**
 * Represents interval start/end type.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public enum Endpoint {

    /**
     * Open endpoint does <em>not</em> include the coordinate.
     */
    OPEN,

    /**
     * Closed endpoint <em>includes</em> the coordinate.
     */
    CLOSED

}
