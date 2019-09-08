package org.voidspark.iceprogjava;

import org.voidspark.iceprogjava.exceptions.AppException;
import org.voidspark.iceprogjava.exceptions.UserException;
import org.voidspark.iceprogjava.mixins.InputFileOption;
import org.voidspark.iceprogjava.mixins.OffsetOption;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "check", description = "Read bytes from flash and compare to file.")
public final class Check extends AbstractCommand {
    @Mixin
    private OffsetOption offsetOption = new OffsetOption();

    @Mixin
    private InputFileOption inputFileOption = new InputFileOption();

    public Check() {
    }

    @Override
    protected void prepare() throws UserException {
        openInputFile(inputFileOption.fileName);
    }

    @Override
    protected void perform() throws AppException {
        verify(offsetOption.offset);
    }
}
