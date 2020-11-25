package org.monarchinitiative.variant.api;

/**
 * Represents a {@link Position} on a {@link Contig}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenomicPosition extends Comparable<GenomicPosition>, Stranded<GenomicPosition>, CoordinateSystemed<GenomicPosition> {

    static int compare(GenomicPosition x, GenomicPosition y) {
        int result = Contig.compare(x.contig(), y.contig());
        if (result == 0) {
            result = Position.compare(x.position(), y.position());
        }
        if (result == 0) {
            result = Strand.compare(x.strand(), y.strand());
        }

        // TODO: 25. 11. 2020 also consider the coordinate system here?
        return result;
    }

    Contig contig();

    // Reserved range 1-25 in the case of human for the 'assembled-molecule' in the assembly report file
    default int contigId() {
        return contig().id();
    }

    // column 0 of the assembly report file 1-22, X,Y,MT
    default String contigName() {
        return contig().name();
    }

    Position position();

    default int pos() {
        return position().pos();
    }

    default Strand strand() {
        return Strand.POSITIVE;
    }

    default ConfidenceInterval ci() {
        return position().confidenceInterval();
    }

    default int min() {
        return position().minPos();
    }

    default int max() {
        return position().maxPos();
    }

    default int differenceTo(GenomicPosition other) {
        if (contigId() != other.contigId()) {
            throw new IllegalArgumentException("Coordinates are on different chromosomes: " + contigName() + " vs. " + other.contigName());
        }
        other = other.withStrand(strand()).withCoordinateSystem(coordinateSystem());

        return pos() - other.pos();
    }

    default int differenceTo(GenomicRegion other) {
        if (contigId() != other.contigId()) {
            throw new IllegalArgumentException("Coordinates are on different chromosomes: " + contigName() + " vs. " + other.contigName());
        }

        other = other.withStrand(strand()).withCoordinateSystem(coordinateSystem());

        if (other.contains(this)) {
            return 0;
        }

        int s = pos() - other.start();
        int e = pos() - other.end();
        return Math.abs(s) < Math.abs(e) ? s : e;
    }

    @Override
    default int compareTo(GenomicPosition o) {
        return compare(this, o);
    }
}
