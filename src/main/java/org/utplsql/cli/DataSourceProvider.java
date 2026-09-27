package org.utplsql.cli;

import org.utplsql.cli.datasource.TestedDataSourceProvider;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;

/**
 * Helper class to give you a ready-to-use datasource
 *
 * @author pesse
 */
public class DataSourceProvider {

    private static final String TNS_ADMIN_PROPERTY = "oracle.net.tns_admin";

    static {
        String tnsAdminFallback = getTnsAdminFallback(
                System.getProperty(TNS_ADMIN_PROPERTY), System.getenv("TNS_ADMIN"), System.getenv("ORACLE_HOME"));
        if (tnsAdminFallback != null) {
            System.setProperty(TNS_ADMIN_PROPERTY, tnsAdminFallback);
        }
    }

    /**
     * The JDBC driver resolves tnsnames.ora / ojdbc.properties from the {@value TNS_ADMIN_PROPERTY} property
     * or the TNS_ADMIN environment variable on its own, but it doesn't look into ORACLE_HOME.
     * The property takes precedence over the environment variable, so it must not be set when TNS_ADMIN is.
     *
     * @return ORACLE_HOME/NETWORK/ADMIN when neither {@value TNS_ADMIN_PROPERTY} nor TNS_ADMIN is set, otherwise null
     */
    static String getTnsAdminFallback(String tnsAdminProperty, String tnsAdminEnv, String oracleHome) {
        if (isEmpty(tnsAdminProperty) && isEmpty(tnsAdminEnv) && !isEmpty(oracleHome)) {
            return String.join(File.separator, oracleHome, "NETWORK", "ADMIN");
        }
        return null;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static DataSource getDataSource(String connectString, int maxConnections) throws SQLException {

        requireOjdbc();

        ConnectionConfig config = new ConnectionConfig(connectString);
        warnIfSysDba(config);

        return new TestedDataSourceProvider(config, maxConnections).getDataSource();
    }

    private static void requireOjdbc() {
        if (!OracleLibraryChecker.checkOjdbcExists()) {
            System.out.println("Could not find Oracle JDBC driver in classpath. Please download the jar from Oracle website" +
                    " and copy it to the 'lib' folder of your utPLSQL-cli installation.");
            System.out.println("Download from http://www.oracle.com/technetwork/database/features/jdbc/jdbc-ucp-122-3110062.html");

            throw new RuntimeException("Can't run utPLSQL-cli without Oracle JDBC driver");
        }
    }

    private static void warnIfSysDba(ConnectionConfig config) {
        if (config.isSysDba()) {
            System.out.println("WARNING: You are connecting to the database as SYSDBA or SYSOPER, which is NOT RECOMMENDED and can put your database at risk!");
        }
    }
}
