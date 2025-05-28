package org.monarchinitiative.svart.coordinates;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.Convertible;
import org.monarchinitiative.svart.CoordinateSystem;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ConvertibleTest {

    private final ConvertibleThing zeroBased = new ConvertibleThing(CoordinateSystem.ZERO_BASED);
    private final ConvertibleThing oneBased = new ConvertibleThing(CoordinateSystem.ONE_BASED);

    @Test
    void withCoordinateSystem() {
        assertThat(zeroBased.withCoordinateSystem(CoordinateSystem.ZERO_BASED), equalTo(zeroBased));
        assertThat(zeroBased.withCoordinateSystem(CoordinateSystem.ONE_BASED), equalTo(oneBased));
        assertThat(oneBased.withCoordinateSystem(CoordinateSystem.ONE_BASED), equalTo(oneBased));
        assertThat(oneBased.withCoordinateSystem(CoordinateSystem.ZERO_BASED), equalTo(zeroBased));
    }

    @Test
    void toZeroBased() {
        assertThat(zeroBased.toZeroBased(), equalTo(zeroBased));
        assertThat(oneBased.toZeroBased(), equalTo(zeroBased));
    }

    @Test
    void toOneBased() {
        assertThat(zeroBased.toOneBased(), equalTo(oneBased));
        assertThat(oneBased.toOneBased(), equalTo(oneBased));
    }


    private record ConvertibleThing(CoordinateSystem coordinateSystem) implements Convertible<ConvertibleThing> {
        @Override
        public ConvertibleThing withCoordinateSystem(CoordinateSystem coordinateSystem) {
            return this.coordinateSystem == coordinateSystem ? this : new ConvertibleThing(coordinateSystem);
        }
    }
}