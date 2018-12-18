package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.api.JavaApiVersionInfo;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CliVersionInfoTest {

    @Test
    void getCliVersionInfo() {
        assertTrue(CliVersionInfo.getVersion().startsWith("3.1"));
    }
}
