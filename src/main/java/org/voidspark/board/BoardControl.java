package org.voidspark.board;

import static java.lang.String.format;

import org.voidspark.mpssse.Mpsse;
import org.voidspark.mpssse.MpssseException;
import org.voidspark.spi.SpiBus;

public class BoardControl {
    private final Mpsse mpsse;

    public BoardControl() {
        mpsse = new Mpsse();
    }

    public void init(final boolean slow_clock) throws BoardException {
        try {
            mpsse.init(/* ifnum, devstr, */ slow_clock);
        } catch (MpssseException ex) {
            throw new BoardException(format("Failed to initialize board: %s", ex.getMessage()), ex);
        }
    }

    public SpiBus getSpiBus() {
        return new SpiBusImpl(this, mpsse);
    }

    // the FPGA reset is released so also FLASH chip select should be deasserted
    public void flash_release_reset() throws BoardException {
        set_cs_creset(true, true);
    }

    // FLASH chip select deassert
    public void flash_chip_deselect() throws BoardException {
        set_cs_creset(true, false);
    }

    // FLASH chip select assert
    // should only happen while FPGA reset is asserted
    public void flash_chip_select() throws BoardException {
        set_cs_creset(false, false);
    }

    public void set_cs_creset(boolean cs_b, boolean creset_b) throws BoardException {
        int gpio = 0;
        int direction = 0x93;

        if (cs_b) {
            // ADBUS4 (GPIOL0)
            gpio |= 0x10;
        }

        if (creset_b) {
            // ADBUS7 (GPIOL3)
            gpio |= 0x80;
        }

        try {
            mpsse.setGpio(gpio, direction);
        } catch (MpssseException ex) {
            throw new BoardException(format("Failed to set CS and CRESET: %s", ex.getMessage()), ex);
        }
    }

    public boolean get_cdone() throws BoardException {
        // ADBUS6 (GPIOL2)
        try {
            return (mpsse.readbLow() & 0x40) != 0;
        } catch (MpssseException ex) {
            throw new BoardException(format("Failed to get CDONE: %s", ex.getMessage()), ex);
        }
    }

    public void abort() {
        mpsse.abort();
    }

    public void close() {
        mpsse.close();
    }

}
