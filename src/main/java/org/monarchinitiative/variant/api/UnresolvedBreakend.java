package org.monarchinitiative.variant.api;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
final class UnresolvedBreakend implements Breakend {

    private static final UnresolvedBreakend INSTANCE = new UnresolvedBreakend();

    private static final String ID = "";
    private static final Strand strand = Strand.UNKNOWN;
    private static final Position position = Position.of(0);
    private static final Contig contig = Contig.unknown();

    private UnresolvedBreakend() {
    }

    static UnresolvedBreakend instance() {
        return INSTANCE;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Breakend withStrand(Strand strand) {
        return this;
    }

    @Override
    public Contig contig() {
        return contig;
    }

    @Override
    public Position position() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "UnresolvedBreakend{" +
                "id='" + ID + '\'' +
                ", strand=" + strand +
                ", position=" + position +
                ", contig=" + contig +
                '}';
    }
}
