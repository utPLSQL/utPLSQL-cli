package org.utplsql.cli;

import org.junit.jupiter.api.Test;

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
}
