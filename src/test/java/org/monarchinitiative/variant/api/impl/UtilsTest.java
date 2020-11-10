package org.monarchinitiative.variant.api.impl;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class UtilsTest {

    @Test
    void reverseComplementByteArray() {
        byte[] seq = "ACGTUacgtuWSMKwsmkRYryBDHVNbdhvn".getBytes(StandardCharsets.US_ASCII);
        byte[] reversed = Utils.reverseComplement(seq);
        String rev = new String(reversed, StandardCharsets.US_ASCII);
        assertThat(rev, is("nbdhvNBDHVryRYmkswMKSWaacgtAACGT"));
    }

    @Test
    void reverseComplementString() {
        assertThat(Utils.reverseComplement("ACGTUacgtuWSMKwsmkRYryBDHVNbdhvn"),
                is("nbdhvNBDHVryRYmkswMKSWaacgtAACGT"));
    }
}