package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.impl.ContigDefault;

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GenomicPositionTest {

    private final Contig ctg1 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, 10, "", "", "chr1");

    private GenomicPosition oneBased, zeroBased;

    @BeforeEach
    public void setUp() {
        zeroBased = new GenomicPositionDefault(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(7, ConfidenceInterval.of(-1, 2)));
        oneBased = new GenomicPositionDefault(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3, ConfidenceInterval.of(-2, 3)));
    }

    @Test
    public void properties() {
        assertThat(zeroBased.contig(), equalTo(ctg1));
        assertThat(zeroBased.contigId(), equalTo(1));
        assertThat(zeroBased.contigName(), equalTo("1"));
        assertThat(zeroBased.position(), equalTo(Position.of(7, ConfidenceInterval.of(-1, 2))));
        assertThat(zeroBased.pos(), equalTo(7));
        assertThat(zeroBased.ci(), equalTo(ConfidenceInterval.of(-1, 2)));
        assertThat(zeroBased.strand(), equalTo(Strand.POSITIVE));
        assertThat(zeroBased.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));
        assertThat(zeroBased.toString(), equalTo("[1:7(±1,2))+"));

        assertThat(oneBased.contig(), equalTo(ctg1));
        assertThat(oneBased.position(), equalTo(Position.of(3, ConfidenceInterval.of(-2, 3))));
        assertThat(oneBased.pos(), equalTo(3));
        assertThat(oneBased.ci(), equalTo(ConfidenceInterval.of(-2, 3)));
        assertThat(oneBased.strand(), equalTo(Strand.POSITIVE));
        assertThat(oneBased.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
        assertThat(oneBased.toString(), equalTo("(1:3(±2,3))+"));
    }

    @Test
    public void differenceToPosition() {
        assertThat(zeroBased.differenceTo(oneBased), equalTo(5));
        assertThat(oneBased.differenceTo(zeroBased), equalTo(-5));
    }

    @Test
    public void differenceToPositionWhenOnDifferentContig() {
        Contig ctg2 = new ContigDefault(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 20, "", "", "");
        GenomicPosition other = new GenomicPositionDefault(ctg2, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(2));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> zeroBased.differenceTo(other));
        assertThat(e.getMessage(), equalTo("Coordinates are on different chromosomes: 1 vs. 2"));
    }

    @Test
    public void differenceToRegion() {
        GenomicRegion region = GenomicRegionTest.ContigRegion.oneBased(ctg1, Strand.POSITIVE, Position.of(5), Position.of(6));

        assertThat(zeroBased.differenceTo(region), equalTo(1));
        assertThat(oneBased.differenceTo(region), equalTo(-2));
    }

    @Test
    public void differenceToRegionWhenOnDifferentContig() {
        Contig ctg2 = new ContigDefault(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 20, "", "", "");
        GenomicRegion other = GenomicRegionTest.ContigRegion.oneBased(ctg2, Strand.POSITIVE, Position.of(5), Position.of(6));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> zeroBased.differenceTo(other));
        assertThat(e.getMessage(), equalTo("Coordinates are on different chromosomes: 1 vs. 2"));
    }

    @Test
    public void toOppositeStrand() {
        GenomicPosition zeroNeg = zeroBased.withStrand(Strand.NEGATIVE);
        assertThat(zeroNeg.position(), equalTo(Position.of(3, ConfidenceInterval.of(-2, 1))));
        assertThat(zeroNeg.strand(), equalTo(Strand.NEGATIVE));
        assertThat(zeroNeg.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));

        GenomicPosition oneNeg = oneBased.toOppositeStrand();
        assertThat(oneNeg.position(), equalTo(Position.of(8, ConfidenceInterval.of(-3, 2))));
        assertThat(oneNeg.strand(), equalTo(Strand.NEGATIVE));
        assertThat(oneNeg.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }

    @Test
    public void compareToWhenDifferingContigs() {
        Contig ctg1 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, 10, "", "", "chr1");
        Contig ctg2 = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 10, "", "", "chr2");
        Contig ctg3 = Contig.of(3, "3", SequenceRole.ASSEMBLED_MOLECULE, 10, "", "", "chr3");

        GenomicPosition two = new GenomicPositionDefault(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3));
        GenomicPosition three = new GenomicPositionDefault(ctg2, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3));
        GenomicPosition four = new GenomicPositionDefault(ctg3, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3));

        assertThat(three.compareTo(two), equalTo(1));
        assertThat(three.compareTo(three), equalTo(0));
        assertThat(three.compareTo(four), equalTo(-1));
    }

    @Test
    public void compareToWhenDifferingPositions() {
        GenomicPosition two = new GenomicPositionDefault(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(2));
        GenomicPosition three = new GenomicPositionDefault(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3));
        GenomicPosition four = new GenomicPositionDefault(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(4));

        assertThat(three.compareTo(two), equalTo(1));
        assertThat(three.compareTo(three), equalTo(0));
        assertThat(three.compareTo(four), equalTo(-1));
    }

    @Test
    public void compareToWhenDifferingStrands() {
        // TODO: 25. 11. 2020 implement
    }

    static class GenomicPositionDefault implements GenomicPosition {

        private final Contig contig;
        private final Position position;
        private final CoordinateSystem coordinateSystem;
        private final Strand strand;

        private GenomicPositionDefault(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position position) {
            if (position.minPos() < 0) {
                throw new IllegalArgumentException("Cannot create genomic position " + position + "that extends beyond first contig base");
            }
            if (position.maxPos() > contig.length()) {
                throw new IllegalArgumentException("Cannot create genomic position " + position + "that extends beyond contig end " + contig.length());
            }

            this.contig = contig;
            this.position = position;
            this.coordinateSystem = coordinateSystem;
            this.strand = strand;
        }

        @Override
        public Contig contig() {
            return contig;
        }

        @Override
        public Position position() {
            return position;
        }

        @Override
        public CoordinateSystem coordinateSystem() {
            return coordinateSystem;
        }

        @Override
        public GenomicPositionDefault withCoordinateSystem(CoordinateSystem coordinateSystem) {
            if (this.coordinateSystem == coordinateSystem) {
                return this;
            }
            int startDelta = this.coordinateSystem.delta(coordinateSystem);
            return new GenomicPositionDefault(contig, strand, coordinateSystem, position().shiftPos(startDelta));
        }

        @Override
        public GenomicPositionDefault withStrand(Strand strand) {
            if (this.strand.hasComplement()) {
                if (this.strand == strand) {
                    return this;
                } else {
                    Position pos = coordinateSystem.isOneBased()
                            ? Position.of(contig.length() - pos() + 1, position.confidenceInterval().toOppositeStrand())
                            : Position.of(contig.length() - pos(), position.confidenceInterval().toOppositeStrand());
                    return new GenomicPositionDefault(contig, strand, coordinateSystem, pos);
                }
            }
            return this;
        }

        @Override
        public Strand strand() {
            return strand;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GenomicPositionDefault that = (GenomicPositionDefault) o;
            return Objects.equals(contig, that.contig) &&
                    Objects.equals(position, that.position) &&
                    coordinateSystem == that.coordinateSystem &&
                    strand == that.strand;
        }

        @Override
        public int hashCode() {
            return Objects.hash(contig, position, coordinateSystem, strand);
        }

        @Override
        public String toString() {
            return coordinateSystem.isOneBased()
                    ? '(' + contig.name() + ':' + position + ')' + strand
                    : '[' + contig.name() + ':' + position + ')' + strand;
        }
    }


}