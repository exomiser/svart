package org.monarchinitiative.variant.api;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class StrandTest {

    @Test
    void opposite() {
        assertThat(Strand.POSITIVE.opposite(), equalTo(Strand.NEGATIVE));
        assertThat(Strand.NEGATIVE.opposite(), equalTo(Strand.POSITIVE));
        assertThat(Strand.UNKNOWN.opposite(), equalTo(Strand.UNKNOWN));
        assertThat(Strand.UNSTRANDED.opposite(), equalTo(Strand.UNSTRANDED));
    }
}