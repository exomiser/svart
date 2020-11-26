package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.impl.GenomicRegionDefault;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomicRegionTest {


    private final Contig chr1 = Contig.of(1, "1", SequenceRole.ASSEMBLED_MOLECULE, 5, "", "", "chr1");

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

}