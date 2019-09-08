package org.voidspark.flash;

import static java.lang.String.format;
import static org.voidspark.flash.FlashCommands.BLOCK_ERASE_64KB;
import static org.voidspark.flash.FlashCommands.CHIP_ERASE;
import static org.voidspark.flash.FlashCommands.JEDEC_ID;
import static org.voidspark.flash.FlashCommands.PAGE_PROGRAM;
import static org.voidspark.flash.FlashCommands.POWER_DOWN;
import static org.voidspark.flash.FlashCommands.READ_DATA;
import static org.voidspark.flash.FlashCommands.READ_STATUS_REGISTER_1;
import static org.voidspark.flash.FlashCommands.RELEASE_POWER_DOWN;
import static org.voidspark.flash.FlashCommands.WRITE_ENABLE;
import static org.voidspark.flash.FlashCommands.WRITE_STATUS_REGISTER;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.voidspark.spi.SpiBus;
import org.voidspark.spi.SpiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SPI Flash operations<br>
 * Source: Winbond W25Q80, W25Q16, W25Q32 datasheet.
 */
public final class FlashOperations {
    private static final Logger LOG = LoggerFactory.getLogger(FlashOperations.class);


    // Status register
    static final int BUSY = 0x01 << 0;
    static final int WRITE_ENABLE_LATCH = 0x01 << 1;

    private final SpiBus spi;
    private final boolean verbose;

    public FlashOperations(final SpiBus spi, final boolean verbose) {
        this.spi = spi;
        this.verbose = verbose;
    }

    /**
     * Should take the chip out of any special mode.<br>
     * On an Upduino (W25Q32) it triggers a 'Mode Bit Reset', but that should only need one or two bytes.<br>
     * I guess other flash chips want more.
     */
    public void reset() throws FlashException {
        try {
            basicSend(0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff);
        } catch (final SpiException ex) {
            throw new FlashException(format("Failed to reset flash chip: %s", ex.getMessage()), ex);
        }
    }

    public void powerUp() throws FlashException {
        try {
            basicSend(RELEASE_POWER_DOWN);
        } catch (final SpiException ex) {
            throw new FlashException(format("Failed to power up flash chip: %s", ex.getMessage()), ex);
        }
    }

    public void powerDown() throws FlashException {
        try {
            basicSend(POWER_DOWN);
        } catch (final SpiException ex) {
            throw new FlashException(format("Failed to power down flash chip: %s", ex.getMessage()), ex);
        }
    }

