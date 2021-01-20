package org.monarchinitiative.svart;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface CoordinateSystemed<T> {

    CoordinateSystem coordinateSystem();

    T withCoordinateSystem(CoordinateSystem coordinateSystem);

    /**
     * @return <code>true</code> if the region is represented using {@link CoordinateSystem#LEFT_OPEN}, where a region
     * is specified by a half-closed-half-open interval
     */
    default boolean isZeroBased() {
        return coordinateSystem().isZeroBased();
    }

    /**
     * @return <code>true</code> if the region is represented using {@link CoordinateSystem#FULLY_CLOSED}, where a region
     * is specified by a closed interval
     */
    default boolean isOneBased() {
        return coordinateSystem().isOneBased();
    }

    default T toZeroBased() {
        return withCoordinateSystem(CoordinateSystem.LEFT_OPEN);
    }

    default T toOneBased() {
        return withCoordinateSystem(CoordinateSystem.FULLY_CLOSED);
    }

}
