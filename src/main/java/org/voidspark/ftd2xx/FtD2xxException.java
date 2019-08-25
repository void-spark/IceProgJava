package org.voidspark.ftd2xx;

@SuppressWarnings("serial")
public class FtD2xxException extends RuntimeException {

    public FtD2xxException(String message, Throwable cause) {
        super(message, cause);
    }

    public FtD2xxException(String message) {
        super(message);
    }

}
