package org.utplsql.cli;


import org.utplsql.api.JavaApiVersionInfo;
import org.utplsql.api.Version;
import org.utplsql.api.db.DefaultDatabaseInformation;
import org.utplsql.api.exception.UtPLSQLNotInstalledException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Command(name = "info", description = "prints version information of cli, java-api and - if connection is given - database utPLSQL framework")
public class VersionInfoCommand implements ICommand {

    @Parameters(description = UtplsqlPicocliCommand.COMMANDLINE_PARAM_DESCRIPTION, arity = "0..1")
    private String connectionString;

    @Option(names = "-h", usageHelp = true, description = "display this help and exit")
    boolean help;

    public int run() {

        System.out.println(CliVersionInfo.getInfo());
        System.out.println(JavaApiVersionInfo.getInfo());

        try {
            writeUtPlsqlVersion(connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    private void writeUtPlsqlVersion(String connectString) throws SQLException {
        if (connectString != null) {

            DataSource dataSource = DataSourceProvider.getDataSource(connectString, 1);

            try (Connection con = dataSource.getConnection()) {
                Version v = new DefaultDatabaseInformation().getUtPlsqlFrameworkVersion(con);
                System.out.println("utPLSQL " + v.getNormalizedString());
            } catch (UtPLSQLNotInstalledException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
