package org.voidspark.iceprogjava;

import org.voidspark.flash.FlashException;

import picocli.CommandLine.Command;

@Command(name = "test", description = "Just read the flash ID sequence")
public final class Test extends AbstractCommand {

    @Override
    protected void perform() throws FlashException {
        flash.readId();
    }
}
