package org.monarchinitiative.svart.variant;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.ConfidenceInterval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class AbstractGenomicVariantTest {

    private final TestContig contig = TestContig.of(1, 1000000);

    static class TestAbstractVariant extends AbstractGenomicVariant<TestAbstractVariant> {

        protected TestAbstractVariant(GenomicVariant genomicVariant) {
            super(genomicVariant);
        }

        public TestAbstractVariant(AbstractGenomicVariant.Builder<?> builder) {
            super(builder);
        }

        /**
         * @param genomicVariant 
         * @return
         */
        @Override
        protected TestAbstractVariant newVariantInstance(GenomicVariant genomicVariant) {
            return new TestAbstractVariant(genomicVariant);
        }


        static class Builder extends AbstractGenomicVariant.Builder<Builder> {

            public TestAbstractVariant build() {
                return new TestAbstractVariant(this);
            }

            @Override
            protected Builder self() {
                return this;
            }
        }
    }

    @Test
    void constructor() {
        TestAbstractVariant instance = new TestAbstractVariant(GenomicVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "A", "G"));
        assertThat(instance.genomicVariant(), instanceOf(CompactSequenceVariant.class));
    }

    @Test
    void builderWithoutVariant() {
        TestAbstractVariant instance = new TestAbstractVariant.Builder()
                .variant(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "A", "G")
                .build();
        assertThat(instance.genomicVariant(), instanceOf(CompactSequenceVariant.class));
        assertThat(instance.genomicVariant(), equalTo(GenomicVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "A", "G")));
    }

    @Test
    void builderWithVariant() {
        GenomicVariant variant = GenomicVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "A", "G");
        TestAbstractVariant instance = new TestAbstractVariant.Builder()
                .variant(variant)
                .build();
        assertThat(instance.genomicVariant(), instanceOf(CompactSequenceVariant.class));
        assertThat(instance.genomicVariant(), equalTo(variant));
    }

    @Test
    void contig() {
        GenomicVariant instance = new TestAbstractVariant(GenomicVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "A", "G"));
        assertThat(instance.contig(), equalTo(contig));
    }

    @Test
    void contigId() {
        GenomicVariant instance = new TestAbstractVariant(GenomicVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "A", "G"));
        assertThat(instance.contigId(), equalTo(contig.id()));
    }

    @Test
    void contigName() {
        GenomicVariant instance = new TestAbstractVariant(GenomicVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "A", "G"));
        assertThat(instance.contigName(), equalTo(contig.name()));
    }

    @Test
    void coordinates() {
        GenomicVariant instance = new TestAbstractVariant(GenomicVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "A", "G"));
        assertThat(instance.coordinates(), equalTo(Coordinates.of(CoordinateSystem.ONE_BASED, 12345, 12345)));
    }

    @Test
    void start() {
        GenomicVariant instance = new TestAbstractVariant(GenomicVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "A", "G"));
        assertThat(instance.start(), equalTo(12345));
        assertThat(instance.startZeroBased(), equalTo(12344));
        assertThat(instance.startStd(), equalTo(12344));
        assertThat(instance.startOnStrandWithCoordinateSystem(Strand.NEGATIVE, CoordinateSystem.ZERO_BASED), equalTo(987655));
    }

    @Test
    void end() {
        GenomicVariant instance = new TestAbstractVariant(GenomicVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "A", "G"));
        assertThat(instance.end(), equalTo(12345));
        assertThat(instance.endZeroBased(), equalTo(12345));
        assertThat(instance.endStd(), equalTo(12345));
        assertThat(instance.endOnStrandWithCoordinateSystem(Strand.NEGATIVE, CoordinateSystem.ZERO_BASED), equalTo(987656));
    }

    @Test
    void startConfidenceInterval() {
        GenomicVariant instance = new TestAbstractVariant(GenomicVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "A", "G"));
        assertThat(instance.startConfidenceInterval(), equalTo(ConfidenceInterval.precise()));
    }

    @Test
    void coordinateSystem() {
        GenomicVariant instance = new TestAbstractVariant(GenomicVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "A", "G"));
        GenomicVariant instanceZeroBased = new TestAbstractVariant(GenomicVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 12344, "A", "G"));
        assertThat(instance.withCoordinateSystem(CoordinateSystem.ONE_BASED), equalTo(instance));
        assertThat(instance.toOneBased(), equalTo(instance));
        assertThat(instance.withCoordinateSystem(CoordinateSystem.ZERO_BASED), equalTo(instanceZeroBased));
        assertThat(instance.toZeroBased(), equalTo(instanceZeroBased));
    }

    @Test
    void strand() {
        GenomicVariant instance = new TestAbstractVariant(GenomicVariant.of(contig, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 12345, "A", "G"));
        GenomicVariant instanceNegative = new TestAbstractVariant(GenomicVariant.of(contig, Strand.NEGATIVE, CoordinateSystem.ONE_BASED, 987656, "T", "C"));
        assertThat(instance.withStrand(Strand.POSITIVE), equalTo(instance));
        assertThat(instance.toPositiveStrand(), equalTo(instance));
        assertThat(instance.toOppositeStrand(), equalTo(instanceNegative));
        assertThat(instance.withStrand(Strand.NEGATIVE), equalTo(instanceNegative));
        assertThat(instance.toNegativeStrand(), equalTo(instanceNegative));
    }
}