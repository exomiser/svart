package org.monarchinitiative.variant.api;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
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
    public int id() {
        return 0;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public SequenceRole sequenceRole() {
        return SequenceRole.UNKNOWN;
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public String genBankAccession() {
        return "";
    }

    @Override
    public String refSeqAccession() {
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
