package org.monarchinitiative.svart.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.*;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VcfBreakendFormatterTest {

    @ParameterizedTest
    @CsvSource({
            //#STRAND CHROM  START END  STRAND CHROM  START END  REF INS   ALT
            "POSITIVE, 1, 3, 3,   POSITIVE, 2, 7, 7, G, '',    G[2:8[",
            "POSITIVE, 1, 3, 3,   NEGATIVE, 2, 7, 7, G, '',    G]2:8]",
            "NEGATIVE, 1, 3, 3,   POSITIVE, 2, 7, 7, C, '',    [2:8[G",
            "NEGATIVE, 1, 3, 3,   NEGATIVE, 2, 7, 7, C, '',    ]2:8]G",
            "POSITIVE, 1, 3, 3,   POSITIVE, 2, 7, 7, G, AGTC,  GAGTC[2:8[",
            "NEGATIVE, 1, 3, 3,   POSITIVE, 2, 7, 7, C, GACT,  [2:8[AGTCG",
            "POSITIVE, 1, 3, 3,   POSITIVE, 0, 0, 0, G, '',    G.",
            "NEGATIVE, 1, 3, 3,   POSITIVE, 0, 0, 0, C, '',    .G",
    })
    public void makeAltVcfField(Strand leftStrand, String leftChr, int leftStart, int leftEnd,
                          Strand rightStrand, String rightChr, int rightStart, int rightEnd,
                          String ref, String inserted,
                          String expected) {
        GenomicAssembly assembly = testAssembly(TestContig.of(1, 5), TestContig.of(2, 10));
        Breakend left = makeBreakend(leftStrand, leftChr, leftStart, leftEnd, assembly);
        Breakend right = makeBreakend(rightStrand, rightChr, rightStart, rightEnd, assembly);
        BreakendVariant variant = BreakendVariant.of("", left, right, ref, inserted);
        assertThat(VcfBreakendFormatter.makeAltVcfField(variant), equalTo(expected));
    }

    public Breakend makeBreakend(Strand strand, String chr, int start, int end, GenomicAssembly assembly) {
        if (assembly.contigByName(chr) == Contig.unknown()) {
            return Breakend.unresolved(CoordinateSystem.LEFT_OPEN);
        }
        return Breakend.of(assembly.contigByName(chr), "", Strand.POSITIVE, CoordinateSystem.LEFT_OPEN, Position.of(start), Position.of(end)).withStrand(strand);
    }

    private GenomicAssembly testAssembly(Contig... contigs) {
        return GenomicAssembly.of("TestAssembly", "Wookie", "9607", "Han", "3021-01-15", "GB1", "RS1", Arrays.asList(contigs));
    }
}