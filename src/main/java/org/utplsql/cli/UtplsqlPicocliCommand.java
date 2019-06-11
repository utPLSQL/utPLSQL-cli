package org.utplsql.cli;

import picocli.CommandLine;

@CommandLine.Command(
        name = "utplsql",
        description = "utPLSQL cli",
        subcommands = { RunPicocliCommand.class })
public class UtplsqlPicocliCommand {

}
