package org.monarchinitiative.variant.api;

import static org.monarchinitiative.variant.api.AssignedMoleculeType.*;
import static org.monarchinitiative.variant.api.SequenceRole.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TestContigs {

    // real GRCh38.p13 contig data
    public static final Contig chr1 = Contig.of(1, "1", ASSEMBLED_MOLECULE, "1", CHROMOSOME, 249250621, "NC_000001.10", "CM000663.1", "chr1");
    public static final Contig chr2 = Contig.of(2, "2", ASSEMBLED_MOLECULE, "2", CHROMOSOME, 243_199_373, "CM000664.1", "NC_000002.11", "chr2");
    public static final Contig chr13 = Contig.of(13, "13", ASSEMBLED_MOLECULE, "13", CHROMOSOME, 115_169_878, "CM000675.1", "NC_000013.10", "chr13");
    public static final Contig chr17 = Contig.of(17, "17", ASSEMBLED_MOLECULE, "17", CHROMOSOME, 83_257_441, "CM000679.2", "NC_000017.11", "chr17");

    private TestContigs() {
    }

}
