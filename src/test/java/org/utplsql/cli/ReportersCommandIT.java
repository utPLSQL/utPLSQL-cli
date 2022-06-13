package org.utplsql.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportersCommandIT {

    @Test
    void callReportersWorks() {

        int result = TestHelper.runApp("reporters", TestHelper.getConnectionString());

        assertEquals(0, result);
    }
}
