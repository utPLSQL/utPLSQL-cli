package org.utplsql.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import picocli.CommandLine;

import java.util.List;

public class Cli {

    static final int DEFAULT_ERROR_CODE = 1;

    static final String HELP_CMD = "-h";

    public static void main(String[] args) {

        int exitCode = runWithExitCode(args);

        System.exit(exitCode);
    }

    static int runWithExitCode( String[] args ) {

        LoggerConfiguration.configure(LoggerConfiguration.ConfigLevel.NONE);
        LocaleInitializer.initLocale();

        JCommander jc = new JCommander();
        jc.setProgramName("utplsql");

        CommandProvider cmdProvider = new CommandProvider(jc);

        cmdProvider.commands().forEach(cmd -> jc.addCommand(cmd.getCommand(), cmd));

        int exitCode = DEFAULT_ERROR_CODE;

        if ( args.length >= 1 && args[0].equals("-h") ) // Help?
        {
            exitCode = 0;
            jc.usage();
        }
        else {
            try {
                jc.parse(args);

                exitCode = cmdProvider.getCommand(jc.getParsedCommand()).run();

            } catch (ParameterException e) {
                exitCode = new HelpCommand(jc, e.getMessage()).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return exitCode;
    }
    static int runPicocliWithExitCode( String[] args ) {

        LoggerConfiguration.configure(LoggerConfiguration.ConfigLevel.NONE);
        LocaleInitializer.initLocale();

        CommandLine commandLine = new CommandLine(UtplsqlPicocliCommand.class);
        commandLine.setTrimQuotes(true);

        int exitCode = DEFAULT_ERROR_CODE;

        try {

            List<CommandLine> parsedLines = commandLine.parse(args);

            for ( CommandLine parsedLine : parsedLines ) {
                Object command = parsedLine.getCommand();
                if ( command instanceof ICommand ) {
                    exitCode = ((ICommand)command).run();
                    break;
                }
            }
        } catch (ParameterException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return exitCode;
    }

}
