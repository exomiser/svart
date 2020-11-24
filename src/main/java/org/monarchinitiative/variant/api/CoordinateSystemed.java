package org.monarchinitiative.variant.api;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface CoordinateSystemed<T> {

    CoordinateSystem coordinateSystem();

    T withCoordinateSystem(CoordinateSystem coordinateSystem);

    /**
     * @return <code>true</code> if the region is represented using {@link CoordinateSystem#ZERO_BASED}, where a region
     * is specified by a half-closed-half-open interval
     */
    default boolean isZeroBased() {
        return coordinateSystem().isZeroBased();
    }

    /**
     * @return <code>true</code> if the region is represented using {@link CoordinateSystem#ONE_BASED}, where a region
     * is specified by a closed interval
     */
    default boolean isOneBased() {
        return coordinateSystem().isOneBased();
    }

    default T toZeroBased() {
        return withCoordinateSystem(CoordinateSystem.ZERO_BASED);
    }

    default T toOneBased() {
        return withCoordinateSystem(CoordinateSystem.ONE_BASED);
    }

}
