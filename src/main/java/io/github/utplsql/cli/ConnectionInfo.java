package io.github.utplsql.cli;

import com.beust.jcommander.ParameterException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Vinicius on 21/04/2017.
 */
public class ConnectionInfo {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 1521;

    private String user;
    private String password;
    private String host;
    private int port;
    private String database;

    public ConnectionInfo() {}

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getConnectionUrl(), getUser(), getPassword());
    }

    public ConnectionInfo parseConnectionString(String connectionString)
            throws ParameterException, IllegalArgumentException {

        if (connectionString == null || connectionString.isEmpty())
            throw invalidConnectionString();

        int i = connectionString.lastIndexOf("@");
        if (i == -1)
            throw invalidConnectionString();

        String credentials = connectionString.substring(0, i);
        String host = connectionString.substring(i+1);
        parseCredentials(credentials);
        parseHost(host);

        return this;
    }

    private void parseCredentials(String str) throws ParameterException, IllegalArgumentException {
        int barIdx = str.indexOf("/");

        if (barIdx == -1 || str.length() == 1)
            throw invalidConnectionString();

        this.setUser(str.substring(0, barIdx));
        this.setPassword(str.substring(barIdx+1));
    }

    private void parseHost(String str) throws ParameterException, IllegalArgumentException {
        if (str == null || str.isEmpty())
            throw invalidConnectionString();

        int colonIdx = str.indexOf(":");
        int barIdx = str.indexOf("/");

        if (colonIdx != -1 && barIdx != -1) {
            setHost(str.substring(0, colonIdx));
            setPort(Integer.parseInt(str.substring(colonIdx + 1, barIdx)));
            setDatabase(str.substring(barIdx + 1));
        }
        else
        if (colonIdx == -1 && barIdx != -1) {
            setHost(str.substring(0, barIdx));
            setPort(DEFAULT_PORT);
            setDatabase(str.substring(barIdx + 1));
        }
        else
        if (colonIdx != -1) {
            throw invalidConnectionString();
        }
        else {
            setHost(DEFAULT_HOST);
            setPort(DEFAULT_PORT);
            setDatabase(str);
        }
    }

    private ParameterException invalidConnectionString() {
        return new ParameterException("Invalid connection string.");
    }

    public String getUser() {
        return user;
    }

    private void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    private void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    private void setPort(int port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    private void setDatabase(String database) {
        this.database = database;
    }

    public String getConnectionUrl() {
        return String.format("jdbc:oracle:thin:@//%s:%d/%s", getHost(), getPort(), getDatabase());
    }

    @Override
    public String toString() {
        return String.format("%s@%s:%d/%s", getUser(), getHost(), getPort(), getDatabase());
    }

}
