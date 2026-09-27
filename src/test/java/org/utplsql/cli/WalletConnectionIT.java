package org.utplsql.cli;

import oracle.security.pki.OracleSecretStore;
import oracle.security.pki.OracleWallet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.utplsql.cli.datasource.TestedDataSourceProvider;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Connecting with {@code /@<TNS alias>} using credentials stored in an Oracle Wallet
 * (Secure External Password Store), see issue #225.
 * <p>
 * The wallet, tnsnames.ora and ojdbc.properties are created on the fly from DB_URL / DB_USER / DB_PASS,
 * so no Oracle client tooling (mkstore/orapki) is needed.
 */
class WalletConnectionIT {

    private static final String TNS_ALIAS = "UTPLSQL_CLI_WALLET";
    private static final Pattern EZ_CONNECT = Pattern.compile("^//([^:/]+)(?::(\\d+))?/(.+)$");

    @TempDir
    static Path tnsAdmin;

    private static String walletConnectString;

    @BeforeAll
    static void createWallet() throws Exception {
        Matcher m = EZ_CONNECT.matcher(TestHelper.getUrl());
        assumeTrue(m.matches(), "DB_URL must be in //host[:port]/service format to generate tnsnames.ora");
        String host = m.group(1);
        String port = m.group(2) == null ? "1521" : m.group(2);
        String service = m.group(3);

        Path walletDir = Files.createDirectories(tnsAdmin.resolve("wallet"));
        char[] walletPassword = "Wallet_Pwd_123".toCharArray();

        OracleWallet wallet = new OracleWallet();
        wallet.create(walletPassword);
        OracleSecretStore secretStore = wallet.getSecretStore();
        secretStore.createCredential(TNS_ALIAS.toCharArray(),
                TestHelper.getUser().toCharArray(),
                TestHelper.getPass().toCharArray());
        wallet.setSecretStore(secretStore);
        wallet.saveAs(walletDir.toString());
        wallet.createSSO();
        wallet.saveSSO();

        assertTrue(Files.exists(walletDir.resolve("cwallet.sso")), "auto-login wallet was not created");

        Files.writeString(tnsAdmin.resolve("tnsnames.ora"),
                TNS_ALIAS + " = (DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST = " + host + ")(PORT = " + port + "))"
                        + "(CONNECT_DATA = (SERVICE_NAME = " + service + ")))\n");
        Files.writeString(tnsAdmin.resolve("ojdbc.properties"),
                "oracle.net.wallet_location=(SOURCE=(METHOD=FILE)(METHOD_DATA=(DIRECTORY="
                        + forwardSlashes(walletDir) + ")))\n");

        walletConnectString = "/@" + TNS_ALIAS + "?TNS_ADMIN=" + forwardSlashes(tnsAdmin);
    }

    private static String forwardSlashes(Path path) {
        return path.toAbsolutePath().toString().replace('\\', '/');
    }

    @Test
    void connectsAsWalletUser() throws Exception {
        ConnectionConfig config = new ConnectionConfig(walletConnectString);
        DataSource dataSource = new TestedDataSourceProvider(config, 1).getDataSource();

        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("select user from dual")) {
            assertTrue(rs.next());
            assertEquals(TestHelper.getUser().toUpperCase(), rs.getString(1));
        }
    }

    @Test
    void runCommandWithWallet() {
        int result = TestHelper.runApp("run",
                walletConnectString,
                "-f=ut_documentation_reporter",
                "-s",
                "--failure-exit-code=0");

        assertEquals(0, result);
    }
}
