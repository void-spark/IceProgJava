package org.voidspark.spi;

/**
 * Interface to the SPI implementation.
 */
public interface SpiBus {
    void chipSelect() throws SpiException;

    void chipDeselect() throws SpiException;

    void send(final byte[] data) throws SpiException;

    void send(final byte[] data, int length) throws SpiException;

    void xfer(final byte[] data) throws SpiException;

    void xfer(final byte[] data, int length) throws SpiException;
}
