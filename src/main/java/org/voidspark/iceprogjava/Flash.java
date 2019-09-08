package org.voidspark.iceprogjava;

import org.voidspark.iceprogjava.exceptions.AppException;
import org.voidspark.iceprogjava.exceptions.UserException;
import org.voidspark.iceprogjava.mixins.InputFileOption;
import org.voidspark.iceprogjava.mixins.OffsetOption;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "write", description = { "write file contents to flash, then verify",
        "Default: erase aligned chunks of 64kB in write mode. This means that some data after the written data (or even before when -o is used) may be erased as well." })
public final class Flash extends AbstractCommand {

    @Mixin
    private OffsetOption offsetOption = new OffsetOption();

    @ArgGroup(exclusive = true)
    EraseGroup eraseGroup = new EraseGroup();

    static class EraseGroup {
        // Do not erase before writing
        @Option(names = "-w", description = "do not erase flash before writing")
        boolean dontErase = false;

        // Bulk erase
        @Option(names = "-b", description = "bulk erase entire flash")
        public boolean bulkErase = false;
    }

    // Disable verification
    @Option(names = "-x", description = "do not read and verify against file after writing")
    boolean disableVerify = false;

    @Mixin
    private InputFileOption inputFileOption = new InputFileOption();

    public Flash() {
    }

    @Override
    protected void prepare() throws UserException {
        openInputFile(inputFileOption.fileName);
    }

    @Override
    protected void perform() throws AppException {

        flash.readId();

        if (!eraseGroup.dontErase) {
            if (eraseGroup.bulkErase) {
                bulkErase();
            } else {
                erase(offsetOption.offset, fileSize);
            }
        }

        write(offsetOption.offset);

        if (!disableVerify) {
            verify(offsetOption.offset);
        }

    }
}
