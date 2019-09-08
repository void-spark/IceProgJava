package org.voidspark.iceprogjava;

import org.voidspark.iceprogjava.exceptions.AppException;

import picocli.CommandLine.Command;

@Command(name = "dwp", description = "Disable write protection. This can be useful if flash memory appears to be bricked and won't respond to erasing or programming.")
public final class DisableProtection extends AbstractCommand {

    @Override
    protected void perform() throws AppException {
        disableProtection();
    }
}
