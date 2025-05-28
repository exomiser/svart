package org.monarchinitiative.svart.coordinates;


import org.monarchinitiative.svart.Coordinates;

/**
 * Utility class which provides a static method to format {@link Coordinates} objects into a string representation
 * for use in Object.toString() override methods for classes implementing {@link Coordinates}.
 */
public final class CoordinatesFormat {

    private CoordinatesFormat() {
    }

    /**
     * Formats the given Coordinates object into a string representation for use in Object.toString() override methods
     * where adding the surrounding `Coordinates{}` tags are unnecessary noise.
     *
     * @param coordinates the Coordinates object to be formatted
     * @return a string representation of the Coordinates object
     */
    public static String formatCoordinates(Coordinates coordinates) {
        if (coordinates.isPrecise()) {
            return "coordinateSystem=" + coordinates.coordinateSystem() +
                   ", start=" + coordinates.start() +
                   ", end=" + coordinates.end();
        }
        return "coordinateSystem=" + coordinates.coordinateSystem() +
               ", start=" + coordinates.start() + (coordinates.startConfidenceInterval().isPrecise() ? "" : " " + coordinates.startConfidenceInterval()) +
               ", end=" + coordinates.end() + (coordinates.endConfidenceInterval().isPrecise() ? "" : " " + coordinates.endConfidenceInterval());
    }
}
