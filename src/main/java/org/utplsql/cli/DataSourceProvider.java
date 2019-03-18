package org.utplsql.cli;

import com.zaxxer.hikari.HikariDataSource;
import org.utplsql.api.EnvironmentVariableUtil;
import org.utplsql.cli.datasource.TestedDataSourceProvider;

import javax.sql.DataSource;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

/** Helper class to give you a ready-to-use datasource
 *
 * @author pesse
 */
public class DataSourceProvider {

    private static String oracleHome = null;
    private static String tnsAdmin = null;

    static {
        String tnsAdmin = loadOracleHomeAndTnsAdmin();
        if (tnsAdmin != null && System.getProperty("oracle.net.tns_admin") == null) {
            System.setProperty("oracle.net.tns_admin", tnsAdmin);
        }
    }

    private static String loadOracleHomeAndTnsAdmin() {
        tnsAdmin = EnvironmentVariableUtil.getEnvValue("TNS_ADMIN");
        if (directoryExists(tnsAdmin)) {
            return tnsAdmin;
        }

        oracleHome = EnvironmentVariableUtil.getEnvValue("ORACLE_HOME");
        if (oracleHome != null) {
            tnsAdmin = String.join(File.separator, oracleHome, "NETWORK", "ADMIN");

            if (directoryExists(tnsAdmin)) {
                return tnsAdmin;
            }
        }

        try {
            final int root = WinRegistry.HKEY_LOCAL_MACHINE;
            final String key = "SOFTWARE\\Oracle";

            List<String> oracleKeys = WinRegistry.readStringSubKeys(root, key);
            for (String oracleKey : oracleKeys) {
                String temp = key + "\\" + oracleKey;

                // check for HKEY_LOCAL_MACHINE\SOFTWARE\Oracle\<oracle client>\TNS_ADMIN
                tnsAdmin = WinRegistry.readString(root, temp, "TNS_ADMIN");
                if (directoryExists(tnsAdmin)) {
                    return tnsAdmin;
                }

                // check for HKEY_LOCAL_MACHINE\SOFTWARE\Oracle\<oracle client>\ORACLE_HOME
                oracleHome = WinRegistry.readString(root, temp, "ORACLE_HOME");
                tnsAdmin = String.join(File.separator, oracleHome, "NETWORK", "ADMIN");
                if (directoryExists(tnsAdmin)) {
                    return tnsAdmin;
                }
            }
        } catch (ExceptionInInitializerError | IllegalAccessException | InvocationTargetException e) {
        }

        return null;
    }

    private static boolean directoryExists(String path) {
        return path != null && new File(path).exists();
    }

    public static DataSource getDataSource(ConnectionInfo info, int maxConnections ) throws SQLException {

        requireOjdbc();

        ConnectionConfig config = new ConnectionConfig(info.getConnectionString());

        HikariDataSource pds = new TestedDataSourceProvider(config).getDataSource();
        pds.setAutoCommit(false);
        pds.setMaximumPoolSize(maxConnections);
        return pds;
    }

    public static String getOracleHome() {
        return oracleHome;
    }

    public static String getTnsAdmin() {
        return tnsAdmin;
    }

    private static void requireOjdbc() {
        if ( !OracleLibraryChecker.checkOjdbcExists() )
        {
            System.out.println("Could not find Oracle JDBC driver in classpath. Please download the jar from Oracle website" +
                    " and copy it to the 'lib' folder of your utPLSQL-cli installation.");
            System.out.println("Download from http://www.oracle.com/technetwork/database/features/jdbc/jdbc-ucp-122-3110062.html");

            throw new RuntimeException("Can't run utPLSQL-cli without Oracle JDBC driver");
        }
    }
}
