package org.monarchinitiative.svart.impl;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.assembly.AssignedMoleculeType;
import org.monarchinitiative.svart.assembly.SequenceRole;
import org.monarchinitiative.svart.TestContig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultContigTest {

    @Test
    public void throwsExceptionWithZeroId() {
        assertThrows(IllegalArgumentException.class, () -> DefaultContig.of(0, "0", SequenceRole.UNKNOWN, "0", AssignedMoleculeType.UNKNOWN, 1, "", "", ""));
    }

    @Test
    public void throwsExceptionWithNegativeId() {
        assertThrows(IllegalArgumentException.class, () -> DefaultContig.of(-1, "0", SequenceRole.UNKNOWN, "0", AssignedMoleculeType.UNKNOWN, 1, "", "", ""));
    }

    @Test
    public void throwsExceptionWithLengthLessThanOne() {
        assertThrows(IllegalArgumentException.class, () -> DefaultContig.of(1, "1", SequenceRole.UNKNOWN, "1", AssignedMoleculeType.UNKNOWN, 0, "", "", ""));
    }

    @Test
    public void compareTo() {
        assertThat(TestContig.of(1, 1).compareTo(TestContig.of(2, 2)), equalTo(-1));
        assertThat(TestContig.of(1, 1).compareTo(TestContig.of(1, 1)), equalTo(0));
        assertThat(TestContig.of(2, 1).compareTo(TestContig.of(1, 2)), equalTo(1));
    }
}