package io.github.utplsql.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class Cli {

    public static final String HELP_CMD = "-h";
    public static final String RUN_CMD = "run";

    public static void main(String[] args) {
        JCommander jc = new JCommander();
        // jc.addCommand(HELP_CMD, new HelpCommand());
        RunCommand runCmd = new RunCommand();
        jc.addCommand(RUN_CMD, runCmd);

        try {
            jc.parse(args);
            boolean hasCmd = jc.getParsedCommand() != null;

            if (hasCmd && jc.getParsedCommand().equals(RUN_CMD)) {
                runCmd.run();
            } else {
                jc.usage();
            }
        } catch (ParameterException e) {
            if (jc.getParsedCommand() != null) {
                System.err.println(e.getMessage());
                jc.usage(jc.getParsedCommand());
            } else {
                jc.usage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class HelpCommand {

        @Parameter(names = {HELP_CMD, "--help"}, help = true)
        public boolean callHelp;

    }

}
