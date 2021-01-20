package org.monarchinitiative.variant.api;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 */
final class UnresolvedBreakend extends BaseGenomicRegion<UnresolvedBreakend> implements Breakend {

    private static final UnresolvedBreakend INSTANCE = new UnresolvedBreakend();

    private static final String ID = "";

    private UnresolvedBreakend() {
        super(Contig.unknown(), Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(1), Position.of(0));
    }

    static UnresolvedBreakend instance() {
        return INSTANCE;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    protected UnresolvedBreakend newRegionInstance(Contig contig, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition) {
        return this;
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
                ", strand=" + Strand.POSITIVE +
                ", coordinateSystem=" + CoordinateSystem.FULLY_CLOSED +
                ", position=" + Position.of(1) +
                ", contig=" + Contig.unknown().id() +
                '}';
    }
}
