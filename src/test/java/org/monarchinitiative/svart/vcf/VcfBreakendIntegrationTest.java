package org.monarchinitiative.svart.vcf;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.monarchinitiative.svart.ConfidenceInterval;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.Strand;
import org.monarchinitiative.svart.sequence.VariantTrimmer;
import org.monarchinitiative.svart.GenomicBreakendVariant;
import org.monarchinitiative.svart.GenomicVariant;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class VcfBreakendIntegrationTest {

    private final GenomicAssembly b37 = GenomicAssembly.readAssembly(Path.of("src/test/resources/GCF_000001405.25_GRCh37.p13_assembly_report.txt"));
    private final VcfConverter vcfConverter = new VcfConverter(b37, VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));

    @ParameterizedTest
    @CsvSource({
            //#CHROM  POS ID   REF  ALT         MATEID EVENTID
            "2,  321681, bndW, G, G]17:198982], bndY, event1",
            "2,  321682, bndV, T, ]13:123456]T, bndU, event2",
            "13, 123456, bndU, C, C[2:321682[,  bndV, event2",
            "13, 123457, bndX, A, [17:198983[A, bndZ, event3",
            "17, 198982, bndY, A, A]2:321681],  bndW, event1",
            "17, 198983, bndZ, C, [13:123457[C, bndX, event3",
            "17, 198983, bndZ, C, C., '', event4",
            "17, 198983, bndZ, C, .C, '', event5",
    })
    void roundTrip(String chr, int pos, String id, String ref, String alt, String mateId, String eventId) {
        GenomicBreakendVariant breakendVariant = vcfConverter.convertBreakend(vcfConverter.parseContig(chr), id, pos, ConfidenceInterval.precise(), ref, alt, ConfidenceInterval.precise(), mateId, eventId);
        GenomicVariant expected = GenomicVariant.builder()
                .variant(vcfConverter.parseContig(chr), Strand.POSITIVE, Coordinates.oneBased(pos, pos), ref, alt, 0)
                .id(id)
                .mateId(mateId)
                .eventId(eventId)
                .build();
        assertThat(breakendVariant.toSymbolicGenomicVariant(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            //#CHROM  POS ID   REF  ALT         MATEID EVENTID
            "2,  321682, bndV, T, ]13:123456]AGTNNNNNCAT, bndU, event2",
            "13, 123456, bndU, C, CAGTNNNNNCA[2:321682[,  bndV, event2",
    })
    void roundTripWithInsertedSequence(String chr, int pos, String id, String ref, String alt, String mateId, String eventId) {
        GenomicBreakendVariant breakendVariant = vcfConverter.convertBreakend(vcfConverter.parseContig(chr), id, pos, ref, alt, mateId, eventId);
        GenomicVariant genomicVariant = GenomicVariant.builder()
                .variant(vcfConverter.parseContig(chr), Strand.POSITIVE, Coordinates.oneBased(pos, pos), ref, alt, 0)
                .id(id)
                .mateId(mateId)
                .eventId(eventId)
                .build();
        assertThat(breakendVariant.toSymbolicGenomicVariant(), equalTo(genomicVariant));
    }
}
