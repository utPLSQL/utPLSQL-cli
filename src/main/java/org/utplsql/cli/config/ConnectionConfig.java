package org.utplsql.cli.config;

import java.beans.ConstructorProperties;

public class ConnectionConfig {

    private final String connectString;

    @ConstructorProperties({"connectString"})
    public ConnectionConfig(String connectString) {
        this.connectString = connectString;
    }

    public String getConnectString() {
        return connectString;
    }

}
