package org.monarchinitiative.svart.impl;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DefaultGenomicBreakendTest {

    private static final Contig ctg1 = TestContig.of(1, 10);

    @Test
    public void properties() {
        int position = 3;
        ConfidenceInterval ciPos = ConfidenceInterval.of(-2, 1);
        Coordinates coordinates = Coordinates.of(CoordinateSystem.zeroBased(), position, ciPos, position, ciPos);
        GenomicBreakend breakend = DefaultGenomicBreakend.of(ctg1, "a", Strand.POSITIVE, coordinates);

        assertThat(breakend.contig(), equalTo(ctg1));
        assertThat(breakend.strand(), equalTo(Strand.POSITIVE));
        assertThat(breakend.coordinates(), equalTo(coordinates));
        assertThat(breakend.id(), equalTo("a"));
    }

    @Test
    public void withStrand() {
        int position = 3;
        ConfidenceInterval ciPos = ConfidenceInterval.of(-2, 1);
        Coordinates coordinates = Coordinates.of(CoordinateSystem.zeroBased(), position, ciPos, position, ciPos);
        GenomicBreakend posBreakend = DefaultGenomicBreakend.of(ctg1, "a", Strand.POSITIVE, coordinates);

        assertThat(posBreakend.withStrand(Strand.POSITIVE), is(sameInstance(posBreakend)));

        GenomicBreakend negBreakend = posBreakend.withStrand(Strand.NEGATIVE);
        assertThat(negBreakend.contig(), equalTo(ctg1));
        assertThat(negBreakend.strand(), equalTo(Strand.NEGATIVE));
        assertThat(negBreakend.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));
        assertThat(negBreakend.start(), equalTo(7));
        assertThat(negBreakend.end(), equalTo(7));
        assertThat(negBreakend.id(), is("a"));
    }

    @Test
    public void unresolved() {
        GenomicBreakend unresolved = GenomicBreakend.unresolved(CoordinateSystem.ZERO_BASED);
        assertThat(unresolved.isUnresolved(), equalTo(true));
        assertThat(unresolved.contig(), equalTo(Contig.unknown()));
        assertThat(unresolved.strand(), equalTo(Strand.POSITIVE));
        assertThat(unresolved.coordinateSystem(), equalTo(CoordinateSystem.ZERO_BASED));
        assertThat(unresolved.start(), equalTo(0));
        assertThat(unresolved.end(), equalTo(0));
        assertThat(unresolved.id(), equalTo(""));
    }
}