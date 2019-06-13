package org.utplsql.cli;

/** This is the very basic interface that should be implemented by all utPLSQL cli commands
 *
 * @author pesse
 */
public interface ICommand {

    /** We expect the command to handle all eventually occuring exceptions
     * and return an exit code
     *
     * @return exit code integer
     */
    int run();
}
