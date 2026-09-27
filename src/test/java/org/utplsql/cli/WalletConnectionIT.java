package org.utplsql.cli;

import oracle.security.pki.OracleSecretStore;
import oracle.security.pki.OracleWallet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.utplsql.cli.datasource.TestedDataSourceProvider;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Connecting with {@code /@<TNS alias>} using credentials stored in an Oracle Wallet
 * (Secure External Password Store), see issue #225.
 * <p>
 * The wallet, tnsnames.ora and ojdbc.properties are created on the fly from DB_URL / DB_USER / DB_PASS,
 * so no Oracle client tooling (mkstore/orapki) is needed.
 * <p>
 * TNS_ADMIN and ORACLE_HOME are read once per JVM and environment variables can't be changed at runtime,
 * so the scenarios depending on them run the CLI in a separate JVM with a controlled environment.
 */
class WalletConnectionIT {

    private static final String TNS_ALIAS = "UTPLSQL_CLI_WALLET";
    private static final Pattern EZ_CONNECT = Pattern.compile("^//([^:/]+)(?::(\\d+))?/(.+)$");
    private static final long SUBPROCESS_TIMEOUT_MINUTES = 3;

    @TempDir
    static Path tempDir;

    private static Path tnsAdmin;
    private static Path oracleHome;
    private static Path emptyOracleHome;
    private static String walletConnectString;

    @BeforeAll
    static void createWallet() throws Exception {
        Matcher m = EZ_CONNECT.matcher(TestHelper.getUrl());
        assumeTrue(m.matches(), "DB_URL must be in //host[:port]/service format to generate tnsnames.ora");
        String host = m.group(1);
        String port = m.group(2) == null ? "1521" : m.group(2);
        String service = m.group(3);

        Path walletDir = Files.createDirectories(tempDir.resolve("wallet"));
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

        String tnsnamesOra = TNS_ALIAS + " = (DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST = " + host + ")(PORT = " + port + "))"
                + "(CONNECT_DATA = (SERVICE_NAME = " + service + ")))\n";
        String ojdbcProperties = "oracle.net.wallet_location=(SOURCE=(METHOD=FILE)(METHOD_DATA=(DIRECTORY="
                + forwardSlashes(walletDir) + ")))\n";

        tnsAdmin = writeNetworkConfig(tempDir.resolve("tns_admin"), tnsnamesOra, ojdbcProperties);
        oracleHome = tempDir.resolve("oracle_home");
        writeNetworkConfig(oracleHome.resolve("network").resolve("admin"), tnsnamesOra, ojdbcProperties);
        emptyOracleHome = Files.createDirectories(tempDir.resolve("empty_oracle_home"));

        walletConnectString = "/@" + TNS_ALIAS + "?TNS_ADMIN=" + forwardSlashes(tnsAdmin);
    }

    private static Path writeNetworkConfig(Path dir, String tnsnamesOra, String ojdbcProperties) throws IOException {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("tnsnames.ora"), tnsnamesOra);
        Files.writeString(dir.resolve("ojdbc.properties"), ojdbcProperties);
        return dir;
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

    @Test
    void runCommandWithTnsAdminEnvironmentVariable() throws Exception {
        assertCliConnectsWithWallet(Map.of("TNS_ADMIN", tnsAdmin.toString()));
    }

    @Test
    void runCommandWithOracleHomeFallback() throws Exception {
        assertCliConnectsWithWallet(Map.of("ORACLE_HOME", oracleHome.toString()));
    }

    @Test
    void tnsAdminEnvironmentVariableTakesPrecedenceOverOracleHome() throws Exception {
        // Fails if ORACLE_HOME/network/admin (without tnsnames.ora) is used instead of TNS_ADMIN
        assertCliConnectsWithWallet(Map.of(
                "TNS_ADMIN", tnsAdmin.toString(),
                "ORACLE_HOME", emptyOracleHome.toString()));
    }

    /**
     * Runs {@code utplsql run /@<TNS alias>} in a separate JVM with TNS_ADMIN and ORACLE_HOME
     * replaced by the given environment
     */
    private void assertCliConnectsWithWallet(Map<String, String> environment) throws Exception {
        Path java = Paths.get(System.getProperty("java.home"), "bin", "java");
        ProcessBuilder processBuilder = new ProcessBuilder(
                java.toString(), "-cp", System.getProperty("java.class.path"),
                Cli.class.getName(), "run", "/@" + TNS_ALIAS,
                "-f=ut_documentation_reporter", "-s", "--failure-exit-code=0");

        processBuilder.environment().remove("TNS_ADMIN");
        processBuilder.environment().remove("ORACLE_HOME");
        processBuilder.environment().putAll(environment);

        Path output = Files.createTempFile(tempDir, "cli-output", ".log");
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(output.toFile());

        Process process = processBuilder.start();
        if (!process.waitFor(SUBPROCESS_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
            process.destroyForcibly();
            fail("CLI did not finish within " + SUBPROCESS_TIMEOUT_MINUTES + " minutes. Output:\n" + Files.readString(output));
        }

        String cliOutput = Files.readString(output);
        assertEquals(0, process.exitValue(), () -> "CLI failed with environment " + environment + ". Output:\n" + cliOutput);
        assertTrue(cliOutput.contains("Use connection string jdbc:oracle:thin:/@" + TNS_ALIAS),
                () -> "Expected wallet connection with environment " + environment + ". Output:\n" + cliOutput);
    }
}
