package org.voidspark.iceprogjava;

import org.voidspark.iceprogjava.mixins.ExitStatus;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(name = "iceprogjava", sortOptions = false, usageHelpAutoWidth = true, description = "Simple programming tool for FTDI-based Lattice iCE programmers.", synopsisSubcommandLabel = "COMMAND", subcommands = {
        Flash.class, Check.class, Read.class, Erase.class, Test.class, DisableProtection.class, CommandLine.HelpCommand.class })
public class IceProgJava implements Runnable {

    @Mixin
    private ExitStatus exitStatusMixin = new ExitStatus();

    @Spec
    CommandSpec spec;

    public static void main(String... args) {
        int exitCode = new CommandLine(new IceProgJava()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        throw new ParameterException(spec.commandLine(), "Missing required subcommand");
    }
}
