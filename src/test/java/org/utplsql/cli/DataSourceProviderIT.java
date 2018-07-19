package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.cli.datasource.TestedDataSourceProvider;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DataSourceProviderIT {

    @Test
    public void connectToDatabase() throws IOException, SQLException {

        ConnectionConfig config = new ConnectionConfig(TestHelper.getConnectionString());

        DataSource dataSource = new TestedDataSourceProvider(config).getDataSource();

        assertNotNull(dataSource);
    }
}
