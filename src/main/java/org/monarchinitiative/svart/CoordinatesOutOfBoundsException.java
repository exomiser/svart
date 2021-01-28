package org.monarchinitiative.svart;

public class CoordinatesOutOfBoundsException extends InvalidCoordinatesException {

    public CoordinatesOutOfBoundsException() {
        super();
    }

    public CoordinatesOutOfBoundsException(String message) {
        super(message);
    }

    public CoordinatesOutOfBoundsException(String message, Throwable cause) {
        super(message, cause);
    }

    public CoordinatesOutOfBoundsException(Throwable cause) {
        super(cause);
    }

    protected CoordinatesOutOfBoundsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
