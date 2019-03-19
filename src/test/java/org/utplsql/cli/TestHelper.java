package org.utplsql.cli;

import com.beust.jcommander.JCommander;
import org.utplsql.api.DBHelper;
import org.utplsql.api.EnvironmentVariableUtil;
import org.utplsql.api.Version;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

class TestHelper {
    private static String sUrl;
    private static String sUser;
    private static String sPass;

    static {
        sUrl  = EnvironmentVariableUtil.getEnvValue("DB_URL", "192.168.99.100:1521:XE");
        sUser = EnvironmentVariableUtil.getEnvValue("DB_USER", "app");
        sPass = EnvironmentVariableUtil.getEnvValue("DB_PASS", "app");
    }

    static RunCommand createRunCommand(String... args) {
        RunCommand runCmd = new RunCommand();

        JCommander.newBuilder()
                .addObject(runCmd)
                .args(args)
                .build();

        return runCmd;
    }

    static int runApp(String... args) {
        return Cli.runWithExitCode(args);
    }

    static Version getFrameworkVersion() throws SQLException {
        DataSource ds = DataSourceProvider.getDataSource(new ConnectionInfo(TestHelper.getConnectionString()), 1);
        try (Connection con = ds.getConnection() ) {
            return DBHelper.getDatabaseFrameworkVersion(con);
        }
    }

    static String getConnectionString() {
        return sUser + "/" + sPass + "@" + sUrl;
    }

    static String getUser() {
        return sUser;
    }

    static String getPass() {
        return sPass;
    }

    static String getUrl() {
        return sUrl;
    }
}
