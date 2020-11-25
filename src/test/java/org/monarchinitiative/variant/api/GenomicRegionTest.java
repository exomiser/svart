package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomicRegionTest {


    private final Contig chr1 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, 5, "", "", "chr1");
    private final Contig chr2 = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 10, "", "", "chr2");

    @Test
    public void oneBasedSingleBase() {
        GenomicRegion instance = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(1), Position.of(1));
        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(1));
        assertThat(instance.length(), equalTo(1));
        assertThat(instance.toOppositeStrand(), equalTo(ContigRegion.oneBased(chr1, Strand.NEGATIVE, Position.of(5), Position.of(5))));
        assertThat(instance.toOppositeStrand().toZeroBased(), equalTo(ContigRegion.zeroBased(chr1, Strand.NEGATIVE, Position.of(4), Position.of(5))));
    }

    @Test
    public void zeroBasedSingleBase() {
        GenomicRegion instance = ContigRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(0), Position.of(1));
        assertThat(instance.start(), equalTo(0));
        assertThat(instance.end(), equalTo(1));
        assertThat(instance.length(), equalTo(1));
        assertThat(instance.toOppositeStrand(), equalTo(ContigRegion.zeroBased(chr1, Strand.NEGATIVE, Position.of(4), Position.of(5))));
    }

    @Test
    public void oneBasedMultiBase() {
        GenomicRegion instance = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(1), Position.of(2));
        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(2));
        assertThat(instance.length(), equalTo(2));
        assertThat(instance.toOppositeStrand(), equalTo(ContigRegion.oneBased(chr1, Strand.NEGATIVE, Position.of(4), Position.of(5))));
    }

    @Test
    public void zeroBasedMultiBase() {
        GenomicRegion instance = ContigRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(0), Position.of(2));
        assertThat(instance.start(), equalTo(0));
        assertThat(instance.end(), equalTo(2));
        assertThat(instance.length(), equalTo(2));
        assertThat(instance.toOppositeStrand(), equalTo(ContigRegion.zeroBased(chr1, Strand.NEGATIVE, Position.of(3), Position.of(5))));
    }

    @Test
    public void flipStrandAndChangeCoordinateSystem() {
        GenomicRegion instance = ContigRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(0), Position.of(2));
        assertThat(instance.toOppositeStrand().toOneBased(), equalTo(ContigRegion.oneBased(chr1, Strand.NEGATIVE, Position.of(4), Position.of(5))));
    }

    @Test
    public void containsPosition() {
        GenomicPosition three = new GenomicPositionDefault(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3));
        GenomicPosition four = new GenomicPositionDefault(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(4));
        GenomicPosition five = new GenomicPositionDefault(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(5));
        GenomicPosition otherContig = new GenomicPositionDefault(chr2, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(4));

        // 0-based region
        GenomicRegion zeroBasedFour = ContigRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(3), Position.of(4));

        assertThat(zeroBasedFour.contains(three), equalTo(false));
        assertThat(zeroBasedFour.contains(four), equalTo(true));
        assertThat(zeroBasedFour.contains(five), equalTo(false));
        assertThat(zeroBasedFour.contains(otherContig), equalTo(false));

        // 1-based region
        GenomicRegion oneBasedFour = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(4), Position.of(4));

        assertThat(oneBasedFour.contains(three), equalTo(false));
        assertThat(oneBasedFour.contains(four), equalTo(true));
        assertThat(oneBasedFour.contains(five), equalTo(false));
        assertThat(oneBasedFour.contains(otherContig), equalTo(false));
    }

    @Test
    public void containsRegion() {
        GenomicRegion three = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(3), Position.of(3));
        GenomicRegion four = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(4), Position.of(4));
        GenomicRegion five = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(5), Position.of(5));
        GenomicRegion threeToFive = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(3), Position.of(5));
        GenomicRegion otherContig = ContigRegion.oneBased(chr2, Strand.POSITIVE, Position.of(4), Position.of(4));

        // 0-based region
        GenomicRegion zeroBasedFour = ContigRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(3), Position.of(4));

        assertThat(zeroBasedFour.contains(three), equalTo(false));
        assertThat(zeroBasedFour.contains(four), equalTo(true));
        assertThat(zeroBasedFour.contains(five), equalTo(false));
        assertThat(zeroBasedFour.contains(threeToFive), equalTo(false));
        assertThat(zeroBasedFour.contains(otherContig), equalTo(false));

        // 1-based region
        GenomicRegion oneBasedFour = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(4), Position.of(4));

        assertThat(oneBasedFour.contains(three), equalTo(false));
        assertThat(oneBasedFour.contains(four), equalTo(true));
        assertThat(oneBasedFour.contains(five), equalTo(false));
        assertThat(oneBasedFour.contains(threeToFive), equalTo(false));
        assertThat(oneBasedFour.contains(otherContig), equalTo(false));
    }

    @Test
    public void overlapsWith() {
        GenomicRegion three = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(3), Position.of(3));
        GenomicRegion four = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(4), Position.of(4));
        GenomicRegion five = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(5), Position.of(5));
        GenomicRegion threeToFive = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(3), Position.of(5));
        GenomicRegion otherContig = ContigRegion.oneBased(chr2, Strand.POSITIVE, Position.of(4), Position.of(4));

        // 0-based region
        GenomicRegion zeroBasedFour = ContigRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(3), Position.of(4));

        assertThat(zeroBasedFour.overlapsWith(three), equalTo(false));
        assertThat(zeroBasedFour.overlapsWith(four), equalTo(true));
        assertThat(zeroBasedFour.overlapsWith(five), equalTo(false));
        assertThat(zeroBasedFour.overlapsWith(threeToFive), equalTo(true));
        assertThat(zeroBasedFour.overlapsWith(otherContig), equalTo(false));


        // 1-based region
        GenomicRegion oneBasedFour = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(4), Position.of(4));

        assertThat(oneBasedFour.overlapsWith(three), equalTo(false));
        assertThat(oneBasedFour.overlapsWith(four), equalTo(true));
        assertThat(oneBasedFour.overlapsWith(five), equalTo(false));
        assertThat(oneBasedFour.overlapsWith(threeToFive), equalTo(true));
        assertThat(oneBasedFour.overlapsWith(otherContig), equalTo(false));
    }

    /**
     * 0- or 1-based region e.g. half-open [a,b) or fully closed [a,b] as indicated by the {@link CoordinateSystem}
     */
    static class ContigRegion implements GenomicRegion {

        private final Contig contig;
        private final Strand strand;
        private final CoordinateSystem coordinateSystem;
        private final Position startPosition;
        private final Position endPosition;

        private final int startZeroBased;

        private ContigRegion(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
            this.contig = Objects.requireNonNull(contig);
            this.strand = Objects.requireNonNull(strand);
            this.coordinateSystem = coordinateSystem;
            this.startPosition = Objects.requireNonNull(startPosition);
            this.endPosition = Objects.requireNonNull(endPosition);
            this.startZeroBased = coordinateSystem == CoordinateSystem.ZERO_BASED ? startPosition.pos() : startPosition.pos() - 1;
        }

        public static ContigRegion of(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
            return new ContigRegion(contig, strand, coordinateSystem, startPosition, endPosition);
        }

        public static ContigRegion oneBased(Contig contig, Strand strand, Position startPosition, Position endPosition) {
            return new ContigRegion(contig, strand, CoordinateSystem.ONE_BASED, startPosition, endPosition);
        }

        public static ContigRegion zeroBased(Contig contig, Strand strand, Position startPosition, Position endPosition) {
            return new ContigRegion(contig, strand, CoordinateSystem.ZERO_BASED, startPosition, endPosition);
        }


        @Override
        public Contig contig() {
            return contig;
        }


        @Override
        public int startZeroBased() {
            return startZeroBased;
        }

        @Override
        public CoordinateSystem coordinateSystem() {
            return coordinateSystem;
        }

        @Override
        public ContigRegion withCoordinateSystem(CoordinateSystem coordinateSystem) {
            if (this.coordinateSystem == coordinateSystem) {
                return this;
            }
            int startDelta = this.coordinateSystem.delta(coordinateSystem);
            return new ContigRegion(contig, strand, coordinateSystem, startPosition.shiftPos(startDelta), endPosition);
        }

        @Override
        public Position startPosition() {
            return startPosition;
        }

        @Override
        public Position endPosition() {
            return endPosition;
        }

        @Override
        public Strand strand() {
            return strand;
        }

        @Override
        public ContigRegion withStrand(Strand strand) {
            if (this.strand == strand) {
                return this;
            } else if (coordinateSystem.isOneBased()) {
                Position start = Position.of(contig.length() - start() + 1, startPosition.confidenceInterval().toOppositeStrand());
                Position end = Position.of(contig.length() - end() + 1, endPosition.confidenceInterval().toOppositeStrand());
                return new ContigRegion(contig, strand, coordinateSystem, end, start);
            }
            Position start = Position.of(contig.length() - start(), startPosition.confidenceInterval().toOppositeStrand());
            Position end = Position.of(contig.length() - end(), endPosition.confidenceInterval().toOppositeStrand());
            return new ContigRegion(contig, strand, coordinateSystem, end, start);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ContigRegion)) return false;
            ContigRegion that = (ContigRegion) o;
            return contig.equals(that.contig) &&
                    strand == that.strand &&
                    coordinateSystem == that.coordinateSystem &&
                    startPosition.equals(that.startPosition) &&
                    endPosition.equals(that.endPosition);
        }

        @Override
        public int hashCode() {
            return Objects.hash(contig, strand, coordinateSystem, startPosition, endPosition);
        }

        @Override
        public String toString() {
            return "ContigRegion{" +
                    "contig=" + contig.id() +
                    ", strand=" + strand +
                    ", coordinateSystem=" + coordinateSystem +
                    ", startPosition=" + startPosition +
                    ", endPosition=" + endPosition +
                    '}';
        }
    }
}