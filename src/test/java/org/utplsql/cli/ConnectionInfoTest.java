package org.utplsql.cli;

/**
 * Created by Vinicius on 21/04/2017.
 */
public class ConnectionInfoTest {

    /**
     * Regex pattern to match following connection strings:
     * user/pass@host:port/db
     * user/pass@host/db
     * user/pass@db
     */

//    @Test
//    public void valid_Full() {
//        try {
//            ConnectionInfo ci = new ConnectionInfo()
//                    .parseConnectionString("my_user/p@ss!@some.server.123-abc.com:3000/db_1.acme.com");
//            Assert.assertEquals("my_user", ci.getUser());
//            Assert.assertEquals("p@ss!", ci.getPassword());
//            Assert.assertEquals("some.server.123-abc.com", ci.getHost());
//            Assert.assertEquals(3000, ci.getPort());
//            Assert.assertEquals("db_1.acme.com", ci.getDatabase());
//            Assert.assertEquals("my_user@some.server.123-abc.com:3000/db_1.acme.com", ci.toString());
//            Assert.assertEquals("jdbc:oracle:thin:@//some.server.123-abc.com:3000/db_1.acme.com", ci.getConnectionUrl());
//        } catch (Exception e) {
//            Assert.fail(e.getMessage());
//        }
//    }
//
//    @Test
//    public void valid_WithoutPort() {
//        try {
//            ConnectionInfo ci = new ConnectionInfo()
//                    .parseConnectionString("my_user/p@ss!@some.server.123-abc.com/db_1.acme.com");
//            Assert.assertEquals("my_user", ci.getUser());
//            Assert.assertEquals("p@ss!", ci.getPassword());
//            Assert.assertEquals("some.server.123-abc.com", ci.getHost());
//            Assert.assertEquals(1521, ci.getPort());
//            Assert.assertEquals("db_1.acme.com", ci.getDatabase());
//            Assert.assertEquals("my_user@some.server.123-abc.com:1521/db_1.acme.com", ci.toString());
//            Assert.assertEquals("jdbc:oracle:thin:@//some.server.123-abc.com:1521/db_1.acme.com", ci.getConnectionUrl());
//        } catch (Exception e) {
//            Assert.fail(e.getMessage());
//        }
//    }
//
//    @Test
//    public void valid_WithoutHostAndPort() {
//        try {
//            ConnectionInfo ci = new ConnectionInfo()
//                    .parseConnectionString("my_user/p@ss!@127.0.0.1/db_1.acme.com");
//            Assert.assertEquals("my_user", ci.getUser());
//            Assert.assertEquals("p@ss!", ci.getPassword());
//            Assert.assertEquals("127.0.0.1", ci.getHost());
//            Assert.assertEquals(1521, ci.getPort());
//            Assert.assertEquals("db_1.acme.com", ci.getDatabase());
//            Assert.assertEquals("my_user@127.0.0.1:1521/db_1.acme.com", ci.toString());
//            Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:1521/db_1.acme.com", ci.getConnectionUrl());
//        } catch (Exception e) {
//            Assert.fail(e.getMessage());
//        }
//    }
//
//    @Test
//    public void invalid_WithoutDatabase_1() {
//        try {
//            new ConnectionInfo().parseConnectionString("user/pass@");
//            Assert.fail();
//        } catch (ParameterException ignored) {}
//    }
//
//    @Test
//    public void invalid_WithoutDatabase_2() {
//        try {
//            new ConnectionInfo().parseConnectionString("user/pass");
//            Assert.fail();
//        } catch (ParameterException ignored) {}
//    }
//
//    @Test
//    public void invalid_WithoutDatabase_3() {
//        try {
//            new ConnectionInfo().parseConnectionString("user/pass@localhost:1521");
//            Assert.fail();
//        } catch (ParameterException ignored) {}
//    }
//
//    @Test
//    public void invalid_WithoutHost() {
//        try {
//            new ConnectionInfo().parseConnectionString("user/pass@/db");
//            Assert.fail();
//        } catch (ParameterException ignored) {}
//    }
//
//    @Test
//    public void invalid_WithoutPassword() {
//        try {
//            new ConnectionInfo().parseConnectionString("user/@db");
//            Assert.fail();
//        } catch (ParameterException ignored) {}
//    }
//
//    @Test
//    public void invalid_WithoutUsername() {
//        try {
//            new ConnectionInfo().parseConnectionString("/pass@db");
//            Assert.fail();
//        } catch (ParameterException ignored) {}
//    }
//
//    @Test
//    public void invalid_WithoutUserPassDb_1() {
//        try {
//            new ConnectionInfo().parseConnectionString("/@db");
//            Assert.fail();
//        } catch (ParameterException ignored) {}
//    }
//
//    @Test
//    public void invalid_WithoutUserPass() {
//        try {
//            new ConnectionInfo().parseConnectionString("@db");
//            Assert.fail();
//        } catch (ParameterException ignored) {}
//    }

}
