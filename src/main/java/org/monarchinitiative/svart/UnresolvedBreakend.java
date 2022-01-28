package org.monarchinitiative.svart;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
final class UnresolvedBreakend extends BaseGenomicRegion<UnresolvedBreakend> implements Breakend {

    private static final UnresolvedBreakend FULLY_CLOSED = new UnresolvedBreakend(CoordinateSystem.FULLY_CLOSED, 1);
    private static final UnresolvedBreakend LEFT_OPEN = new UnresolvedBreakend(CoordinateSystem.LEFT_OPEN, 0);
    private static final UnresolvedBreakend RIGHT_OPEN = new UnresolvedBreakend(CoordinateSystem.RIGHT_OPEN, 1);
    private static final UnresolvedBreakend FULLY_OPEN = new UnresolvedBreakend(CoordinateSystem.FULLY_OPEN, 0);

    private static final String ID = "";

    private UnresolvedBreakend(CoordinateSystem coordinateSystem, int start) {
        super(Contig.unknown(), Strand.POSITIVE, Coordinates.of(coordinateSystem, start, start + Coordinates.endDelta(coordinateSystem)));
    }

    static UnresolvedBreakend instance(CoordinateSystem coordinateSystem) {
        switch (coordinateSystem){
            case FULLY_CLOSED:
                return FULLY_CLOSED;
            case LEFT_OPEN:
                return LEFT_OPEN;
            case RIGHT_OPEN:
                return RIGHT_OPEN;
            case FULLY_OPEN:
                return FULLY_OPEN;
        }
        throw new IllegalArgumentException("Unknown coordinateSystem!");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    protected UnresolvedBreakend newRegionInstance(Contig contig, Strand strand, Coordinates coordinates) {
        return instance(coordinates.coordinateSystem());
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
                "contig=" + Contig.unknown().id() +
                ", id='" + ID + '\'' +
                ", strand=" + Strand.POSITIVE +
                ", coordinateSystem=" + coordinateSystem() +
                ", start=" + start() +
                ", end=" + end() +
                '}';
    }
}
