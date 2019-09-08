package org.voidspark.iceprogjava.exceptions;

public class UserException extends AppException {

    private static final long serialVersionUID = 1L;

    private static final int EXIT_CODE = 1;

    public UserException(String message, Throwable cause) {
        super(EXIT_CODE, message, cause);
    }

    public UserException(String message) {
        super(EXIT_CODE, message);
    }
}
