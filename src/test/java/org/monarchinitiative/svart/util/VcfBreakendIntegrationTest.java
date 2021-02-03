package org.monarchinitiative.svart.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.*;

import java.nio.file.Path;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class VcfBreakendIntegrationTest {

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
    public void roundTrip(String chr, int pos, String id, String ref, String alt, String mateId, String eventId) {
        BreakendVariant variant = vcfConverter.convertBreakend(chr, id, Position.of(pos), ref, alt, ConfidenceInterval.precise(), mateId, eventId);
        assertThat(VcfBreakendFormatter.makeAltVcfField(variant), equalTo(alt));
    }

    // schema:
    // table: genome assembly
    // table: contigs
    // standard genomic coordinates or the indicated strand?
    // contig, start, end, strand
    // 1, 1, 5, +
}
