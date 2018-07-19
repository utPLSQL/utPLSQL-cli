package org.utplsql.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReportersCommandIT {

    @Test
    public void callReportersWorks() {

        int result = TestHelper.runApp("reporters", TestHelper.getConnectionString());

        assertEquals(0, result);
    }
}
