package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.cli.datasource.DataSourceProvider;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DataSourceProviderIT {

    @Test
    public void connectToDatabase() throws IOException, SQLException {

        ConnectionConfig config = new ConnectionConfig(RunCommandTestHelper.getConnectionString());

        DataSource dataSource = new DataSourceProvider(config).getDataSource();

        assertNotNull(dataSource);
    }
}
