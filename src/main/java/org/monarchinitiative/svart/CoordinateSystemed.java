package org.monarchinitiative.svart;

/**
 * Marker interface to indicate that a class has a {@link CoordinateSystem}. If a class requires converting between one
 * {@link CoordinateSystem} and another implementing the {@link Convertible} interface will enable the library to convert
 * a set of {@link Coordinates} between the zero and one-based coordinate systems commonly used in bioinformatics.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface CoordinateSystemed {

    CoordinateSystem coordinateSystem();

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

}
