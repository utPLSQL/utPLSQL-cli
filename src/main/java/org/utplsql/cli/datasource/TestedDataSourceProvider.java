package org.utplsql.cli.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utplsql.api.EnvironmentVariableUtil;
import org.utplsql.cli.ConnectionConfig;
import org.utplsql.cli.exception.DatabaseConnectionFailed;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestedDataSourceProvider {

    interface ConnectStringPossibility {
        String getConnectString(ConnectionConfig config);
        String getMaskedConnectString(ConnectionConfig config);
    }

    private static final Logger logger = LoggerFactory.getLogger(TestedDataSourceProvider.class);
    private final ConnectionConfig config;
    private final List<ConnectStringPossibility> possibilities = new ArrayList<>();

    public TestedDataSourceProvider(ConnectionConfig config) {
        this.config = config;

        possibilities.add(new ThickConnectStringPossibility());
        possibilities.add(new ThinConnectStringPossibility());
    }

    public HikariDataSource getDataSource() throws SQLException {

        HikariDataSource ds = new HikariDataSource();

        setInitSqlFrom_NLS_LANG(ds);
        setThickOrThinJdbcUrl(ds);

        return ds;
    }

    private void setThickOrThinJdbcUrl(HikariDataSource ds ) throws SQLException
    {
        List<String> errors = new ArrayList<>();
        Throwable lastException = null;

        ds.setUsername(config.getUser());
        ds.setPassword(config.getPassword());

        for (ConnectStringPossibility possibility : possibilities) {
            ds.setJdbcUrl(possibility.getConnectString(config));
            try (Connection ignored = ds.getConnection()) {
                logger.info("Use connection string {}", possibility.getMaskedConnectString(config));
                return;
            } catch (UnsatisfiedLinkError | Exception e) {
                errors.add(possibility.getMaskedConnectString(config) + ": " + e.getMessage());
                lastException = e;
            }
        }

        errors.forEach(System.out::println);
        throw new DatabaseConnectionFailed(lastException);
    }

    private void setInitSqlFrom_NLS_LANG(HikariDataSource ds ) {
        String nls_lang = EnvironmentVariableUtil.getEnvValue("NLS_LANG");

        if ( nls_lang != null ) {
            Pattern pattern = Pattern.compile("^([a-zA-Z ]+)?_?([a-zA-Z ]+)?\\.?([a-zA-Z0-9]+)?$");
            Matcher matcher = pattern.matcher(nls_lang);

            List<String> sqlCommands = new ArrayList<>(2);
            if (matcher.matches()) {
                if ( matcher.group(1) != null)
                    sqlCommands.add(String.format("ALTER SESSION SET NLS_LANGUAGE='%s'", matcher.group(1)));
                if ( matcher.group(2) != null)
                    sqlCommands.add(String.format("ALTER SESSION SET NLS_TERRITORY='%s'", matcher.group(2)));

                if ( sqlCommands.size() > 0 ) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("BEGIN\n");
                    for (String command : sqlCommands)
                        sb.append(String.format("EXECUTE IMMEDIATE q'[%s]';\n", command));
                    sb.append("END;");

                    logger.debug("NLS settings: {}", sb.toString());
                    ds.setConnectionInitSql(sb.toString());
                }
            }
        }
    }

    private static class ThickConnectStringPossibility implements ConnectStringPossibility {
        @Override
        public String getConnectString(ConnectionConfig config) {
            return "jdbc:oracle:oci8:@" + config.getConnect();
        }

        @Override
        public String getMaskedConnectString(ConnectionConfig config) {
            return "jdbc:oracle:oci8:****/****@" + config.getConnect();
        }
    }

    private static class ThinConnectStringPossibility implements ConnectStringPossibility {
        @Override
        public String getConnectString(ConnectionConfig config) {
            return "jdbc:oracle:thin:@" + config.getConnect();
        }

        @Override
        public String getMaskedConnectString(ConnectionConfig config) {
            return "jdbc:oracle:thin:****/****@" + config.getConnect();
        }
    }
}
