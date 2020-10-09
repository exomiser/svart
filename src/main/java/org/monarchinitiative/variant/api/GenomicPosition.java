/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.variant.api;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GenomicPosition extends Comparable<GenomicPosition> {

    public <T extends GenomicContig<T>> GenomicContig<T> getContig();

    // Reserved range 1-25 in the case of human for the 'assembled-molecule' in the assembly report file
    public default int getContigId() {
        return getContig().getId();
    }

    // column 0 of the assembly report file 1-22, X,Y,MT
    public default String getContigName() {
        return getContig().getName();
    }

    public int getPosition();

    public CoordinateSystem getCoordinateSystem();

    public default Strand getStrand() {
        return Strand.POSITIVE;
    }

    public default ConfidenceInterval getCi() {
        return ConfidenceInterval.precise();
    }

    public default int getMin() {
        return getCi().getMinPos(getPosition());
    }

    public default int getMax() {
        return getCi().getMaxPos(getPosition());
    }

    public default GenomicPosition withStrand(Strand strand) {
        if (this.getStrand() == strand) {
            return this;
        }
        // transform coordinate system
        int position = getContig().getLength() - getPosition() - 1;
        return new DefaultGenomicPosition(this.getContig(), position, this.getCoordinateSystem(), strand, this.getCi());
    }

    @Override
    public default int compareTo(GenomicPosition o) {
        int contigCompare = Integer.compare(this.getContigId(), o.getContigId());
        if (contigCompare == 0) {
            return Integer.compare(this.getPosition(), o.getPosition());
        }
        return contigCompare;
    }

    /**
     *
     * @param contig
     * @param position
     * @return a ONE-BASED position on the POSITIVE strand.
     */
    public static GenomicPosition precise(GenomicContig contig, int position) {
        return new DefaultGenomicPosition(contig, position, CoordinateSystem.ONE_BASED, Strand.POSITIVE, ConfidenceInterval.precise());
    }

    public static GenomicPosition precise(GenomicContig contig, int position, Strand strand) {
        return new DefaultGenomicPosition(contig, position, CoordinateSystem.ONE_BASED, strand, ConfidenceInterval.precise());
    }

    public static GenomicPosition precise(GenomicContig contig, int position, CoordinateSystem coordinateSystem, Strand strand) {
        return new DefaultGenomicPosition(contig, position, coordinateSystem, strand, ConfidenceInterval.precise());
    }

    class DefaultGenomicPosition implements GenomicPosition {

        private final GenomicContig contig;
        private final int position;
        private final CoordinateSystem coordinateSystem;
        private final Strand strand;
        private final ConfidenceInterval confidenceInterval;

        public DefaultGenomicPosition(GenomicContig contig, int position, CoordinateSystem coordinateSystem, Strand strand, ConfidenceInterval confidenceInterval) {
            this.contig = contig;
            this.position = position;
            this.coordinateSystem = coordinateSystem;
            this.strand = strand;
            this.confidenceInterval = confidenceInterval;
        }

        @Override
        public GenomicContig<? extends GenomicContig<?>> getContig() {
            return contig;
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public CoordinateSystem getCoordinateSystem() {
            return coordinateSystem;
        }

        @Override
        public Strand getStrand() {
            return strand;
        }

        @Override
        public ConfidenceInterval getCi() {
            return confidenceInterval;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DefaultGenomicPosition)) return false;
            DefaultGenomicPosition that = (DefaultGenomicPosition) o;
            return position == that.position &&
                    contig.equals(that.contig) &&
                    coordinateSystem == that.coordinateSystem &&
                    strand == that.strand &&
                    confidenceInterval.equals(that.confidenceInterval);
        }

        @Override
        public int hashCode() {
            return Objects.hash(contig, position, coordinateSystem, strand, confidenceInterval);
        }

        @Override
        public String toString() {
            return "DefaultGenomicPosition{" +
                    "contig=" + contig +
                    ", position=" + position +
                    ", coordinateSystem=" + coordinateSystem +
                    ", strand=" + strand +
                    ", confidenceInterval=" + confidenceInterval +
                    '}';
        }
    }

}
