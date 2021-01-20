package org.monarchinitiative.svart.util;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.BreakendVariant;
import org.monarchinitiative.svart.impl.TestVariants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VcfBreakendFormatterTest {

    @Test
    public void makeAltVcfField_PosPos() {
        BreakendVariant variant = TestVariants.breakendVariant_UV();
        String alt = VcfBreakendFormatter.makeAltVcfField(variant);
        assertThat(alt, equalTo("C[2:321682["));
    }

    @Test
    public void makeAltVcfField_NegPos() {
        BreakendVariant variant = TestVariants.breakendVariant_XZ();
        String alt = VcfBreakendFormatter.makeAltVcfField(variant);
        assertThat(alt, equalTo("[17:198983[A"));
    }

    @Test
    public void makeAltVcfField_NegNeg() {
        BreakendVariant variant = TestVariants.breakendVariant_VU();

        String alt = VcfBreakendFormatter.makeAltVcfField(variant);

        assertThat(alt, equalTo("]13:123456]T"));
    }

    // with inserted sequence

    @Test
    public void makeAltVcfField_PosPos_withInsertedSeq() {
        BreakendVariant variant = TestVariants.breakendVariant_UV_withInsertion();

        String alt = VcfBreakendFormatter.makeAltVcfField(variant);

        assertThat(alt, equalTo("CAGTNNNNNCA[2:321682["));
    }

    @Test
    public void makeAltVcfField_NegNeg_withInsertedSeq() {
        BreakendVariant variant = TestVariants.breakendVariant_VU_withInsertion();

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
}