package org.utplsql.cli;

import picocli.CommandLine;

@CommandLine.Command(
        name = "utplsql",
        description = "utPLSQL cli",
        subcommands = {
                RunPicocliCommand.class,
                VersionInfoCommand.class,
                ReportersCommand.class,
                CommandLine.HelpCommand.class
        })
public class UtplsqlPicocliCommand {

        public static final String COMMANDLINE_PARAM_DESCRIPTION = "<user>/<password>@//<host>[:<port>]/<service> OR <user>/<password>@<TNSName> OR <user>/<password>@<host>:<port>:<SID>";

        @CommandLine.Option(names = "-h", usageHelp = true, description = "display this help and exit")
        boolean help;

}
