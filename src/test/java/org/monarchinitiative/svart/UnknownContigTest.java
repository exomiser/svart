package org.monarchinitiative.svart;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class UnknownContigTest {

    private Contig unknown = Contig.unknown();

    @Test
    public void id() {
        assertThat(unknown.id(), equalTo(0));
    }

    @Test
    public void name() {
        assertThat(unknown.name(), equalTo("na"));
    }

    @Test
    public void sequenceRole() {
        assertThat(unknown.sequenceRole(), equalTo(SequenceRole.UNKNOWN));
    }

    @Test
    public void length() {
        assertThat(unknown.length(), equalTo(0));
    }

    @Test
    public void genBankAccession() {
        assertThat(unknown.genBankAccession(), equalTo(""));
    }

    @Test
    public void refSeqAccession() {
        assertThat(unknown.refSeqAccession(), equalTo(""));
    }

    @Test
    public void ucscName() {
        assertThat(unknown.ucscName(), equalTo("na"));
    }

    @Test
    public void isUnknown() {
        assertThat(unknown.isUnknown(), is(true));
    }
}