package org.utplsql.cli;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionConfig {

    private final String user;
    private final String password;
    private final String connect;

    public ConnectionConfig( String connectString ) {
        Matcher m = Pattern.compile("^(\".+\"|[^/]+)/(\".+\"|[^@]+)@(.*)$").matcher(connectString);
        if ( m.find() ) {
            user = stripEnclosingQuotes(m.group(1));
            password = stripEnclosingQuotes(m.group(2));
            connect = m.group(3);
        }
        else
            throw new IllegalArgumentException("Not a valid connectString: '" + connectString + "'");
    }

    private String stripEnclosingQuotes( String value ) {
        if ( value.length() > 1
                && value.startsWith("\"")
                && value.endsWith("\"")) {
            return value.substring(1, value.length()-1);
        } else {
            return value;
        }
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
