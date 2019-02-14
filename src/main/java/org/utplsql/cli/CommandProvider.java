package org.utplsql.cli;

import com.beust.jcommander.JCommander;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CommandProvider {

    private final Map<String, ICommand> commands = new HashMap<>();
    private final JCommander jCommander;

    public CommandProvider( JCommander jCommander ) {
        this.jCommander = jCommander;
        init();
    }

    private void init() {
        addCommand(new RunCommand());
        addCommand(new VersionInfoCommand());
        addCommand(new ReportersCommand());
        addCommand(new HelpCommand(jCommander));
    }

    private void addCommand( ICommand command ) {
        commands.put(command.getCommand().toLowerCase(), command);
    }

    public ICommand getCommand( String key ) {
        if ( commands.containsKey(key))
            return commands.get(key.toLowerCase());
        else
            return new HelpCommand(jCommander, "Unknown command: '" + key + "'");
    }

    public Stream<ICommand> commands() {
        return commands.values().stream();
    }
}
