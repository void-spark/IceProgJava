package org.voidspark.iceprogjava.exceptions;

public class VerifyException extends AppException {

    private static final long serialVersionUID = 1L;

    private static final int EXIT_CODE = 3;

    public VerifyException(String message, Throwable cause) {
        super(EXIT_CODE, message, cause);
    }

    public VerifyException(String message) {
        super(EXIT_CODE, message);
    }
}
