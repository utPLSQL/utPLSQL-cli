package org.utplsql.cli;

import com.beust.jcommander.JCommander;
import org.utplsql.api.EnvironmentVariableUtil;

class RunCommandTestHelper {
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

    static String getConnectionString() {
        return sUser + "/" + sPass + "@" + sUrl;
    }
}
