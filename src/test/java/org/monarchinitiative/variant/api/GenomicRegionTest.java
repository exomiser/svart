package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.variant.api.impl.GenomicRegionDefault;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomicRegionTest {


    private final Contig chr1 = new TestContig(1, "1", 5);

    @Test
    public void oneBasedSingleBase() {
        GenomicRegion instance = GenomicRegion.oneBased(chr1, Strand.POSITIVE, Position.of(1), Position.of(1));
        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(1));
        assertThat(instance.length(), equalTo(1));
        assertThat(instance.toOppositeStrand(), equalTo(GenomicRegionDefault.oneBased(chr1, Strand.NEGATIVE, Position.of(5), Position.of(5))));
        assertThat(instance.toOppositeStrand().toZeroBased(), equalTo(GenomicRegionDefault.zeroBased(chr1, Strand.NEGATIVE, Position.of(4), Position.of(5))));
    }

    @Test
    public void zeroBasedSingleBase() {
        GenomicRegion instance = GenomicRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(0), Position.of(1));
        assertThat(instance.start(), equalTo(0));
        assertThat(instance.end(), equalTo(1));
        assertThat(instance.length(), equalTo(1));
        assertThat(instance.toOppositeStrand(), equalTo(GenomicRegionDefault.zeroBased(chr1, Strand.NEGATIVE, Position.of(4), Position.of(5))));
    }

    @Test
    public void oneBasedMultiBase() {
        GenomicRegion instance = GenomicRegion.oneBased(chr1, Strand.POSITIVE, Position.of(1), Position.of(2));
        assertThat(instance.start(), equalTo(1));
        assertThat(instance.end(), equalTo(2));
        assertThat(instance.length(), equalTo(2));
        assertThat(instance.toOppositeStrand(), equalTo(GenomicRegionDefault.oneBased(chr1, Strand.NEGATIVE, Position.of(4), Position.of(5))));
    }

    @Test
    public void zeroBasedMultiBase() {
        GenomicRegion instance = GenomicRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(0), Position.of(2));
        assertThat(instance.start(), equalTo(0));
        assertThat(instance.end(), equalTo(2));
        assertThat(instance.length(), equalTo(2));
        assertThat(instance.toOppositeStrand(), equalTo(GenomicRegionDefault.zeroBased(chr1, Strand.NEGATIVE, Position.of(3), Position.of(5))));
    }

    @Test
    public void flipStrandAndChangeCoordinateSystem() {
        GenomicRegion instance = GenomicRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(0), Position.of(2));
        assertThat(instance.toOppositeStrand().toOneBased(), equalTo(GenomicRegionDefault.oneBased(chr1, Strand.NEGATIVE, Position.of(4), Position.of(5))));
    }

    @ParameterizedTest
    @CsvSource({
            // source  start end  target     expected   pos
            "POSITIVE, 3, 5,   POSITIVE,   POSITIVE,   3, 5",
            "POSITIVE, 3, 5,   NEGATIVE,   NEGATIVE,   1, 3",
            "POSITIVE, 3, 5,   UNSTRANDED, UNSTRANDED, 3, 5",
            "POSITIVE, 3, 5,   UNKNOWN,    UNKNOWN,    3, 5",

            "NEGATIVE, 3, 5,   POSITIVE,   POSITIVE,   1, 3",
            "NEGATIVE, 3, 5,   NEGATIVE,   NEGATIVE,   3, 5",
            "NEGATIVE, 3, 5,   UNSTRANDED, UNSTRANDED, 3, 5",
            "NEGATIVE, 3, 5,   UNKNOWN,    UNKNOWN,    3, 5",

            "UNSTRANDED, 3, 5, POSITIVE,   UNSTRANDED, 3, 5",
            "UNSTRANDED, 3, 5, NEGATIVE,   UNSTRANDED, 3, 5",
            "UNSTRANDED, 3, 5, UNSTRANDED, UNSTRANDED, 3, 5",
            "UNSTRANDED, 3, 5, UNKNOWN,    UNKNOWN,    3, 5",

            "UNKNOWN, 3, 5,    POSITIVE,   UNKNOWN,    3, 5",
            "UNKNOWN, 3, 5,    NEGATIVE,   UNKNOWN,    3, 5",
            "UNKNOWN, 3, 5,    UNSTRANDED, UNKNOWN,    3, 5",
            "UNKNOWN, 3, 5,    UNKNOWN,    UNKNOWN,    3, 5"})
    public void withStrand_strandConversions(Strand source, int start, int end, Strand target, Strand expected, int exptStart, int exptEnd) {
        GenomicRegion initial = GenomicRegion.oneBased(chr1, source, Position.of(start), Position.of(end));

        GenomicRegion actual = initial.withStrand(target);
        assertThat(actual.strand(), equalTo(expected));
        assertThat(actual.start(), equalTo(exptStart));
        assertThat(actual.end(), equalTo(exptEnd));
        assertThat(actual.coordinateSystem(), equalTo(initial.coordinateSystem()));
    }

    @ParameterizedTest
    @CsvSource({
            "ZERO_BASED, 2, 3,   0,   2, 3",
            "ONE_BASED,  2, 3,   0,   2, 3",
            "ZERO_BASED, 2, 3,   1,   1, 4",
            "ONE_BASED,  2, 3,   1,   1, 4",
            "ZERO_BASED, 2, 3,   2,   0, 5",
    })
    public void withPadding_singlePadding(CoordinateSystem coordinateSystem,
                                          int start, int end,
                                          int padding,
                                          int expectedStart, int expectedEnd) {
        GenomicRegion actual = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(start), Position.of(end)).withPadding(padding);
        GenomicRegion expected = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(expectedStart), Position.of(expectedEnd));
        assertThat(actual, equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "ZERO_BASED, 2, 3,   0, 0,   2, 3",
            "ONE_BASED,  2, 3,   0, 0,   2, 3",
            "ZERO_BASED, 2, 3,   1, 2,   1, 5",
            "ONE_BASED,  2, 3,   1, 2,   1, 5",
            "ZERO_BASED, 2, 3,   2, 0,   0, 3",
            "ONE_BASED,  2, 3,   1, 0,   1, 3",
    })
    public void withPadding_upDownPadding(CoordinateSystem coordinateSystem,
                                          int start, int end,
                                          int upPadding, int downPadding,
                                          int expectedStart, int expectedEnd) {
        GenomicRegion actual = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(start), Position.of(end)).withPadding(upPadding, downPadding);
        GenomicRegion expected = GenomicRegion.of(chr1, Strand.POSITIVE, coordinateSystem, Position.of(expectedStart), Position.of(expectedEnd));
        assertThat(actual, equalTo(expected));
    }
}