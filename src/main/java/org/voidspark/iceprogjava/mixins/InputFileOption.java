package org.voidspark.iceprogjava.mixins;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command()
public class InputFileOption {

    @Parameters(index = "0", arity = "1", paramLabel = "<input file>", description = "input file to read from")
    public String fileName;
}
