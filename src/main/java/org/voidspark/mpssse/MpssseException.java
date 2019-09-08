package org.voidspark.mpssse;

public class MpssseException extends Exception {
    private static final long serialVersionUID = 1L;

    public MpssseException(String message, Throwable cause) {
        super(message, cause);
    }

    public MpssseException(String message) {
        super(message);
    }
}
