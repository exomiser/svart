package org.monarchinitiative.variant.api.impl;

import org.monarchinitiative.variant.api.*;

/**
 * Implementation of a structural variant that involves two different contigs.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public final class DefaultBreakendVariant extends BaseBreakendVariant<DefaultBreakendVariant> {

    public static DefaultBreakendVariant of(String eventId, Breakend left, Breakend right, String ref, String alt) {
        return new DefaultBreakendVariant(eventId, left, right, ref, "", alt);
    }

    private DefaultBreakendVariant(String eventId, Breakend left, Breakend right, String ref, String trailingRef, String alt) {
        super(eventId, left, right, ref, trailingRef, alt);
    }

    @Override
    protected DefaultBreakendVariant newBreakendVariantInstance(String eventId, Breakend left, Breakend right, String ref, String trailingRef, String alt) {
        return new DefaultBreakendVariant(eventId, left, right, ref, trailingRef, alt);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "BreakendVariant{" +
                "eventId='" + eventId() + '\'' +
                ", left=" + left() +
                ", right=" + right() +
                ", ref=" + ref() +
                ", alt='" + alt() + '\'' +
                '}';
    }
}
