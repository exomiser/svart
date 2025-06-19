package org.monarchinitiative.svart;

import java.util.Objects;

/**
 * Represents an unresolved genomic breakend that serves as a placeholder when the mate breakend
 * location cannot be determined or is not specified.
 *
 * <p>An unresolved breakend is typically used in scenarios where:
 * <ul>
 *   <li>A breakend variant has no mate partner (e.g., telomeric breakends)</li>
 *   <li>The mate breakend is located on an unknown or unmapped contig</li>
 *   <li>Parsing errors occur that prevent mate breakend resolution</li>
 *   <li>Single breakend records are processed independently</li>
 *   <li>Non-canonical breakend representations like Manta's unresolved breakends (e.g., {@code C.} or {@code .C})</li>
 * </ul>
 *
 * <p>This class extends {@link BaseGenomicRegion} and implements {@link GenomicBreakend},
 * providing a concrete implementation for breakends that cannot be resolved to a specific
 * genomic location. All unresolved breakends are located on an {@link Contig#unknown()}
 * contig and positioned on the {@link Strand#POSITIVE} strand by default.
 *
 * <p>The class maintains immutability and provides factory methods for creation with
 * different levels of information available. It supports coordinate system conversions
 * while preserving the unresolved state and maintains optional mate and event identifiers
 * for tracking relationships between breakend pairs, if known.

 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create a basic unresolved breakend
 * GenomicBreakend breakend = UnresolvedGenomicBreakend.of(CoordinateSystem.ONE_BASED);
 *
 * // Create with identifiers for mate tracking
 * GenomicBreakend breakend = UnresolvedGenomicBreakend.of(CoordinateSystem.ONE_BASED, "bnd_A", "bnd_B", "event1");
 *
 * // Access via GenomicBreakend interface
 * GenomicBreakend unresolved = GenomicBreakend.unresolved(CoordinateSystem.ZERO_BASED);
 * assert unresolved.isUnresolved() == true;
 * }</pre>
 *
 * <h3>VCF Integration:</h3>
 * <p>This class is used by {@link org.monarchinitiative.svart.vcf.VcfBreakendResolver} when processing
 * VCF breakend records that cannot be resolved to specific genomic coordinates, and by
 * {@link org.monarchinitiative.svart.vcf.VcfBreakendFormatter} when formatting unresolved breakends
 * back to VCF representation (using {@code .} notation).
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @author Daniel Danis <daniel.danis@jax.org>
 * @see GenomicBreakend
 * @see BaseGenomicRegion
 * @see org.monarchinitiative.svart.variant.DefaultGenomicBreakend
 * @see org.monarchinitiative.svart.vcf.VcfBreakendResolver
 * @see org.monarchinitiative.svart.vcf.VcfBreakendFormatter
 */
final class UnresolvedGenomicBreakend extends BaseGenomicRegion<UnresolvedGenomicBreakend> implements GenomicBreakend {

    private final String id;

    private UnresolvedGenomicBreakend(CoordinateSystem coordinateSystem, int start, String id) {
        super(Contig.unknown(), Strand.POSITIVE, Coordinates.of(coordinateSystem, start, start + Coordinates.endDelta(coordinateSystem)));
        this.id = Objects.requireNonNullElse(id, "");
    }

    static UnresolvedGenomicBreakend of(CoordinateSystem coordinateSystem) {
        return new UnresolvedGenomicBreakend(coordinateSystem, calculateStart(coordinateSystem), "");
    }

    static UnresolvedGenomicBreakend of(CoordinateSystem coordinateSystem, String id) {
        return new UnresolvedGenomicBreakend(coordinateSystem, calculateStart(coordinateSystem), id);
    }

    private static int calculateStart(CoordinateSystem coordinateSystem) {
        return coordinateSystem == CoordinateSystem.ZERO_BASED ? 0 : 1;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    protected UnresolvedGenomicBreakend newRegionInstance(Contig contig, Strand strand, Coordinates coordinates) {
        return of(coordinates.coordinateSystem(), id);
    }

    @Override
    public boolean isUnresolved() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UnresolvedGenomicBreakend that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

    @Override
    public String toString() {
        return "UnresolvedBreakend{" +
               "contig=" + Contig.unknown().id() +
               ", id='" + id + '\'' +
               ", strand=" + Strand.POSITIVE +
               ", coordinateSystem=" + coordinateSystem() +
               ", start=" + start() +
               ", end=" + end() +
               '}';
    }
}
