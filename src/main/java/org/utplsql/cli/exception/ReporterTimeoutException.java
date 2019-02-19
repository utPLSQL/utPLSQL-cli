package org.utplsql.cli.exception;

public class ReporterTimeoutException extends Exception {

    private final int timeOutInMinutes;

    public ReporterTimeoutException( int timeoutInMinutes ) {
        super("Timeout while waiting for reporters to finish for " + timeoutInMinutes + " minutes");
        this.timeOutInMinutes = timeoutInMinutes;
    }

    public int getTimeOutInMinutes() {
        return timeOutInMinutes;
    }
}
