package org.monarchinitiative.svart.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.svart.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultGenomicRegionTest {

    Contig chr1 = TestContig.of(1, 10);

    @ParameterizedTest
    @CsvSource({
            "ONE_BASED,  1, 1,  ZERO_BASED, 0, 1",
            "ZERO_BASED, 0, 1,  ONE_BASED,  1, 1,",
            "ONE_BASED,  1, 2,  ZERO_BASED, 0, 2",
            "ZERO_BASED, 0, 2,  ONE_BASED,  1, 2",

    })
    public void singleOrEmptyBase(CoordinateSystem inputCoords, int inputStart, int inputEnd,
                                  CoordinateSystem exptCoords, int exptStart, int exptEnd) {
        GenomicRegion instance = DefaultGenomicRegion.of(chr1, Strand.POSITIVE, Coordinates.of(inputCoords, inputStart, inputEnd));
        GenomicRegion expected = DefaultGenomicRegion.of(chr1, Strand.POSITIVE, Coordinates.of(exptCoords, exptStart, exptEnd));
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
        GenomicRegion instance = DefaultGenomicRegion.of(chr1, inputStrand, Coordinates.of(inputCoords, inputStart, inputEnd));
        GenomicRegion expected = DefaultGenomicRegion.of(chr1, exptStrand, Coordinates.of(exptCoords, exptStart, exptEnd));
        assertThat(instance.withStrand(exptStrand), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, ONE_BASED,  1, 1,  NEGATIVE, ONE_BASED,   10, 10",
            "POSITIVE, ZERO_BASED, 0, 1,  NEGATIVE, ZERO_BASED,   9, 10",
            "POSITIVE, ONE_BASED , 1, 2,  NEGATIVE, ONE_BASED,    9, 10"
    })
    public void toOppositeStrandAndBackAgain(Strand inputStrand, CoordinateSystem inputCoords, int inputStart, int inputEnd,
                                 Strand exptStrand, CoordinateSystem exptCoords, int exptStart, int exptEnd) {
        GenomicRegion positive = DefaultGenomicRegion.of(chr1, inputStrand, Coordinates.of(inputCoords, inputStart, inputEnd));
        GenomicRegion negative = DefaultGenomicRegion.of(chr1, exptStrand, Coordinates.of(exptCoords, exptStart, exptEnd));
        assertThat(positive.withStrand(exptStrand), equalTo(negative));

        assertThat(positive.toOppositeStrand(), equalTo(negative));
        assertThat(positive.toNegativeStrand(), equalTo(negative));
        assertThat(positive.toNegativeStrand().toOppositeStrand(), equalTo(positive));

        assertThat(negative.toOppositeStrand(), equalTo(positive));
        assertThat(negative.toPositiveStrand(), equalTo(positive));
        assertThat(negative.toPositiveStrand().toNegativeStrand(), equalTo(negative));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, ONE_BASED,  1, 0,  NEGATIVE, ONE_BASED,   1, 0",
            "POSITIVE, ZERO_BASED, 0, 0,  NEGATIVE, ZERO_BASED,  0, 0",
    })
    public void emptyRegionUnknownChromosomeToOppositeStrand(Strand inputStrand, CoordinateSystem inputCoords, int inputStart, int inputEnd,
                                                             Strand exptStrand, CoordinateSystem exptCoords, int exptStart, int exptEnd) {
        GenomicRegion instance = DefaultGenomicRegion.of(Contig.unknown(), inputStrand, Coordinates.of(inputCoords, inputStart, inputEnd));
        GenomicRegion expected = DefaultGenomicRegion.of(Contig.unknown(), exptStrand, Coordinates.of(exptCoords, exptStart, exptEnd));
        assertThat(instance.withStrand(exptStrand), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, ONE_BASED,  10,  1",
            "POSITIVE, ONE_BASED,   0, 10",
            "POSITIVE, ONE_BASED,   0, -1",
            "POSITIVE, ZERO_BASED, 10,  0",
            "POSITIVE, ZERO_BASED,  0, -1",
            "POSITIVE, ZERO_BASED, -1, 10",
    })
    public void testThrowsExceptionWithInvalidCoordinates(Strand inputStrand, CoordinateSystem inputCoords, int inputStart, int inputEnd) {
        assertThrows(InvalidCoordinatesException.class, () -> GenomicRegion.of(chr1, inputStrand, inputCoords, inputStart, inputEnd));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, ONE_BASED,  1, 11",
            "POSITIVE, ZERO_BASED, 0, 11",
    })
    public void testThrowsExceptionWithCoordinatesOutOfBounds(Strand inputStrand, CoordinateSystem inputCoords, int inputStart, int inputEnd) {
        assertThrows(CoordinatesOutOfBoundsException.class, () -> GenomicRegion.of(chr1, inputStrand, inputCoords, inputStart, inputEnd));
    }

    @Test
    public void comparableTest() {
        GenomicRegion first = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 1);
        GenomicRegion second = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 2, 10).toNegativeStrand();
        GenomicRegion third = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 2, 8);
        Contig chr2 = TestContig.of(2, 2000);
        GenomicRegion fourth = GenomicRegion.of(chr2, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 1, 2);

        List<GenomicRegion> sorted = Stream.of(second, first, fourth, third).sorted().collect(Collectors.toUnmodifiableList());
        sorted.forEach(System.out::println);
        assertThat(sorted, equalTo(List.of(first, second, third, fourth)));
    }
}