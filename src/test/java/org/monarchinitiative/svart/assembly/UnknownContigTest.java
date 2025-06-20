package org.monarchinitiative.svart.assembly;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.Contig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class UnknownContigTest {

    private Contig unknown = Contig.unknown();

    @Test
    void id() {
        assertThat(unknown.id(), equalTo(0));
    }

    @Test
    void name() {
        assertThat(unknown.name(), equalTo("na"));
    }

    @Test
    void sequenceRole() {
        assertThat(unknown.sequenceRole(), equalTo(SequenceRole.UNKNOWN));
    }

    @Test
    void length() {
        assertThat(unknown.length(), equalTo(0));
    }

    @Test
    void genBankAccession() {
        assertThat(unknown.genBankAccession(), equalTo(""));
    }

    @Test
    void refSeqAccession() {
        assertThat(unknown.refSeqAccession(), equalTo(""));
    }

    @Test
    void ucscName() {
        assertThat(unknown.ucscName(), equalTo("na"));
    }

    @Test
    void isUnknown() {
        assertThat(unknown.isUnknown(), is(true));
    }
}