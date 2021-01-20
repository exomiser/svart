package org.monarchinitiative.svart.impl;

import org.hamcrest.MatcherAssert;
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
        DefaultBreakend three = DefaultBreakend.of(ctg1, "a", Strand.POSITIVE, CoordinateSystem.oneBased(), position);

        MatcherAssert.assertThat(three.contig(), equalTo(ctg1));
        MatcherAssert.assertThat(three.contigId(), equalTo(1));
        MatcherAssert.assertThat(three.contigName(), equalTo("1"));
        MatcherAssert.assertThat(three.startPosition(), equalTo(position));
        MatcherAssert.assertThat(three.strand(), equalTo(Strand.POSITIVE));
        assertThat(three.id(), equalTo("a"));
    }

    @Test
    public void withStrand() {
        Position position = Position.of(3, -2, 1);
        DefaultBreakend three = DefaultBreakend.of(ctg1, "a", Strand.POSITIVE, CoordinateSystem.oneBased(), position);

        MatcherAssert.assertThat(three.withStrand(Strand.POSITIVE), is(sameInstance(three)));

        DefaultBreakend breakend = three.withStrand(Strand.NEGATIVE);
        MatcherAssert.assertThat(breakend.contig(), equalTo(ctg1));
        MatcherAssert.assertThat(breakend.start(), equalTo(8));
        MatcherAssert.assertThat(breakend.startPosition().confidenceInterval(), equalTo(ConfidenceInterval.of(-1, 2)));
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