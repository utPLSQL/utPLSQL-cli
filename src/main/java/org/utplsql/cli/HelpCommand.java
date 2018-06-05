package org.utplsql.cli;

public class HelpCommand implements ICommand {

    private String errorMessage;

    public HelpCommand( String errorMessage ) {
        this.errorMessage = errorMessage;
    }

    @Override
    public int run() {
        if ( errorMessage != null )
            System.out.println(errorMessage);

        return 1;
    }

    @Override
    public String getCommand() {
        return "-h";
    }
}
