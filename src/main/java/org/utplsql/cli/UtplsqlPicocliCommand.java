package org.utplsql.cli;

import picocli.CommandLine;

@CommandLine.Command(
        name = "utplsql",
        description = "utPLSQL cli",
        subcommands = {
                RunPicocliCommand.class,
                VersionInfoCommand.class,
                ReportersCommand.class
        })
public class UtplsqlPicocliCommand {

        @CommandLine.Option(names = "-h", usageHelp = true, description = "display this help and exit")
        boolean help;

}