    public int readStatus() throws FlashException {
        try {
            final int status = readStatusRegister1();

            if (verbose) {
                LOG.info(format("SR1: 0x%02X", status));
                LOG.info(format(" - SPRL: %s", ((status & (1 << 7)) == 0) ? "unlocked" : "locked"));
                LOG.info(format(" -  SPM: %s", ((status & (1 << 6)) == 0) ? "Byte/Page Prog Mode" : "Sequential Prog Mode"));
                LOG.info(format(" -  EPE: %s", ((status & (1 << 5)) == 0) ? "Erase/Prog success" : "Erase/Prog error"));
                LOG.info(format("-  SPM: %s", ((status & (1 << 4)) == 0) ? "~WP asserted" : "~WP deasserted"));
                LOG.info(format(" -  SWP: "));
                switch ((status >> 2) & 0x3) {
                case 0:
                    LOG.info(format("All sectors unprotected"));
                    break;
                case 1:
                    LOG.info(format("Some sectors protected"));
                    break;
                case 2:
                    LOG.info(format("Reserved (xxxx 10xx)"));
                    break;
                case 3:
                    LOG.info(format("All sectors protected"));
                    break;
                }
                LOG.info(format(" -  WEL: %s", ((status & WRITE_ENABLE_LATCH) == 0) ? "Not write enabled" : "Write enabled"));
                LOG.info(format(" - ~RDY: %s", ((status & BUSY) == 0) ? "Ready" : "Busy"));
            }

            LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(1000));
            return status;
        } catch (final SpiException ex) {
            throw new FlashException(format("Failed to get flash chip status: %s", ex.getMessage()), ex);
        }
    }

    public void blockErase64kB(final int addr) throws FlashException {
        LOG.info(format("Erase 64kB block at 0x%06X..", addr));
        try {
            basicSend(BLOCK_ERASE_64KB, addr >> 16, addr >> 8, addr);
        } catch (final SpiException ex) {
            throw new FlashException(format("Failed to erase 64k flash chip block: %s", ex.getMessage()), ex);
        }
    }

    public void pageProgram(final int addr, final byte[] data, final int n) throws FlashException {
        if (verbose) {
            LOG.info(format("prog 0x%06X +0x%03X..", addr, n));
        }

        try {
            spi.chipSelect();
            spi.send(data(PAGE_PROGRAM, addr >> 16, addr >> 8, addr));
            spi.send(data, n);
            spi.chipDeselect();

            if (verbose) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < n; i++) {
                    builder.append(format("%02x%c", data[i], i == n - 1 || i % 32 == 31 ? '\n' : ' '));
                }
                LOG.info(builder.toString());
            }
        } catch (final SpiException ex) {
            throw new FlashException(format("Failed to program flash chip page: %s", ex.getMessage()), ex);
        }
    }

    public void readData(final int addr, final byte[] data, final int n) throws FlashException {
        if (verbose) {
            LOG.info(format("read 0x%06X +0x%03X..", addr, n));
        }

        try {
            // Make sure we don't send random data, just in case.
            Arrays.fill(data, 0, n, (byte) 0);

            spi.chipSelect();
            spi.send(data(READ_DATA, addr >> 16, addr >> 8, addr));
            spi.xfer(data, n);
            spi.chipDeselect();

            if (verbose) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < n; i++) {
                    builder.append(format("%02x%c", data[i], i == n - 1 || i % 32 == 31 ? '\n' : ' '));
                }
                LOG.info(builder.toString());
            }
        } catch (final SpiException ex) {
            throw new FlashException(format("Failed to read data from flash chip: %s", ex.getMessage()), ex);
        }
    }

    public void waitWhileBusy() throws FlashException {
        if (verbose) {
            LOG.info(format("waiting.."));
        }

        try {
            int count = 0;
            while (true) {
                if ((readStatusRegister1() & BUSY) == 0) {
                    if (count < 2) {
                        count++;
//                        if (verbose) {
//                            fprintf(stderr, "r");
//                            fflush(stderr);
//                        }
                    } else {
                        if (verbose) {
//                            fprintf(stderr, "R");
//                            fflush(stderr);
                        }
                        break;
                    }
                } else {
                    if (verbose) {
//                        fprintf(stderr, ".");
//                        fflush(stderr);
                    }
                    count = 0;
                }

                LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(1000));
            }

            if (verbose) {
//                fprintf(stderr, "\n");
            }
        } catch (final SpiException ex) {
            throw new FlashException(format("Failed waiting for flash chip to become not busy: %s", ex.getMessage()), ex);
        }
    }

    public void writeEnable() throws FlashException {
        if (verbose) {
            LOG.info(format("status before enable:"));
            readStatus();
        }

        if (verbose) {
            LOG.info(format("write enable.."));
        }

        try {
            basicSend(WRITE_ENABLE);

            if (verbose) {
                LOG.info(format("status after enable:"));
                readStatus();
            }
        } catch (final SpiException ex) {
            throw new FlashException(format("Failed setting flash chip write enable: %s", ex.getMessage()), ex);
        }
    }

    public void disableProtection() throws FlashException {
        LOG.info(format("disable flash protection..."));

        try {
            // Write Status Register <- 0x00
            basicSend(WRITE_STATUS_REGISTER, 0x00);

            waitWhileBusy();

            final int statusRegister1 = readStatusRegister1();
            if (statusRegister1 != 0x00) {
                LOG.info(format("failed to disable protection, SR now equal to 0x%02x (expected 0x00)\n", statusRegister1));
            }
        } catch (final SpiException ex) {
            throw new FlashException(format("Failed disabling flash chip write protection: %s", ex.getMessage()), ex);
        }
    }

    public void bulkErase() throws FlashException {
        LOG.info(format("bulk erase.."));
        try {
            basicSend(CHIP_ERASE);
        } catch (final SpiException ex) {
            throw new FlashException(format("Failed erasing flash chip: %s", ex.getMessage()), ex);
        }
    }

    /**
     * Reads JEDEC ID
     * 
     * <pre>
     * JEDEC ID structure:
     * Byte No. | Data Type
     * ---------+----------
     *        0 | FC_JEDECID Request Command
     *        1 | MFG ID
     *        2 | Dev ID 1
     *        3 | Dev ID 2
     *        4 | Ext Dev Str Len
     * </pre>
     */
    public void readId() throws FlashException {
        try {
            // command + 4 response bytes
            final byte[] data1 = data(JEDEC_ID, 0x00, 0x00, 0x00, 0x00);

            if (verbose) {
                LOG.info(format("read flash ID.."));
            }

            spi.chipSelect();

            // Write command and read first 4 bytes
            spi.xfer(data1);
            int edsl = data1[4] & 0xff;
            byte[] data2 = null;
            if (edsl == 0xFF) {
                LOG.warn("Extended Device String Length is 0xFF, this is likely a read error. Ignoring...");
            } else {
                // Read extended JEDEC ID bytes
                if (edsl != 0) {
                    data2 = new byte[edsl];
                    spi.xfer(data2);
                }
            }

            spi.chipDeselect();

            // TODO: Add full decode of the JEDEC ID.
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < data1.length; i++) {
                builder.append(format(" 0x%02X", data1[i]));
            }
            if (data2 != null) {
                for (int i = 1; i < edsl; i++) {
                    builder.append(format(" 0x%02X", data2[i]));
                }
            }
            LOG.info(format("flash ID:%s", builder.toString()));
        } catch (final SpiException ex) {
            throw new FlashException(format("Failed reading flash chip ID: %s", ex.getMessage()), ex);
        }
    }

    private int readStatusRegister1() throws SpiException {
        final byte[] data = data(READ_STATUS_REGISTER_1, 0x00);

        spi.chipSelect();
        spi.xfer(data);
        spi.chipDeselect();

        return data[1] & 0xff;
    }

    private void basicSend(int... intData) throws SpiException {
        spi.chipSelect();
        spi.send(data(intData));
        spi.chipDeselect();
    }

    private byte[] data(int... intData) {
        final byte[] data = new byte[intData.length];
        for (int pos = 0; pos < intData.length; pos++) {
            data[pos] = (byte) intData[pos];
        }
        return data;
    }
}
