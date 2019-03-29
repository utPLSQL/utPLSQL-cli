package org.utplsql.cli;

import com.beust.jcommander.IStringConverter;

public class ConnectionInfo {

    public static final String COMMANDLINE_PARAM_DESCRIPTION = "<user>/<password>@//<host>[:<port>]/<service> OR <user>/<password>@<TNSName> OR <user>/<password>@<host>:<port>:<SID>";
    private final String connectionInfo;

    public ConnectionInfo(String connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public String getConnectionString() {
        return connectionInfo;
    }

    public static class ConnectionStringConverter implements IStringConverter<ConnectionInfo> {

        @Override
        public ConnectionInfo convert(String s) {
            return new ConnectionInfo(s);
        }
    }
}
