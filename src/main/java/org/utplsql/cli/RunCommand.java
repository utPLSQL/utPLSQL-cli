package org.utplsql.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utplsql.api.*;
import org.utplsql.api.compatibility.CompatibilityProxy;
import org.utplsql.api.db.DefaultDatabaseInformation;
import org.utplsql.api.exception.DatabaseNotCompatibleException;
import org.utplsql.api.exception.OracleCreateStatmenetStuckException;
import org.utplsql.api.exception.SomeTestsFailedException;
import org.utplsql.api.exception.UtPLSQLNotInstalledException;
import org.utplsql.api.reporter.Reporter;
import org.utplsql.api.reporter.ReporterFactory;
import org.utplsql.cli.exception.DatabaseConnectionFailed;
import org.utplsql.cli.exception.ReporterTimeoutException;
import org.utplsql.cli.log.StringBlockFormatter;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Issues a Run-Command with all the options
 *
 * Uses an executor to start a RunTestRunnerTask and one ReporterGatheringTask per Reporter requested.
 *
 * @author vinicious moreira
 * @author pesse
 */
@Parameters(separators = "=", commandDescription = "run tests")
public class RunCommand implements ICommand {

    private static final Logger logger = LoggerFactory.getLogger(RunCommand.class);

    @Parameter(
            required = true,
            converter = ConnectionInfo.ConnectionStringConverter.class,
            arity = 1,
            description = ConnectionInfo.COMMANDLINE_PARAM_DESCRIPTION)
    private List<ConnectionInfo> connectionInfoList = new ArrayList<>();

    @Parameter(
            names = {"-p", "--path"},
            description = "run suites/tests by path, format: " +
                    "-p=[schema|schema:[suite ...][.test]|schema[.suite ...][.test]")
    private List<String> testPaths = new ArrayList<>();

    @Parameter(
            names = {"-f", "--format"},
            variableArity = true,
            description = "-f=reporter_name [-o=output_file [-s]] - enables specified format reporting to specified " +
                    "output file (-o) and to screen (-s)")
    private List<String> reporterParams = new ArrayList<>();

    @Parameter(
            names = {"-c", "--color"},
            description = "enables printing of test results in colors as defined by ANSICONSOLE standards")
    private boolean colorConsole = false;

    @Parameter(
            names = {"--failure-exit-code"},
            description = "override the exit code on failure, default = 1")
    private int failureExitCode = 1;

    @Parameter(
            names = {"-source_path"},
            variableArity = true,
            description = "-source_path [-owner=\"owner\" -regex_expression=\"pattern\" " +
                    "-type_mapping=\"matched_string=TYPE/matched_string=TYPE\" " +
                    "-owner_subexpression=0 -type_subexpression=0 -name_subexpression=0] - path to project source files")
    private List<String> sourcePathParams = new ArrayList<>();

    @Parameter(
            names = {"-test_path"},
            variableArity = true,
            description = "-test_path [-regex_expression=\"pattern\" -owner_subexpression=0 -type_subexpression=0 " +
                    "-name_subexpression=0] - path to project test files")
    private List<String> testPathParams = new ArrayList<>();

    @Parameter(
            names = {"-scc", "--skip-compatibility-check"},
            description = "Skips the check for compatibility with database framework. CLI expects the framework to be " +
                    "most actual. Use this if you use CLI with a development version of utPLSQL-framework")
    private boolean skipCompatibilityCheck = false;

    @Parameter(
            names = {"-include"},
            description = "Comma-separated object list to include in the coverage report. " +
                    "Format: [schema.]package[,[schema.]package ...]. See coverage reporting options in framework documentation"
    )
    private String includeObjects = null;

    @Parameter(
            names = {"-exclude"},
            description = "Comma-separated object list to exclude from the coverage report. " +
                    "Format: [schema.]package[,[schema.]package ...]. See coverage reporting options in framework documentation"
    )
    private String excludeObjects = null;

    @Parameter(
            names = {"-q", "--quiet"},
            description = "Does not output the informational messages normally printed to console")
    private boolean logSilent = false;

    @Parameter(
            names = {"-d", "--debug"},
            description = "Outputs a load of debug information to console")
    private boolean logDebug = false;

    @Parameter(
            names = {"-t", "--timeout"},
            description = "Sets the timeout in minutes after which the cli will abort. Default 60")
    private int timeoutInMinutes = 60;

    private CompatibilityProxy compatibilityProxy;
    private ReporterFactory reporterFactory;
    private ReporterManager reporterManager;

    private ConnectionInfo getConnectionInfo() {
        return connectionInfoList.get(0);
    }

