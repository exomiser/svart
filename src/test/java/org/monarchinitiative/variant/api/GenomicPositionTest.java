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
        GenomicPosition seven = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(7, ConfidenceInterval.of(-1, 2)));

        assertThat(seven.contig(), equalTo(ctg1));
        assertThat(seven.contigId(), equalTo(1));
        assertThat(seven.contigName(), equalTo("1"));
        assertThat(seven.position(), equalTo(Position.of(7, ConfidenceInterval.of(-1, 2))));
        assertThat(seven.pos(), equalTo(7));
        assertThat(seven.ci(), equalTo(ConfidenceInterval.of(-1, 2)));
        assertThat(seven.min(), equalTo(6));
        assertThat(seven.max(), equalTo(9));
        assertThat(seven.strand(), equalTo(Strand.POSITIVE));
    }

    @Test
    public void distanceToPosition() {
        GenomicPosition seven = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(7));
        GenomicPosition three = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(3));

        assertThat(seven.distanceTo(three), equalTo(-4));
        assertThat(three.distanceTo(seven), equalTo(4));
    }

    @Test
    public void distanceToPositionWhenOnDifferentContig() {
        Contig ctg2 = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 20, "", "", "");
        GenomicPosition seven = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(7));

        GenomicPosition other = GenomicPosition.zeroBased(ctg2, Strand.POSITIVE, Position.of(2));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> seven.distanceTo(other));
        assertThat(e.getMessage(), equalTo("Coordinates are on different chromosomes: 1 vs. 2"));
    }

    @Test
    public void distanceToRegion() {
        GenomicPosition seven = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(7));
        GenomicPosition three = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(3));

        GenomicRegion region = GenomicRegion.oneBased(ctg1, Strand.POSITIVE, Position.of(5), Position.of(6));

        assertThat(seven.distanceTo(region), equalTo(-1));
        assertThat(three.distanceTo(region), equalTo(2));

        GenomicRegion containing = GenomicRegion.oneBased(ctg1, Strand.POSITIVE, Position.of(2), Position.of(4));
        assertThat(three.distanceTo(containing), equalTo(0));
    }

    @Test
    public void distanceToRegionWhenOnDifferentContig() {
        Contig ctg2 = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 20, "", "", "");
        GenomicPosition seven = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(7));

        GenomicRegion other = GenomicRegion.oneBased(ctg2, Strand.POSITIVE, Position.of(5), Position.of(6));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> seven.distanceTo(other));
        assertThat(e.getMessage(), equalTo("Coordinates are on different chromosomes: 1 vs. 2"));
    }

    @Test
    public void withStrand() {
        GenomicPosition seven = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(7, ConfidenceInterval.of(-1, 2)));

        assertThat(seven.withStrand(Strand.POSITIVE), is(sameInstance(seven)));

        GenomicPosition position = seven.withStrand(Strand.NEGATIVE);
        assertThat(position.position(), equalTo(Position.of(3, ConfidenceInterval.of(-2, 1))));
        assertThat(position.strand(), equalTo(Strand.NEGATIVE));
    }

    @ParameterizedTest
    @CsvSource({
            // source  pos   target     expected   pos
            "POSITIVE, 3,   POSITIVE,   POSITIVE,   3",
            "POSITIVE, 3,   NEGATIVE,   NEGATIVE,   7",

            "NEGATIVE, 3,   POSITIVE,   POSITIVE,   7",
            "NEGATIVE, 3,   NEGATIVE,   NEGATIVE,   3"
            })
    public void withStrand_strandConversions(Strand source, int pos, Strand target, Strand expected, int expectedPosition) {
        GenomicPosition initial = GenomicPosition.zeroBased(ctg1, source, Position.of(pos));

        GenomicPosition actual = initial.withStrand(target);
        assertThat(actual.strand(), equalTo(expected));
        assertThat(actual.pos(), equalTo(expectedPosition));
    }

    @Test
    public void toRegion() {
        // a position is turned into a region of length 0
        GenomicPosition three = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(3));

        GenomicRegion region = three.toRegion();
        assertThat(region.length(), equalTo(0));
        assertThat(region.startPosition(), equalTo(three.position()));
        assertThat(region.endPosition(), equalTo(three.position()));
        assertThat(region.strand(), equalTo(three.strand()));
        assertThat(region.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));
    }

    @ParameterizedTest
    @CsvSource({
            "3,   0,   3, 3",
            "3,   1,   2, 4",
            "3,   2,   1, 5",

            "3,   0,   3, 3",
            "3,   1,   2, 4",
            "3,   2,   1, 5"
    })
    public void toRegion_singlePadding(int pos, int padding,
                                          int expectedStart, int expectedEnd) {
        GenomicRegion actual = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(pos)).toRegion(padding);
        GenomicRegion expected = GenomicRegion.of(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(expectedStart), Position.of(expectedEnd));
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void toRegion_singleNegativePadding() {
        GenomicPosition three = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(3, ConfidenceInterval.of(-2, 3)));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> three.toRegion(-1));
        assertThat(e.getMessage(), equalTo("Cannot apply negative padding: -1"));

        GenomicPosition seven = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(7, ConfidenceInterval.of(-1, 2)));
        e = assertThrows(IllegalArgumentException.class, () -> seven.toRegion(-1));
        assertThat(e.getMessage(), equalTo("Cannot apply negative padding: -1"));
    }

    @ParameterizedTest
    @CsvSource({
            "3,  -2, -1,   1, 2",
            "3,   0, -0,   3, 3",
            "3,  -1,  0,   2, 3",
            "3,  -2,  1,   1, 4",

            "3,   0, -0,   3, 3",
            "3,   0,  1,   3, 4",
            "3,  -1,  2,   2, 5"
    })
    public void toRegion_upDownPadding(int pos,
                                          int upstream, int downstream,
                                          int expectedStart, int expectedEnd) {
        GenomicRegion actual = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(pos)).toRegion(upstream, downstream);
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
        GenomicPosition position = GenomicPosition.oneBased(ctg1, Strand.POSITIVE, Position.of(pos));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> position.toRegion(upstream, downstream));
        assertThat(e.getMessage(), equalTo("Cannot apply negative padding: " + upstream + ", " + downstream));
    }

    @Test
    public void compareToWhenDifferingContigs() {
        Contig ctg1 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, 10, "", "", "chr1");
        Contig ctg2 = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 10, "", "", "chr2");
        Contig ctg3 = Contig.of(3, "3", SequenceRole.ASSEMBLED_MOLECULE, 10, "", "", "chr3");

        GenomicPosition two = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(3));
        GenomicPosition three = GenomicPosition.zeroBased(ctg2, Strand.POSITIVE, Position.of(3));
        GenomicPosition four = GenomicPosition.zeroBased(ctg3, Strand.POSITIVE, Position.of(3));

        assertThat(three.compareTo(two), equalTo(1));
        assertThat(three.compareTo(three), equalTo(0));
        assertThat(three.compareTo(four), equalTo(-1));
    }

    @ParameterizedTest
    @CsvSource({
            "2,  1",
            "3,  0",
            "4, -1"})
    public void compareToWhenDifferingPositions(int pos, int expected) {
        GenomicPosition three = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(3));

        GenomicPosition gp = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(pos));

        assertThat(three.compareTo(gp), equalTo(expected));
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
        GenomicPosition aPos = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(a));
        GenomicPosition bPos = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(b));
        assertThat(aPos.isUpstreamOf(bPos), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "3,  ONE_BASED, 2, 2,   false",
            "3,  ONE_BASED, 2, 3,   false",
            "3,  ONE_BASED, 3, 3,   false",
            "3,  ONE_BASED, 4, 4,   false",
            "3,  ONE_BASED, 5, 5,   true",

            "3,  ZERO_BASED, 1, 2,   false",
            "3,  ZERO_BASED, 2, 3,   false",
            "3,  ZERO_BASED, 3, 3,   false",
            "3,  ZERO_BASED, 3, 4,   false",
            "3,  ZERO_BASED, 4, 5,   true"
    })
    public void isUpstreamOfRegion(int pos,
                                   CoordinateSystem coordinateSystem, int start, int end,
                                   boolean expected) {
        GenomicPosition position = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(pos));
        GenomicRegion region = GenomicRegion.of(ctg1, Strand.POSITIVE, coordinateSystem, Position.of(start), Position.of(end));
        assertThat(position.isUpstreamOf(region), equalTo(expected));
    }


    @ParameterizedTest
    @CsvSource({
            "2,   true",
            "3,   false",
            "4,   false"})
    public void isDownstreamOfPosition(int pos, boolean expected) {
        GenomicPosition three = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(3));
        GenomicPosition gp = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(pos));
        assertThat(three.isDownstreamOf(gp), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "2,2,true",
            "3,3,false",
            "4,4,false",
            "2,4,false"})
    public void isDownstreamOfRegion(int start, int end, boolean expected) {
        GenomicPosition three = GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(3));
        GenomicRegion region = GenomicRegion.oneBased(ctg1, Strand.POSITIVE, Position.of(start), Position.of(end));
        assertThat(three.isDownstreamOf(region), equalTo(expected));
    }


    @Test
    public void illegalPositionPastContigEnd() {
        IllegalArgumentException eo = assertThrows(IllegalArgumentException.class,
                () -> GenomicPosition.zeroBased(ctg1, Strand.POSITIVE, Position.of(11)));
        assertThat(eo.getMessage(), equalTo("Cannot create genomic position 11 that extends beyond contig end 10"));

    }

    @ParameterizedTest
    @CsvSource({
            "0, ZERO_BASED,   1",
            "1, ONE_BASED,    1",
    })
    public void posOneBased(int position, CoordinateSystem coordinateSystem, int expected) {
        GenomicPosition pos = GenomicPosition.of(ctg1, Strand.POSITIVE, coordinateSystem, Position.of(position));
        assertThat(pos.posOneBased(), equalTo(expected));
    }
}