package org.utplsql.cli.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utplsql.api.EnvironmentVariableUtil;
import org.utplsql.cli.ConnectionConfig;
import org.utplsql.cli.exception.DatabaseConnectionFailed;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestedDataSourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(TestedDataSourceProvider.class);
    /**
     * JDBC URL prefixes tried in this order: thick (OCI) driver first, then thin driver
     */
    private static final List<String> JDBC_URL_PREFIXES = List.of("jdbc:oracle:oci8:", "jdbc:oracle:thin:");

    private final ConnectionConfig config;
    private final int maxConnections;

    public TestedDataSourceProvider(ConnectionConfig config, int maxConnections) {
        this.config = config;
        this.maxConnections = maxConnections;
    }

    public DataSource getDataSource() throws SQLException {

        InitializableOracleDataSource ds = new InitializableOracleDataSource();

        setInitSqlFrom_NLS_LANG(ds);
        setThickOrThinJdbcUrl(ds);

        return ds;
    }

    private void setThickOrThinJdbcUrl(InitializableOracleDataSource ds) throws SQLException {
        List<String> errors = new ArrayList<>();
        Throwable lastException = null;

        // With external authentication (Oracle Wallet) the driver looks up the credentials itself
        if (!config.isExternalAuthentication()) {
            ds.setUser(config.getUser());
            ds.setPassword(config.getPassword());
        }

        for (String jdbcUrlPrefix : JDBC_URL_PREFIXES) {
            String maskedUrl = jdbcUrlPrefix + config.getMaskedConnectString();
            logger.debug("Try connecting {}", maskedUrl);
            ds.setURL(jdbcUrlPrefix + "@" + config.getConnect());
            try (Connection ignored = ds.getConnection()) {
                logger.info("Use connection string {}", maskedUrl);
                return;
            } catch (Error | Exception e) {
                errors.add(maskedUrl + ": " + e.getMessage());
                lastException = e;
            }
        }

        errors.forEach(System.out::println);
        throw new DatabaseConnectionFailed(lastException);
    }

    private void setInitSqlFrom_NLS_LANG(InitializableOracleDataSource ds) {
        String nls_lang = EnvironmentVariableUtil.getEnvValue("NLS_LANG");

        if (nls_lang != null) {
            Pattern pattern = Pattern.compile("^([a-zA-Z ]+)?_?([a-zA-Z ]+)?\\.?([a-zA-Z0-9]+)?$");
            Matcher matcher = pattern.matcher(nls_lang);

            List<String> sqlCommands = new ArrayList<>(2);
            if (matcher.matches()) {
                if (matcher.group(1) != null) {
                    sqlCommands.add(String.format("ALTER SESSION SET NLS_LANGUAGE='%s'", matcher.group(1)));
                }
                if (matcher.group(2) != null) {
                    sqlCommands.add(String.format("ALTER SESSION SET NLS_TERRITORY='%s'", matcher.group(2)));
                }

                if (!sqlCommands.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("BEGIN\n");
                    for (String command : sqlCommands) {
                        sb.append(String.format("EXECUTE IMMEDIATE q'[%s]';\n", command));
                    }
                    sb.append("END;");

                    logger.debug("NLS settings: {}", sb);
                    ds.setConnectionInitSql(sb.toString());
                }
            }
        }
    }
}
