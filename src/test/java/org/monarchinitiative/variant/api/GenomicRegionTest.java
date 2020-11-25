package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

    @ParameterizedTest
    @CsvSource({
            "3,false",
            "4,true",
            "5,false",
            "3,false"})
    public void containsPosition(int pos, boolean expected) {
        GenomicPosition gp = GenomicPosition.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(pos));

        // 0-based region
        GenomicRegion zeroBasedFour = ContigRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(3), Position.of(4));
        assertThat(zeroBasedFour.contains(gp), equalTo(expected));

        // 1-based region
        GenomicRegion oneBasedFour = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(4), Position.of(4));
        assertThat(oneBasedFour.contains(gp), equalTo(expected));
    }

    @Test
    public void containsPositionOnDifferentContig() {
        GenomicPosition gp = GenomicPosition.of(chr2, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(4));

        GenomicRegion zeroBasedFour = ContigRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(3), Position.of(4));
        assertThat(zeroBasedFour.contains(gp), equalTo(false));

        GenomicRegion oneBasedFour = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(4), Position.of(4));
        assertThat(oneBasedFour.contains(gp), equalTo(false));
    }

    @ParameterizedTest
    @CsvSource({
            "3,3,false",
            "4,4,true",
            "5,5,false",
            "3,5,false"})
    public void containsRegion(int start, int end, boolean expected) {
        GenomicRegion gr = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(start), Position.of(end));

        // 0-based region
        GenomicRegion zeroBasedFour = ContigRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(3), Position.of(4));
        assertThat(zeroBasedFour.contains(gr), equalTo(expected));

        // 1-based region
        GenomicRegion oneBasedFour = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(4), Position.of(4));
        assertThat(oneBasedFour.contains(gr), equalTo(expected));
    }

    @Test
    public void containsRegionOnDifferentContig() {
        GenomicRegion gr = ContigRegion.oneBased(chr2, Strand.POSITIVE, Position.of(4), Position.of(4));

        GenomicRegion zeroBasedFour = ContigRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(3), Position.of(4));
        assertThat(zeroBasedFour.contains(gr), equalTo(false));

        GenomicRegion oneBasedFour = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(4), Position.of(4));
        assertThat(oneBasedFour.contains(gr), equalTo(false));
    }

    @ParameterizedTest
    @CsvSource({
            "3,3,false",
            "4,4,true",
            "5,5,false",
            "3,5,true"})
    public void overlapsWith(int start, int end, boolean expected) {
        GenomicRegion gr = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(start), Position.of(end));

        GenomicRegion zeroBasedFour = ContigRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(3), Position.of(4));
        assertThat(zeroBasedFour.overlapsWith(gr), equalTo(expected));

        GenomicRegion oneBasedFour = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(4), Position.of(4));
        assertThat(oneBasedFour.overlapsWith(gr), equalTo(expected));
    }

    @Test
    public void overlapsWithOnDifferentContig() {
        GenomicRegion gr = ContigRegion.oneBased(chr2, Strand.POSITIVE, Position.of(4), Position.of(4));

        GenomicRegion zeroBasedFour = ContigRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(3), Position.of(4));
        assertThat(zeroBasedFour.overlapsWith(gr), equalTo(false));

        GenomicRegion oneBasedFour = ContigRegion.oneBased(chr1, Strand.POSITIVE, Position.of(4), Position.of(4));
        assertThat(oneBasedFour.overlapsWith(gr), equalTo(false));
    }
}