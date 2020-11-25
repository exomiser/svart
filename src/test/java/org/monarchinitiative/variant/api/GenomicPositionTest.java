package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.impl.ContigDefault;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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
        assertThat(zeroBased.min(), equalTo(6));
        assertThat(zeroBased.max(), equalTo(9));
        assertThat(zeroBased.strand(), equalTo(Strand.POSITIVE));
        assertThat(zeroBased.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));
        assertThat(zeroBased.toString(), equalTo("[1:7(±1,2))+"));

        assertThat(oneBased.contig(), equalTo(ctg1));
        assertThat(oneBased.position(), equalTo(Position.of(3, ConfidenceInterval.of(-2, 3))));
        assertThat(oneBased.pos(), equalTo(3));
        assertThat(oneBased.ci(), equalTo(ConfidenceInterval.of(-2, 3)));
        assertThat(oneBased.min(), equalTo(1));
        assertThat(oneBased.max(), equalTo(6));
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

        GenomicRegion containing = GenomicRegionTest.ContigRegion.oneBased(ctg1, Strand.POSITIVE, Position.of(2), Position.of(4));
        assertThat(oneBased.differenceTo(containing), equalTo(0));
    }

    @Test
    public void differenceToRegionWhenOnDifferentContig() {
        Contig ctg2 = new ContigDefault(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 20, "", "", "");
        GenomicRegion other = GenomicRegionTest.ContigRegion.oneBased(ctg2, Strand.POSITIVE, Position.of(5), Position.of(6));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> zeroBased.differenceTo(other));
        assertThat(e.getMessage(), equalTo("Coordinates are on different chromosomes: 1 vs. 2"));
    }

    @Test
    public void withStrand() {
        assertThat(zeroBased.withStrand(Strand.POSITIVE), is(sameInstance(zeroBased)));

        GenomicPosition zeroNeg = zeroBased.withStrand(Strand.NEGATIVE);
        assertThat(zeroNeg.position(), equalTo(Position.of(3, ConfidenceInterval.of(-2, 1))));
        assertThat(zeroNeg.strand(), equalTo(Strand.NEGATIVE));
        assertThat(zeroNeg.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));

        assertThat(oneBased.withStrand(Strand.POSITIVE), is(sameInstance(oneBased)));

        GenomicPosition oneNeg = oneBased.withStrand(Strand.NEGATIVE);
        assertThat(oneNeg.position(), equalTo(Position.of(8, ConfidenceInterval.of(-3, 2))));
        assertThat(oneNeg.strand(), equalTo(Strand.NEGATIVE));
        assertThat(oneNeg.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }

    @Test
    public void withCoordinateSystem() {
        assertThat(zeroBased.withCoordinateSystem(CoordinateSystem.ZERO_BASED), is(sameInstance(zeroBased)));

        GenomicPosition zpos = zeroBased.withCoordinateSystem(CoordinateSystem.ONE_BASED);
        assertThat(zpos.pos(), equalTo(8));
        assertThat(zpos.ci(), equalTo(zeroBased.ci()));
        assertThat(zpos.coordinateSystem(), is(CoordinateSystem.ONE_BASED));

        assertThat(oneBased.withCoordinateSystem(CoordinateSystem.ONE_BASED), is(sameInstance(oneBased)));

        GenomicPosition opos = oneBased.withCoordinateSystem(CoordinateSystem.ZERO_BASED);
        assertThat(opos.pos(), equalTo(2));
        assertThat(opos.ci(), equalTo(oneBased.ci()));
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


}