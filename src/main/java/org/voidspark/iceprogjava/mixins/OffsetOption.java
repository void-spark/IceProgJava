package org.voidspark.iceprogjava.mixins;

import org.voidspark.iceprogjava.types.OffsetTypeConverter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

@Command()
public class OffsetOption {

    // Set address offset 
    @Option(showDefaultValue = Visibility.ALWAYS, defaultValue = "0", names = "-o", paramLabel = "<offset in bytes>", converter = OffsetTypeConverter.class, description = "start address for read/write (append 'k' to the argument for size in kilobytes, or 'M' for size in megabytes)")
    public int offset;
}
