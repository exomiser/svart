package org.monarchinitiative.svart;

/**
 * Class for testing the builder-related functionality
 */
public final class TestGenomicVariant extends BaseGenomicVariant<TestGenomicVariant> {

    private TestGenomicVariant(Builder builder) {
        super(builder);
    }

    public TestGenomicVariant(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String mateId, String eventId) {
        super(contig, id, strand, coordinates, ref, alt, changeLength, mateId, eventId);
    }

    @Override
    protected TestGenomicVariant newVariantInstance(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String mateId, String eventId) {
        return new TestGenomicVariant(contig, id, strand, coordinates, ref, alt, changeLength, mateId, eventId);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseGenomicVariant.Builder<Builder> {

        @Override
        public TestGenomicVariant build() {
            return new TestGenomicVariant(selfWithEndIfMissing());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
