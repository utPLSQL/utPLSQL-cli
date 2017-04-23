package io.github.utplsql.cli;

import com.beust.jcommander.ParameterException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Vinicius on 21/04/2017.
 */
public class ConnectionInfoTest {

    /**
     * Regex pattern to match following connection strings:
     * user/pass@127.0.0.1:1521/sid
     * user/pass@127.0.0.1/sid
     * user/pass@sid
     * user/pass
     */

    @Test
    public void connectionStr_Full() {
        try {
            ConnectionInfo ci = new ConnectionInfo().parseConnectionString("user/pass@localhost:3000/sid");
            Assert.assertEquals("user", ci.getUser());
            Assert.assertEquals("pass", ci.getPassword());
            Assert.assertEquals("localhost", ci.getHost());
            Assert.assertEquals(3000, ci.getPort());
            Assert.assertEquals("sid", ci.getSid());
            Assert.assertEquals("user@localhost:3000/sid", ci.toString());
            Assert.assertEquals("jdbc:oracle:thin:@localhost:3000:sid", ci.getConnectionUrl());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void connectionStr_WithoutPort() {
        try {
            ConnectionInfo ci = new ConnectionInfo().parseConnectionString("user/pass@localhost/sid");
            Assert.assertEquals("user", ci.getUser());
            Assert.assertEquals("pass", ci.getPassword());
            Assert.assertEquals("localhost", ci.getHost());
            Assert.assertEquals(1521, ci.getPort());
            Assert.assertEquals("sid", ci.getSid());
            Assert.assertEquals("user@localhost:1521/sid", ci.toString());
            Assert.assertEquals("jdbc:oracle:thin:@localhost:1521:sid", ci.getConnectionUrl());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void connectionStr_WithoutHostAndPort() {
        try {
            ConnectionInfo ci = new ConnectionInfo().parseConnectionString("user/pass@sid");
            Assert.assertEquals("user", ci.getUser());
            Assert.assertEquals("pass", ci.getPassword());
            Assert.assertEquals("127.0.0.1", ci.getHost());
            Assert.assertEquals(1521, ci.getPort());
            Assert.assertEquals("sid", ci.getSid());
            Assert.assertEquals("user@127.0.0.1:1521/sid", ci.toString());
            Assert.assertEquals("jdbc:oracle:thin:@127.0.0.1:1521:sid", ci.getConnectionUrl());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void connectionStr_Invalid() {
        try {
            new ConnectionInfo().parseConnectionString("user/pass");
            Assert.fail();
        } catch (ParameterException ignored) {}
    }

}
