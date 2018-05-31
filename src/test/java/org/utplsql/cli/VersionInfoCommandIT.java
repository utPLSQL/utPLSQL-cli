package org.utplsql.cli;

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;
import org.utplsql.api.EnvironmentVariableUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VersionInfoCommandIT {

    @Test
    public void infoCommandRunsWithoutConnection() throws Exception {

        VersionInfoCommand infoCmd = new VersionInfoCommand();

        JCommander.newBuilder()
                .addObject(infoCmd)
                .args(new String[]{})
                .build();

        int result = infoCmd.run();

        assertEquals(0, result);
    }
    @Test
    public void infoCommandRunsWithConnection() throws Exception {

        String Url  = EnvironmentVariableUtil.getEnvValue("DB_URL", "192.168.99.100:1521:XE");
        String sUser = EnvironmentVariableUtil.getEnvValue("DB_USER", "app");
        String sPass = EnvironmentVariableUtil.getEnvValue("DB_PASS", "app");

        VersionInfoCommand infoCmd = new VersionInfoCommand();

        JCommander.newBuilder()
                .addObject(infoCmd)
                .args(new String[]{sUser + "/" + sPass + "@" + Url})
                .build();

        int result = infoCmd.run();

        assertEquals(0, result);
    }
}
