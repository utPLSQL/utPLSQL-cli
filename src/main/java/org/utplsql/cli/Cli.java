package org.utplsql.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.utplsql.api.exception.DatabaseNotCompatibleException;
import org.utplsql.api.exception.UtPLSQLNotInstalledException;
import org.utplsql.cli.exception.DatabaseConnectionFailed;

public class Cli {

    static final int DEFAULT_ERROR_CODE = 1;

    static final String HELP_CMD = "-h";

    public static void main(String[] args) {

        int exitCode = runWithExitCode(args);

        System.exit(exitCode);
    }

    static int runWithExitCode( String[] args ) {
        LocaleInitializer.initLocale();

        JCommander jc = new JCommander();
        jc.setProgramName("utplsql");

        CommandProvider cmdProvider = new CommandProvider();

        cmdProvider.commands().forEach(cmd -> jc.addCommand(cmd.getCommand(), cmd));

        int exitCode = DEFAULT_ERROR_CODE;

        try {
            jc.parse(args);

            exitCode = cmdProvider.getCommand(jc.getParsedCommand()).run();

        } catch (ParameterException e) {
            if (jc.getParsedCommand() != null) {
                System.err.println(e.getMessage());
                jc.usage(jc.getParsedCommand());
            } else {
                jc.usage();
            }
        }  catch (Exception e) {
            e.printStackTrace();
        }

        return exitCode;
    }

    private static class HelpCommand {

        @Parameter(names = {HELP_CMD, "--help"}, help = true)
        public boolean callHelp;

    }

}
