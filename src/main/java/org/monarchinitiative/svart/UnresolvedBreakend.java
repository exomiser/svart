package org.monarchinitiative.svart;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
final class UnresolvedBreakend extends BaseGenomicRegion<UnresolvedBreakend> implements Breakend {

    private static final UnresolvedBreakend ONE_BASED_UNRESOLVED_BREAKEND = new UnresolvedBreakend(CoordinateSystem.ONE_BASED, 1);
    private static final UnresolvedBreakend ZERO_BASED_UNRESOLVED_BREAKEND = new UnresolvedBreakend(CoordinateSystem.ZERO_BASED, 0);

    private static final String ID = "";

    private UnresolvedBreakend(CoordinateSystem coordinateSystem, int start) {
        super(Contig.unknown(), Strand.POSITIVE, Coordinates.of(coordinateSystem, start, start + Coordinates.endDelta(coordinateSystem)));
    }

    static UnresolvedBreakend instance(CoordinateSystem coordinateSystem) {
        switch (coordinateSystem) {
            case ONE_BASED:
                return ONE_BASED_UNRESOLVED_BREAKEND;
            case ZERO_BASED:
                return ZERO_BASED_UNRESOLVED_BREAKEND;
            default:
                throw new IllegalStateException("Unexpected coordinate system: " + coordinateSystem);
        }
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
