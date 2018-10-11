package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.cli.datasource.TestedDataSourceProvider;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DataSourceProviderIT {

    @Test
    public void connectToDatabase() throws IOException, SQLException {

        ConnectionConfig config = new ConnectionConfig(TestHelper.getConnectionString());

        DataSource dataSource = new TestedDataSourceProvider(config).getDataSource();

        assertNotNull(dataSource);
    }

    @Test
    public void initNlsLang() throws SQLException {
        ConnectionConfig config = new ConnectionConfig(TestHelper.getConnectionString());
        System.setProperty("NLS_LANG", "BRAZILIAN PORTUGUESE_BRAZIL.WE8ISO8859P1");


        DataSource dataSource = new TestedDataSourceProvider(config).getDataSource();

        assertNotNull(dataSource);

        try ( Connection con = dataSource.getConnection() ) {
            try (PreparedStatement stmt = con.prepareStatement("select value from nls_session_parameters where parameter = 'NLS_LANGUAGE'")) {
                ResultSet rs = stmt.executeQuery();
                if ( rs.next() ) {
                    assertEquals("BRAZILIAN PORTUGUESE", rs.getString(1));
                }
            }
        }
    }

    @Test
    public void initPartialNlsLang() throws SQLException {
        ConnectionConfig config = new ConnectionConfig(TestHelper.getConnectionString());
        System.setProperty("NLS_LANG", "_SOMALIA");


        DataSource dataSource = new TestedDataSourceProvider(config).getDataSource();

        assertNotNull(dataSource);

        try ( Connection con = dataSource.getConnection() ) {
            try (PreparedStatement stmt = con.prepareStatement("select value from nls_session_parameters where parameter = 'NLS_TERRITORY'")) {
                ResultSet rs = stmt.executeQuery();
                if ( rs.next() ) {
                    assertEquals("SOMALIA", rs.getString(1));
                }
            }
        }
    }
}
