package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.cli.datasource.TestedDataSourceProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DataSourceProviderIT {

    @Test
    void connectToDatabase() throws SQLException {
        DataSource dataSource = getDataSource();

        assertNotNull(dataSource);
    }

    @Test
    void initNlsLang() throws SQLException {
        System.setProperty("NLS_LANG", "BRAZILIAN PORTUGUESE_BRAZIL.WE8ISO8859P1");
        DataSource dataSource = getDataSource();

        assertNotNull(dataSource);
        checkNlsSessionParameter(dataSource, "NLS_LANGUAGE", "BRAZILIAN PORTUGUESE");
        checkNlsSessionParameter(dataSource, "NLS_TERRITORY", "BRAZIL");
    }

    @Test
    void initPartialNlsLangTerritory() throws SQLException {
        System.setProperty("NLS_LANG", "_SOMALIA");
        DataSource dataSource = getDataSource();

        assertNotNull(dataSource);
        checkNlsSessionParameter(dataSource, "NLS_TERRITORY", "SOMALIA");
    }

    @Test
    void initPartialNlsLangLanguage() throws SQLException {
        System.setProperty("NLS_LANG", "HINDI");
        DataSource dataSource = getDataSource();

        assertNotNull(dataSource);
        checkNlsSessionParameter(dataSource, "NLS_LANGUAGE", "HINDI");
    }

    @Test
    void initNlsLangEmpty() throws SQLException {
        System.setProperty("NLS_LANG", "");
        DataSource dataSource = getDataSource();

        assertNotNull(dataSource);
    }

    private DataSource getDataSource() throws SQLException {
        ConnectionConfig config = new ConnectionConfig(TestHelper.getConnectionString());
        return new TestedDataSourceProvider(config).getDataSource();
    }

    private void checkNlsSessionParameter( DataSource dataSource, String parameterName, String expectedValue ) throws SQLException {
        try ( Connection con = dataSource.getConnection() ) {
            try (PreparedStatement stmt = con.prepareStatement("select value from nls_session_parameters where parameter = ?")) {
                stmt.setString(1, parameterName);
                ResultSet rs = stmt.executeQuery();
                if ( rs.next() )
                    assertEquals(expectedValue, rs.getString(1));
                else
                    fail("Could not get NLS Session parameter value for '" + parameterName + "'");
            }
        }
    }
}
