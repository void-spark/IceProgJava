package org.voidspark.iceprogjava;

import org.voidspark.iceprogjava.exceptions.AppException;
import org.voidspark.iceprogjava.mixins.OffsetOption;
import org.voidspark.iceprogjava.types.SizeTypeConverter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "erase", description = { "(partially) erase flash",
        "Default: erase aligned chunks of 64kB in write mode. This means that some data after the written data (or even before when -o is used) may be erased as well." })
public final class Erase extends AbstractCommand {

    // Erase blocks as if we were writing n bytes
    @Option(names = "-n", required = true, paramLabel = "<size in bytes>", converter = SizeTypeConverter.class, description = "number of bytes (append 'k' to the argument for size in kilobytes, or 'M' for size in megabytes)")
    private int size;

    @Mixin
    private OffsetOption offsetOption = new OffsetOption();

    // Bulk erase
    @Option(names = "-b", description = "bulk erase entire flash")
    public boolean bulkErase = false;

    @Override
    protected void perform() throws AppException {
        flash.readId();

        if (bulkErase) {
            bulkErase();
        } else {
            erase(offsetOption.offset, size);
        }
    }
}
