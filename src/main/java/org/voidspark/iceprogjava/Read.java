package org.voidspark.iceprogjava;

import org.voidspark.iceprogjava.exceptions.AppException;
import org.voidspark.iceprogjava.exceptions.UserException;
import org.voidspark.iceprogjava.mixins.OffsetOption;
import org.voidspark.iceprogjava.types.SizeTypeConverter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "read", description = "Read bytes from flash and write to file")
public final class Read extends AbstractCommand {

    // Read n bytes to file
    @Option(showDefaultValue = Visibility.ALWAYS, defaultValue = "256k", names = "-n", paramLabel = "<size in bytes>", converter = SizeTypeConverter.class, description = "bytes to read from flash (append 'k' to the argument for size in kilobytes, or 'M' for size in megabytes)")
    private int size;

    @Mixin
    private OffsetOption offsetOption = new OffsetOption();

    @Parameters(index = "0", arity = "1", paramLabel = "<output file>")
    private String fileName;

    public Read() {
    }

    @Override
    protected void prepare() throws UserException {
        openOutputFile(fileName);
    }

    @Override
    protected void perform() throws AppException {
        read(offsetOption.offset, size);
    }
}
