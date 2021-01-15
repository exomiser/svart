package org.monarchinitiative.variant.api.parsers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.variant.api.*;
import org.monarchinitiative.variant.api.impl.BreakendVariant;
import org.monarchinitiative.variant.api.impl.DefaultGenomicAssembly;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.monarchinitiative.variant.api.AssignedMoleculeType.CHROMOSOME;
import static org.monarchinitiative.variant.api.SequenceRole.ASSEMBLED_MOLECULE;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
public class BreakendParserTest {

    public static final Contig chr1 = Contig.of(1, "1", ASSEMBLED_MOLECULE, "1", CHROMOSOME, 249250621, "NC_000001.10", "CM000663.1", "chr1");
    public static final Contig chr2 = Contig.of(2, "2", ASSEMBLED_MOLECULE, "2", CHROMOSOME, 243_199_373, "CM000664.1", "NC_000002.11", "chr2");
    public static final Contig chr13 = Contig.of(3, "13", ASSEMBLED_MOLECULE, "13", CHROMOSOME, 115_169_878, "CM000675.1", "NC_000013.10", "chr13");
    public static final Contig chr17 = Contig.of(4, "17", ASSEMBLED_MOLECULE, "17", CHROMOSOME, 83_257_441, "CM000679.2", "NC_000017.11", "chr17");

    private static GenomicAssembly ASSEMBLY;

    private BreakendParser parser;

    @BeforeAll
    public static void beforeAll() {
        List<Contig> contigs = List.of(chr1, chr2, chr13, chr17);
        ASSEMBLY = DefaultGenomicAssembly.builder().name("TestAssembly").name("test").organismName("Wookie")
                .taxId("9607").date("3021-01-15").submitter("Han").genBankAccession("GB1").refSeqAccession("RS1")
                .contigs(contigs)
                .build();
    }

    @BeforeEach
    public void setUp() {
        parser = new BreakendParser(ASSEMBLY);
    }

    @Test
    public void resolve_PosPos() {
        // 13	123456	bnd_U	C	C[2:321682[	6	PASS	SVTYPE=BND;MATEID=bnd_V;EVENT=tra2
        BreakendVariant variant = parser.resolve("tra2", "bnd_U", "bnd_V", chr13,
                Position.of(123_456), ConfidenceInterval.precise(), "C", "C[2:321682[");

        // Breakended bits
        assertThat(variant.mateId(), equalTo("bnd_V"));
        assertThat(variant.eventId(), equalTo("tra2"));

        // Variant bits
        assertThat(variant.id(), equalTo("bnd_U"));
        assertThat(variant.ref(), equalTo("C"));
        assertThat(variant.alt(), equalTo("")); // no inserted sequence
        assertThat(variant.refLength(), equalTo(1));
        assertThat(variant.changeLength(), equalTo(0)); // length of the inserted sequence
        assertThat(variant.length(), equalTo(0)); // length of the inserted sequence
        assertThat(variant.variantType(), equalTo(VariantType.BND));
        assertThat(variant.isSymbolic(), equalTo(true));

        // Left breakend
        Breakend left = variant.left();
        assertThat(left.id(), equalTo("bnd_U"));
        assertThat(left.isUnresolved(), equalTo(false));
        assertThat(left.contig(), equalTo(chr13));
        assertThat(left.startPosition(), equalTo(Position.of(123_456)));
        assertThat(left.endPosition(), equalTo(Position.of(123_456)));
        assertThat(left.strand(), equalTo(Strand.POSITIVE));
        assertThat(left.coordinateSystem(), equalTo(CoordinateSystem.oneBased()));

        // Right breakend
        Breakend right = variant.right();
        assertThat(right.id(), equalTo("bnd_V"));
        assertThat(right.isUnresolved(), equalTo(false));
        assertThat(right.contig(), equalTo(chr2));
        assertThat(right.startPosition(), equalTo(Position.of(321_682)));
        assertThat(right.endPosition(), equalTo(Position.of(321_682)));
        assertThat(right.strand(), equalTo(Strand.POSITIVE));
        assertThat(right.coordinateSystem(), equalTo(CoordinateSystem.oneBased()));
    }

    @Test
    public void resolve_NegPos() {
        // 13  123457  bnd_X   A   [17:198983[A    6   PASS    SVTYPE=BND;MATEID=bnd_Z;EVENT=tra3
        BreakendVariant variant = parser.resolve("tra3", "bnd_X", "bnd_Z", chr13,
                Position.of(123_457), ConfidenceInterval.precise(), "A", "[17:198983[A");

        assertThat(variant.left().contig(), equalTo(chr13));
        assertThat(variant.left().startPosition(), equalTo(Position.of(chr13.length() - 123_457 + 1)));
        assertThat(variant.left().strand(), equalTo(Strand.NEGATIVE));

        assertThat(variant.right().contig(), equalTo(chr17));
        assertThat(variant.right().startPosition(), equalTo(Position.of(198_983)));
        assertThat(variant.right().strand(), equalTo(Strand.POSITIVE));
    }

    @Test
    public void resolve_PosNeg() {
        // 2	321681	bnd_W	G	G]17:198982]	6	PASS	SVTYPE=BND;MATEID=bnd_Y;EVENT=tra1
        BreakendVariant variant = parser.resolve("tra1", "bnd_W", "bnd_Y", chr2,
                Position.of(321_681), ConfidenceInterval.precise(), "G", "G]17:198982]");

        assertThat(variant.left().contig(), equalTo(chr2));
        assertThat(variant.left().startPosition(), equalTo(Position.of(321_681)));
        assertThat(variant.left().strand(), equalTo(Strand.POSITIVE));

        assertThat(variant.right().contig(), equalTo(chr17));
        assertThat(variant.right().startPosition(), equalTo(Position.of(chr17.length() - 198_982 + 1)));
        assertThat(variant.right().strand(), equalTo(Strand.NEGATIVE));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "C,  C(2:321682[,      Invalid breakend alt record C(2:321682[",
            "C,  C[2:32I682[,      Invalid breakend alt record C[2:32I682[",
            "C,  C[X:321682[,      Unknown mate contig `X`",
            "C,  C[2:321682[G,     Sequence present both at the beginning (`C`) and the end (`G`) of alt field",
            "C,  G[2:321682[,      Invalid breakend alt `G[2:321682[`. No match for ref allele `C` neither at the beginning nor at the end",
            "C,  [2:321682[G,      Invalid breakend alt `[2:321682[G`. No match for ref allele `C` neither at the beginning nor at the end",
            "C,  C[2:321682],      Invalid bracket orientation in `C[2:321682]`",
    })
    public void resolve_invalidInput(String ref, String alt, String message) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> parser.resolve("tra2", "bnd_U", "bnd_V", chr13,
                Position.of(123_456), ConfidenceInterval.precise(), ref, alt));
        assertThat(e.getMessage(), equalTo(message));
    }
}