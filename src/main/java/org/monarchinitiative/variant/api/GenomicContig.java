package org.monarchinitiative.variant.api;

import java.util.Collections;
import java.util.Set;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenomicContig<T> extends Comparable<T> {

    // Reserved range 1-25 in the case of human for the 'assembled-molecule' in the assembly report file
    // *MUST* be unique within an assembly
    int getId();

    // column 0 of the assembly report file 1-22, X,Y,MT
    String getName();

    // column 8 (zero-based) of assembly-report
    int getLength();

    // the RefSeq accession of the contig or empty if unknown
    // RefSeq is recommended by the HGVS for use when reporting variants.
    // http://varnomen.hgvs.org/bg-material/refseq/
    String getRefSeqAccession();

    String getGenBankAccession();

    default Set<String> getAlternateAccessions() {
        return Collections.emptySet();
    }
}
