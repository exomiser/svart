package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.variant.api.impl.ContigDefault;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GenomicPositionTest {

    private final Contig ctg1 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, 10, "", "", "chr1");

    private GenomicPosition oneBasedThree, zeroBasedSeven, oneBasedThreePrecise;

    @BeforeEach
    public void setUp() {
        zeroBasedSeven = new GenomicPositionDefault(ctg1, Strand.POSITIVE, CoordinateSystem.ZERO_BASED, Position.of(7, ConfidenceInterval.of(-1, 2)));
        oneBasedThree = new GenomicPositionDefault(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3, ConfidenceInterval.of(-2, 3)));
        oneBasedThreePrecise = new GenomicPositionDefault(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(3));
    }

    @Test
    public void properties() {
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
        assertThat(zeroBasedSeven.toString(), equalTo("[1:7(±1,2))+"));

        assertThat(oneBasedThree.contig(), equalTo(ctg1));
        assertThat(oneBasedThree.position(), equalTo(Position.of(3, ConfidenceInterval.of(-2, 3))));
        assertThat(oneBasedThree.pos(), equalTo(3));
        assertThat(oneBasedThree.ci(), equalTo(ConfidenceInterval.of(-2, 3)));
        assertThat(oneBasedThree.min(), equalTo(1));
        assertThat(oneBasedThree.max(), equalTo(6));
        assertThat(oneBasedThree.strand(), equalTo(Strand.POSITIVE));
        assertThat(oneBasedThree.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
        assertThat(oneBasedThree.toString(), equalTo("(1:3(±2,3))+"));
    }

    @Test
    public void differenceToPosition() {
        assertThat(zeroBasedSeven.differenceTo(oneBasedThree), equalTo(5));
        assertThat(oneBasedThree.differenceTo(zeroBasedSeven), equalTo(-5));
    }

    @Test
    public void differenceToPositionWhenOnDifferentContig() {
        Contig ctg2 = new ContigDefault(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 20, "", "", "");
        GenomicPosition other = new GenomicPositionDefault(ctg2, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(2));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> zeroBasedSeven.differenceTo(other));
        assertThat(e.getMessage(), equalTo("Coordinates are on different chromosomes: 1 vs. 2"));
    }

    @Test
    public void differenceToRegion() {
        GenomicRegion region = ContigRegion.oneBased(ctg1, Strand.POSITIVE, Position.of(5), Position.of(6));

        assertThat(zeroBasedSeven.differenceTo(region), equalTo(1));
        assertThat(oneBasedThree.differenceTo(region), equalTo(-2));

        GenomicRegion containing = ContigRegion.oneBased(ctg1, Strand.POSITIVE, Position.of(2), Position.of(4));
        assertThat(oneBasedThree.differenceTo(containing), equalTo(0));
    }

    @Test
    public void differenceToRegionWhenOnDifferentContig() {
        Contig ctg2 = new ContigDefault(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 20, "", "", "");
        GenomicRegion other = ContigRegion.oneBased(ctg2, Strand.POSITIVE, Position.of(5), Position.of(6));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> zeroBasedSeven.differenceTo(other));
        assertThat(e.getMessage(), equalTo("Coordinates are on different chromosomes: 1 vs. 2"));
    }

    @Test
    public void withStrand() {
        assertThat(zeroBasedSeven.withStrand(Strand.POSITIVE), is(sameInstance(zeroBasedSeven)));

        GenomicPosition zeroNeg = zeroBasedSeven.withStrand(Strand.NEGATIVE);
        assertThat(zeroNeg.position(), equalTo(Position.of(3, ConfidenceInterval.of(-2, 1))));
        assertThat(zeroNeg.strand(), equalTo(Strand.NEGATIVE));
        assertThat(zeroNeg.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));

        assertThat(oneBasedThree.withStrand(Strand.POSITIVE), is(sameInstance(oneBasedThree)));

        GenomicPosition oneNeg = oneBasedThree.withStrand(Strand.NEGATIVE);
        assertThat(oneNeg.position(), equalTo(Position.of(8, ConfidenceInterval.of(-3, 2))));
        assertThat(oneNeg.strand(), equalTo(Strand.NEGATIVE));
        assertThat(oneNeg.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }

    @Test
    public void withCoordinateSystem() {
        assertThat(zeroBasedSeven.withCoordinateSystem(CoordinateSystem.ZERO_BASED), is(sameInstance(zeroBasedSeven)));

        GenomicPosition zpos = zeroBasedSeven.withCoordinateSystem(CoordinateSystem.ONE_BASED);
        assertThat(zpos.pos(), equalTo(8));
        assertThat(zpos.ci(), equalTo(zeroBasedSeven.ci()));
        assertThat(zpos.coordinateSystem(), is(CoordinateSystem.ONE_BASED));

        assertThat(oneBasedThree.withCoordinateSystem(CoordinateSystem.ONE_BASED), is(sameInstance(oneBasedThree)));

        GenomicPosition opos = oneBasedThree.withCoordinateSystem(CoordinateSystem.ZERO_BASED);
        assertThat(opos.pos(), equalTo(2));
        assertThat(opos.ci(), equalTo(oneBasedThree.ci()));
        assertThat(opos.coordinateSystem(), is(CoordinateSystem.ZERO_BASED));
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

    @ParameterizedTest
    @CsvSource({
            "2,1",
            "3,0",
            "4,-1"})
    public void compareToWhenDifferingPositions(int pos, int expected) {
        GenomicPosition gp = new GenomicPositionDefault(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(pos));

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
        GenomicPosition gp = new GenomicPositionDefault(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(pos));
        assertThat(oneBasedThree.isUpstreamOf(gp), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "2,2,false",
            "3,3,false",
            "4,4,true",
            "2,4,false"})
    public void isUpstreamOfRegion(int start, int end, boolean expected) {
        GenomicRegion gr = ContigRegion.oneBased(ctg1, Strand.POSITIVE, Position.of(start), Position.of(end));
        assertThat(oneBasedThree.isUpstreamOf(gr), equalTo(expected));
    }


    @ParameterizedTest
    @CsvSource({
            "2,true",
            "3,false",
            "4,false"})
    public void isDownstreamOfPosition(int pos, boolean expected) {
        GenomicPosition gp = new GenomicPositionDefault(ctg1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, Position.of(pos));
        assertThat(oneBasedThree.isDownstreamOf(gp), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "2,2,true",
            "3,3,false",
            "4,4,false",
            "2,4,false"})
    public void isDownstreamOfRegion(int start, int end, boolean expected) {
        GenomicRegion region = ContigRegion.oneBased(ctg1, Strand.POSITIVE, Position.of(start), Position.of(end));
        assertThat(oneBasedThree.isDownstreamOf(region), equalTo(expected));
    }
}