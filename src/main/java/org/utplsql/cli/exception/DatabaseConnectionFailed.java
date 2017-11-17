package org.utplsql.cli.exception;

import java.sql.SQLException;

public class DatabaseConnectionFailed extends SQLException {

    public DatabaseConnectionFailed(SQLException cause ) {
        super( "Could not establish connection to database. Reason: " + cause.getMessage(), cause);
    }
}
