package org.utplsql.cli;

import org.utplsql.api.DBHelper;
import org.utplsql.api.EnvironmentVariableUtil;
import org.utplsql.api.Version;
import org.utplsql.cli.config.RunCommandConfig;
import picocli.CommandLine;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class TestHelper {
    private static String sUrl;
    private static String sUser;
    private static String sPass;

    static {
        sUrl  = EnvironmentVariableUtil.getEnvValue("DB_URL", "192.168.99.100:1521:XE");
        sUser = EnvironmentVariableUtil.getEnvValue("DB_USER", "app");
        sPass = EnvironmentVariableUtil.getEnvValue("DB_PASS", "app");
    }

    static RunPicocliCommand createPicocliRunCommand(String... args ) {
        Object obj = new UtplsqlPicocliCommand();
        CommandLine cline = new CommandLine(obj);
        cline.setTrimQuotes(true);
        List<CommandLine> parsed = cline.parse(args);

        return parsed.get(1).getCommand();
    }

    static IRunCommand createRunCommand(String... args) {
        ArrayList<String> newArgs = new ArrayList<>(args.length+1);
        newArgs.add("run");
        newArgs.addAll(Arrays.asList(args));
        return createPicocliRunCommand(newArgs.toArray(new String[0]));
    }

    static RunCommandConfig parseRunConfig(String... args ) throws Exception {
        Object obj = new UtplsqlPicocliCommand();
        CommandLine cline = new CommandLine(obj);
        cline.setTrimQuotes(true);
        List<CommandLine> parsed = cline.parse(args);

        RunPicocliCommand runCmd = parsed.get(1).getCommand();
        return runCmd.getRunCommandConfig();
    }

    static RunAction createRunAction(String... args) throws Exception {
        ArrayList<String> newArgs = new ArrayList<>(args.length+1);
        newArgs.add("run");
        newArgs.addAll(Arrays.asList(args));
        return new RunAction(parseRunConfig(newArgs.toArray(new String[0])));
    }

    static int runApp(String... args) {
        return Cli.runPicocliWithExitCode(args);
    }

    static Version getFrameworkVersion() throws SQLException {
        DataSource ds = DataSourceProvider.getDataSource(TestHelper.getConnectionString(), 1);
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
