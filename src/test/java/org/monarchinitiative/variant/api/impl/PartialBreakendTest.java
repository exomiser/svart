package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PartialBreakendTest {

    private static final Contig ctg1 = new ContigDefault(1, "1", SequenceRole.ASSEMBLED_MOLECULE, 10, "", "", "chr1");

    private PartialBreakend oneBasedThree;

    @BeforeEach
    public void setUp() {
        oneBasedThree = PartialBreakend.oneBased("a", ctg1, Strand.POSITIVE, Position.of(3, -2, 1));
    }

    @Test
    public void properties() {
        assertThat(oneBasedThree.contig(), equalTo(ctg1));
        assertThat(oneBasedThree.contigId(), equalTo(1));
        assertThat(oneBasedThree.contigName(), equalTo("1"));
        assertThat(oneBasedThree.position(), equalTo(Position.of(3, ConfidenceInterval.of(-2, 1))));
        assertThat(oneBasedThree.pos(), equalTo(3));
        assertThat(oneBasedThree.ci(), equalTo(ConfidenceInterval.of(-2, 1)));
        assertThat(oneBasedThree.min(), equalTo(1));
        assertThat(oneBasedThree.max(), equalTo(4));
        assertThat(oneBasedThree.strand(), equalTo(Strand.POSITIVE));
        assertThat(oneBasedThree.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
        assertThat(oneBasedThree.toString(), equalTo("BND(a)[1:3(±2,1)]+"));
    }

    @Test
    public void withStrand() {
        assertThat(oneBasedThree.withStrand(Strand.POSITIVE), is(sameInstance(oneBasedThree)));

        PartialBreakend breakend = oneBasedThree.withStrand(Strand.NEGATIVE);
        assertThat(breakend.contig(), equalTo(ctg1));
        assertThat(breakend.position(), equalTo(Position.of(8, -1, 2)));
        assertThat(breakend.id(), is("a"));
    }

    @Test
    public void withCoordinateSystem() {
        assertThat(oneBasedThree.withCoordinateSystem(CoordinateSystem.ONE_BASED), is(sameInstance(oneBasedThree)));

        PartialBreakend breakend = oneBasedThree.withCoordinateSystem(CoordinateSystem.ZERO_BASED);
        assertThat(breakend.contig(), equalTo(ctg1));
        assertThat(breakend.position(), equalTo(Position.of(2, -2, 1)));
        assertThat(breakend.id(), is("a"));
    }

    @Test
    public void zeroBasedPartialBreakend() {
        PartialBreakend pos = PartialBreakend.zeroBased("a", ctg1, Strand.POSITIVE, Position.of(2));

        assertThat(pos.id(), equalTo("a"));
        assertThat(pos.contig(), equalTo(ctg1));
        assertThat(pos.strand(), equalTo(Strand.POSITIVE));
        assertThat(pos.position(), equalTo(Position.of(2)));
        assertThat(pos.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));
    }

    @Test
    public void oneBasedPartialBreakend() {
        PartialBreakend pos = PartialBreakend.oneBased("a", ctg1, Strand.POSITIVE, Position.of(3));

        assertThat(pos.id(), equalTo("a"));
        assertThat(pos.contig(), equalTo(ctg1));
        assertThat(pos.strand(), equalTo(Strand.POSITIVE));
        assertThat(pos.position(), equalTo(Position.of(3)));
        assertThat(pos.coordinateSystem(), equalTo(CoordinateSystem.ONE_BASED));
    }
}