package org.monarchinitiative.variant.api;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
final class UnknownContig implements Contig {

    private static final String NAME = "na";
    private static final UnknownContig UNKNOWN_CONTIG = new UnknownContig();

    private UnknownContig() {
    }

    static UnknownContig instance() {
        return UNKNOWN_CONTIG;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public SequenceRole getSequenceRole() {
        return SequenceRole.UNKNOWN;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public String getGenBankAccession() {
        return "";
    }

    @Override
    public String getRefSeqAccession() {
        return "";
    }

    @Override
    public String ucscName() {
        return NAME;
    }

    @Override
    public int compareTo(Contig o) {
        return o == this ? 0 : -1;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
