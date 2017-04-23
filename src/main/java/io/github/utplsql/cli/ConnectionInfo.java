package io.github.utplsql.cli;

import com.beust.jcommander.ParameterException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Vinicius on 21/04/2017.
 */
public class ConnectionInfo {

    /**
     * Regex pattern to match following connection strings:
     * user/pass@127.0.0.1:1521/sid
     * user/pass@127.0.0.1/sid
     * user/pass@sid
     */
    private static final String CONNSTR_PATTERN =
            "^(?<user>[0-9a-z]+)/(?<pass>[0-9a-z]+)" +
                    "(?:(?:@(?<host>[^:/]+)?(?::(?<port>[0-9]+))?(?:/(?<sid1>[0-9a-z]+))$)|(?:@(?<sid2>[0-9a-z]+)$))";

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 1521;

    private String user;
    private String password;
    private String host;
    private int port;
    private String sid;

    public ConnectionInfo() {
    }

    public ConnectionInfo parseConnectionString(String connectionString) throws ParameterException {
        Pattern p = Pattern.compile(CONNSTR_PATTERN);
        Matcher m = p.matcher(connectionString);

        if (!m.matches())
            throw new ParameterException("Invalid connection string!");

        this.setUser(m.group("user"));
        this.setPassword(m.group("pass"));
        this.setHost(m.group("host") != null ? m.group("host") : "127.0.0.1");
        this.setPort(m.group("port") != null ? Integer.parseInt(m.group("port")) : 1521);
        this.setSid(m.group("sid1") != null ? m.group("sid1") : m.group("sid2"));

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

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getConnectionUrl() {
        return String.format("jdbc:oracle:thin:@%s:%d:%s", getHost(), getPort(), getSid());
    }

    @Override
    public String toString() {
        return String.format("%s@%s:%d/%s", getUser(), getHost(), getPort(), getSid());
    }

}
