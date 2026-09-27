package org.utplsql.cli;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionConfig {

    /**
     * Either {@code <user>/<password>@<connect>} or {@code /@<connect>}.
     * An unquoted password extends to the last {@code @}, so it may contain {@code @} itself.
     */
    private static final Pattern CONNECT_STRING_PATTERN =
            Pattern.compile("^(?:(\".+\"|[^/]+)/(\".+\"|.+)|/)@(.*)$");

    private final String user;
    private final String password;
    private final String connect;

    public ConnectionConfig(String connectString) {
        Matcher m = CONNECT_STRING_PATTERN.matcher(connectString);
        if (m.find()) {
            user = m.group(1) == null ? null : stripEnclosingQuotes(m.group(1));
            password = m.group(2) == null ? null : stripEnclosingQuotes(m.group(2));
            connect = m.group(3);
        } else {
            throw new IllegalArgumentException("Not a valid connectString: '" + connectString + "'");
        }
    }

    /**
     * Masks the credentials of a connect string, e.g. for logging.
     *
     * @param value any string, e.g. a command line argument
     * @return the value as returned by {@link #getMaskedConnectString()} for a connect string, otherwise the unchanged value
     */
    public static String maskCredentials(String value) {
        if (value == null || !CONNECT_STRING_PATTERN.matcher(value).matches()) {
            return value;
        }
        return new ConnectionConfig(value).getMaskedConnectString();
    }

    private String stripEnclosingQuotes(String value) {
        if (value.length() > 1
                && value.startsWith("\"")
                && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
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

    /**
     * @return true when no user/password was given (connect string {@code /@<connect>}),
     * meaning credentials are provided externally, e.g. by an Oracle Wallet
     */
    public boolean isExternalAuthentication() {
        return user == null;
    }

    public String getConnectString() {
        if (isExternalAuthentication()) {
            return "/@" + connect;
        }
        return user + "/" + password + "@" + connect;
    }

    /**
     * @return the connect string with user and password replaced by asterisks,
     * or {@code /@<connect>} for external authentication
     */
    public String getMaskedConnectString() {
        if (isExternalAuthentication()) {
            return "/@" + connect;
        }
        return "****/****@" + connect;
    }

    public boolean isSysDba() {
        return user != null &&
                (user.toLowerCase().endsWith(" as sysdba")
                        || user.toLowerCase().endsWith(" as sysoper"));
    }
}
