package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionConfigTest {

    @Test
    void parse() {
        ConnectionConfig info = new ConnectionConfig("test/pw@my.local.host/service");

        assertEquals("test", info.getUser());
        assertEquals("pw", info.getPassword());
        assertEquals("my.local.host/service", info.getConnect());
        assertFalse(info.isSysDba());
    }

    @Test
    void parseSysDba() {
        ConnectionConfig info = new ConnectionConfig("sys as sysdba/pw@my.local.host/service");

        assertEquals("sys as sysdba", info.getUser());
        assertEquals("pw", info.getPassword());
        assertEquals("my.local.host/service", info.getConnect());
        assertTrue(info.isSysDba());
    }
    @Test
    void parseSysOper() {
        ConnectionConfig info = new ConnectionConfig("myOperUser as sysoper/passw0rd@my.local.host/service");

        assertEquals("myOperUser as sysoper", info.getUser());
        assertEquals("passw0rd", info.getPassword());
        assertEquals("my.local.host/service", info.getConnect());
        assertTrue(info.isSysDba());
    }

    @Test
    void parseSpecialCharsPW() {
        ConnectionConfig info = new ConnectionConfig("test/\"p@ssw0rd=\"@my.local.host/service");

        assertEquals("test", info.getUser());
        assertEquals("p@ssw0rd=", info.getPassword());
        assertEquals("my.local.host/service", info.getConnect());
        assertFalse(info.isSysDba());
    }

    @Test
    void parseSpecialCharsUser() {
        ConnectionConfig info = new ConnectionConfig("\"User/Mine@=\"/pw@my.local.host/service");

        assertEquals("User/Mine@=", info.getUser());
        assertEquals("pw", info.getPassword());
        assertEquals("my.local.host/service", info.getConnect());
        assertFalse(info.isSysDba());
    }

    @Test
    void parseCredentialsIsNotExternalAuthentication() {
        ConnectionConfig info = new ConnectionConfig("test/pw@MY_TNS_ALIAS");

        assertFalse(info.isExternalAuthentication());
        assertEquals("test/pw@MY_TNS_ALIAS", info.getConnectString());
    }

    @Test
    void parseExternalAuthentication() {
        ConnectionConfig info = new ConnectionConfig("/@MY_TNS_ALIAS");

        assertNull(info.getUser());
        assertNull(info.getPassword());
        assertEquals("MY_TNS_ALIAS", info.getConnect());
        assertTrue(info.isExternalAuthentication());
        assertFalse(info.isSysDba());
        assertEquals("/@MY_TNS_ALIAS", info.getConnectString());
    }

    @Test
    void parseExternalAuthenticationWithTnsAdminInUrl() {
        ConnectionConfig info = new ConnectionConfig("/@MY_TNS_ALIAS?TNS_ADMIN=/home/me/oracle/network/admin");

        assertNull(info.getUser());
        assertNull(info.getPassword());
        assertEquals("MY_TNS_ALIAS?TNS_ADMIN=/home/me/oracle/network/admin", info.getConnect());
        assertTrue(info.isExternalAuthentication());
    }

    @Test
    void parseExternalAuthenticationWithEzConnect() {
        ConnectionConfig info = new ConnectionConfig("/@//my.local.host:1521/service");

        assertEquals("//my.local.host:1521/service", info.getConnect());
        assertTrue(info.isExternalAuthentication());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/pw@MY_TNS_ALIAS",   // password without user
            "test/@MY_TNS_ALIAS", // user without password
            "@MY_TNS_ALIAS",
            "test@MY_TNS_ALIAS",
            "MY_TNS_ALIAS"
    })
    void rejectInvalidConnectString(String connectString) {
        assertThrows(IllegalArgumentException.class, () -> new ConnectionConfig(connectString));
    }
}
