package org.monarchinitiative.svart.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The tests use examples listed in section 5.4. of
 * <a href="http://samtools.github.io/hts-specs/VCFv4.2.pdf">VCF 4.2</a> specs.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class DefaultBreakendVariantTest {

    private static final Contig chr2 = TestContigs.chr2;
    private static final Contig chr13 = TestContigs.chr13;

    /**
     * Make variant representing event consisting of <em>bnd_U</em> and <em>bnd_V</em> described in section 5.4 of VCF specs.
     * <p>
     * The variant joins <em>chr13</em> {@link Strand#POSITIVE} to <em>chr2</em> {@link Strand#POSITIVE}.
     * <p>
     * <pre>13	123456	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2</pre>
     */
    @Test
    public void variantProperties() {
        // 13	123456	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
        Breakend bnd_U = Breakend.of(chr13, "bnd_U", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(123_457), Position.of(123_456));
        Breakend bnd_V = Breakend.of(chr2, "bnd_V", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(321_682), Position.of(321_681));
        BreakendVariant variant = BreakendVariant.of("tra2", bnd_U, bnd_V, "C", "");
        assertThat(variant.contig(), equalTo(chr13));
        assertThat(variant.start(), equalTo(123_456));
        assertThat(variant.end(), equalTo(123_456));
        assertThat(variant.strand(), equalTo(Strand.POSITIVE));
        assertThat(variant.variantType(), equalTo(VariantType.BND));
        assertThat(variant.coordinateSystem(), equalTo(CoordinateSystem.oneBased()));
        assertThat(variant.isBreakend(), equalTo(true));

        assertThat(variant.length(), equalTo(1));
        assertThat(variant.changeLength(), equalTo(0));

        assertThat(variant.isSymbolic(), equalTo(true));
        assertThat(variant.variantType(), equalTo(VariantType.BND));
        assertThat(variant.id(), equalTo("bnd_U"));
        assertThat(variant.right().mateId(), equalTo("bnd_V"));
        assertThat(variant.eventId(), equalTo("tra2"));
    }

    @Test
    public void throwsExceptionWithMixedCoordinatesSystems() {
        Breakend left = Breakend.of(chr13, "left", Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(123_457), Position.of(123_456));
        Breakend right = Breakend.of(chr2, "right", Strand.POSITIVE, CoordinateSystem.LEFT_OPEN, Position.of(321_681), Position.of(321_681));
        Exception exception = assertThrows(IllegalStateException.class, () -> DefaultBreakendVariant.of("", left, right, "", ""));
        assertThat(exception.getMessage(), equalTo("Breakend variant left and right breakends must have same coordinate system!"));
    }

    @Test
    public void throwsExceptionWithLeftUnplacedBreakend() {
        Breakend left = Breakend.unresolved(CoordinateSystem.LEFT_OPEN);
        Breakend right = Breakend.of(chr2, "right", Strand.POSITIVE, CoordinateSystem.LEFT_OPEN, Position.of(321_681), Position.of(321_681));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> DefaultBreakendVariant.of("", left, right, "", ""));
        assertThat(exception.getMessage(), equalTo("Left breakend cannot be unresolved."));
    }
}