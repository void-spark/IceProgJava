package org.voidspark.spi;

/**
 * Exception which indicates a problem at the SPI level/implementation.
 */
public class SpiException extends Exception {
    private static final long serialVersionUID = 1L;

    public SpiException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpiException(String message) {
        super(message);
    }
}
