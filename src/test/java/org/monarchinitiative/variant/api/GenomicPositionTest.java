package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GenomicPositionTest {

    private final Contig ctg1 = TestContig.of(1, 10);

    @Test
    public void properties() {
        GenomicPosition seven = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(7, ConfidenceInterval.of(-1, 2)));

        assertThat(seven.contig(), equalTo(ctg1));
        assertThat(seven.contigId(), equalTo(1));
        assertThat(seven.contigName(), equalTo("1"));
        assertThat(seven.pos(), equalTo(7));
        assertThat(seven.confidenceInterval(), equalTo(ConfidenceInterval.of(-1, 2)));
        assertThat(seven.minPos(), equalTo(6));
        assertThat(seven.maxPos(), equalTo(9));
        assertThat(seven.strand(), equalTo(Strand.POSITIVE));
    }

    @Test
    public void distanceToPosition() {
        GenomicPosition seven = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(7));
        GenomicPosition three = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(3));

        assertThat(seven.distanceTo(three), equalTo(-4));
        assertThat(three.distanceTo(seven), equalTo(4));
    }

    @Test
    public void distanceToPositionWhenOnDifferentContig() {
        Contig ctg2 = TestContig.of(2, 20);
        GenomicPosition seven = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(7));

        GenomicPosition other = GenomicPosition.of(ctg2, Strand.POSITIVE, Position.of(2));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> seven.distanceTo(other));
        assertThat(e.getMessage(), equalTo("Coordinates are on different chromosomes: 1 vs. 2"));
    }

    @Test
    public void distanceToRegion() {
        GenomicPosition seven = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(7));
        GenomicPosition three = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(3));

        GenomicRegion region = GenomicRegion.oneBased(ctg1, Strand.POSITIVE, 5, 6);

        assertThat(seven.distanceTo(region), equalTo(-1));
        assertThat(three.distanceTo(region), equalTo(2));

        GenomicRegion containing = GenomicRegion.oneBased(ctg1, Strand.POSITIVE, 2, 4);
        assertThat(three.distanceTo(containing), equalTo(0));
    }

    @Test
    public void distanceToRegionWhenOnDifferentContig() {
        Contig ctg2 = TestContig.of(2, 20);
        GenomicPosition seven = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(7));

        GenomicRegion other = GenomicRegion.oneBased(ctg2, Strand.POSITIVE, 5, 6);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> seven.distanceTo(other));
        assertThat(e.getMessage(), equalTo("Coordinates are on different chromosomes: 1 vs. 2"));
    }

    @Test
    public void withStrand() {
        GenomicPosition seven = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(7, ConfidenceInterval.of(-1, 2)));

        assertThat(seven.withStrand(Strand.POSITIVE), is(sameInstance(seven)));

        GenomicPosition position = seven.withStrand(Strand.NEGATIVE);
        assertThat(position.pos(), equalTo(4));
        assertThat(position.confidenceInterval(), equalTo(ConfidenceInterval.of(-2, 1)));
        assertThat(position.strand(), equalTo(Strand.NEGATIVE));
    }

    @ParameterizedTest
    @CsvSource({
            // source  pos   target     pos
            "POSITIVE, 3,   POSITIVE,   3",
            "POSITIVE, 3,   NEGATIVE,   8",

            "NEGATIVE, 3,   POSITIVE,   8",
            "NEGATIVE, 3,   NEGATIVE,   3"
            })
    public void withStrand_strandConversions(Strand source, int pos, Strand target, int expectedPosition) {
        GenomicPosition initial = GenomicPosition.of(ctg1, source, Position.of(pos));

        GenomicPosition actual = initial.withStrand(target);
        assertThat(actual.strand(), equalTo(target));
        assertThat(actual.pos(), equalTo(expectedPosition));
    }

    @ParameterizedTest
    @CsvSource({
            // strand  pos  expected coords  start end
            "POSITIVE, 3,   ZERO_BASED,   2, 3",
            "NEGATIVE, 3,   ZERO_BASED,   2, 3",
    })
    public void toRegion(Strand strand, int initPos, CoordinateSystem exptCoords, int exptStart, int exptEnd) {
        // a position is turned into a region of length 1
        GenomicPosition position = GenomicPosition.of(ctg1, strand, Position.of(initPos));
        GenomicRegion expected = GenomicRegion.of(ctg1, strand, exptCoords, Position.of(exptStart), Position.of(exptEnd));
        assertThat(position.toRegion(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "3,   1,   2, 4",
            "3,   2,   1, 5",
    })
    public void toRegion_singlePadding(int pos, int padding,
                                          int expectedStart, int expectedEnd) {
        GenomicRegion actual = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(pos)).toRegion(padding);
        GenomicRegion expected = GenomicRegion.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(expectedStart), Position.of(expectedEnd));
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void toRegion_singleNegativePadding() {
        GenomicPosition three = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(3, ConfidenceInterval.of(-2, 3)));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> three.toRegion(-1));
        assertThat(e.getMessage(), equalTo("Cannot apply negative padding: -1"));

        GenomicPosition seven = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(7, ConfidenceInterval.of(-1, 2)));
        e = assertThrows(IllegalArgumentException.class, () -> seven.toRegion(-1));
        assertThat(e.getMessage(), equalTo("Cannot apply negative padding: -1"));
    }

    @ParameterizedTest
    @CsvSource({
            "3,  -2, -1,   1, 2",
            "3,  -1,  0,   2, 3",
            "3,  -2,  1,   1, 4",

            "3,   0,  1,   3, 4",
            "3,  -1,  2,   2, 5"
    })
    public void toRegion_upDownPadding(int pos,
                                          int upstream, int downstream,
                                          int expectedStart, int expectedEnd) {
        GenomicRegion actual = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(pos)).toRegion(upstream, downstream);
        GenomicRegion expected = GenomicRegion.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(expectedStart), Position.of(expectedEnd));
        assertThat(actual, equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "2, -1, -2",
            "2,  0, -1",
            "2,  1,  0",
            "2,  2,  1"
    })
    public void toRegion_illegalPadding(int pos, int upstream, int downstream) {
        GenomicPosition position = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(pos));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> position.toRegion(upstream, downstream));
        assertThat(e.getMessage(), equalTo("Cannot apply negative padding: " + upstream + ", " + downstream));
    }

    @Test
    public void compareWhenDifferingContigs() {
        Contig ctg1 = TestContig.of(1, 10);
        Contig ctg2 = TestContig.of(2, 10);
        Contig ctg3 = TestContig.of(3, 10);

        GenomicPosition two = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(3));
        GenomicPosition three = GenomicPosition.of(ctg2, Strand.POSITIVE, Position.of(3));
        GenomicPosition four = GenomicPosition.of(ctg3, Strand.POSITIVE, Position.of(3));

        assertThat(GenomicPosition.compare(three, two), equalTo(1));
        assertThat(GenomicPosition.compare(three, three), equalTo(0));
        assertThat(GenomicPosition.compare(three, four), equalTo(-1));
    }

    @ParameterizedTest
    @CsvSource({
            "2,  1",
            "3,  0",
            "4, -1"})
    public void compareWhenDifferingPositions(int pos, int expected) {
        GenomicPosition three = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(3));

        GenomicPosition gp = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(pos));

        assertThat(GenomicPosition.compare(three, gp), equalTo(expected));
    }

    @Test
    public void compareToWhenDifferingStrands() {
        // TODO: 25. 11. 2020 implement
    }

    @ParameterizedTest
    @CsvSource({
            "3, 2,   false",
            "3, 3,   false",
            "3, 4,   true"})
    public void isUpstreamOfPosition(int a, int b, boolean expected) {
        GenomicPosition aPos = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(a));
        GenomicPosition bPos = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(b));
        assertThat(aPos.isUpstreamOf(bPos), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "3,  ONE_BASED, 2, 2,   false",
            "3,  ONE_BASED, 2, 3,   false",
            "3,  ONE_BASED, 3, 3,   false",
            "3,  ONE_BASED, 4, 4,   true",
            "3,  ONE_BASED, 5, 5,   true",

            "3,  ZERO_BASED, 1, 2,   false",
            "3,  ZERO_BASED, 2, 3,   false",
            "3,  ZERO_BASED, 3, 4,   true",
            "3,  ZERO_BASED, 4, 5,   true"
    })
    public void isUpstreamOfRegion(int pos,
                                   CoordinateSystem coordinateSystem, int start, int end,
                                   boolean expected) {
        GenomicPosition position = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(pos));
        GenomicRegion region = GenomicRegion.of(ctg1, Strand.POSITIVE, coordinateSystem, Position.of(start), Position.of(end));
        assertThat(position.isUpstreamOf(region), equalTo(expected));
    }


    @ParameterizedTest
    @CsvSource({
            "2,   true",
            "3,   false",
            "4,   false"})
    public void isDownstreamOfPosition(int pos, boolean expected) {
        GenomicPosition three = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(3));
        GenomicPosition gp = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(pos));
        assertThat(three.isDownstreamOf(gp), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "2,2,true",
            "3,3,false",
            "4,4,false",
            "2,4,false"})
    public void isDownstreamOfRegion(int start, int end, boolean expected) {
        GenomicPosition three = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(3));
        GenomicRegion region = GenomicRegion.oneBased(ctg1, Strand.POSITIVE, start, end);
        assertThat(three.isDownstreamOf(region), equalTo(expected));
    }


    @Test
    public void illegalPositionPastContigEnd() {
        IllegalArgumentException eo = assertThrows(IllegalArgumentException.class,
                () -> GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(11)));
        assertThat(eo.getMessage(), equalTo("Cannot create genomic position 11 that extends beyond contig end 10"));

    }

//    TODO - candidate for removal, I think we should remove the oneBased/zeroBased positions
//    @ParameterizedTest
//    @CsvSource({
//            "0, ZERO_BASED,   1",
//            "1, ONE_BASED,    1",
//    })
//    public void posOneBased(int position, CoordinateSystem coordinateSystem, int expected) {
//        GenomicPosition pos = GenomicPosition.of(ctg1, Strand.POSITIVE, Position.of(position, ));
//        assertThat(pos.posOneBased(), equalTo(expected));
//    }
}