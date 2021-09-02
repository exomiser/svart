package org.monarchinitiative.svart.impl;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DefaultBreakendTest {

    private static final Contig ctg1 = TestContig.of(1, 10);

    @Test
    public void properties() {
        Position position = Position.of(3, -2, 1);
        Breakend breakend = DefaultBreakend.of(ctg1, "a", Strand.POSITIVE, CoordinateSystem.zeroBased(), position, position);

        assertThat(breakend.contig(), equalTo(ctg1));
        assertThat(breakend.strand(), equalTo(Strand.POSITIVE));
        assertThat(breakend.coordinateSystem(), equalTo(CoordinateSystem.LEFT_OPEN));
        assertThat(breakend.start(), equalTo(3));
        assertThat(breakend.end(), equalTo(3));
        assertThat(breakend.id(), equalTo("a"));
    }

    @Test
    public void withStrand() {
        Position position = Position.of(3, -2, 1);
        Breakend posBreakend = DefaultBreakend.of(ctg1, "a", Strand.POSITIVE, CoordinateSystem.zeroBased(), position, position);

        assertThat(posBreakend.withStrand(Strand.POSITIVE), is(sameInstance(posBreakend)));

        Breakend negBreakend = posBreakend.withStrand(Strand.NEGATIVE);
        assertThat(negBreakend.contig(), equalTo(ctg1));
        assertThat(negBreakend.strand(), equalTo(Strand.NEGATIVE));
        assertThat(negBreakend.coordinateSystem(), equalTo(CoordinateSystem.LEFT_OPEN));
        assertThat(negBreakend.start(), equalTo(7));
        assertThat(negBreakend.end(), equalTo(7));
        assertThat(negBreakend.id(), is("a"));
    }

    @Test
    public void unresolved() {
        Breakend unresolved = Breakend.unresolved(CoordinateSystem.LEFT_OPEN);
        assertThat(unresolved.isUnresolved(), equalTo(true));
        assertThat(unresolved.contig(), equalTo(Contig.unknown()));
        assertThat(unresolved.strand(), equalTo(Strand.POSITIVE));
        assertThat(unresolved.coordinateSystem(), equalTo(CoordinateSystem.LEFT_OPEN));
        assertThat(unresolved.start(), equalTo(0));
        assertThat(unresolved.end(), equalTo(0));
        assertThat(unresolved.id(), equalTo(""));
    }
}