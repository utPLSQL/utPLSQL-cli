package org.utplsql.cli;

import com.beust.jcommander.IStringConverter;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionInfo {

    static {
        String oracleHome = System.getenv("ORACLE_HOME");
        if (oracleHome != null) {
            System.setProperty("oracle.net.tns_admin",
                    String.join(File.separator, oracleHome, "NETWORK", "ADMIN"));
        }
    }

    private PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();

    public ConnectionInfo(String connectionInfo) {
        try {
            this.pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
            this.pds.setURL("jdbc:oracle:thin:" + connectionInfo);
            this.pds.setInitialPoolSize(2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return pds.getConnection();
    }

    public static class ConnectionStringConverter implements IStringConverter<ConnectionInfo> {

        @Override
        public ConnectionInfo convert(String s) {
            return new ConnectionInfo(s);
        }
    }

}
