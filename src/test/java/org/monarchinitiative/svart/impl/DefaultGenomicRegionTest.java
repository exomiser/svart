package org.monarchinitiative.svart.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DefaultGenomicRegionTest {

    Contig chr1 = TestContig.of(1, 10);

    @ParameterizedTest
    @CsvSource({
            "FULLY_CLOSED,  1, 1,  LEFT_OPEN, 0, 1",
            "LEFT_OPEN, 0, 1,  FULLY_CLOSED,  1, 1,",
            "FULLY_CLOSED,  1, 2,  LEFT_OPEN, 0, 2",
            "LEFT_OPEN, 0, 2,  FULLY_CLOSED,  1, 2",

    })
    public void singleOrEmptyBase(CoordinateSystem inputCoords, int inputStart, int inputEnd,
                                  CoordinateSystem exptCoords, int exptStart, int exptEnd) {
        GenomicRegion instance = DefaultGenomicRegion.of(chr1, Strand.POSITIVE, Coordinates.of(inputCoords, inputStart, inputEnd));
        GenomicRegion expected = DefaultGenomicRegion.of(chr1, Strand.POSITIVE, Coordinates.of(exptCoords, exptStart, exptEnd));
        assertThat(instance.withCoordinateSystem(exptCoords), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, FULLY_CLOSED,  1, 1,  NEGATIVE, FULLY_CLOSED,   10, 10",
            "POSITIVE, LEFT_OPEN, 0, 1,  NEGATIVE, LEFT_OPEN,   9, 10",
            "POSITIVE, FULLY_CLOSED , 1, 2,  NEGATIVE, FULLY_CLOSED,    9, 10"
    })
    public void toOppositeStrand(Strand inputStrand, CoordinateSystem inputCoords, int inputStart, int inputEnd,
                                 Strand exptStrand, CoordinateSystem exptCoords, int exptStart, int exptEnd) {
        GenomicRegion instance = DefaultGenomicRegion.of(chr1, inputStrand, Coordinates.of(inputCoords, inputStart, inputEnd));
        GenomicRegion expected = DefaultGenomicRegion.of(chr1, exptStrand, Coordinates.of(exptCoords, exptStart, exptEnd));
        assertThat(instance.withStrand(exptStrand), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, FULLY_CLOSED,  1, 0,  NEGATIVE, FULLY_CLOSED,   1, 0",
            "POSITIVE, LEFT_OPEN, 0, 0,  NEGATIVE, LEFT_OPEN,  0, 0",
    })
    public void emptyRegionUnknownChromosomeToOppositeStrand(Strand inputStrand, CoordinateSystem inputCoords, int inputStart, int inputEnd,
                                                             Strand exptStrand, CoordinateSystem exptCoords, int exptStart, int exptEnd) {
        GenomicRegion instance = DefaultGenomicRegion.of(Contig.unknown(), inputStrand, Coordinates.of(inputCoords, inputStart, inputEnd));
        GenomicRegion expected = DefaultGenomicRegion.of(Contig.unknown(), exptStrand, Coordinates.of(exptCoords, exptStart, exptEnd));
        assertThat(instance.withStrand(exptStrand), equalTo(expected));
    }
}