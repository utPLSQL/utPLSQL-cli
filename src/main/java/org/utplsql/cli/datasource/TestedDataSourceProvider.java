package org.utplsql.cli.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.utplsql.cli.ConnectionConfig;
import org.utplsql.cli.exception.DatabaseConnectionFailed;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TestedDataSourceProvider {

    interface ConnectStringPossibility {
        String getConnectString(ConnectionConfig config);
        String getMaskedConnectString(ConnectionConfig config);
    }


    private final ConnectionConfig config;
    private List<ConnectStringPossibility> possibilities = new ArrayList<>();

    public TestedDataSourceProvider(ConnectionConfig config) {
        this.config = config;

        possibilities.add(new ThickConnectStringPossibility());
        possibilities.add(new ThinConnectStringPossibility());
    }

    public HikariDataSource getDataSource() throws SQLException {

        HikariDataSource ds = new HikariDataSource();

        testAndSetJdbcUrl(ds);

        return ds;
    }

    public void testAndSetJdbcUrl( HikariDataSource ds ) throws SQLException
    {
        List<String> errors = new ArrayList<>();
        Throwable lastException = null;
        for (ConnectStringPossibility possibility : possibilities) {
            ds.setJdbcUrl(possibility.getConnectString(config));
            try (Connection con = ds.getConnection()) {
                return;
            } catch (UnsatisfiedLinkError | Exception e) {
                errors.add(possibility.getMaskedConnectString(config) + ": " + e.getMessage());
                lastException = e;
            }
        }

        errors.forEach(System.out::println);
        throw new DatabaseConnectionFailed(lastException);
    }

    private static class ThickConnectStringPossibility implements ConnectStringPossibility {
        @Override
        public String getConnectString(ConnectionConfig config) {
            return "jdbc:oracle:oci8:" + config.getConnectString();
        }

        @Override
        public String getMaskedConnectString(ConnectionConfig config) {
            return "jdbc:oracle:oci8:****/****@" + config.getConnect();
        }
    }

    private static class ThinConnectStringPossibility implements ConnectStringPossibility {
        @Override
        public String getConnectString(ConnectionConfig config) {
            return "jdbc:oracle:thin:" + config.getConnectString();
        }

        @Override
        public String getMaskedConnectString(ConnectionConfig config) {
            return "jdbc:oracle:thin:****/****@" + config.getConnect();
        }
    }
}
