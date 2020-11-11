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
        Breakend left = PartialBreakend.of(chr2, Position.of(4150333, -8,10, CoordinateSystem.ONE_BASED), Strand.POSITIVE, "28382_1");
        Breakend right = PartialBreakend.of(chr13, Position.of(33423926, -2,1, CoordinateSystem.ONE_BASED), Strand.POSITIVE, "28382_2");
        BreakendVariant instance = new BreakendVariant("28382", left, right, "N", "N");
        assertThat(instance.getContig(), equalTo(chr2));
        assertThat(instance.getStartPosition().getPos(), equalTo(4150333));
        assertThat(instance.getRef(), equalTo("N"));
        assertThat(instance.getAlt(), equalTo("N"));
        assertThat(instance.getLeft().getContig(), equalTo(chr2));
        assertThat(instance.getLeft().getPos(), equalTo(4150333));
        assertThat(instance.getRight().getContig(), equalTo(chr13));
        assertThat(instance.getRight().getPos(), equalTo(33423926));

        System.out.println(instance.withStrand(Strand.NEGATIVE));
        // 13      33423926        28382_2 N       N]2:4150333]    284.1   PASS    SVTYPE=BND;POS=33423926;STRANDS=++:31;CIPOS=-2,1;CIEND=-8,10;CIPOS95=0,0;CIEND95=0,0;MATEID=28382_1;EVENT=28382;
        Breakend matedLeft = PartialBreakend.of(chr13, Position.of(33423926, -2,1, CoordinateSystem.ONE_BASED), Strand.POSITIVE, "28382_2");
        Breakend matedRight = PartialBreakend.of(chr2, Position.of(4150333, -8,10, CoordinateSystem.ONE_BASED), Strand.POSITIVE, "28382_1");
        BreakendVariant matedInstance = new BreakendVariant("28382", matedLeft, matedRight, "N", "N");

        // these can be combined using their id and mateids to recreate the full SequenceRearrangement
    }

    @Test
    void insertedSequence() {
        // #CHROM  POS  ID  REF  ALT  QUAL  FILTER  INFO
        // 2  321682  bndV  T  ]13:123456]AGTNNNNNCAT  6  PASS  SVTYPE=BND;MATEID=bndU
        // 13  123456  bndU  C  CAGTNNNNNCA[2:321682[  6  PASS  SVTYPE=BND;MATEID=bndV
        Breakend left = PartialBreakend.of(chr2, Position.of(321682), Strand.POSITIVE, "bndV");
        Breakend right = PartialBreakend.of(chr13, Position.of(123456), Strand.POSITIVE, "bndU");

        BreakendVariant first = new BreakendVariant("", left, right, "T", "AGTNNNNNCAT");
        System.out.println(first);
        System.out.println(first.toOppositeStrand());
        BreakendVariant second = new BreakendVariant("", right, left, "C", "CAGTNNNNNCA");
    }

    @Test
    void positiveNegativeStrand() {
        // 2       135592459       28388_1 N       N[13:36400702[  2049.07 LOW     SVTYPE=BND;POS=135592459;STRANDS=+-:125;CIPOS=-2,10;CIEND=-9,9;CIPOS95=0,0;CIEND95=0,0;MATEID=28388_2;EVENT=28388;
        // 13      36400702        28388_2 N       ]2:135592459]N  2049.07 LOW     SVTYPE=BND;POS=36400702;STRANDS=+-:125;CIPOS=-9,9;CIEND=-2,10;CIPOS95=0,0;CIEND95=0,0;MATEID=28388_1;EVENT=28388;

    }

    @Test
    void negativeNegativeStrand() {
        // 1       10567   2838_1  N       [15:102520406[N 47911.3 LOW     SVTYPE=BND;POS=10567;STRANDS=--:97;IMPRECISE;CIPOS=-205,24;CIEND=-104,42;CIPOS95=-20,20;CIEND95=-6,6;MATEID=2838_2;EVENT=2838;
        //15      102520406       2838_2  N       [1:10567[N      47911.3 LOW     SVTYPE=BND;POS=102520406;STRANDS=--:97;IMPRECISE;CIPOS=-104,42;CIEND=-205,24;CIPOS95=-6,6;CIEND95=-20,20;MATEID=2838_1;EVENT=2838;

    }
}