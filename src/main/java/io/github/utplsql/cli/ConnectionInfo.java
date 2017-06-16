package io.github.utplsql.cli;

import com.beust.jcommander.ParameterException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Vinicius on 21/04/2017.
 */
public class ConnectionInfo {

    /**
     * Regex pattern to match following connection strings:
     * user/pass@127.0.0.1:1521/db
     * user/pass@127.0.0.1/db
     * user/pass@db
     */
    private static final String CONNSTR_PATTERN =
            "^(?<user>[0-9a-z]+)/(?<pass>[0-9a-z]+)" +
                    "(?:(?:@(?<host>[^:/]+)?(?::(?<port>[0-9]+))?(?:/(?<db1>[0-9a-z_.]+))$)|(?:@(?<db2>[0-9a-z_.]+)$))";

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 1521;

    private String user;
    private String password;
    private String host;
    private int port;
    private String db;

    public ConnectionInfo() {
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getConnectionUrl(), getUser(), getPassword());
    }

    public ConnectionInfo parseConnectionString(String connectionString) throws ParameterException {
        Pattern p = Pattern.compile(CONNSTR_PATTERN);
        Matcher m = p.matcher(connectionString);

        if (!m.matches())
            throw new ParameterException("Invalid connection string!");

        this.setUser(m.group("user"));
        this.setPassword(m.group("pass"));
        this.setHost(m.group("host") != null ? m.group("host") : DEFAULT_HOST);
        this.setPort(m.group("port") != null ? Integer.parseInt(m.group("port")) : DEFAULT_PORT);
        this.setDb(m.group("db1") != null ? m.group("db1") : m.group("db2"));

        return this;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getConnectionUrl() {
        return String.format("jdbc:oracle:thin:@//%s:%d/%s", getHost(), getPort(), getDb());
    }

    @Override
    public String toString() {
        return String.format("%s@%s:%d/%s", getUser(), getHost(), getPort(), getDb());
    }

}
