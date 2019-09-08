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
import java.util.concurrent.Callable;

import org.voidspark.board.BoardControl;
import org.voidspark.flash.FlashOperations;
import org.voidspark.iceprogjava.exceptions.AppException;
import org.voidspark.iceprogjava.exceptions.UserException;
import org.voidspark.iceprogjava.exceptions.VerifyException;
import org.voidspark.iceprogjava.mixins.ExitStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

public abstract class AbstractCommand implements Callable<Integer> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCommand.class);

    protected BoardControl boardControl;

    protected FlashOperations flash;

    private Path file;

    protected long fileSize;

    private FileChannel fileChannel;

    @Mixin
    private ExitStatus exitStatusMixin = new ExitStatus();

    @ArgGroup(validate = false, heading = "%nGlobal options:%n")
    protected GlobalOptions globalOptions = new GlobalOptions();

    static class GlobalOptions {
        // Use slow SPI clock
        @Option(names = "-s", description = "slow SPI (50 kHz instead of 6 MHz)")
        boolean slowClock = false;

        // Provide verbose output
        @Option(names = "-v", description = "verbose output")
        boolean verbose = false;

        //        fprintf(stderr, "  -d <device string>    use the specified USB device [default: i:0x0403:0x6010 or i:0x0403:0x6014]\n");
        //        fprintf(stderr, "                          d:<devicenode>               (e.g. d:002/005)\n");
        //        fprintf(stderr, "                          i:<vendor>:<product>         (e.g. i:0x0403:0x6010)\n");
        //        fprintf(stderr, "                          i:<vendor>:<product>:<index> (e.g. i:0x0403:0x6010:0)\n");
        //        fprintf(stderr, "                          s:<vendor>:<product>:<serial-string>\n");
        //        fprintf(stderr, "  -I [ABCD]             connect to the specified interface on the FTDI chip\n");
        //        fprintf(stderr, "                          [default: A]\n");

        //    switch (opt) {
        //    case 'd': /* device string */
        //        devstr = optarg;
        //        break;
        //    case 'I': /* FTDI Chip interface select */
        //        if (!strcmp(optarg, "A"))
        //            ifnum = 0;
        //        else if (!strcmp(optarg, "B"))
        //            ifnum = 1;
        //        else if (!strcmp(optarg, "C"))
        //            ifnum = 2;
        //        else if (!strcmp(optarg, "D"))
        //            ifnum = 3;
        //        else {
        //            fprintf(stderr, "%s: `%s' is not a valid interface (must be `A', `B', `C', or `D')\n", my_name, optarg);
        //            return EXIT_FAILURE;
        //        }

    }

    @Override
    public Integer call() throws InterruptedException {

        try {
            prepare();
        } catch (final UserException ex) {
            LOG.error(ex.getMessage(), ex);
            return ex.getExitCode();
        }

        LOG.info(format("init.."));

        boardControl = new BoardControl();
        try {

            // ---------------------------------------------------------
            // Initialize USB connection to FT2232H
            // ---------------------------------------------------------

            boardControl.init(globalOptions.slowClock);

            flash = new FlashOperations(boardControl.getSpiBus(), globalOptions.verbose);

            LOG.info(format("cdone: %s", boardControl.get_cdone() ? "high" : "low"));

            boardControl.flash_release_reset();
            Thread.sleep(100);

            reset();

            perform();

            release();

            // ---------------------------------------------------------
            // Exit
            // ---------------------------------------------------------

            LOG.info(format("Bye."));
            boardControl.close();
            return 0;
        } catch (final AppException ex) {
            LOG.error(ex.getMessage(), ex);
            boardControl.abort();
            return ex.getExitCode();
        }
    }

    @SuppressWarnings("unused")
    protected void prepare() throws UserException {
    };

    protected abstract void perform() throws AppException, InterruptedException;

    protected final void openInputFile(final String fileName) throws UserException {
        file = Paths.get(fileName);
        try {
            fileChannel = FileChannel.open(file, StandardOpenOption.READ);
        } catch (IOException ex) {
            throw new UserException(format("Can't open '%s' for reading: %s", fileName, ex.getMessage()), ex);
        }

        try {
            fileSize = Files.size(file);
        } catch (final IOException ex) {
            throw new UserException(format("Can't get '%s' size: %s", fileName, ex.getMessage()), ex);
        }
    }

    protected final void openOutputFile(final String fileName) throws UserException {
        file = Paths.get(fileName);
        try {
            fileChannel = FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException ex) {
            throw new UserException(format("Can't open '%s' for writing: %s", fileName, ex.getMessage()), ex);
        }
    }

    protected final void disableProtection() throws AppException {
        flash.writeEnable();
        flash.disableProtection();
    }

    protected final void bulkErase() throws AppException {
        flash.writeEnable();
        flash.bulkErase();
        flash.waitWhileBusy();
    }

    protected final void erase(final int offset, final long size) throws AppException {
        LOG.info(format("Erasing %d bytes", size));

        int begin_addr = offset & ~0xffff;
        int end_addr = (offset + (int) size + 0xffff) & ~0xffff;

        for (int addr = begin_addr; addr < end_addr; addr += 0x10000) {
            flash.writeEnable();
            flash.blockErase64kB(addr);
            if (globalOptions.verbose) {
                LOG.info(format("Status after block erase:"));
                flash.readStatus();
            }
            flash.waitWhileBusy();
        }
    }

    protected final void write(final int offset) throws AppException {
        LOG.info(format("programming.."));

        resetFilePosition();

        for (int rc, addr = 0; true; addr += rc) {
            ByteBuffer buffer = ByteBuffer.allocate(256);
            int page_size = 256 - (offset + addr) % 256;
            buffer.limit(page_size);
            rc = 0;
            try {
                rc = fileChannel.read(buffer);
            } catch (IOException ex) {
                throw new UserException(format("Can't read '%s': %s", file.getFileName(), ex.getMessage()), ex);
            }
            if (rc <= 0) {
                break;
            }
            flash.writeEnable();
            flash.pageProgram(offset + addr, buffer.array(), rc);
            flash.waitWhileBusy();
        }
    }

    protected final void verify(final int offset) throws AppException {
        LOG.info(format("reading.."));

        resetFilePosition();

        for (int addr = 0; true; addr += 256) {
            byte[] buffer_flash = new byte[256];
            ByteBuffer buffer_file = ByteBuffer.allocate(256);
            int rc = 0;
            try {
                rc = fileChannel.read(buffer_file);
            } catch (IOException ex) {
                throw new UserException(format("Can't read '%s': %s", file.getFileName(), ex.getMessage()), ex);
            }

            if (rc <= 0) {
                break;
            }
            flash.readData(offset + addr, buffer_flash, rc);

            if (!Arrays.equals(buffer_file.array(), 0, rc, buffer_flash, 0, rc)) {
                throw new VerifyException(format("Found difference between flash and file!"));
            }
        }

        LOG.info(format("VERIFY OK"));
    }

    protected final void read(final int offset, final int size) throws AppException {
        LOG.info(format("reading.."));

        for (int addr = 0; addr < size; addr += 256) {
            byte[] buffer = new byte[256];
            flash.readData(offset + addr, buffer, 256);
            int toWrite = size - addr > 256 ? 256 : size - addr;
            ByteBuffer writeBuffer = ByteBuffer.wrap(buffer);
            writeBuffer.limit(toWrite);
            try {
                fileChannel.write(writeBuffer);
            } catch (IOException ex) {
                throw new UserException(format("can't write '%s': %s", file.getFileName(), ex.getMessage()), ex);
            }
        }
    }

    private void resetFilePosition() throws UserException {
        try {
            fileChannel.position(0);
        } catch (IOException ex) {
            throw new UserException(format("Can't set position on '%s': %s", file.getFileName(), ex.getMessage()), ex);
        }
    }

    private final void reset() throws AppException, InterruptedException {
        LOG.info(format("reset.."));

        boardControl.flash_chip_deselect();
        Thread.sleep(250);

        LOG.info(format("cdone: %s", boardControl.get_cdone() ? "high" : "low"));

        flash.reset();
        flash.powerUp();
    }

    private final void release() throws AppException, InterruptedException {
        flash.powerDown();

        boardControl.flash_release_reset();
        Thread.sleep(250);

        LOG.info(format("cdone: %s", boardControl.get_cdone() ? "high" : "low"));
    }
}
