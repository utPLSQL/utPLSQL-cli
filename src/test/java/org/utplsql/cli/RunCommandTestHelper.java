package org.utplsql.cli;

import com.beust.jcommander.JCommander;

class RunCommandTestHelper {
    private static String sUrl;
    private static String sUser;
    private static String sPass;

    static {
        sUrl  = System.getenv("DB_URL")  != null ? System.getenv("DB_URL")  : "192.168.99.100:1521:XE";
        sUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "app";
        sPass = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : "app";
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
