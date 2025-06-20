package org.monarchinitiative.svart;


/**
 * Indicates that a class has a {@link CoordinateSystem} and can be converted into another coordinate system.
 *
 * @param <T>
 */
public interface Convertible<T> extends CoordinateSystemed {

    T withCoordinateSystem(CoordinateSystem coordinateSystem);
    
    default T toZeroBased() {
        return withCoordinateSystem(CoordinateSystem.ZERO_BASED);
    }

    default T toOneBased() {
        return withCoordinateSystem(CoordinateSystem.ONE_BASED);
    }
}
