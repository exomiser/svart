package org.monarchinitiative.svart.impl;


import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.GenomicVariant;

/**
 * Utility class which provides a static method to format {@link Coordinates} objects into a string representation
 * for use in Object.toString() override methods for instances of {@link GenomicVariant}.
 */
class CoordinatesFormat {

    private CoordinatesFormat() {
    }

    /**
     * Formats the given Coordinates object into a string representation for use in Object.toString() override methods
     * where adding the surrounding `Coordinates{}` tags are uneccesary noise.
     *
     * @param coordinates the Coordinates object to be formatted
     * @return a string representation of the Coordinates object
     */
    static String formatCoordinates(Coordinates coordinates) {
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
