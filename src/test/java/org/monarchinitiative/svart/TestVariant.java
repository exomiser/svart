package org.monarchinitiative.svart;

/**
 * Class for testing the builder-related functionality
 */
public final class TestVariant extends BaseVariant<TestVariant> {

    private TestVariant(Builder builder) {
        super(builder);
    }

    public TestVariant(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
        super(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    @Override
    protected TestVariant newVariantInstance(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength) {
        return new TestVariant(contig, id, strand, coordinates, ref, alt, changeLength);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseVariant.Builder<Builder> {

        @Override
        public TestVariant build() {
            return new TestVariant(selfWithEndIfMissing());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
