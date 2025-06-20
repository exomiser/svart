package org.monarchinitiative.svart.variant;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.Strand;

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
class DefaultGenomicBreakendVariantTest {

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
    void variantProperties() {
        // 13	123456	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
        GenomicBreakend bnd_U = GenomicBreakend.of(chr13, "bnd_U", Strand.POSITIVE, CoordinateSystem.oneBased(), 123_457, 123_456);
        GenomicBreakend bnd_V = GenomicBreakend.of(chr2, "bnd_V", Strand.POSITIVE, CoordinateSystem.oneBased(), 321_682, 321_681);
        GenomicBreakendVariant variant = GenomicBreakendVariant.of("tra2", bnd_U, bnd_V, "C", "");
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
        assertThat(variant.mateId(), equalTo("bnd_V"));
        assertThat(variant.eventId(), equalTo("tra2"));
    }

    @Test
    void throwsExceptionWithMixedCoordinatesSystems() {
        GenomicBreakend left = GenomicBreakend.of(chr13, "left", Strand.POSITIVE, Coordinates.of(CoordinateSystem.ONE_BASED, 123_457, 123_456));
        GenomicBreakend right = GenomicBreakend.of(chr2, "right", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 321_681, 321_681);
        Exception exception = assertThrows(IllegalStateException.class, () -> DefaultGenomicBreakendVariant.of("", left, right, "", ""));
        assertThat(exception.getMessage(), equalTo("Breakend variant left and right breakends must have same coordinate system!"));
    }

    @Test
    void throwsExceptionWithLeftUnplacedBreakend() {
        GenomicBreakend left = GenomicBreakend.unresolved(CoordinateSystem.ZERO_BASED);
        GenomicBreakend right = GenomicBreakend.of(chr2, "right", Strand.POSITIVE, CoordinateSystem.ZERO_BASED, 321_681, 321_681);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> DefaultGenomicBreakendVariant.of("", left, right, "", ""));
        assertThat(exception.getMessage(), equalTo("Left breakend cannot be unresolved."));
    }

    @Test
    void builderTests() {
        GenomicBreakend bnd_U = GenomicBreakend.of(chr13, "bnd_U", Strand.POSITIVE, CoordinateSystem.oneBased(), 123_457, 123_456);
        GenomicBreakend bnd_V = GenomicBreakend.of(chr2, "bnd_V", Strand.POSITIVE, CoordinateSystem.oneBased(), 321_682, 321_681);

        GenomicBreakendVariant oneBasedPositive = DefaultGenomicBreakendVariant.builder()
                .breakendVariant("tra2", bnd_U, bnd_V, "C", "")
                .build().toOppositeStrand();

        GenomicBreakendVariant zeroBasedNegative = DefaultGenomicBreakendVariant.builder()
                .breakendVariant(oneBasedPositive)
                .asOneBased()
                .asZeroBased()
                .build();
        assertThat(oneBasedPositive, equalTo(zeroBasedNegative.toOneBased().toPositiveStrand()));
    }
}