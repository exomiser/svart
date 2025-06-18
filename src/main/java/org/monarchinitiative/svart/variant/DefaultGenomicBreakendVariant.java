package org.monarchinitiative.svart.variant;

import org.monarchinitiative.svart.BaseGenomicBreakendVariant;
import org.monarchinitiative.svart.GenomicBreakend;
import org.monarchinitiative.svart.GenomicVariant;

/**
 * Implementation of a structural variant that involves two different contigs.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public final class DefaultGenomicBreakendVariant extends BaseGenomicBreakendVariant<DefaultGenomicBreakendVariant> implements Comparable<GenomicVariant> {

    private DefaultGenomicBreakendVariant(String eventId, GenomicBreakend left, GenomicBreakend right, String ref, String alt) {
        super(eventId, left, right, ref, alt);
    }

    private DefaultGenomicBreakendVariant(Builder builder) {
        super(builder);
    }

    public static DefaultGenomicBreakendVariant of(String eventId, GenomicBreakend left, GenomicBreakend right, String ref, String alt) {
        return new DefaultGenomicBreakendVariant(eventId, left, right, ref, alt);
    }

    @Override
    protected DefaultGenomicBreakendVariant newBreakendVariantInstance(String eventId, GenomicBreakend left, GenomicBreakend right, String ref, String alt) {
        return new DefaultGenomicBreakendVariant(eventId, left, right, ref, alt);
    }

    @Override
    public int compareTo(GenomicVariant o) {
        return GenomicVariant.compare(this, o);
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

    public static class Builder extends BaseGenomicBreakendVariant.Builder<Builder> {

        @Override
        public DefaultGenomicBreakendVariant build() {
            return new DefaultGenomicBreakendVariant(self());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
