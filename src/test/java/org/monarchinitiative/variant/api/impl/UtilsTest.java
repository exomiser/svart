package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.Breakend;
import org.monarchinitiative.variant.api.Position;
import org.monarchinitiative.variant.api.Strand;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class UtilsTest {


    @Test
    public void makeAltVcfField_PosPos() {
        BreakendVariant variant = TestVariants.bnd_U();
        Optional<String> altOpt = Utils.makeAltVcfField(variant);

        assertThat(altOpt.isPresent(), equalTo(true));

        String alt = altOpt.get();

        assertThat(alt, equalTo("C[2:321682["));
    }

    @Test
    public void makeAltVcfField_NegPos() {
        BreakendVariant variant = TestVariants.bnd_X();

        Optional<String> altOpt = Utils.makeAltVcfField(variant);

        assertThat(altOpt.isPresent(), equalTo(true));
        String alt = altOpt.get();
        assertThat(alt, equalTo("[17:198983[A"));
    }

    @Test
    public void makeAltVcfField_NegNeg() {
        BreakendVariant variant = TestVariants.bnd_V();

        Optional<String> altOpt = Utils.makeAltVcfField(variant);

        assertThat(altOpt.isPresent(), equalTo(true));
        String alt = altOpt.get();
        assertThat(alt, equalTo("]13:123456]T"));
    }

    // with inserted sequence

    @Test
    public void makeAltVcfField_PosPos_withInsertedSeq() {
        BreakendVariant variant = TestVariants.bnd_U_withInserted();

        Optional<String> altOpt = Utils.makeAltVcfField(variant);

        assertThat(altOpt.isPresent(), equalTo(true));
        String alt = altOpt.get();
        assertThat(alt, equalTo("CAGTNNNNNCA[2:321682["));
    }

    @Test
    public void makeAltVcfField_NegNeg_withInsertedSeq() {
        BreakendVariant variant = TestVariants.bnd_V_withInserted();

        Optional<String> altOpt = Utils.makeAltVcfField(variant);

        assertThat(altOpt.isPresent(), equalTo(true));
        String alt = altOpt.get();
        assertThat(alt, equalTo("]13:123456]AGTNNNNNCAT"));
    }

    // unresolved breakend
    @Test
    public void makeAltVcfField_leftUnresolved() {
        BreakendVariant variant = TestVariants.bnd_X_leftUnresolved();

        Optional<String> altOpt = Utils.makeAltVcfField(variant);

        assertThat(altOpt.isPresent(), equalTo(true));
        String alt = altOpt.get();
        assertThat(alt, equalTo(".A"));
    }

    @Test
    public void makeAltVcfField_rightUnresolved() {
        BreakendVariant variant = TestVariants.bnd_W_rightUnresolved();

        Optional<String> altOpt = Utils.makeAltVcfField(variant);

        assertThat(altOpt.isPresent(), equalTo(true));
        String alt = altOpt.get();
        assertThat(alt, equalTo("G."));
    }

    @Test
    public void makeAltVcfField_invalidStrand() {
        Breakend bnd_U = PartialBreakend.oneBased("bnd_U", TestVariants.chr13, Strand.UNSTRANDED, Position.of(123_456));
        Breakend bnd_V = PartialBreakend.oneBased("bnd_V", TestVariants.chr2, Strand.POSITIVE, Position.of(321_682));

        Optional<String> altOpt = Utils.makeAltVcfField(bnd_U, bnd_V, "C", "");

        assertThat(altOpt.isPresent(), equalTo(false));
    }
}