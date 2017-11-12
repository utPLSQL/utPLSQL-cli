package org.utplsql.cli;

import com.beust.jcommander.IStringConverter;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionInfo {

    private String databaseVersion;

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

    public String getOracleDatabaseVersion() throws SQLException
    {
        try ( Connection conn = getConnection() ) {
            return getOracleDatabaseVersion(conn);
        }
    }

    public String getOracleDatabaseVersion( Connection conn ) throws SQLException
    {
        if ( databaseVersion == null ) {
            databaseVersion = getOracleDatabaseVersionFromConnection( conn );
        }

        return databaseVersion;
    }

    /** TODO: Outsource this to Java-API
     *
     * @param conn
     * @return
     * @throws SQLException
     */
    public static String getOracleDatabaseVersionFromConnection( Connection conn ) throws SQLException {
        assert conn != null;
        String result = null;
        try (PreparedStatement stmt = conn.prepareStatement("select version from product_component_version where product like 'Oracle Database%'"))
        {
            ResultSet rs = stmt.executeQuery();

            if ( rs.next() )
                result = rs.getString(1);
        }

        return result;
    }

}
