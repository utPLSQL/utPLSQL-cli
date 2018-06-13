package org.utplsql.cli;

import com.zaxxer.hikari.HikariDataSource;
import org.utplsql.api.EnvironmentVariableUtil;

import javax.sql.DataSource;
import java.io.File;

/** Helper class to give you a ready-to-use datasource
 *
 * @author pesse
 */
public class DataSourceProvider {

    static {
        String oracleHome = EnvironmentVariableUtil.getEnvValue("ORACLE_HOME");
        if (oracleHome != null) {
            System.setProperty("oracle.net.tns_admin",
                    String.join(File.separator, oracleHome, "NETWORK", "ADMIN"));
        }
    }

    public static DataSource getDataSource(ConnectionInfo info, int maxConnections ) {

        requireOjdbc();

        HikariDataSource pds = new HikariDataSource();
        pds.setJdbcUrl("jdbc:oracle:thin:" + info.getConnectionString());
        pds.setAutoCommit(false);
        pds.setMaximumPoolSize(maxConnections);
        return pds;
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
