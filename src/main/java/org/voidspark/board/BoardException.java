package org.voidspark.board;

import org.voidspark.iceprogjava.exceptions.HardwareException;

/**
 * Exception which indicates a problem with the board.
 */
public class BoardException extends HardwareException {
    private static final long serialVersionUID = 1L;

    public BoardException(String message, Throwable cause) {
        super(message, cause);
    }

    public BoardException(String message) {
        super(message);
    }
}
