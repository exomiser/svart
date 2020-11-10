package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class BreakendVariantTest {

    private final Contig chr2 = Contig.of(2, "2", SequenceRole.ASSEMBLED_MOLECULE, 243199373, "CM000664.1", "NC_000002.11", "chr2");
    private final Contig chr13 = Contig.of(13, "13", SequenceRole.ASSEMBLED_MOLECULE, 115169878, "CM000675.1", "NC_000013.10", "chr2");

    @Test
    void matedBreakend() {
        // 2       4150333 28382_1 N       N]13:33423926]  284.1   PASS    SVTYPE=BND;POS=4150333;STRANDS=++:31;CIPOS=-8,10;CIEND=-2,1;CIPOS95=0,0;CIEND95=0,0;MATEID=28382_2;EVENT=28382;
        Breakend left = PartialBreakend.of(chr2, Position.of(4150333, -8,10, CoordinateSystem.ONE_BASED), Strand.POSITIVE, "N");
        Breakend right = PartialBreakend.of(chr13, Position.of(33423926, -1,1, CoordinateSystem.ONE_BASED), Strand.POSITIVE, "");
        BreakendVariant instance = new BreakendVariant("28382_1", "28382_2", "28382", left, right, "", VariantType.BND);
        assertThat(instance.getContig(), equalTo(chr2));
        assertThat(instance.getStartPosition().getPos(), equalTo(4150333));
        assertThat(instance.getRef(), equalTo("N"));
        assertThat(instance.getAlt(), equalTo(""));
        assertThat(instance.getLeft().getContig(), equalTo(chr2));
        assertThat(instance.getLeft().getPos(), equalTo(4150333));
        assertThat(instance.getRight().getContig(), equalTo(chr13));
        assertThat(instance.getRight().getPos(), equalTo(33423926));

        System.out.println(instance.withStrand(Strand.NEGATIVE));
        // 13      33423926        28382_2 N       N]2:4150333]    284.1   PASS    SVTYPE=BND;POS=33423926;STRANDS=++:31;CIPOS=-2,1;CIEND=-8,10;CIPOS95=0,0;CIEND95=0,0;MATEID=28382_1;EVENT=28382;

        // these can be combined using their id and mateids to recreate the full SequenceRearrangement
        // TODO: do we still need an adjacency or is a BreakendVariant OK? - there is a conflict with Adjacency::withStrand
        // possibly best to have a toAdjacency() which can be used in creating a SequenceRearrangement?
    }

}