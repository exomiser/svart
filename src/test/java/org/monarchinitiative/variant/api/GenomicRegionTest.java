package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.impl.GenomicRegionDefault;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void illegalRegionPastContigStart() {
        // We cannot make this to fail for zero-based position since calling `Position.of(-1)` throws an exception

        // one-based region
        IllegalArgumentException eo = assertThrows(IllegalArgumentException.class,
                () -> GenomicRegion.oneBased(chr1, Strand.POSITIVE, Position.of(0), Position.of(2)));
        assertThat(eo.getMessage(), equalTo("Cannot create genomic region with a position 0 that extends beyond contig start 1"));
    }

    @Test
    public void illegalRegionPastContigEnd() {
        IllegalArgumentException ez = assertThrows(IllegalArgumentException.class,
                () -> GenomicRegion.zeroBased(chr1, Strand.POSITIVE, Position.of(5), Position.of(6)));
        assertThat(ez.getMessage(), equalTo("Cannot create genomic region with a position 6 that extends beyond contig end 5"));

        IllegalArgumentException eo = assertThrows(IllegalArgumentException.class,
                () -> GenomicRegion.oneBased(chr1, Strand.POSITIVE, Position.of(5), Position.of(6)));
        assertThat(eo.getMessage(), equalTo("Cannot create genomic region with a position 6 that extends beyond contig end 5"));
    }

    @Test
    public void illegalNullPosition() {
        NullPointerException ez = assertThrows(NullPointerException.class,
                () -> GenomicRegion.zeroBased(chr1, Strand.POSITIVE, null, Position.of(6)));
        assertThat(ez.getMessage(), equalTo("Position cannot be null"));

        NullPointerException eo = assertThrows(NullPointerException.class,
                () -> GenomicRegion.oneBased(chr1, Strand.POSITIVE, null, Position.of(6)));
        assertThat(eo.getMessage(), equalTo("Position cannot be null"));
    }
}