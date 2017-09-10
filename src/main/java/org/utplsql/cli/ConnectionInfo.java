package org.utplsql.cli;

import com.beust.jcommander.IStringConverter;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionInfo {

    static {
        String oracleHome = System.getenv("ORACLE_HOME");
        if (oracleHome != null) {
            System.setProperty("oracle.net.tns_admin",
                    String.join(File.separator, oracleHome, "NETWORK", "ADMIN"));
        }
    }

    private String connectionInfo;

    public ConnectionInfo(String connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:oracle:thin:" + this.connectionInfo);
    }

    public static class ConnectionStringConverter implements IStringConverter<ConnectionInfo> {

        @Override
        public ConnectionInfo convert(String s) {
            return new ConnectionInfo(s);
        }
    }

}
