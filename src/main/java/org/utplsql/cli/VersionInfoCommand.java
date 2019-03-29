package org.utplsql.cli;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.utplsql.api.JavaApiVersionInfo;
import org.utplsql.api.Version;
import org.utplsql.api.db.DefaultDatabaseInformation;
import org.utplsql.api.exception.UtPLSQLNotInstalledException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Parameters(separators = "=", commandDescription = "prints version information of cli, java-api and - if connection is given - database utPLSQL framework")
public class VersionInfoCommand implements ICommand {

    @Parameter(
            converter = ConnectionInfo.ConnectionStringConverter.class,
            variableArity = true,
            description = ConnectionInfo.COMMANDLINE_PARAM_DESCRIPTION)
    private List<ConnectionInfo> connectionInfoList = new ArrayList<>();

    private ConnectionInfo getConnectionInfo() {
        if ( connectionInfoList != null && connectionInfoList.size() > 0 )
            return connectionInfoList.get(0);
        else
            return null;
    }

    public int run() {

        System.out.println(CliVersionInfo.getInfo());
        System.out.println(JavaApiVersionInfo.getInfo());

        try {
            writeUtPlsqlVersion(getConnectionInfo());
        }
        catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    private void writeUtPlsqlVersion( ConnectionInfo ci ) throws SQLException {
        if ( ci != null ) {

            DataSource dataSource = DataSourceProvider.getDataSource(ci, 1);

            try (Connection con = dataSource.getConnection()) {
                Version v = new DefaultDatabaseInformation().getUtPlsqlFrameworkVersion(con);
                System.out.println("utPLSQL " + v.getNormalizedString());
            }
            catch ( UtPLSQLNotInstalledException e ) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public String getCommand() {
        return "info";
    }
}
