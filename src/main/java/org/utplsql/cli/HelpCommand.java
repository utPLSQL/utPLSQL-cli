package org.utplsql.cli;

import com.beust.jcommander.JCommander;

/** Simple Help-Command which outputs an (optional) error message and the command line usage
 * @author pesse
 */
public class HelpCommand implements ICommand {

    private final String errorMessage;
    private final JCommander jCommander;

    public HelpCommand(JCommander jCommander) {
        this.jCommander = jCommander;
        this.errorMessage = null;
    }

    public HelpCommand( JCommander jCommander, String errorMessage ) {
        this.jCommander = jCommander;
        this.errorMessage = errorMessage;
    }

    @Override
    public int run() {
        if ( errorMessage != null ) {
            System.out.println(errorMessage);
            if (jCommander.getParsedCommand() != null)
                jCommander.usage(jCommander.getParsedCommand());
            else
                jCommander.usage();
            return 1;
        }
        else {
            jCommander.usage();
            return 0;
        }
    }

    @Override
    public String getCommand() {
        return "-h";
    }
}
