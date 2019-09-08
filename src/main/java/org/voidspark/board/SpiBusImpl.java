package org.voidspark.board;

import static java.lang.String.format;

import org.voidspark.mpssse.Mpsse;
import org.voidspark.mpssse.MpssseException;
import org.voidspark.spi.SpiBus;
import org.voidspark.spi.SpiException;

final class SpiBusImpl implements SpiBus {
    private final BoardControl boardControl;
    private final Mpsse mpsse;

    public SpiBusImpl(final BoardControl boardControl, final Mpsse mpsse) {
        this.boardControl = boardControl;
        this.mpsse = mpsse;
    }

    @Override
    public void chipSelect() throws SpiException {
        try {
            boardControl.flash_chip_select();
        } catch (final BoardException ex) {
            throw new SpiException(format("Failed to select chip over SPI: %s", ex.getMessage()), ex);
        }
    }

    @Override
    public void chipDeselect() throws SpiException {
        try {
            boardControl.flash_chip_deselect();
        } catch (final BoardException ex) {
            throw new SpiException(format("Failed to deselect chip over SPI: %s", ex.getMessage()), ex);
        }
    }

    @Override
    public void send(final byte[] data) throws SpiException {
        try {
            mpsse.sendSpi(data);
        } catch (final MpssseException ex) {
            throw new SpiException(format("Failed to send data over SPI: %s", ex.getMessage()), ex);
        }
    }

    @Override
    public void send(byte[] data, int length) throws SpiException {
        try {
            mpsse.sendSpi(data, length);
        } catch (final MpssseException ex) {
            throw new SpiException(format("Failed to send data over SPI: %s", ex.getMessage()), ex);
        }
    }

    @Override
    public void xfer(final byte[] data) throws SpiException {
        try {
            mpsse.xferSpi(data);
        } catch (final MpssseException ex) {
            throw new SpiException(format("Failed to xfer data over SPI: %s", ex.getMessage()), ex);
        }
    }

    @Override
    public void xfer(final byte[] data, final int length) throws SpiException {
        try {
            mpsse.xferSpi(data, length);
        } catch (final MpssseException ex) {
            throw new SpiException(format("Failed to xfer data over SPI: %s", ex.getMessage()), ex);
        }
    }
}
