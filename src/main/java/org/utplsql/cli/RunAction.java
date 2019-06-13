package org.utplsql.cli;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utplsql.api.*;
import org.utplsql.api.compatibility.CompatibilityProxy;
import org.utplsql.api.compatibility.OptionalFeatures;
import org.utplsql.api.db.DefaultDatabaseInformation;
import org.utplsql.api.exception.DatabaseNotCompatibleException;
import org.utplsql.api.exception.OracleCreateStatmenetStuckException;
import org.utplsql.api.exception.SomeTestsFailedException;
import org.utplsql.api.exception.UtPLSQLNotInstalledException;
import org.utplsql.api.reporter.Reporter;
import org.utplsql.api.reporter.ReporterFactory;
import org.utplsql.cli.config.FileMapperConfig;
import org.utplsql.cli.config.ReporterConfig;
import org.utplsql.cli.config.RunCommandConfig;
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
 * Starts a test-runner and gathers the results based on the given Configuration
 *
 * @author pesse
 */
public class RunAction {

    private static final Logger logger = LoggerFactory.getLogger(RunAction.class);

    private RunCommandConfig config;

    private CompatibilityProxy compatibilityProxy;
    private ReporterFactory reporterFactory;
    private ReporterManager reporterManager;

    public RunAction( RunCommandConfig config ) {
        this.config = config;
    }

    void init() {
        LoggerConfiguration.configure(config.getLogConfigLevel());
    }

    public RunCommandConfig getConfig() {
        return config;
    }

