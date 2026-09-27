package org.utplsql.cli;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataSourceProviderTest {

    private static final String ORACLE_HOME = "/opt/oracle";

    @Test
    void tnsAdminFallsBackToOracleHome() {
        assertEquals(String.join(File.separator, ORACLE_HOME, "NETWORK", "ADMIN"),
                DataSourceProvider.getTnsAdminFallback(null, null, ORACLE_HOME));
    }

    @Test
    void tnsAdminEnvVariableIsLeftToJdbcDriver() {
        // Setting the property would override the TNS_ADMIN environment variable in the JDBC driver
        assertNull(DataSourceProvider.getTnsAdminFallback(null, "/my/tns_admin", ORACLE_HOME));
    }

    @Test
    void tnsAdminPropertyIsNotOverridden() {
        assertNull(DataSourceProvider.getTnsAdminFallback("/my/tns_admin", null, ORACLE_HOME));
    }

    @Test
    void noFallbackWithoutOracleHome() {
        assertNull(DataSourceProvider.getTnsAdminFallback(null, null, null));
        assertNull(DataSourceProvider.getTnsAdminFallback("", "", ""));
    }
}
