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
    private static final String RUN_CMD = "run";

    public static void main(String[] args) {

        LocaleInitializer.initLocale();

        JCommander jc = new JCommander();
        jc.setProgramName("utplsql");
        // jc.addCommand(HELP_CMD, new HelpCommand());
        RunCommand runCmd = new RunCommand();
        jc.addCommand(RUN_CMD, runCmd);

        int exitCode = DEFAULT_ERROR_CODE;

        try {
            jc.parse(args);

            if (RUN_CMD.equals(jc.getParsedCommand())) {
                exitCode = runCmd.run();
            } else {
                throw new ParameterException("Command not specified.");
            }
        } catch (ParameterException e) {
            if (jc.getParsedCommand() != null) {
                System.err.println(e.getMessage());
                jc.usage(jc.getParsedCommand());
            } else {
                jc.usage();
            }
        } catch ( DatabaseNotCompatibleException | UtPLSQLNotInstalledException | DatabaseConnectionFailed e ) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(exitCode);
    }

    private static class HelpCommand {

        @Parameter(names = {HELP_CMD, "--help"}, help = true)
        public boolean callHelp;

    }

}
