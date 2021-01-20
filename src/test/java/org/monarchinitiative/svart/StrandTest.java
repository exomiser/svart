package org.monarchinitiative.svart;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class StrandTest {

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, NEGATIVE",
            "NEGATIVE, POSITIVE"
    })
    public void opposite(Strand strand, Strand expected) {
        assertThat(strand.opposite(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, true",
            "NEGATIVE, false",
    })
    public void isPositive(Strand strand, boolean expected) {
        assertThat(strand.isPositive(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, false",
            "NEGATIVE, true",
    })
    public void isNegative(Strand strand, boolean expected) {
        assertThat(strand.isNegative(), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "+, POSITIVE",
            "-, NEGATIVE",
    })
    public void parseStrand(String value, Strand expected) {
        assertThat(Strand.parseStrand(value), equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "POSITIVE, +",
            "NEGATIVE, -",
    })
    public void toString(Strand strand, String expected) {
        assertThat(strand.toString(), equalTo(expected));
    }

}