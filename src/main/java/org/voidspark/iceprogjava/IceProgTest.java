package org.voidspark.iceprogjava;
import static java.lang.String.format;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.voidspark.mpssse.Mpsse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IceProgTest {
    private static final Logger LOG = LoggerFactory.getLogger(IceProgTest.class);

    /* Flash command definitions */
    /* This command list is based on the Winbond W25Q128JV Datasheet */
    private static final int FC_WE = 0x06; /* Write Enable */
    private static final int FC_SRWE = 0x50; /* Volatile SR Write Enable */
    private static final int FC_WD = 0x04; /* Write Disable */
    private static final int FC_RPD = 0xAB; /* Release Power-Down; returns Device ID */
    private static final int FC_MFGID = 0x90; /*  Read Manufacturer/Device ID */
    private static final int FC_JEDECID = 0x9F; /* Read JEDEC ID */
    private static final int FC_UID = 0x4B; /* Read Unique ID */
    private static final int FC_RD = 0x03; /* Read Data */
    private static final int FC_FR = 0x0B; /* Fast Read */
    private static final int FC_PP = 0x02; /* Page Program */
    private static final int FC_SE = 0x20; /* Sector Erase 4kb */
    private static final int FC_BE32 = 0x52; /* Block Erase 32kb */
    private static final int FC_BE64 = 0xD8; /* Block Erase 64kb */
    private static final int FC_CE = 0xC7; /* Chip Erase */
    private static final int FC_RSR1 = 0x05; /* Read Status Register 1 */
    private static final int FC_WSR1 = 0x01; /* Write Status Register 1 */
    private static final int FC_RSR2 = 0x35; /* Read Status Register 2 */
    private static final int FC_WSR2 = 0x31; /* Write Status Register 2 */
    private static final int FC_RSR3 = 0x15; /* Read Status Register 3 */
    private static final int FC_WSR3 = 0x11; /* Write Status Register 3 */
    private static final int FC_RSFDP = 0x5A; /* Read SFDP Register */
    private static final int FC_ESR = 0x44; /* Erase Security Register */
    private static final int FC_PSR = 0x42; /* Program Security Register */
    private static final int FC_RSR = 0x48; /* Read Security Register */
    private static final int FC_GBL = 0x7E; /* Global Block Lock */
    private static final int FC_GBU = 0x98; /* Global Block Unlock */
    private static final int FC_RBL = 0x3D; /* Read Block Lock */
    private static final int FC_RPR = 0x3C; /* Read Sector Protection Registers (adesto) */
    private static final int FC_IBL = 0x36; /* Individual Block Lock */
    private static final int FC_IBU = 0x39; /* Individual Block Unlock */
    private static final int FC_EPS = 0x75; /* Erase / Program Suspend */
    private static final int FC_EPR = 0x7A; /* Erase / Program Resume */
    private static final int FC_PD = 0xB9; /* Power-down */
    private static final int FC_QPI = 0x38; /* Enter QPI mode */
    private static final int FC_ERESET = 0x66; /* Enable Reset */
    private static final int FC_RESET = 0x99; /* Reset Device */
    
    static Mpsse mpsse;

    static boolean verbose = false;

    public static void main(String[] args) throws InterruptedException {

        boolean test_mode = false;

        boolean slow_clock = false;

        boolean read_mode =        false;
        boolean check_mode =       false;
        boolean erase_mode =       false;
        boolean disable_protect =  false;
        boolean dont_erase =       false;
        boolean bulk_erase =       false;
        boolean disable_verify =   false;
        
        int read_size = 256 * 1024;
        int erase_size = 0;
        int rw_offset = 0;

        final String my_name = "iceprog";
        final String filename = "chip.bin";
        
        /* open input/output file in advance
        so we can fail before initializing the hardware */

        FileChannel fc = null;
        long file_size = -1;
        Path file = Paths.get(filename);

        if (test_mode) {
            /* nop */;
        } else if (erase_mode) {
            file_size = erase_size;
        } else if (read_mode) {
            try {
                fc = FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            } catch (IOException ex) {
                LOG.error(format("%s: can't open '%s' for writing: %s", my_name, filename, ex.getMessage()), ex);
                System.exit(1);
            }
        } else {
            try {
                fc = FileChannel.open(file, StandardOpenOption.READ);
            } catch (IOException ex) {
                LOG.error(format("%s: can't open '%s' for reading: %s", my_name, filename, ex.getMessage()), ex);
                System.exit(1);
            }

             /* For regular programming, we need to read the file
                twice--once for programming and once for verifying--and
                need to know the file size in advance in order to erase
                the correct amount of memory.*/

            if (!check_mode) {
                try {
                    file_size = Files.size(file);
                } catch (IOException ex) {
                    LOG.error(format("%s: can't get '%s' size: %s", my_name, filename, ex.getMessage()), ex);
                    System.exit(1);
                }
            }
        }

        // ---------------------------------------------------------
        // Initialize USB connection to FT2232H
        // ---------------------------------------------------------

        
        
        LOG.info(format("init.."));
        mpsse = new Mpsse();

        mpsse.init(/* ifnum, devstr, */ slow_clock);

        LOG.info(format("cdone: %s", get_cdone() ? "high" : "low"));

        flash_release_reset();
        Thread.sleep(100);

        if (test_mode) {
            LOG.info(format("reset.."));

            flash_chip_deselect();
            Thread.sleep(250);

            LOG.info(format("cdone: %s", get_cdone() ? "high" : "low"));

            flash_reset();
            flash_power_up();

            flash_read_id();

            flash_power_down();

            flash_release_reset();
            Thread.sleep(250);

            LOG.info(format("cdone: %s", get_cdone() ? "high" : "low"));
        } else /* program flash */
        {
            // ---------------------------------------------------------
            // Reset
            // ---------------------------------------------------------

            LOG.info(format("reset.."));

            flash_chip_deselect();
            Thread.sleep(250);

            LOG.info(format("cdone: %s", get_cdone() ? "high" : "low"));

            flash_reset();
            flash_power_up();

            flash_read_id();


            // ---------------------------------------------------------
            // Program
            // ---------------------------------------------------------

            if (!read_mode && !check_mode)
            {
                if (disable_protect)
                {
                    flash_write_enable();
                    flash_disable_protection();
                }
                
                if (!dont_erase)
                {
                    if (bulk_erase)
                    {
                        flash_write_enable();
                        flash_bulk_erase();
                        flash_wait();
                    }
                    else
                    {
                        LOG.info(format( "file size: %d", file_size));

                        int begin_addr = rw_offset & ~0xffff;
                        int end_addr = (rw_offset + (int)file_size + 0xffff) & ~0xffff;

                        for (int addr = begin_addr; addr < end_addr; addr += 0x10000) {
                            flash_write_enable();
                            flash_64kB_sector_erase(addr);
                            if (verbose) {
                                LOG.info(format( "Status after block erase:"));
                                flash_read_status();
                            }
                            flash_wait();
                        }
                    }
                }

                if (!erase_mode)
                {
                    LOG.info(format("programming.."));

                    for (int rc, addr = 0; true; addr += rc) {
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        int page_size = 256 - (rw_offset + addr) % 256;
                        buffer.limit(page_size);
                        rc = 0;
                        try {
                            rc = fc.read(buffer);
                        } catch (IOException ex) {
                            LOG.error(format("%s: can't read '%s': %s", my_name, filename, ex.getMessage()), ex);
                            System.exit(1);
                        }
                        if (rc <= 0) {
                            break;
                        }
                        flash_write_enable();
                        flash_prog(rw_offset + addr, buffer.array(), rc);
                        flash_wait();
                    }

                    /* seek to the beginning for second pass */
                    try {
                        fc.position(0);
                    } catch (IOException ex) {
                        LOG.error(format("%s: can't set position on '%s': %s", my_name, filename, ex.getMessage()), ex);
                        System.exit(1);
                    }

                }
            }

            // ---------------------------------------------------------
            // Read/Verify
            // ---------------------------------------------------------

            if (read_mode) {
                LOG.info(format("reading.."));
                for (int addr = 0; addr < read_size; addr += 256) {
                    byte[] buffer = new byte[256];
                    flash_read(rw_offset + addr, buffer, 256);
                    int toWrite = read_size - addr > 256 ? 256 : read_size - addr;
                    ByteBuffer writeBuffer = ByteBuffer.wrap(buffer);
                    writeBuffer.limit(toWrite);
                    try {
                        fc.write(writeBuffer);
                    } catch (IOException ex) {
                        LOG.error(format("%s: can't write '%s': %s", my_name, filename, ex.getMessage()), ex);
                        System.exit(1);
                    }
                }
            } else if (!erase_mode && !disable_verify) {
                LOG.info(format("reading.."));
                for (int addr = 0; true; addr += 256) {
                    byte[]  buffer_flash = new byte[256];
                    ByteBuffer buffer_file = ByteBuffer.allocate(256);
                    int rc = 0;
                    try {
                        rc = fc.read(buffer_file);
                    } catch (IOException ex) {
                        LOG.error(format("%s: can't read '%s': %s", my_name, filename, ex.getMessage()), ex);
                        System.exit(1);
                    }

                    if (rc <= 0) {
                        break;
                    }
                    flash_read(rw_offset + addr, buffer_flash, rc);
                    
                    if(!Arrays.equals(buffer_file.array(), 0, rc, buffer_flash, 0, rc)) {
                        LOG.info(format("Found difference between flash and file!"));
                        mpsse.error(3);
                    }
                }

                LOG.info(format("VERIFY OK"));
            }


            // ---------------------------------------------------------
            // Reset
            // ---------------------------------------------------------

            flash_power_down();

            set_cs_creset(true, true);
            Thread.sleep(250);

            LOG.info(format("cdone: %s", get_cdone() ? "high" : "low"));
        }
        
        
        
        // ---------------------------------------------------------
        // Exit
        // ---------------------------------------------------------

        LOG.info(format("Bye."));
        mpsse.close();       
    }

    static boolean get_cdone() {
        // ADBUS6 (GPIOL2)
        return (mpsse.readbLow() & 0x40) != 0;
    }

    // the FPGA reset is released so also FLASH chip select should be deasserted
    static void flash_release_reset() {
        set_cs_creset(true, true);
    }

    // FLASH chip select deassert
    static void flash_chip_deselect() {
        set_cs_creset(true, false);
    }

    // FLASH chip select assert
    // should only happen while FPGA reset is asserted
    static void flash_chip_select() {
        set_cs_creset(false, false);
    }

    static void flash_reset() {
        byte[] data = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };

        flash_chip_select();
        mpsse.xferSpi(data);
        flash_chip_deselect();
    }

    static void flash_power_up() {
        byte[] data_rpd = { (byte) FC_RPD };
        flash_chip_select();
        mpsse.xferSpi(data_rpd);
        flash_chip_deselect();
    }

    static void flash_power_down() {
        byte[] data = { (byte) FC_PD };
        flash_chip_select();
        mpsse.xferSpi(data);
        flash_chip_deselect();
    }

    static int flash_read_status() {
        byte[] data = new byte[2];
        data[0] = (byte) FC_RSR1;

        flash_chip_select();
        mpsse.xferSpi(data);
        flash_chip_deselect();

        final int status = data[1] & 0xff;

        if (verbose) {
            LOG.info(format("SR1: 0x%02X\n", status));
            LOG.info(format(" - SPRL: %s\n", ((status & (1 << 7)) == 0) ? "unlocked" : "locked"));
            LOG.info(format(" -  SPM: %s\n", ((status & (1 << 6)) == 0) ? "Byte/Page Prog Mode" : "Sequential Prog Mode"));
            LOG.info(format(" -  EPE: %s\n", ((status & (1 << 5)) == 0) ? "Erase/Prog success" : "Erase/Prog error"));
            LOG.info(format("-  SPM: %s\n", ((status & (1 << 4)) == 0) ? "~WP asserted" : "~WP deasserted"));
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
            LOG.info(format(" -  WEL: %s\n", ((status & (1 << 1)) == 0) ? "Not write enabled" : "Write enabled"));
            LOG.info(format(" - ~RDY: %s\n", ((status & (1 << 0)) == 0) ? "Ready" : "Busy"));
        }

        LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(1000));

        return status;
    }

    static void flash_64kB_sector_erase(int addr) {
        LOG.info(format("erase 64kB sector at 0x%06X..", addr));

        byte[] command = { (byte) FC_BE64, (byte) (addr >> 16), (byte) (addr >> 8), (byte) addr };
        flash_chip_select();
        mpsse.sendSpi(command);
        flash_chip_deselect();
    }

    static void flash_prog(int addr, byte[] data, int n) {
        if (verbose) {
            LOG.info(format("prog 0x%06X +0x%03X..", addr, n));
        }

        byte[] command = { (byte) FC_PP, (byte) (addr >> 16), (byte) (addr >> 8), (byte) addr };

        flash_chip_select();
        mpsse.sendSpi(command);
        mpsse.sendSpi(data, n);
        flash_chip_deselect();

        if (verbose) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < n; i++) {
                builder.append(format("%02x%c", data[i], i == n - 1 || i % 32 == 31 ? '\n' : ' '));
            }
            LOG.info(builder.toString());
        }
    }

    static void flash_read(int addr, byte[] data, int n) {
        if (verbose) {
            LOG.info(format("read 0x%06X +0x%03X..", addr, n));
        }

        byte[] command = { (byte) FC_RD, (byte) (addr >> 16), (byte) (addr >> 8), (byte) addr };

        flash_chip_select();
        mpsse.sendSpi(command, 4);
        Arrays.fill(data, 0, n, (byte) 0);
        mpsse.xferSpi(data, n);
        flash_chip_deselect();

        if (verbose) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < n; i++) {
                builder.append(format("%02x%c", data[i], i == n - 1 || i % 32 == 31 ? '\n' : ' '));
            }
            LOG.info(builder.toString());
        }
    }

    static void flash_wait() {
        if (verbose) {
            LOG.info(format("waiting.."));
        }

        int count = 0;
        while (true) {
            byte[] data = new byte[2];
            data[0] = (byte) FC_RSR1;

            flash_chip_select();
            mpsse.xferSpi(data, 2);
            flash_chip_deselect();

            if ((data[1] & 0x01) == 0) {
                if (count < 2) {
                    count++;
//                    if (verbose) {
//                        fprintf(stderr, "r");
//                        fflush(stderr);
//                    }
                } else {
                    if (verbose) {
//                        fprintf(stderr, "R");
//                        fflush(stderr);
                    }
                    break;
                }
            } else {
                if (verbose) {
//                    fprintf(stderr, ".");
//                    fflush(stderr);
                }
                count = 0;
            }

            LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(1000));
        }

        if (verbose) {
//            fprintf(stderr, "\n");
        }

    }

    static void flash_write_enable() {
        if (verbose) {
            LOG.info(format("status before enable:"));
            flash_read_status();
        }

        if (verbose) {
            LOG.info(format("write enable.."));
        }

        byte[] data = { (byte) FC_WE };
        flash_chip_select();
        mpsse.xferSpi(data);
        flash_chip_deselect();

        if (verbose) {
            LOG.info(format("status after enable:"));
            flash_read_status();
        }
    }

    static void flash_disable_protection() {
        LOG.info(format("disable flash protection..."));

        // Write Status Register 1 <- 0x00
        byte[] data = { FC_WSR1, 0x00 };
        flash_chip_select();
        mpsse.xferSpi(data, 2);
        flash_chip_deselect();

        flash_wait();

        // Read Status Register 1
        data[0] = FC_RSR1;

        flash_chip_select();
        mpsse.xferSpi(data, 2);
        flash_chip_deselect();

        if (data[1] != 0x00) {
            LOG.info(format("failed to disable protection, SR now equal to 0x%02x (expected 0x00)\n", data[1]));
        }

    }

    static void flash_bulk_erase() {
        LOG.info(format("bulk erase.."));

        byte[] data = { (byte) FC_CE };
        flash_chip_select();
        mpsse.xferSpi(data);
        flash_chip_deselect();
    }

    static void flash_read_id() {
        /* JEDEC ID structure:
         * Byte No. | Data Type
         * ---------+----------
         *        0 | FC_JEDECID Request Command
         *        1 | MFG ID
         *        2 | Dev ID 1
         *        3 | Dev ID 2
         *        4 | Ext Dev Str Len
         */

        byte[] data1 = new byte[5]; // command + 4 response bytes
        byte[] data2 = new byte[255];
        data1[0] = (byte) FC_JEDECID;

        if (verbose) {
            LOG.info(format("read flash ID.."));
        }

        flash_chip_select();

        // Write command and read first 4 bytes
        mpsse.xferSpi(data1);
        int edsl = data1[4] & 0xff;
        if (edsl == 0xFF) {
            LOG.warn("Extended Device String Length is 0xFF, this is likely a read error. Ignoring...");
        } else {
            // Read extended JEDEC ID bytes
            if (edsl != 0) {
                mpsse.xferSpi(data2, edsl);
            }
        }

        flash_chip_deselect();

        // TODO: Add full decode of the JEDEC ID.
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < data1.length; i++) {
            builder.append(format(" 0x%02X", data1[i]));
        }
        for (int i = 1; i < edsl; i++) {
            builder.append(format(" 0x%02X", data2[i]));
        }
        LOG.info(format("flash ID:%s", builder.toString()));
    }

    static void set_cs_creset(boolean cs_b, boolean creset_b) {
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

        mpsse.setGpio(gpio, direction);
    }

}
