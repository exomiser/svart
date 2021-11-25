package org.monarchinitiative.svart.impl;

import org.monarchinitiative.svart.BaseBreakendVariant;
import org.monarchinitiative.svart.Breakend;

/**
 * Implementation of a structural variant that involves two different contigs.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public final class DefaultBreakendVariant extends BaseBreakendVariant<DefaultBreakendVariant> {

    private DefaultBreakendVariant(String eventId, Breakend left, Breakend right, String ref, String alt) {
        super(eventId, left, right, ref, alt);
    }

    private DefaultBreakendVariant(Builder builder) {
        super(builder);
    }

    public static DefaultBreakendVariant of(String eventId, Breakend left, Breakend right, String ref, String alt) {
        return new DefaultBreakendVariant(eventId, left, right, ref, alt);
    }

    @Override
    protected DefaultBreakendVariant newBreakendVariantInstance(String eventId, Breakend left, Breakend right, String ref, String alt) {
        return new DefaultBreakendVariant(eventId, left, right, ref, alt);
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseBreakendVariant.Builder<Builder> {

        @Override
        public DefaultBreakendVariant build() {
            return new DefaultBreakendVariant(self());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
