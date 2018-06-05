package org.utplsql.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CommandProvider {

    private Map<String, ICommand> commands;

    public CommandProvider() {
        init();
    }

    private void init() {
        commands = new HashMap<>();

        addCommand(new RunCommand());
        addCommand(new VersionInfoCommand());
    }

    private void addCommand( ICommand command ) {
        commands.put(command.getCommand().toLowerCase(), command);
    }

    public ICommand getCommand( String key ) {
        if ( commands.containsKey(key))
            return commands.get(key.toLowerCase());
        else
            return new HelpCommand("Unknown command: '" + key + "'");
    }

    public Stream<ICommand> commands() {
        return commands.values().stream();
    }
}
