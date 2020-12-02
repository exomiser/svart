package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GenomicPositionTest {

    private final Contig ctg1 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, 10, "", "", "chr1");

    @Test
    public void properties() {
        GenomicPosition zeroBasedSeven = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(7, ConfidenceInterval.of(-1, 2)));

        assertThat(zeroBasedSeven.contig(), equalTo(ctg1));
        assertThat(zeroBasedSeven.contigId(), equalTo(1));
        assertThat(zeroBasedSeven.contigName(), equalTo("1"));
        assertThat(zeroBasedSeven.position(), equalTo(Position.of(7, ConfidenceInterval.of(-1, 2))));
        assertThat(zeroBasedSeven.pos(), equalTo(7));
        assertThat(zeroBasedSeven.ci(), equalTo(ConfidenceInterval.of(-1, 2)));
        assertThat(zeroBasedSeven.min(), equalTo(6));
        assertThat(zeroBasedSeven.max(), equalTo(9));
        assertThat(zeroBasedSeven.strand(), equalTo(Strand.POSITIVE));
        assertThat(zeroBasedSeven.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));
        assertThat(zeroBasedSeven.toString(), equalTo("[1:7 (-1, +2))+"));

        GenomicPosition oneBasedThree = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3, ConfidenceInterval.of(-2, 3)));

        assertThat(oneBasedThree.contig(), equalTo(ctg1));
        assertThat(oneBasedThree.position(), equalTo(Position.of(3, ConfidenceInterval.of(-2, 3))));
        assertThat(oneBasedThree.pos(), equalTo(3));
        assertThat(oneBasedThree.ci(), equalTo(ConfidenceInterval.of(-2, 3)));
        assertThat(oneBasedThree.min(), equalTo(1));
        assertThat(oneBasedThree.max(), equalTo(6));
        assertThat(oneBasedThree.strand(), equalTo(Strand.POSITIVE));
        assertThat(oneBasedThree.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
        assertThat(oneBasedThree.toString(), equalTo("[1:3 (-2, +3)]+"));
    }

    @Test
    public void differenceToPosition() {
        GenomicPosition zeroBasedSeven = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(7, ConfidenceInterval.of(-1, 2)));
        GenomicPosition oneBasedThree = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3, ConfidenceInterval.of(-2, 3)));

        assertThat(zeroBasedSeven.differenceTo(oneBasedThree), equalTo(5));
        assertThat(oneBasedThree.differenceTo(zeroBasedSeven), equalTo(-5));
    }

    @Test
    public void differenceToPositionWhenOnDifferentContig() {
        Contig ctg2 = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 20, "", "", "");
        GenomicPosition zeroBasedSeven = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(7, ConfidenceInterval.of(-1, 2)));

        GenomicPosition other = GenomicPosition.of(ctg2, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(2));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> zeroBasedSeven.differenceTo(other));
        assertThat(e.getMessage(), equalTo("Coordinates are on different chromosomes: 1 vs. 2"));
    }

    @Test
    public void differenceToRegion() {
        GenomicPosition zeroBasedSeven = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(7, ConfidenceInterval.of(-1, 2)));
        GenomicPosition oneBasedThree = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3, ConfidenceInterval.of(-2, 3)));

        GenomicRegion region = GenomicRegion.oneBased(ctg1, Strand.POSITIVE, Position.of(5), Position.of(6));

        assertThat(zeroBasedSeven.differenceTo(region), equalTo(1));
        assertThat(oneBasedThree.differenceTo(region), equalTo(-2));

        GenomicRegion containing = GenomicRegion.oneBased(ctg1, Strand.POSITIVE, Position.of(2), Position.of(4));
        assertThat(oneBasedThree.differenceTo(containing), equalTo(0));
    }

    @Test
    public void differenceToRegionWhenOnDifferentContig() {
        Contig ctg2 = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 20, "", "", "");
        GenomicPosition zeroBasedSeven = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(7, ConfidenceInterval.of(-1, 2)));

        GenomicRegion other = GenomicRegion.oneBased(ctg2, Strand.POSITIVE, Position.of(5), Position.of(6));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> zeroBasedSeven.differenceTo(other));
        assertThat(e.getMessage(), equalTo("Coordinates are on different chromosomes: 1 vs. 2"));
    }

    @Test
    public void withStrand() {
        GenomicPosition zeroBasedSeven = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(7, ConfidenceInterval.of(-1, 2)));

        assertThat(zeroBasedSeven.withStrand(Strand.POSITIVE), is(sameInstance(zeroBasedSeven)));

        GenomicPosition zeroNeg = zeroBasedSeven.withStrand(Strand.NEGATIVE);
        assertThat(zeroNeg.position(), equalTo(Position.of(3, ConfidenceInterval.of(-2, 1))));
        assertThat(zeroNeg.strand(), equalTo(Strand.NEGATIVE));
        assertThat(zeroNeg.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));


        GenomicPosition oneBasedThree = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3, ConfidenceInterval.of(-2, 3)));

        assertThat(oneBasedThree.withStrand(Strand.POSITIVE), is(sameInstance(oneBasedThree)));

        GenomicPosition oneNeg = oneBasedThree.withStrand(Strand.NEGATIVE);
        assertThat(oneNeg.position(), equalTo(Position.of(8, ConfidenceInterval.of(-3, 2))));
        assertThat(oneNeg.strand(), equalTo(Strand.NEGATIVE));
        assertThat(oneNeg.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }

    @ParameterizedTest
    @CsvSource({
            // source  pos   target     expected   pos
            "POSITIVE, 3,  POSITIVE,   POSITIVE,   3",
            "POSITIVE, 3,   NEGATIVE,   NEGATIVE,   8",
            "POSITIVE, 3,   UNSTRANDED, UNSTRANDED, 3",
            "POSITIVE, 3,   UNKNOWN,    UNKNOWN,    3",

            "NEGATIVE, 3,   POSITIVE,   POSITIVE,   8",
            "NEGATIVE, 3,   NEGATIVE,   NEGATIVE,   3",
            "NEGATIVE, 3,   UNSTRANDED, UNSTRANDED, 3",
            "NEGATIVE, 3,   UNKNOWN,    UNKNOWN,    3",

            "UNSTRANDED, 3, POSITIVE,   UNSTRANDED, 3",
            "UNSTRANDED, 3, NEGATIVE,   UNSTRANDED, 3",
            "UNSTRANDED, 3, UNSTRANDED, UNSTRANDED, 3",
            "UNSTRANDED, 3, UNKNOWN,    UNKNOWN,    3",

            "UNKNOWN, 3,    POSITIVE,   UNKNOWN,    3",
            "UNKNOWN, 3,    NEGATIVE,   UNKNOWN,    3",
            "UNKNOWN, 3,    UNSTRANDED, UNKNOWN,    3",
            "UNKNOWN, 3,    UNKNOWN,    UNKNOWN,    3"})
    public void withStrand_strandConversions(Strand source, int pos, Strand target, Strand expected, int expectedPosition) {
        GenomicPosition initial = GenomicPosition.oneBased(ctg1, source, Position.of(pos));

        GenomicPosition actual = initial.withStrand(target);
        assertThat(actual.strand(), equalTo(expected));
        assertThat(actual.pos(), equalTo(expectedPosition));
        assertThat(actual.coordinateSystem(), equalTo(initial.coordinateSystem()));
    }

    @Test
    public void withCoordinateSystem() {
        GenomicPosition zeroBasedSeven = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(7, ConfidenceInterval.of(-1, 2)));

        assertThat(zeroBasedSeven.withCoordinateSystem(CoordinateSystem.ZERO_BASED), is(sameInstance(zeroBasedSeven)));

        GenomicPosition zpos = zeroBasedSeven.withCoordinateSystem(CoordinateSystem.ONE_BASED);
        assertThat(zpos.pos(), equalTo(8));
        assertThat(zpos.ci(), equalTo(zeroBasedSeven.ci()));
        assertThat(zpos.coordinateSystem(), is(CoordinateSystem.ONE_BASED));


        GenomicPosition oneBasedThree = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3, ConfidenceInterval.of(-2, 3)));

        assertThat(oneBasedThree.withCoordinateSystem(CoordinateSystem.ONE_BASED), is(sameInstance(oneBasedThree)));

        GenomicPosition opos = oneBasedThree.withCoordinateSystem(CoordinateSystem.ZERO_BASED);
        assertThat(opos.pos(), equalTo(2));
        assertThat(opos.ci(), equalTo(oneBasedThree.ci()));
        assertThat(opos.coordinateSystem(), is(CoordinateSystem.ZERO_BASED));
    }

    @Test
    public void asRegion() {
        // one-based position is turned into a region of length 1
        GenomicPosition oneBasedThree = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3, ConfidenceInterval.of(-2, 3)));

        GenomicRegion region = oneBasedThree.asRegion();
        assertThat(region.length(), equalTo(1));
        assertThat(region.startPosition(), equalTo(oneBasedThree.position()));
        assertThat(region.endPosition(), equalTo(oneBasedThree.position()));
        assertThat(region.coordinateSystem(), equalTo(oneBasedThree.coordinateSystem()));
        assertThat(region.strand(), equalTo(oneBasedThree.strand()));

        // zero-based position is turned into a region of length 0
        GenomicPosition zeroBasedSeven = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(7, ConfidenceInterval.of(-1, 2)));

        region = zeroBasedSeven.asRegion();
        assertThat(region.length(), equalTo(0));
        assertThat(region.startPosition(), equalTo(zeroBasedSeven.position()));
        assertThat(region.endPosition(), equalTo(zeroBasedSeven.position()));
        assertThat(region.coordinateSystem(), equalTo(zeroBasedSeven.coordinateSystem()));
        assertThat(region.strand(), equalTo(zeroBasedSeven.strand()));
    }

    @ParameterizedTest
    @CsvSource({
            "ZERO_BASED, 3,  0,  3, 3",
            "ZERO_BASED, 3,  1,  2, 4",
            "ZERO_BASED, 3,  2,  1, 5",

            "ONE_BASED,  3,  0,  3, 3",
            "ONE_BASED,  3,  1,  2, 4",
            "ONE_BASED,  3,  2,  1, 5"
    })
    public void withPadding_singlePadding(CoordinateSystem coordinateSystem, int pos,
                                          int padding,
                                          int expectedStart, int expectedEnd) {
        GenomicRegion actual = GenomicPosition.of(ctg1, Strand.POSITIVE, coordinateSystem, Position.of(pos)).withPadding(padding);
        GenomicRegion expected = GenomicRegion.of(ctg1, Strand.POSITIVE, coordinateSystem, Position.of(expectedStart), Position.of(expectedEnd));
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void withPadding_singleNegativePadding() {
        GenomicPosition oneBasedThree = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3, ConfidenceInterval.of(-2, 3)));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> oneBasedThree.withPadding(-1));
        assertThat(e.getMessage(), equalTo("Cannot apply negative padding: -1, -1"));

        GenomicPosition zeroBasedSeven = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(7, ConfidenceInterval.of(-1, 2)));
        e = assertThrows(IllegalArgumentException.class, () -> zeroBasedSeven.withPadding(-1));
        assertThat(e.getMessage(), equalTo("Cannot apply negative padding: -1, -1"));
    }

    @ParameterizedTest
    @CsvSource({
            "ZERO_BASED, 3,  0, 0,  3, 3",
            "ZERO_BASED, 3,  1, 0,  2, 3",
            "ZERO_BASED, 3,  2, 1,  1, 4",

            "ONE_BASED,  3,  0, 0,  3, 3",
            "ONE_BASED,  3,  0, 1,  3, 4",
            "ONE_BASED,  3,  1, 2,  2, 5"
    })
    public void withPadding_upDownPadding(CoordinateSystem coordinateSystem, int pos,
                                          int upstream, int downstream,
                                          int expectedStart, int expectedEnd) {
        GenomicRegion actual = GenomicPosition.of(ctg1, Strand.POSITIVE, coordinateSystem, Position.of(pos)).withPadding(upstream, downstream);
        GenomicRegion expected = GenomicRegion.of(ctg1, Strand.POSITIVE, coordinateSystem, Position.of(expectedStart), Position.of(expectedEnd));
        assertThat(actual, equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0,   7, 7, 0,   3, 3, 1", // no change
            "1, 2,   6, 9, 3,   2, 5, 4",
            "2, 1,   5, 8, 3,   1, 4, 4"})
    public void withPadding_upstreamDownstreamPadding(int upstream, int downstream,
                                          int zeroExpectedStart, int zeroExpectedEnd, int zeroExpectedLength,
                                          int oneExpectedStart, int oneExpectedEnd, int oneExpectedLength) {
        GenomicPosition zeroBasedSeven = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(7, ConfidenceInterval.of(-1, 2)));

        GenomicRegion zeroBased = zeroBasedSeven.withPadding(upstream, downstream);
        assertThat(zeroBased.length(), equalTo(zeroExpectedLength));
        assertThat(zeroBased.start(), equalTo(zeroExpectedStart));
        assertThat(zeroBased.startPosition().confidenceInterval(), equalTo(zeroBasedSeven.ci()));
        assertThat(zeroBased.end(), equalTo(zeroExpectedEnd));
        assertThat(zeroBased.endPosition().confidenceInterval(), equalTo(zeroBasedSeven.ci()));

        GenomicPosition oneBasedThree = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3, ConfidenceInterval.of(-2, 3)));

        GenomicRegion oneBased = oneBasedThree.withPadding(upstream, downstream);
        assertThat(oneBased.length(), equalTo(oneExpectedLength));
        assertThat(oneBased.start(), equalTo(oneExpectedStart));
        assertThat(oneBased.startPosition().confidenceInterval(), equalTo(oneBasedThree.ci()));
        assertThat(oneBased.end(), equalTo(oneExpectedEnd));
        assertThat(oneBased.endPosition().confidenceInterval(), equalTo(oneBasedThree.ci()));
    }

    @Test
    public void withPadding_upstreamDownstreamNegativePadding() {
        GenomicPosition oneBasedThree = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3, ConfidenceInterval.of(-2, 3)));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> oneBasedThree.withPadding(-1, 1));
        assertThat(e.getMessage(), equalTo("Cannot apply negative padding: -1, 1"));

        GenomicPosition zeroBasedSeven = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(7, ConfidenceInterval.of(-1, 2)));
        e = assertThrows(IllegalArgumentException.class, () -> zeroBasedSeven.withPadding(-1, 0));
        assertThat(e.getMessage(), equalTo("Cannot apply negative padding: -1, 0"));
    }

    @Test
    public void compareToWhenDifferingContigs() {
        Contig ctg1 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, 10, "", "", "chr1");
        Contig ctg2 = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 10, "", "", "chr2");
        Contig ctg3 = Contig.of(3, "3", SequenceRole.ASSEMBLED_MOLECULE, 10, "", "", "chr3");

        GenomicPosition two = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3));
        GenomicPosition three = GenomicPosition.of(ctg2, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3));
        GenomicPosition four = GenomicPosition.of(ctg3, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3));

        assertThat(three.compareTo(two), equalTo(1));
        assertThat(three.compareTo(three), equalTo(0));
        assertThat(three.compareTo(four), equalTo(-1));
    }

    @ParameterizedTest
    @CsvSource({
            "2,1",
            "3,0",
            "4,-1"})
    public void compareToWhenDifferingPositions(int pos, int expected) {
        GenomicPosition oneBasedThreePrecise = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3));

        GenomicPosition gp = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(pos));

        assertThat(oneBasedThreePrecise.compareTo(gp), equalTo(expected));
    }

    @Test
    public void compareToWhenDifferingStrands() {
        // TODO: 25. 11. 2020 implement
    }

    @ParameterizedTest
    @CsvSource({
            "2,false",
            "3,false",
            "4,true"})
    public void isUpstreamOfPosition(int pos, boolean expected) {
        GenomicPosition oneBasedThree = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3, ConfidenceInterval.of(-2, 3)));
        GenomicPosition gp = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(pos));
        assertThat(oneBasedThree.isUpstreamOf(gp), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "2,2,false",
            "3,3,false",
            "4,4,true",
            "2,4,false"})
    public void isUpstreamOfRegion(int start, int end, boolean expected) {
        GenomicPosition oneBasedThree = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3, ConfidenceInterval.of(-2, 3)));
        GenomicRegion gr = GenomicRegion.oneBased(ctg1, Strand.POSITIVE, Position.of(start), Position.of(end));
        assertThat(oneBasedThree.isUpstreamOf(gr), equalTo(expected));
    }


    @ParameterizedTest
    @CsvSource({
            "2,true",
            "3,false",
            "4,false"})
    public void isDownstreamOfPosition(int pos, boolean expected) {
        GenomicPosition oneBasedThree = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3, ConfidenceInterval.of(-2, 3)));
        GenomicPosition gp = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(pos));
        assertThat(oneBasedThree.isDownstreamOf(gp), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "2,2,true",
            "3,3,false",
            "4,4,false",
            "2,4,false"})
    public void isDownstreamOfRegion(int start, int end, boolean expected) {
        GenomicPosition oneBasedThree = GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3, ConfidenceInterval.of(-2, 3)));
        GenomicRegion region = GenomicRegion.oneBased(ctg1, Strand.POSITIVE, Position.of(start), Position.of(end));
        assertThat(oneBasedThree.isDownstreamOf(region), equalTo(expected));
    }

    @Test
    public void illegalPositionPastContigStart() {
        IllegalArgumentException eo = assertThrows(IllegalArgumentException.class,
                () -> GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(1, -1, 1)));
        assertThat(eo.getMessage(), equalTo("Cannot create genomic position 1 (-1, +1) that extends beyond first contig base"));


        IllegalArgumentException ez = assertThrows(IllegalArgumentException.class,
                () -> GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(0, -1, 1)));
        assertThat(ez.getMessage(), equalTo("Cannot create genomic position 0 (-1, +1) that extends beyond first contig base"));
    }

    @Test
    public void illegalPositionPastContigEnd() {
        IllegalArgumentException eo = assertThrows(IllegalArgumentException.class,
                () -> GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(10, -1, 1)));
        assertThat(eo.getMessage(), equalTo("Cannot create genomic position 10 (-1, +1) that extends beyond contig end 10"));


        IllegalArgumentException ez = assertThrows(IllegalArgumentException.class,
                () -> GenomicPosition.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(10, -1, 1)));
        assertThat(ez.getMessage(), equalTo("Cannot create genomic position 10 (-1, +1) that extends beyond contig end 10"));
    }

    @Test
    public void zeroBasedStaticConstructor() {
        GenomicPosition gp = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(4));

        assertThat(gp.contig(), equalTo(ctg1));
        assertThat(gp.strand(), equalTo(Strand.POSITIVE));
        assertThat(gp.position(), equalTo(Position.of(4)));
        assertThat(gp.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));
    }

    @Test
    public void oneBasedStaticConstructor() {
        GenomicPosition gp = GenomicPosition.oneBased(ctg1, Strand.POSITIVE, Position.of(4));

        assertThat(gp.contig(), equalTo(ctg1));
        assertThat(gp.strand(), equalTo(Strand.POSITIVE));
        assertThat(gp.position(), equalTo(Position.of(4)));
        assertThat(gp.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }
}