    void init() {

        LoggerConfiguration.ConfigLevel level = LoggerConfiguration.ConfigLevel.BASIC;
        if ( logSilent ) {
            level = LoggerConfiguration.ConfigLevel.NONE;
        }
        else if ( logDebug ) {
            level = LoggerConfiguration.ConfigLevel.DEBUG;
        }

        LoggerConfiguration.configure(level);
    }

    public int doRun() throws OracleCreateStatmenetStuckException {
        init();
        outputMainInformation();

        HikariDataSource dataSource = null;
        int returnCode = 0;
        try {

            final List<Reporter> reporterList;

            dataSource = (HikariDataSource) DataSourceProvider.getDataSource(getConnectionInfo(), getReporterManager().getNumberOfReporters() + 2);

            initDatabase(dataSource);
            reporterList = initReporters(dataSource);

            // Output a message if --failureExitCode is set but database framework is not capable of
            String msg = RunCommandChecker.getCheckFailOnErrorMessage(failureExitCode, compatibilityProxy.getDatabaseVersion());
            if (msg != null) {
                System.out.println(msg);
            }

            ExecutorService executorService = Executors.newFixedThreadPool(1 + reporterList.size());

            // Run tests.
            Future<Boolean> future = executorService.submit(new RunTestRunnerTask(dataSource, newTestRunner(reporterList)));

            // Gather each reporter results on a separate thread.
            getReporterManager().startReporterGatherers(executorService, dataSource);

            try {
                future.get(timeoutInMinutes, TimeUnit.MINUTES);
            } catch (TimeoutException e) {
                executorService.shutdownNow();
                throw new ReporterTimeoutException(timeoutInMinutes);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof SomeTestsFailedException) {
                    returnCode = failureExitCode;
                } else {
                    executorService.shutdownNow();
                    throw e.getCause();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                throw e;
            }
            finally {
                executorService.shutdown();
                if (!executorService.awaitTermination(timeoutInMinutes, TimeUnit.MINUTES)) {
                    throw new ReporterTimeoutException(timeoutInMinutes);
                }
            }

            logger.info("--------------------------------------");
            logger.info("All tests done.");
        } catch ( OracleCreateStatmenetStuckException e ) {
            throw e;
        } catch ( DatabaseNotCompatibleException | UtPLSQLNotInstalledException | DatabaseConnectionFailed | ReporterTimeoutException e ) {
            System.out.println(e.getMessage());
            returnCode = Cli.DEFAULT_ERROR_CODE;
        } catch (Throwable e) {
            e.printStackTrace();
            returnCode = Cli.DEFAULT_ERROR_CODE;
        } finally {
            if ( dataSource != null )
                dataSource.close();
        }
        return returnCode;
    }

    public int run() {
        for ( int i = 1; i<5; i++ ) {
            try {
                return doRun();
            } catch (OracleCreateStatmenetStuckException e) {
                logger.warn("WARNING: Caught Oracle stuck during creation of Runner-Statement. Retrying ({})", i);
            }
        }

        return Cli.DEFAULT_ERROR_CODE;
    }

    private TestRunner newTestRunner( List<Reporter> reporterList) {

        final File baseDir = new File("").getAbsoluteFile();

        return new TestRunner()
                .addPathList(testPaths)
                .addReporterList(reporterList)
                .sourceMappingOptions(getFileMapperOptionsByParamListItem(this.sourcePathParams, baseDir))
                .testMappingOptions(getFileMapperOptionsByParamListItem(this.testPathParams, baseDir))
                .colorConsole(this.colorConsole)
                .failOnErrors(true)
                .skipCompatibilityCheck(skipCompatibilityCheck)
                .includeObjects(getObjectList(includeObjects))
                .excludeObjects(getObjectList(excludeObjects));
    }

    private ArrayList<String> getObjectList(String includeObjects) {
        ArrayList<String> includeObjectsList;
        if (includeObjects != null && !includeObjects.isEmpty()) {
            includeObjectsList = new ArrayList<>(Arrays.asList(includeObjects.split(",")));
        } else {
            includeObjectsList = new ArrayList<>();
        }
        return includeObjectsList;
    }

    @Override
    public String getCommand() {
        return "run";
    }


    private void outputMainInformation() {

        StringBlockFormatter formatter = new StringBlockFormatter("utPLSQL cli");
        formatter.appendLine(CliVersionInfo.getInfo());
        formatter.appendLine(JavaApiVersionInfo.getInfo());
        formatter.appendLine("Java-Version: " + System.getProperty("java.version"));
        formatter.appendLine("ORACLE_HOME: " + DataSourceProvider.getOracleHome());
        formatter.appendLine("TNS_ADMIN: " + DataSourceProvider.getTnsAdmin());
        formatter.appendLine("NLS_LANG: " + EnvironmentVariableUtil.getEnvValue("NLS_LANG"));
        formatter.appendLine("");
        formatter.appendLine("Thanks for testing!");

        logger.info(formatter.toString());
        logger.info("");
    }

