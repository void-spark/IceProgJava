package org.voidspark.iceprogjava;

import org.voidspark.iceprogjava.mixins.ExitStatus;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "iceprogjava", sortOptions = false, usageHelpAutoWidth = true, description = "Simple programming tool for FTDI-based Lattice iCE programmers.", subcommands = {
        Flash.class, Check.class, Read.class, Erase.class, Test.class, DisableProtection.class, CommandLine.HelpCommand.class })
public class IceProgJava implements Runnable {

    @Mixin
    private ExitStatus exitStatusMixin = new ExitStatus();

    @Option(names = { "--help" }, usageHelp = true, description = "display this help and exit")
    boolean usageHelpRequested;

    public static void main(String... args) {
        int exitCode = new CommandLine(new IceProgJava()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
    }

}
