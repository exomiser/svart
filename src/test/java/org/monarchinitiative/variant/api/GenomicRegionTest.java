package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.variant.api.impl.DefaultGenomicRegion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomicRegionTest {


    private final Contig chr1 = new TestContig(1, 5);

    @Test
    public void oneBasedSingleBase() {
        GenomicRegion instance = GenomicRegion.oneBased(chr1, Strand.POSITIVE, Position.of(1), Position.of(1));
        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(1));
        assertThat(instance.length(), equalTo(1));
        assertThat(instance.toOppositeStrand(), equalTo(DefaultGenomicRegion.oneBased(chr1, Strand.NEGATIVE, Position.of(5), Position.of(5))));
        assertThat(instance.toOppositeStrand().toZeroBased(), equalTo(DefaultGenomicRegion.zeroBased(chr1, Strand.NEGATIVE, Position.of(4), Position.of(5))));
    }

    @Test
    public void zeroBasedSingleBase() {
        GenomicRegion instance = GenomicRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(0), Position.of(1));
        assertThat(instance.start(), equalTo(0));
        assertThat(instance.end(), equalTo(1));
        assertThat(instance.length(), equalTo(1));
        assertThat(instance.toOppositeStrand(), equalTo(DefaultGenomicRegion.zeroBased(chr1, Strand.NEGATIVE, Position.of(4), Position.of(5))));
    }

    @Test
    public void oneBasedMultiBase() {
        GenomicRegion instance = GenomicRegion.oneBased(chr1, Strand.POSITIVE, Position.of(1), Position.of(2));
        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(2));
        assertThat(instance.length(), equalTo(2));
        assertThat(instance.toOppositeStrand(), equalTo(DefaultGenomicRegion.oneBased(chr1, Strand.NEGATIVE, Position.of(4), Position.of(5))));
    }

    @Test
    public void zeroBasedMultiBase() {
        GenomicRegion instance = GenomicRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(0), Position.of(2));
        assertThat(instance.start(), equalTo(0));
        assertThat(instance.end(), equalTo(2));
        assertThat(instance.length(), equalTo(2));
        assertThat(instance.toOppositeStrand(), equalTo(DefaultGenomicRegion.zeroBased(chr1, Strand.NEGATIVE, Position.of(3), Position.of(5))));
    }

    @Test
    public void flipStrandAndChangeCoordinateSystem() {
        GenomicRegion instance = GenomicRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(0), Position.of(2));
        assertThat(instance.toOppositeStrand().toOneBased(), equalTo(DefaultGenomicRegion.oneBased(chr1, Strand.NEGATIVE, Position.of(4), Position.of(5))));
    }

    @ParameterizedTest
    @CsvSource({
            //
            "ONE_BASED,  1, 2,     0",
            "ZERO_BASED, 1, 2,     1"
    })
    public void startGenomicPosition(CoordinateSystem coordinateSystem, int start, int end, int expected) {
        Position startPos = Position.of(start);
        Position endPos = Position.of(end);
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, startPos, endPos);

        GenomicPosition pos = region.startGenomicPosition();
        assertThat(pos.pos(), equalTo(expected));
        assertThat(pos.strand(), equalTo(Strand.POSITIVE));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED,  1, 2,     2",
            "ZERO_BASED, 1, 2,     2"
    })
    public void endGenomicPosition(CoordinateSystem coordinateSystem, int start, int end, int expected) {
        Position startPos = Position.of(start);
        Position endPos = Position.of(end);
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, startPos, endPos);

        GenomicPosition pos = region.endGenomicPosition();

        assertThat(pos.pos(), equalTo(expected));
        assertThat(pos.strand(), equalTo(Strand.POSITIVE));
    }

    @ParameterizedTest
    @CsvSource({
            // region            pos
            "ZERO_BASED, 0, 1,   0,   true",
            "ZERO_BASED, 1, 2,   1,   true",
            "ZERO_BASED, 1, 2,   2,   false",

            "ONE_BASED, 1, 1,    0,   true",
            "ONE_BASED, 1, 1,    1,   false",
            "ONE_BASED, 1, 2,    1,   true",
            "ONE_BASED, 1, 2,    2,   false",
    })
    public void containsPosition(CoordinateSystem coordinateSystem,int start, int end,
                                 int position, boolean expected) {
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(start), Position.of(end));
        GenomicPosition pos = GenomicPosition.zeroBased(chr1, Strand.POSITIVE, Position.of(position));

        assertThat(region.contains(pos), equalTo(expected));
    }

    @Test
    public void containsPosition_otherContig() {
        GenomicRegion oneToThree = GenomicRegion.oneBased(chr1, Strand.POSITIVE, Position.of(1), Position.of(3));

        Contig ctg2 = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 200, "", "", "");
        GenomicPosition other = GenomicPosition.zeroBased(ctg2, Strand.POSITIVE, Position.of(2));
        assertThat(oneToThree.contains(other), equalTo(false));
    }

    @ParameterizedTest
    @CsvSource({
            // this              other               expected
            "ZERO_BASED, 1, 2,   ZERO_BASED, 0, 1,   false",
            "ZERO_BASED, 1, 2,   ZERO_BASED, 1, 2,   true",
            "ZERO_BASED, 1, 2,   ZERO_BASED, 2, 3,   false",

            "ZERO_BASED, 1, 3,   ZERO_BASED, 0, 1,   false",
            "ZERO_BASED, 1, 3,   ZERO_BASED, 1, 2,   true",
            "ZERO_BASED, 1, 3,   ZERO_BASED, 2, 3,   true",
            "ZERO_BASED, 1, 3,   ZERO_BASED, 3, 4,   false",
            // --------------------------------------------

            "ZERO_BASED, 2, 3,   ONE_BASED,  2, 3,   false",
            "ZERO_BASED, 2, 3,   ONE_BASED,  3, 3,   true",
            "ZERO_BASED, 2, 3,   ONE_BASED,  3, 4,   false",

            "ZERO_BASED, 2, 4,   ONE_BASED,  2, 3,   false",
            "ZERO_BASED, 2, 4,   ONE_BASED,  3, 4,   true",
            "ZERO_BASED, 2, 4,   ONE_BASED,  4, 4,   true",
            "ZERO_BASED, 2, 4,   ONE_BASED,  4, 5,   false",
    })
    public void zeroBasedRegionContainsRegion(CoordinateSystem thisCoordinateSystem, int thisStart, int thisEnd,
                               CoordinateSystem otherCoordinateSystem, int otherStart, int otherEnd,
                               boolean expected) {
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, thisCoordinateSystem, Position.of(thisStart), Position.of(thisEnd));
        GenomicRegion other = GenomicRegion.of(chr1, Strand.POSITIVE, otherCoordinateSystem, Position.of(otherStart), Position.of(otherEnd));

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
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, thisCoordinateSystem, Position.of(thisStart), Position.of(thisEnd));
        GenomicRegion other = GenomicRegion.of(chr1, Strand.POSITIVE, otherCoordinateSystem, Position.of(otherStart), Position.of(otherEnd));

        assertThat(region.contains(other), equalTo(expected));
    }

    @Test
    public void containsRegion_otherContig() {
        GenomicRegion oneToThree = GenomicRegion.oneBased(chr1, Strand.POSITIVE, Position.of(1), Position.of(3));
        Contig ctg2 = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 200, "", "", "");
        GenomicRegion other = GenomicRegion.oneBased(ctg2, Strand.POSITIVE, Position.of(2), Position.of(3));
        assertThat(oneToThree.contains(other), equalTo(false));
    }

    @ParameterizedTest
    @CsvSource({
            // source start  end    target      expected    pos
            "POSITIVE,    3, 5,     POSITIVE,   POSITIVE,   3, 5",
            "POSITIVE,    3, 5,     NEGATIVE,   NEGATIVE,   1, 3",

            "NEGATIVE,    3, 5,     POSITIVE,   POSITIVE,   1, 3",
            "NEGATIVE,    3, 5,     NEGATIVE,   NEGATIVE,   3, 5"
    })
    public void withStrand_strandConversions(Strand source, int start, int end,
                                             Strand target, Strand expected, int exptStart, int exptEnd) {
        GenomicRegion initial = GenomicRegion.oneBased(chr1, source, Position.of(start), Position.of(end));

        GenomicRegion actual = initial.withStrand(target);
        assertThat(actual.strand(), equalTo(expected));
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
        GenomicRegion actual = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(start), Position.of(end)).withPadding(padding);
        GenomicRegion expected = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(expectedStart), Position.of(expectedEnd));
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
    public void withPadding_upDownPadding(CoordinateSystem coordinateSystem,
                                          int start, int end,
                                          int upPadding, int downPadding,
                                          int expectedStart, int expectedEnd) {
        GenomicRegion actual = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(start), Position.of(end)).withPadding(upPadding, downPadding);
        GenomicRegion expected = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(expectedStart), Position.of(expectedEnd));
        assertThat(actual, equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED,  3, 3,     1",
            "ZERO_BASED, 2, 3,     1",
            "ONE_BASED,  2, 3,     2",
            "ZERO_BASED, 1, 3,     2",
            "ONE_BASED,  1, 3,     3"
    })
    public void startEndDifferenceEqualToRegionLength(CoordinateSystem coordinateSystem, int start, int end,
                                                      int regionLength) {
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(start), Position.of(end));

        GenomicPosition regionStart = region.startGenomicPosition();
        GenomicPosition regionEnd = region.endGenomicPosition();

        assertThat(regionStart.distanceTo(regionEnd), equalTo(regionLength));
        assertThat(regionEnd.distanceTo(regionStart), equalTo(-regionLength));
    }

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED,   2, 3,   ONE_BASED,   2",
            "ONE_BASED,   2, 3,   ZERO_BASED,  1",
            "ZERO_BASED,  2, 3,   ONE_BASED,   3",
            "ZERO_BASED,  2, 3,   ZERO_BASED,  2",
    })
    public void normalizeStartPosition(CoordinateSystem coordinateSystem, int startPos, int endPos,
                                       CoordinateSystem targetSystem, int expectedStart) {
        Position startPosition = Position.of(startPos);
        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, startPosition, Position.of(endPos));

        assertThat(region.normalisedStartPosition(targetSystem), equalTo(Position.of(expectedStart)));

        if (targetSystem.isZeroBased() && coordinateSystem.isZeroBased())
            assertThat(region.normalisedStartPosition(targetSystem), sameInstance(startPosition));
    }
}