    public int doRun() throws OracleCreateStatmenetStuckException {
        init();
        outputMainInformation();

        HikariDataSource dataSource = null;
        int returnCode = 0;
        try {

            final List<Reporter> reporterList;

            dataSource = (HikariDataSource) DataSourceProvider.getDataSource(config.getConnectString(), getReporterManager().getNumberOfReporters() + 2);

            initDatabase(dataSource);
            reporterList = initReporters(dataSource);

            checkForCompatibility(compatibilityProxy.getUtPlsqlVersion());

            ExecutorService executorService = Executors.newFixedThreadPool(1 + reporterList.size());

            // Run tests.
            Future<Boolean> future = executorService.submit(new RunTestRunnerTask(dataSource, newTestRunner(reporterList), config.isDbmsOutput()));

            // Gather each reporter results on a separate thread.
            getReporterManager().startReporterGatherers(executorService, dataSource);

            try {
                future.get(config.getTimeoutInMinutes(), TimeUnit.MINUTES);
            } catch (TimeoutException e) {
                executorService.shutdownNow();
                throw new ReporterTimeoutException(config.getTimeoutInMinutes());
            } catch (ExecutionException e) {
                if (e.getCause() instanceof SomeTestsFailedException) {
                    returnCode = config.getFailureExitCode();
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
                if (!executorService.awaitTermination(config.getTimeoutInMinutes(), TimeUnit.MINUTES)) {
                    throw new ReporterTimeoutException(config.getTimeoutInMinutes());
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

    private void checkForCompatibility( Version utPlSqlVersion ) {
        if (!OptionalFeatures.FAIL_ON_ERROR.isAvailableFor(utPlSqlVersion) && config.getFailureExitCode() != null ) {
            System.out.println("You specified option `--failure-exit-code` but your database framework version (" +
                    utPlSqlVersion.getNormalizedString() + ") is not able to " +
                    "redirect failureCodes. Please upgrade to a newer version if you want to use that feature.");
        }

        if ( !OptionalFeatures.RANDOM_EXECUTION_ORDER.isAvailableFor(utPlSqlVersion) && config.isRandomTestOrder() ) {
            System.out.println("You specified option `-random` but your database framework version (" +
                    utPlSqlVersion.getNormalizedString() + ") is not able to " +
                    "redirect failureCodes. Please upgrade to a newer version if you want to use that feature.");
        }

        if ( !OptionalFeatures.RANDOM_EXECUTION_ORDER.isAvailableFor(utPlSqlVersion) && config.getRandomTestOrderSeed() != null ) {
            System.out.println("You specified option `-seed` but your database framework version (" +
                    utPlSqlVersion.getNormalizedString() + ") is not able to " +
                    "redirect failureCodes. Please upgrade to a newer version if you want to use that feature.");
        }

    }

    TestRunner newTestRunner( List<Reporter> reporterList) {

        final File baseDir = new File("").getAbsoluteFile();

        return new TestRunner()
                .addPathList(Arrays.asList(config.getSuitePaths()))
                .addReporterList(reporterList)
                .sourceMappingOptions(getFileMapperOptionsByParamListItem(config.getSourceMapping(), baseDir))
                .testMappingOptions(getFileMapperOptionsByParamListItem(config.getTestMapping(), baseDir))
                .colorConsole(config.isOutputAnsiColor())
                .failOnErrors(true)
                .skipCompatibilityCheck(config.isSkipCompatibilityCheck())
                .includeObjects(Arrays.asList(config.getIncludePackages()))
                .excludeObjects(Arrays.asList(config.getExcludePackages()))
                .randomTestOrder(config.isRandomTestOrder())
                .randomTestOrderSeed(config.getRandomTestOrderSeed());
    }

    private void outputMainInformation() {

        StringBlockFormatter formatter = new StringBlockFormatter("utPLSQL cli");
        formatter.appendLine(CliVersionInfo.getInfo());
        formatter.appendLine(JavaApiVersionInfo.getInfo());
        formatter.appendLine("Java-Version: " + System.getProperty("java.version"));
        formatter.appendLine("ORACLE_HOME: " + EnvironmentVariableUtil.getEnvValue("ORACLE_HOME"));
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

            logger.info("Successfully connected to database. UtPLSQL core: {}", compatibilityProxy.getVersionDescription());
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
     * @param fileMapperConfig
     * @param baseDir
     * @return FileMapperOptions or null
     */
    private FileMapperOptions getFileMapperOptionsByParamListItem(FileMapperConfig fileMapperConfig, File baseDir )
    {
        if (fileMapperConfig != null) {
            String sourcePath = fileMapperConfig.getPath();

            logger.debug("BaseDir: {}", baseDir);
            logger.debug("SourcePath: {}", sourcePath);

            List<String> files = new FileWalker().getFileList(baseDir, sourcePath);

            logger.debug("Getting FileMapperOptions - Files: ");
            files.forEach(logger::debug);

            FileMapperOptions options = new FileMapperOptions(files);
            options.setObjectOwner(fileMapperConfig.getOwner());
            options.setRegexPattern(fileMapperConfig.getRegexExpression());
            options.setTypeSubExpression(fileMapperConfig.getTypeSubexpression());
            options.setOwnerSubExpression(fileMapperConfig.getOwnerSubexpression());
            options.setNameSubExpression(fileMapperConfig.getNameSubexpression());

            List<KeyValuePair> mappings = new ArrayList<>();
            fileMapperConfig.getTypeMapping().forEach((k, v) -> mappings.add(new KeyValuePair(v, k)));
            options.setTypeMappings(mappings);

            return options;
        }

        return null;
    }

    /** Checks whether cli is compatible with the database framework
     *
     * @param conn Active Connection
     * @throws SQLException
     */
    private CompatibilityProxy checkFrameworkCompatibility(Connection conn) throws SQLException {

        CompatibilityProxy proxy = new CompatibilityProxy(conn, config.isSkipCompatibilityCheck());

        if ( !config.isSkipCompatibilityCheck() ) {
            proxy.failOnNotCompatible();
        }
        else {
            System.out.println("Skipping Compatibility check with framework version, expecting the latest version " +
                    "to be installed in database");
        }

        return proxy;
    }

    private ReporterManager getReporterManager() {
        if ( reporterManager == null ) {

            ReporterConfig[] reporterConfigs = config.getReporters();
            if ( reporterConfigs != null ) {
                ReporterOptions[] options = new ReporterOptions[reporterConfigs.length];
                boolean printToScreen = false;
                for (int i = 0; i<reporterConfigs.length; i++ ) {
                    options[i] = new ReporterOptions(
                            reporterConfigs[i].getName(),
                            reporterConfigs[i].getOutput());

                    options[i].forceOutputToScreen(reporterConfigs[i].isForceToScreen());

                    // Check printToScreen validity
                    if ( options[i].outputToScreen() && printToScreen )
                        throw new IllegalArgumentException("You cannot configure more than one reporter to output to screen");
                    printToScreen = options[i].outputToScreen();
                }
                reporterManager = new ReporterManager(options);
            }
        }

        return reporterManager;
    }

    List<ReporterOptions> getReporterOptionsList() {
        return getReporterManager().getReporterOptionsList();
    }
}
