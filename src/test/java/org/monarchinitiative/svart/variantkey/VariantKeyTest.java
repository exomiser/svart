package org.monarchinitiative.svart.variantkey;


import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class VariantKeyTest {

    @Test
    void compareTo() {
        VariantKey first = VariantKey.of(0, 0, "", "");
        VariantKey second = VariantKey.of(1, 1, "A", "A");
        VariantKey third = VariantKey.of(1, 2, "A", "A");
        VariantKey fourth = VariantKey.of(1, 2, "C", "A");
        VariantKey fifth = VariantKey.of(2, 2, "C", "A");
        VariantKey sixth = VariantKey.of(16, 2, "C", "A");
        VariantKey seventh = VariantKey.of(23, 2, "C", "A");
        VariantKey eighth = VariantKey.of(24, 5000, "T", "TTTTTTT");
        VariantKey nineth = VariantKey.of(25, 2345567, "TTTTTTTTTT", "T");

        List<VariantKey> actual = Arrays.asList(second, first, nineth, sixth, third, eighth, fifth, seventh, fourth);
        actual.sort(VariantKey::compareTo);
        List<VariantKey> expected = List.of(first, second, third, fourth, fifth, sixth, seventh, eighth, nineth);
        assertThat(actual, equalTo(expected));
    }

}