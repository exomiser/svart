package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.Breakend;
import org.monarchinitiative.variant.api.Position;
import org.monarchinitiative.variant.api.Strand;
import org.monarchinitiative.variant.api.TestContigs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class VcfBreakendFormatterTest {

    @Test
    public void makeAltVcfField_PosPos() {
        BreakendVariant variant = TestVariants.bnd_U();
        String alt = VcfBreakendFormatter.makeAltVcfField(variant);
        assertThat(alt, equalTo("C[2:321682["));
    }

    @Test
    public void makeAltVcfField_NegPos() {
        BreakendVariant variant = TestVariants.bnd_X();
        String alt = VcfBreakendFormatter.makeAltVcfField(variant);
        assertThat(alt, equalTo("[17:198983[A"));
    }

    @Test
    public void makeAltVcfField_NegNeg() {
        BreakendVariant variant = TestVariants.bnd_V();

        String alt = VcfBreakendFormatter.makeAltVcfField(variant);

        assertThat(alt, equalTo("]13:123456]T"));
    }

    // with inserted sequence

    @Test
    public void makeAltVcfField_PosPos_withInsertedSeq() {
        BreakendVariant variant = TestVariants.bnd_U_withInserted();

        String alt = VcfBreakendFormatter.makeAltVcfField(variant);

        assertThat(alt, equalTo("CAGTNNNNNCA[2:321682["));
    }

    @Test
    public void makeAltVcfField_NegNeg_withInsertedSeq() {
        BreakendVariant variant = TestVariants.bnd_V_withInserted();

        String alt = VcfBreakendFormatter.makeAltVcfField(variant);

        assertThat(alt, equalTo("]13:123456]AGTNNNNNCAT"));
    }

    // unresolved breakend
    @Test
    public void makeAltVcfField_leftUnresolved() {
        BreakendVariant variant = TestVariants.bnd_X_leftUnresolved();

        String alt = VcfBreakendFormatter.makeAltVcfField(variant);

        assertThat(alt, equalTo(".A"));
    }

    @Test
    public void makeAltVcfField_rightUnresolved() {
        BreakendVariant variant = TestVariants.bnd_W_rightUnresolved();

        String alt = VcfBreakendFormatter.makeAltVcfField(variant);

        assertThat(alt, equalTo("G."));
    }

    @Test
    public void makeAltVcfField_invalidStrand() {
        Breakend bnd_U = PartialBreakend.oneBased(TestContigs.chr13, "bnd_U", Strand.UNSTRANDED, Position.of(123_456));
        Breakend bnd_V = PartialBreakend.oneBased(TestContigs.chr2, "bnd_V", Strand.POSITIVE, Position.of(321_682));

        String alt = VcfBreakendFormatter.makeAltVcfField(bnd_U, bnd_V, "C", "");

        assertThat(alt, equalTo("<BND>"));
    }
}