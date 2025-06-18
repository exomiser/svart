package org.monarchinitiative.svart;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
final class UnresolvedGenomicBreakend extends BaseGenomicRegion<UnresolvedGenomicBreakend> implements GenomicBreakend {

    private static final UnresolvedGenomicBreakend ONE_BASED_UNRESOLVED_BREAKEND = new UnresolvedGenomicBreakend(CoordinateSystem.ONE_BASED, 1);
    private static final UnresolvedGenomicBreakend ZERO_BASED_UNRESOLVED_BREAKEND = new UnresolvedGenomicBreakend(CoordinateSystem.ZERO_BASED, 0);

    private static final String ID = "";

    private UnresolvedGenomicBreakend(CoordinateSystem coordinateSystem, int start) {
        super(Contig.unknown(), Strand.POSITIVE, Coordinates.of(coordinateSystem, start, start + Coordinates.endDelta(coordinateSystem)));
    }

    static UnresolvedGenomicBreakend instance(CoordinateSystem coordinateSystem) {
        return switch (coordinateSystem) {
            case ONE_BASED -> ONE_BASED_UNRESOLVED_BREAKEND;
            case ZERO_BASED -> ZERO_BASED_UNRESOLVED_BREAKEND;
        };
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    protected UnresolvedGenomicBreakend newRegionInstance(Contig contig, Strand strand, Coordinates coordinates) {
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
