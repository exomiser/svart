package org.monarchinitiative.variant.api;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
final class UnresolvedBreakend implements Breakend {

    private static final UnresolvedBreakend INSTANCE = new UnresolvedBreakend();

    private static final String ID = "";
    private static final Strand STRAND = Strand.UNKNOWN;
    private static final CoordinateSystem COORDINATE_SYSTEM = CoordinateSystem.ONE_BASED;
    private static final Position POSITION = Position.of(1);
    private static final Contig CONTIG = Contig.unknown();

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
        return CONTIG;
    }

    @Override
    public CoordinateSystem coordinateSystem() {
        return COORDINATE_SYSTEM;
    }

    @Override
    public Breakend withCoordinateSystem(CoordinateSystem coordinateSystem) {
        return this;
    }

    @Override
    public Position position() {
        return POSITION;
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
                ", strand=" + STRAND +
                ", coordinateSystem=" + COORDINATE_SYSTEM +
                ", position=" + POSITION +
                ", contig=" + CONTIG +
                '}';
    }
}