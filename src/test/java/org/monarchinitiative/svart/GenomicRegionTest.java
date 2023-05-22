package org.monarchinitiative.svart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomicRegionTest {


    private final Contig chr1 = TestContig.of(1, 5);

    @ParameterizedTest
    @CsvSource({
            // given a coordinate on a contig of length 5
            "ONE_BASED,   5,  6",
            "ONE_BASED,   6,  6",

            "ZERO_BASED,  5,  6",
            "ZERO_BASED,  6,  6",
    })
    public void coordinatesOutOfBounds(CoordinateSystem coordinateSystem, int start, int end) {
        Contig contig = TestContig.of(1, 5);
        Coordinates coordinates = Coordinates.of(coordinateSystem, start, end);
        Exception exception = assertThrows(CoordinatesOutOfBoundsException.class, () -> GenomicRegion.of(contig, Strand.POSITIVE, coordinates));
        assertThat(exception.getMessage(), containsString("coordinates " + contig.name() + ':' + start + '-' + end + " out of contig bounds"));
    }

    @Test
    public void oneBasedSingleBase() {
        GenomicRegion instance = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1);
        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(1));
        assertThat(instance.length(), equalTo(1));
        assertThat(instance, equalTo(GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1)));
        assertThat(instance.toOppositeStrand(), equalTo(GenomicRegion.of(chr1, Strand.NEGATIVE, CoordinateSystem.ONE_BASED, 5, 5)));
        assertThat(instance.toOppositeStrand().toZeroBased(), equalTo(GenomicRegion.of(chr1, Strand.NEGATIVE, CoordinateSystem.ZERO_BASED, 4, 5)));
    }

    @Test
    public void zeroBasedSingleBase() {
        GenomicRegion instance = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, 1);
        assertThat(instance.start(), equalTo(0));
        assertThat(instance.end(), equalTo(1));
        assertThat(instance.length(), equalTo(1));
        assertThat(instance.toOppositeStrand(), equalTo(GenomicRegion.of(chr1, Strand.NEGATIVE, CoordinateSystem.ZERO_BASED, 4, 5)));
    }

    @Test
    public void oneBasedMultiBase() {
        GenomicRegion instance = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 2);
        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(2));
        assertThat(instance.length(), equalTo(2));
        assertThat(instance.toOppositeStrand(), equalTo(GenomicRegion.of(chr1, Strand.NEGATIVE, CoordinateSystem.ONE_BASED, 4, 5)));
    }

    @Test
    public void zeroBasedMultiBase() {
        GenomicRegion instance = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, 2);
        assertThat(instance.start(), equalTo(0));
        assertThat(instance.end(), equalTo(2));
        assertThat(instance.length(), equalTo(2));
        assertThat(instance.toOppositeStrand(), equalTo(GenomicRegion.of(chr1, Strand.NEGATIVE, CoordinateSystem.ZERO_BASED, 3, 5)));
    }

    @Test
    public void flipStrandAndChangeCoordinateSystem() {
        GenomicRegion instance = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 0, 2);
        assertThat(instance.toOppositeStrand().toOneBased(), equalTo(GenomicRegion.of(chr1, Strand.NEGATIVE, CoordinateSystem.ONE_BASED, 4, 5)));
    }

    @ParameterizedTest
    @CsvSource({
            // region            pos
            // region before pos 2
            "ZERO_BASED, 0, 1,   2,   false",
            "ONE_BASED,  1, 1,   2,   false",
            // region containing pos 2
            "ZERO_BASED, 1, 2,   2,   true",
            "ONE_BASED,  2, 2,   2,   true",
            // region after pos 2
            "ZERO_BASED, 2, 3,   2,   false",
            "ONE_BASED,  3, 3,   2,   false",
    })
    public void containsPosition(CoordinateSystem coordinateSystem, int start, int end,
                                 int position,
                                 boolean expected) {
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, start, end);
        assertThat(region.contains(position), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            // this              other               expected
            "POSITIVE, ZERO_BASED, 1, 2,   POSITIVE, ZERO_BASED, 0, 1,   false",
            "POSITIVE, ZERO_BASED, 1, 2,   POSITIVE, ZERO_BASED, 1, 2,   true",
            "POSITIVE, ZERO_BASED, 1, 2,   POSITIVE, ZERO_BASED, 2, 3,   false",

            "POSITIVE, ZERO_BASED, 1, 3,   POSITIVE, ZERO_BASED, 0, 1,   false",
            "POSITIVE, ZERO_BASED, 1, 3,   POSITIVE, ZERO_BASED, 1, 2,   true",
            "POSITIVE, ZERO_BASED, 1, 3,   POSITIVE, ZERO_BASED, 2, 3,   true",
            "POSITIVE, ZERO_BASED, 1, 3,   POSITIVE, ZERO_BASED, 3, 4,   false",

            // --------------------------------------------

            "POSITIVE, ZERO_BASED, 2, 3,   POSITIVE, ONE_BASED,  2, 3,   true",
            "POSITIVE, ZERO_BASED, 2, 3,   POSITIVE, ONE_BASED,  3, 3,   true",
            "POSITIVE, ZERO_BASED, 2, 3,   POSITIVE, ONE_BASED,  3, 4,   true",

            //  ZERO_BASED, POS:     0 1 2 3 4
            //  ONE_BASED, POS:  1 2 3 4 5
            "POSITIVE, ZERO_BASED, 2, 4,   POSITIVE, ONE_BASED,  2, 3,   true",
            "POSITIVE, ZERO_BASED, 2, 4,   POSITIVE, ONE_BASED,  3, 4,   true",
            "POSITIVE, ZERO_BASED, 2, 4,   POSITIVE, ONE_BASED,  4, 4,   true",
            "POSITIVE, ZERO_BASED, 2, 4,   POSITIVE, ONE_BASED,  4, 5,   true",

            // -----------------------------------------------
            //    POS -> 0 1 2 3 4 5
            //    NEG <- 5 4 3 2 1 0
            "POSITIVE, ZERO_BASED, 1, 2,   NEGATIVE, ZERO_BASED, 0, 1,   false",
            "POSITIVE, ZERO_BASED, 0, 5,   NEGATIVE, ZERO_BASED, 0, 5,   true",
            "POSITIVE, ZERO_BASED, 1, 5,   NEGATIVE, ZERO_BASED, 1, 5,   true",
            "POSITIVE, ZERO_BASED, 2, 5,   NEGATIVE, ZERO_BASED, 2, 5,   true",
            "POSITIVE, ZERO_BASED, 3, 5,   NEGATIVE, ZERO_BASED, 3, 5,   false",
            "POSITIVE, ZERO_BASED, 2, 3,   NEGATIVE, ZERO_BASED, 2, 3,   true",
            "POSITIVE, ZERO_BASED, 3, 4,   NEGATIVE, ZERO_BASED, 3, 4,   false",
            "POSITIVE, ZERO_BASED, 1, 3,   NEGATIVE, ZERO_BASED, 2, 4,   true",
    })
    public void GenomigRegionOverlapsOther(Strand thisStrand, CoordinateSystem thisCoordinateSystem, int thisStart, int thisEnd,
                                           Strand otherStrand, CoordinateSystem otherCoordinateSystem, int otherStart, int otherEnd,
                                           boolean expected) {
        GenomicRegion region = GenomicRegion.of(chr1, thisStrand, thisCoordinateSystem, thisStart, thisEnd);
        GenomicRegion other = GenomicRegion.of(chr1, otherStrand, otherCoordinateSystem, otherStart, otherEnd);

        assertThat(region.overlapsWith(other), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            // this              other               expected
            "POSITIVE, ZERO_BASED, 1, 2,   POSITIVE, ZERO_BASED, 0, 1,   false",
            "POSITIVE, ZERO_BASED, 1, 2,   POSITIVE, ZERO_BASED, 1, 2,   true",
            "POSITIVE, ZERO_BASED, 1, 2,   POSITIVE, ZERO_BASED, 2, 3,   false",

            "POSITIVE, ZERO_BASED, 1, 3,   POSITIVE, ZERO_BASED, 0, 1,   false",
            "POSITIVE, ZERO_BASED, 1, 3,   POSITIVE, ZERO_BASED, 1, 2,   true",
            "POSITIVE, ZERO_BASED, 1, 3,   POSITIVE, ZERO_BASED, 2, 3,   true",
            "POSITIVE, ZERO_BASED, 1, 3,   POSITIVE, ZERO_BASED, 3, 4,   false",

            // --------------------------------------------

            "POSITIVE, ZERO_BASED, 2, 3,   POSITIVE, ONE_BASED,  2, 3,   false",
            "POSITIVE, ZERO_BASED, 2, 3,   POSITIVE, ONE_BASED,  3, 3,   true",
            "POSITIVE, ZERO_BASED, 2, 3,   POSITIVE, ONE_BASED,  3, 4,   false",

            "POSITIVE, ZERO_BASED, 2, 4,   POSITIVE, ONE_BASED,  2, 3,   false",
            "POSITIVE, ZERO_BASED, 2, 4,   POSITIVE, ONE_BASED,  3, 4,   true",
            "POSITIVE, ZERO_BASED, 2, 4,   POSITIVE, ONE_BASED,  4, 4,   true",
            "POSITIVE, ZERO_BASED, 2, 4,   POSITIVE, ONE_BASED,  4, 5,   false",

            // -----------------------------------------------
            //    POS -> 0 1 2 3 4 5
            //    NEG <- 5 4 3 2 1 0
            "POSITIVE, ZERO_BASED, 1, 2,   NEGATIVE, ZERO_BASED, 0, 1,   false",
            "POSITIVE, ZERO_BASED, 0, 5,   NEGATIVE, ZERO_BASED, 0, 5,   true",
            "POSITIVE, ZERO_BASED, 1, 5,   NEGATIVE, ZERO_BASED, 1, 5,   false",
            "POSITIVE, ZERO_BASED, 2, 5,   NEGATIVE, ZERO_BASED, 2, 5,   false",
            "POSITIVE, ZERO_BASED, 2, 3,   NEGATIVE, ZERO_BASED, 2, 3,   true",
            "POSITIVE, ZERO_BASED, 3, 4,   NEGATIVE, ZERO_BASED, 3, 4,   false",
            "POSITIVE, ZERO_BASED, 1, 3,   NEGATIVE, ZERO_BASED, 2, 4,   true",
    })
    public void zeroBasedRegionContainsRegion(Strand thisStrand, CoordinateSystem thisCoordinateSystem, int thisStart, int thisEnd,
                                              Strand otherStrand, CoordinateSystem otherCoordinateSystem, int otherStart, int otherEnd,
                                              boolean expected) {
        GenomicRegion region = GenomicRegion.of(chr1, thisStrand, thisCoordinateSystem, thisStart, thisEnd);
        GenomicRegion other = GenomicRegion.of(chr1, otherStrand, otherCoordinateSystem, otherStart, otherEnd);

        assertThat(region.contains(other), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            // this              other               expected
            "ONE_BASED, 1, 1,   ZERO_BASED, 0, 1,   true",
            "ONE_BASED, 1, 1,   ZERO_BASED, 1, 2,   false",

            "ONE_BASED, 2, 3,   ZERO_BASED, 0, 1,   false",
            "ONE_BASED, 2, 3,   ZERO_BASED, 1, 2,   true",
            "ONE_BASED, 2, 3,   ZERO_BASED, 2, 3,   true",
            "ONE_BASED, 2, 3,   ZERO_BASED, 3, 4,   false",

            "ONE_BASED, 2, 4,   ZERO_BASED, 0, 1,   false",
            "ONE_BASED, 2, 4,   ZERO_BASED, 1, 2,   true",
            "ONE_BASED, 2, 4,   ZERO_BASED, 2, 3,   true",
            "ONE_BASED, 2, 4,   ZERO_BASED, 3, 4,   true",
            "ONE_BASED, 2, 4,   ZERO_BASED, 4, 5,   false",
            // --------------------------------------------
            "ONE_BASED, 2, 2,   ONE_BASED,  1, 1,   false",
            "ONE_BASED, 2, 2,   ONE_BASED,  2, 2,   true",
            "ONE_BASED, 2, 2,   ONE_BASED,  3, 3,   false",

            "ONE_BASED, 2, 3,   ONE_BASED,  1, 2,   false",
            "ONE_BASED, 2, 3,   ONE_BASED,  2, 2,   true",
            "ONE_BASED, 2, 3,   ONE_BASED,  2, 3,   true",
            "ONE_BASED, 2, 3,   ONE_BASED,  3, 3,   true",
            "ONE_BASED, 2, 3,   ONE_BASED,  3, 4,   false",

            "ONE_BASED, 2, 4,   ONE_BASED,  1, 3,   false",
            "ONE_BASED, 2, 4,   ONE_BASED,  2, 3,   true",
            "ONE_BASED, 2, 4,   ONE_BASED,  2, 4,   true",
            "ONE_BASED, 2, 4,   ONE_BASED,  3, 4,   true",
            "ONE_BASED, 2, 4,   ONE_BASED,  3, 5,   false",
    })
    public void oneBasedRegionContainsRegion(CoordinateSystem thisCoordinateSystem, int thisStart, int thisEnd,
                                             CoordinateSystem otherCoordinateSystem, int otherStart, int otherEnd,
                                             boolean expected) {
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, thisCoordinateSystem, thisStart, thisEnd);
        GenomicRegion other = GenomicRegion.of(chr1, Strand.POSITIVE, otherCoordinateSystem, otherStart, otherEnd);

        assertThat(region.contains(other), equalTo(expected));
    }

    @Test
    public void containsRegion_otherContig() {
        GenomicRegion oneToThree = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 3);
        Contig ctg2 = TestContig.of(2, 200);
        GenomicRegion other = GenomicRegion.of(ctg2, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 2, 3);
        assertThat(oneToThree.contains(other), equalTo(false));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, 4, 5,   POSITIVE, 1,  2,  -1",
            "POSITIVE, 4, 5,   NEGATIVE, 9, 10,  -1",
            "POSITIVE, 4, 5,   POSITIVE, 2,  3,   0",
            "POSITIVE, 4, 5,   NEGATIVE, 8,  9,   0",
            "POSITIVE, 4, 5,   POSITIVE, 3,  4,   0",
            "POSITIVE, 4, 5,   NEGATIVE, 7,  8,   0",
            "POSITIVE, 4, 5,   POSITIVE, 6,  7,   0",
            "POSITIVE, 4, 5,   NEGATIVE, 4,  5,   0",
            "POSITIVE, 4, 5,   POSITIVE, 7,  8,   1",
            "POSITIVE, 4, 5,   NEGATIVE, 3,  4,   1",
    })
    public void distanceTo(Strand xStrand, int xStart, int xEnd,
                           Strand yStrand, int yStart, int yEnd,
                           int expected) {
        Contig ctg1 = TestContig.of(1, 10);
        GenomicRegion x = GenomicRegion.of(ctg1, xStrand, CoordinateSystem.oneBased(), xStart, xEnd);
        GenomicRegion y = GenomicRegion.of(ctg1, yStrand, CoordinateSystem.oneBased(), yStart, yEnd);
        assertThat(x.distanceTo(y), equalTo(expected));
    }

    @Test
    public void distanceTo_otherContig() {
        GenomicRegion x = GenomicRegion.of(TestContig.of(1, 10), Strand.POSITIVE, CoordinateSystem.oneBased(), 1, 2);
        GenomicRegion y = GenomicRegion.of(TestContig.of(2, 10), Strand.POSITIVE, CoordinateSystem.oneBased(), 1, 2);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> x.distanceTo(y));
        assertThat(e.getMessage(), equalTo("Cannot calculate distance between regions on different contigs: 1 <-> 2"));

        IllegalArgumentException f = assertThrows(IllegalArgumentException.class, () -> y.distanceTo(x));
        assertThat(f.getMessage(), equalTo("Cannot calculate distance between regions on different contigs: 2 <-> 1"));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, ZERO_BASED, 0, 0, 5, 5",
            "NEGATIVE, ZERO_BASED, 5, 5, 0, 0",
            "POSITIVE, ONE_BASED, 1, 0, 6, 5",
            "POSITIVE, ONE_BASED, 6, 5, 1, 0",
    })
    public void withStrand_emptyRegion(Strand strand, CoordinateSystem coordinateSystem, int start, int end, int expectedStart, int expectedEnd) {
        GenomicRegion region = GenomicRegion.of(chr1, strand, coordinateSystem, start, end);
        assertThat(region.length(), equalTo(0));
        GenomicRegion opposite = GenomicRegion.of(chr1, strand.opposite(), coordinateSystem, expectedStart, expectedEnd);
        assertThat(region.toOppositeStrand(), equalTo(opposite));
    }

    @ParameterizedTest
    @CsvSource({
            // we test 2-bp-long region that spans the bases 3-4 of the imaginary contig with length 5
            // source     start      end       target   start  end
            "POSITIVE,    ZERO_BASED, 2,  4,   NEGATIVE,    1, 3",
            "POSITIVE,    ONE_BASED,  3,  4,   NEGATIVE,    2, 3",
            "NEGATIVE,    ZERO_BASED, 1,  3,   POSITIVE,    2, 4",
            "NEGATIVE,    ONE_BASED,  2,  3,   POSITIVE,    3, 4",

            // converting to the same strand preserves the coordinates
            "POSITIVE,    ZERO_BASED, 2,  4,   POSITIVE,    2, 4",
            "POSITIVE,    ONE_BASED,  3,  4,   POSITIVE,    3, 4",
            "NEGATIVE,    ZERO_BASED, 1,  3,   NEGATIVE,    1, 3",
            "NEGATIVE,    ONE_BASED,  2,  3,   NEGATIVE,    2, 3",
    })
    public void withStrand_strandConversions(Strand source, CoordinateSystem coordinateSystem, int start, int end,
                                             Strand target,
                                             int exptStart, int exptEnd) {
        GenomicRegion initial = GenomicRegion.of(chr1, source, coordinateSystem, start, end);

        GenomicRegion actual = initial.withStrand(target);
        assertThat(actual.strand(), equalTo(target));
        assertThat(actual.start(), equalTo(exptStart));
        assertThat(actual.end(), equalTo(exptEnd));
        assertThat(actual.coordinateSystem(), equalTo(initial.coordinateSystem()));
    }

    @ParameterizedTest
    @CsvSource({
            "ZERO_BASED, 2, 3,   0,   2, 3",
            "ONE_BASED,  2, 3,   0,   2, 3",
            "ZERO_BASED, 2, 3,   1,   1, 4",
            "ONE_BASED,  2, 3,   1,   1, 4",
            "ZERO_BASED, 2, 3,   2,   0, 5",
    })
    public void withPadding_singlePadding(CoordinateSystem coordinateSystem,
                                          int start, int end,
                                          int padding,
                                          int expectedStart, int expectedEnd) {
        GenomicRegion actual = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, start, end).withPadding(padding);
        GenomicRegion expected = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, expectedStart, expectedEnd);
        assertThat(actual, equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "ZERO_BASED, 2, 3,   0, 0,   2, 3",
            "ONE_BASED,  2, 3,   0, 0,   2, 3",
            "ZERO_BASED, 2, 3,   1, 2,   1, 5",
            "ONE_BASED,  2, 3,   1, 2,   1, 5",
            "ZERO_BASED, 2, 3,   2, 0,   0, 3",
            "ONE_BASED,  2, 3,   1, 0,   1, 3",
    })
    public void testWithPadding_upDownPadding(CoordinateSystem coordinateSystem,
                                              int start, int end,
                                              int upPadding, int downPadding,
                                              int expectedStart, int expectedEnd) {
        GenomicRegion actual = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, start, end).withPadding(upPadding, downPadding);
        GenomicRegion expected = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, expectedStart, expectedEnd);
        assertThat(actual, equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED,   2, 3,  ONE_BASED,   2",
            "ONE_BASED,   2, 3,     ZERO_BASED,   1",
            "ZERO_BASED,      2, 3,  ONE_BASED,   3",
            "ZERO_BASED,      2, 3,     ZERO_BASED,   2",
    })
    public void testStartPositionWithCoordinateSystem(CoordinateSystem coordinateSystem, int startPos, int endPos, CoordinateSystem targetCoordinateSystem, int expectedStart) {
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, startPos, endPos);
        assertThat(region.startWithCoordinateSystem(targetCoordinateSystem), equalTo(expectedStart));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED,   2, 3,  ONE_BASED,   3",
            "ONE_BASED,   2, 3,     ZERO_BASED,   3",
            "ZERO_BASED,      2, 3,  ONE_BASED,   3",
            "ZERO_BASED,      2, 3,     ZERO_BASED,   3",
    })
    public void testEndWithCoordinateSystem(CoordinateSystem coordinateSystem, int startPos, int endPos,
                                            CoordinateSystem targetCoordinateSystem, int expectedEnd) {
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, startPos, endPos);
        assertThat(region.endWithCoordinateSystem(targetCoordinateSystem), equalTo(expectedEnd));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, ONE_BASED,   2, 3,    POSITIVE, ONE_BASED, 2",
            "POSITIVE, ONE_BASED,   2, 3,    NEGATIVE, ONE_BASED, 3",
            "POSITIVE, ONE_BASED,   2, 3,    NEGATIVE,    ZERO_BASED, 2",

            "POSITIVE,  ZERO_BASED,     1, 3,    POSITIVE,    ZERO_BASED, 1",
            "POSITIVE,  ZERO_BASED,     1, 3,    NEGATIVE,    ZERO_BASED, 2",
            "POSITIVE,  ZERO_BASED,     1, 3,    NEGATIVE, ONE_BASED, 3",
    })
    public void testStartOnStrandWithCoordinateSystem(Strand strand, CoordinateSystem coordinateSystem, int startPos, int endPos, Strand targetStrand, CoordinateSystem targetCoordinateSystem, int expectedStart) {
        GenomicRegion region = GenomicRegion.of(chr1, strand, coordinateSystem, startPos, endPos);
        assertThat(region.startOnStrandWithCoordinateSystem(targetStrand, targetCoordinateSystem), equalTo(expectedStart));
        assertThat(region.withStrand(targetStrand).withCoordinateSystem(targetCoordinateSystem).start(), equalTo(expectedStart));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, ONE_BASED,   2, 3,    POSITIVE, ONE_BASED, 3",
            "POSITIVE, ONE_BASED,   2, 3,    NEGATIVE, ONE_BASED, 4",
            "POSITIVE, ONE_BASED,   2, 3,    NEGATIVE, ZERO_BASED,    4",

            "POSITIVE,  ZERO_BASED,     1, 3,    POSITIVE,    ZERO_BASED, 3",
            "POSITIVE,  ZERO_BASED,     1, 3,    NEGATIVE,    ZERO_BASED, 4",
            "POSITIVE,  ZERO_BASED,     1, 3,    NEGATIVE, ONE_BASED, 4",
    })
    public void testEndOnStrandWithCoordinateSystem(Strand strand, CoordinateSystem coordinateSystem, int startPos, int endPos, Strand targetStrand, CoordinateSystem targetCoordinateSystem, int expectedEnd) {
        GenomicRegion region = GenomicRegion.of(chr1, strand, coordinateSystem, startPos, endPos);
        assertThat(region.endOnStrandWithCoordinateSystem(targetStrand, targetCoordinateSystem), equalTo(expectedEnd));
        assertThat(region.withStrand(targetStrand).withCoordinateSystem(targetCoordinateSystem).end(), equalTo(expectedEnd));
    }

    @ParameterizedTest
    @CsvSource({
            "ZERO_BASED, 1, 3, ZERO_BASED, 1, 3",
            "ZERO_BASED, 1, 3,  ONE_BASED, 2, 3",

            "ONE_BASED, 2, 3,  ZERO_BASED, 1, 3",
            "ONE_BASED, 2, 3,   ONE_BASED, 2, 3",
    })
    public void testWithCoordinateSystem(CoordinateSystem source, int start, int end,
                                         CoordinateSystem target, int targetStart, int targetEnd) {
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, source, start, end);
        GenomicRegion expected = GenomicRegion.of(chr1, Strand.POSITIVE, target, targetStart, targetEnd);
        assertThat(region.withCoordinateSystem(target), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            //    POS -> 0 1 2 3 4 5
            //    NEG <- 5 4 3 2 1 0
            // this                       other                        expected
            "POSITIVE, ZERO_BASED, 1, 2,   NEGATIVE, ZERO_BASED, 0, 1,   0",
            "POSITIVE, ZERO_BASED, 0, 5,   NEGATIVE, ZERO_BASED, 0, 5,   5",
            "POSITIVE, ZERO_BASED, 1, 5,   NEGATIVE, ZERO_BASED, 0, 5,   4",
            "POSITIVE, ZERO_BASED, 1, 5,   NEGATIVE, ZERO_BASED, 1, 5,   3",
            "POSITIVE, ZERO_BASED, 1, 3,   NEGATIVE, ZERO_BASED, 2, 4,   2",
            "POSITIVE, ZERO_BASED, 2, 5,   NEGATIVE, ZERO_BASED, 2, 5,   1",
            "POSITIVE, ZERO_BASED, 2, 3,   NEGATIVE, ZERO_BASED, 2, 3,   1",
            "POSITIVE, ZERO_BASED, 3, 4,   NEGATIVE, ZERO_BASED, 3, 4,   0",
    })
    public void testOverlapLength(Strand thisStrand, CoordinateSystem thisCoordinateSystem, int thisStart, int thisEnd,
                                  Strand otherStrand, CoordinateSystem otherCoordinateSystem, int otherStart, int otherEnd,
                                  int expected) {
        GenomicRegion region = GenomicRegion.of(chr1, thisStrand, thisCoordinateSystem, thisStart, thisEnd);
        GenomicRegion other = GenomicRegion.of(chr1, otherStrand, otherCoordinateSystem, otherStart, otherEnd);

        assertThat(region.overlapLength(other), equalTo(expected));
    }

    @Test
    public void testPreciseRegionMinMax() {
        GenomicRegion imprecise = GenomicRegion.of(TestContig.of(2, 10000), Strand.POSITIVE, Coordinates.of(CoordinateSystem.oneBased(), 1000, 2000));
        assertThat(imprecise.startMin(), equalTo(1000));
        assertThat(imprecise.startMax(), equalTo(1000));

        assertThat(imprecise.endMin(), equalTo(2000));
        assertThat(imprecise.endMax(), equalTo(2000));
    }

    @Test
    public void testImpreciseRegionMinMax() {
        GenomicRegion imprecise = GenomicRegion.of(TestContig.of(2, 10000), Strand.POSITIVE, Coordinates.of(CoordinateSystem.oneBased(), 1000, ConfidenceInterval.of(-10, 20), 2000, ConfidenceInterval.of(-100, 200)));
        assertThat(imprecise.startMin(), equalTo(990));
        assertThat(imprecise.startMax(), equalTo(1020));

        assertThat(imprecise.endMin(), equalTo(1900));
        assertThat(imprecise.endMax(), equalTo(2200));
    }
}