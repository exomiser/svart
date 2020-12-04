package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PartialBreakendTest {

    private static final Contig ctg1 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, 10, "", "", "chr1");

    @Test
    public void properties() {
        PartialBreakend three = PartialBreakend.zeroBased(ctg1, "a", Strand.POSITIVE, Position.of(3, -2, 1));

        assertThat(three.contig(), equalTo(ctg1));
        assertThat(three.contigId(), equalTo(1));
        assertThat(three.contigName(), equalTo("1"));
        assertThat(three.position(), equalTo(Position.of(3, ConfidenceInterval.of(-2, 1))));
        assertThat(three.pos(), equalTo(3));
        assertThat(three.ci(), equalTo(ConfidenceInterval.of(-2, 1)));
        assertThat(three.min(), equalTo(1));
        assertThat(three.max(), equalTo(4));
        assertThat(three.strand(), equalTo(Strand.POSITIVE));
    }

    @Test
    public void withStrand() {
        PartialBreakend three = PartialBreakend.zeroBased(ctg1, "a", Strand.POSITIVE, Position.of(3, -2, 1));

        assertThat(three.withStrand(Strand.POSITIVE), is(sameInstance(three)));

        PartialBreakend breakend = three.withStrand(Strand.NEGATIVE);
        assertThat(breakend.contig(), equalTo(ctg1));
        assertThat(breakend.position(), equalTo(Position.of(7, -1, 2)));
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