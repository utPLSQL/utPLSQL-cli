package org.utplsql.cli;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionConfig {

    private final String user;
    private final String password;
    private final String connect;

    public ConnectionConfig( String connectString ) {
        Matcher m = Pattern.compile("^([^/]+)/([^@]+)@(.*)$").matcher(connectString);
        if ( m.find() ) {
            user = m.group(1);
            password = m.group(2);
            connect = m.group(3);
        }
        else
            throw new IllegalArgumentException("Not a valid connectString: '" + connectString + "'");
    }

    public String getConnect() {
        return connect;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getConnectString() {
        return user + "/" + password + "@" + connect;
    }
}
