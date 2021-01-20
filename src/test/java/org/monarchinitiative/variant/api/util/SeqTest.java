package org.monarchinitiative.variant.api.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class SeqTest {

    @Test
    public void reverseComplementByteArray() {
        byte[] seq = "ACGTUacgtuWSMKwsmkRYryBDHVNbdhvn".getBytes(StandardCharsets.US_ASCII);
        byte[] reversed = Seq.reverseComplement(seq);
        String rev = new String(reversed, StandardCharsets.US_ASCII);
        assertThat(rev, is("nbdhvNBDHVryRYmkswMKSWaacgtAACGT"));
    }

    @Test
    public void reverseComplementString() {
        assertThat(Seq.reverseComplement("ACGTUacgtuWSMKwsmkRYryBDHVNbdhvn"),
                is("nbdhvNBDHVryRYmkswMKSWaacgtAACGT"));
    }

    @Test
    public void reverseComplementSymbolic() {
        assertThat(Seq.reverseComplement("<INS>"), equalTo("<INS>"));
    }

    @Test
    public void reverseComplementMissing() {
        assertThat(Seq.reverseComplement("."), equalTo("."));
    }

    @Test
    public void reverseComplementUpstreamDeletion() {
        assertThat(Seq.reverseComplement("*"), equalTo("*"));
    }
}