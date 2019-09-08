package org.voidspark.flash;

import org.voidspark.iceprogjava.exceptions.HardwareException;

/**
 * Exception which indicates a problem with the flash chip.
 */
public class FlashException extends HardwareException {
    private static final long serialVersionUID = 1L;

    public FlashException(String message, Throwable cause) {
        super(message, cause);
    }

    public FlashException(String message) {
        super(message);
    }
}
