package org.utplsql.cli;

/** Interface to decouple JCommander commands
 *
 * @author pesse
 */
public interface ICommand {
    int run();

    String getCommand();
}
