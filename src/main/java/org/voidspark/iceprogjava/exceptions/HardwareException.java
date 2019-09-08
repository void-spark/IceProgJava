
package org.voidspark.iceprogjava.exceptions;

public class HardwareException extends AppException {

    private static final long serialVersionUID = 1L;

    private static final int EXIT_CODE = 2;

    public HardwareException(String message, Throwable cause) {
        super(EXIT_CODE, message, cause);
    }

    public HardwareException(String message) {
        super(EXIT_CODE, message);
    }
}
