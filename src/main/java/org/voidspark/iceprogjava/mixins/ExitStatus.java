package org.voidspark.iceprogjava.mixins;

import picocli.CommandLine.Command;

@Command(exitCodeListHeading = "%nExit status:%n", exitCodeList = { "0:Success.",
        "1:Non-hardware error occurred (e.g., failure to read from or write to a file, or invoked with invalid options).",
        "2:Communication with the hardware failed (e.g., cannot find the iCE FTDI USB device).", "3:Verification of the data failed." })
public class ExitStatus {
}
