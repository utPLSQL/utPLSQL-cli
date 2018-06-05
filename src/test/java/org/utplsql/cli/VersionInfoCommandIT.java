package org.utplsql.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VersionInfoCommandIT {

    @Test
    public void infoCommandRunsWithoutConnection() throws Exception {

        int result = TestHelper.runApp("info");

        assertEquals(0, result);
    }
    @Test
    public void infoCommandRunsWithConnection() throws Exception {

        int result = TestHelper.runApp("info", TestHelper.getConnectionString());

        assertEquals(0, result);
    }
}