    private void initDatabase(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {

            // Check if orai18n exists if database version is 11g
            RunCommandChecker.checkOracleI18nExists(conn);

            // First of all do a compatibility check and fail-fast
            compatibilityProxy = checkFrameworkCompatibility(conn);

            logger.info("Successfully connected to database. UtPLSQL core: {}", compatibilityProxy.getDatabaseVersion());
            logger.info("Oracle-Version: {}", new DefaultDatabaseInformation().getOracleVersion(conn));
        }
        catch (SQLException e) {
            if (e.getErrorCode() == 1017 || e.getErrorCode() == 12514) {
                throw new DatabaseConnectionFailed(e);
            } else {
                throw e;
            }
        }
    }

    private List<Reporter> initReporters(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            reporterFactory = ReporterFactoryProvider.createReporterFactory(compatibilityProxy);
            return getReporterManager().initReporters(conn, reporterFactory, compatibilityProxy);
        }
    }

    /** Returns FileMapperOptions for the first item of a given param list in a baseDir
     *
     * @param pathParams
     * @param baseDir
     * @return FileMapperOptions or null
     */
    private FileMapperOptions getFileMapperOptionsByParamListItem(List<String> pathParams, File baseDir )
    {
        if (!pathParams.isEmpty()) {
            String sourcePath = pathParams.get(0);
            List<String> files = new FileWalker().getFileList(baseDir, sourcePath);
           return getMapperOptions(pathParams, files);
        }

        return null;
    }

    /** Checks whether cli is compatible with the database framework
     *
     * @param conn Active Connection
     * @throws SQLException
     */
    private CompatibilityProxy checkFrameworkCompatibility(Connection conn) throws SQLException {

        CompatibilityProxy proxy = new CompatibilityProxy(conn, skipCompatibilityCheck);

        if ( !skipCompatibilityCheck ) {
            proxy.failOnNotCompatible();
        }
        else {
            System.out.println("Skipping Compatibility check with framework version, expecting the latest version " +
                    "to be installed in database");
        }

        return proxy;
    }

    private FileMapperOptions getMapperOptions(List<String> mappingParams, List<String> filePaths) {
        FileMapperOptions mapperOptions = new FileMapperOptions(filePaths);

        final String OPT_OWNER="-owner=";
        final String OPT_REGEX="-regex_expression=";
        final String OPT_TYPE_MAPPING="-type_mapping=";
        final String OPT_OWNER_SUBEX="-owner_subexpression=";
        final String OPT_NAME_SUBEX="-name_subexpression=";
        final String OPT_TYPE_SUBEX="-type_subexpression=";

        for (String p : mappingParams) {
            if (p.startsWith(OPT_OWNER)) {
                mapperOptions.setObjectOwner(p.substring(OPT_OWNER.length()));
            }
            else
            if (p.startsWith(OPT_REGEX)) {
                mapperOptions.setRegexPattern(p.substring(OPT_REGEX.length()));
            }
            else
            if (p.startsWith(OPT_TYPE_MAPPING)) {
                String typeMappingsParam = p.substring(OPT_TYPE_MAPPING.length());

                List<KeyValuePair> typeMappings = new ArrayList<>();
                for (String mapping : typeMappingsParam.split("/")) {
                    String[] values = mapping.split("=");
                    typeMappings.add(new KeyValuePair(values[0], values[1]));
                }

                mapperOptions.setTypeMappings(typeMappings);
            }
            else
            if (p.startsWith(OPT_OWNER_SUBEX)) {
                mapperOptions.setOwnerSubExpression(Integer.parseInt(p.substring(OPT_OWNER_SUBEX.length())));
            }
            else
            if (p.startsWith(OPT_NAME_SUBEX)) {
                mapperOptions.setNameSubExpression(Integer.parseInt(p.substring(OPT_NAME_SUBEX.length())));
            }
            else
            if (p.startsWith(OPT_TYPE_SUBEX)) {
                mapperOptions.setTypeSubExpression(Integer.parseInt(p.substring("-type_subexpression=".length())));
            }
        }

        return mapperOptions;
    }

    private ReporterManager getReporterManager() {
        if ( reporterManager == null )
            reporterManager = new ReporterManager(reporterParams);

        return reporterManager;
    }

    List<ReporterOptions> getReporterOptionsList() {
        return getReporterManager().getReporterOptionsList();
    }
}
