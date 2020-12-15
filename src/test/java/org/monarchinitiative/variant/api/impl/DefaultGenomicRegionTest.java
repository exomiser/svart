package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.variant.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DefaultGenomicRegionTest {

    Contig chr1 = new TestContig(1, 10);

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED,  1, 1,  ZERO_BASED, 0, 1",
            "ZERO_BASED, 0, 1,  ONE_BASED,  1, 1,",
            "ONE_BASED,  1, 2,  ZERO_BASED, 0, 2",
            "ZERO_BASED, 0, 2,  ONE_BASED,  1, 2",

    })
    public void singleOrEmptyBase(CoordinateSystem inputCoords, int inputStart, int inputEnd,
                                  CoordinateSystem exptCoords, int exptStart, int exptEnd) {
        GenomicRegion instance = DefaultGenomicRegion.of(chr1, Strand.POSITIVE, inputCoords, Position.of(inputStart), Position.of(inputEnd));
        GenomicRegion expected = DefaultGenomicRegion.of(chr1, Strand.POSITIVE, exptCoords, Position.of(exptStart), Position.of(exptEnd));
        assertThat(instance.withCoordinateSystem(exptCoords), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, ONE_BASED,  1, 1,  NEGATIVE, ONE_BASED,   10, 10",
            "POSITIVE, ZERO_BASED, 0, 1,  NEGATIVE, ZERO_BASED,   9, 10",
            "POSITIVE, ONE_BASED , 1, 2,  NEGATIVE, ONE_BASED,    9, 10"
    })
    public void toOppositeStrand(Strand inputStrand, CoordinateSystem inputCoords, int inputStart, int inputEnd,
                                 Strand exptStrand, CoordinateSystem exptCoords, int exptStart, int exptEnd) {
        GenomicRegion instance = DefaultGenomicRegion.of(chr1, inputStrand, inputCoords, Position.of(inputStart), Position.of(inputEnd));
        GenomicRegion expected = DefaultGenomicRegion.of(chr1, exptStrand, exptCoords, Position.of(exptStart), Position.of(exptEnd));
        assertThat(instance.withStrand(exptStrand), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, ONE_BASED,  1, 1,  NEGATIVE, ONE_BASED,   1, 1",
            "POSITIVE, ZERO_BASED, 0, 1,  NEGATIVE, ZERO_BASED,  0, 1",
    })
    public void emptyRegionUnknownChromosomeToOppositeStrand(Strand inputStrand, CoordinateSystem inputCoords, int inputStart, int inputEnd,
                                                             Strand exptStrand, CoordinateSystem exptCoords, int exptStart, int exptEnd) {
        GenomicRegion instance = DefaultGenomicRegion.of(Contig.unknown(), inputStrand, inputCoords, Position.of(inputStart), Position.of(inputEnd));
        GenomicRegion expected = DefaultGenomicRegion.of(Contig.unknown(), exptStrand, exptCoords, Position.of(exptStart), Position.of(exptEnd));
        assertThat(instance.withStrand(exptStrand), equalTo(expected));
    }
}