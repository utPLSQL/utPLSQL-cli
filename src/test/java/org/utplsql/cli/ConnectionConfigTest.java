package org.utplsql.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConnectionConfigTest {

    @Test
    void testConnectStringWithDot() {
        ConnectionConfig config = new ConnectionConfig("test/test_pwd@//server1.com:1521/service1");

        assertEquals("test", config.getUser());
        assertEquals("test_pwd", config.getPassword());
        assertEquals("//server1.com:1521/service1", config.getConnect());
        assertEquals("test/test_pwd@//server1.com:1521/service1", config.getConnectString());
    }
}
