package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PartialBreakendTest {

    private static final Contig ctg1 = TestContig.of(1, 10);

    @Test
    public void properties() {
        Position position = Position.of(3, -2, 1);
        PartialBreakend three = PartialBreakend.of(ctg1, "a", Strand.POSITIVE, CoordinateSystem.oneBased(), position, position);

        assertThat(three.contig(), equalTo(ctg1));
        assertThat(three.contigId(), equalTo(1));
        assertThat(three.contigName(), equalTo("1"));
        assertThat(three.startPosition(), equalTo(position));
        assertThat(three.strand(), equalTo(Strand.POSITIVE));
        assertThat(three.id(), equalTo("a"));
    }

    @Test
    public void withStrand() {
        Position position = Position.of(3, -2, 1);
        PartialBreakend three = PartialBreakend.of(ctg1, "a", Strand.POSITIVE, CoordinateSystem.oneBased(), position, position);

        assertThat(three.withStrand(Strand.POSITIVE), is(sameInstance(three)));

        PartialBreakend breakend = three.withStrand(Strand.NEGATIVE);
        assertThat(breakend.contig(), equalTo(ctg1));
        assertThat(breakend.start(), equalTo(8));
        assertThat(breakend.startPosition().confidenceInterval(), equalTo(ConfidenceInterval.of(-1, 2)));
        assertThat(breakend.id(), is("a"));
    }

    @Test
    public void unresolved() {
        Breakend unresolved = Breakend.unresolved();
        assertThat(unresolved.isUnresolved(), equalTo(true));
        assertThat(unresolved.contig(), equalTo(Contig.unknown()));
        assertThat(unresolved.strand(), equalTo(Strand.POSITIVE));
        assertThat(unresolved.id(), equalTo(""));
    }
}