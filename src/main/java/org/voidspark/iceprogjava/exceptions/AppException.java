package org.voidspark.iceprogjava.exceptions;

public abstract class AppException extends Exception {
    private static final long serialVersionUID = 1L;

    private final int exitCode;

    public AppException(final int exitCode, final String message) {
        super(message);
        this.exitCode = exitCode;
    }

    public AppException(final int exitCode, String message, Throwable cause) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
