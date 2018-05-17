package org.utplsql.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.utplsql.api.FileMapperOptions;
import org.utplsql.api.KeyValuePair;
import org.utplsql.api.TestRunner;
import org.utplsql.api.Version;
import org.utplsql.api.compatibility.CompatibilityProxy;
import org.utplsql.api.exception.SomeTestsFailedException;
import org.utplsql.api.reporter.Reporter;
import org.utplsql.api.reporter.ReporterFactory;
import org.utplsql.cli.exception.DatabaseConnectionFailed;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by vinicius.moreira on 19/04/2017.
 *
 * @author vinicious moreira
 * @author pesse
 */
@Parameters(separators = "=", commandDescription = "run tests")
public class RunCommand {

    @Parameter(
            required = true,
            converter = ConnectionInfo.ConnectionStringConverter.class,
            arity = 1,
            description = "<user>/<password>@//<host>[:<port>]/<service> OR <user>/<password>@<TNSName> OR <user>/<password>@<host>:<port>:<SID>")
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


    private CompatibilityProxy compatibilityProxy;
    private ReporterFactory reporterFactory;
    private ReporterManager reporterManager;

    public ConnectionInfo getConnectionInfo() {
        return connectionInfoList.get(0);
    }

    public List<String> getTestPaths() {
        return testPaths;
    }

    public int run() throws Exception {

        RunCommandChecker.checkOracleJDBCExists();


        final List<Reporter> reporterList;
        final List<String> testPaths = getTestPaths();

        final File baseDir = new File("").getAbsoluteFile();
        final FileMapperOptions[] sourceMappingOptions = {null};
        final FileMapperOptions[] testMappingOptions = {null};

        final int[] returnCode = {0};

        sourceMappingOptions[0] = getFileMapperOptionsByParamListItem(this.sourcePathParams, baseDir);
        testMappingOptions[0] = getFileMapperOptionsByParamListItem(this.testPathParams, baseDir);

        ArrayList<String> includeObjectsList;
        ArrayList<String> excludeObjectsList;

        if (includeObjects != null && !includeObjects.isEmpty()) {
            includeObjectsList = new ArrayList<>(Arrays.asList(includeObjects.split(",")));
        } else {
            includeObjectsList = new ArrayList<>();
        }

        if (excludeObjects != null && !excludeObjects.isEmpty()) {
            excludeObjectsList = new ArrayList<>(Arrays.asList(excludeObjects.split(",")));
        } else {
            excludeObjectsList = new ArrayList<>();
        }

        final ArrayList<String> finalIncludeObjectsList = includeObjectsList;
        final ArrayList<String> finalExcludeObjectsList = excludeObjectsList;

        final ConnectionInfo ci = getConnectionInfo();
        ci.setMaxConnections(getReporterManager().getNumberOfReporters()+1);

        // Do the reporters initialization, so we can use the id to run and gather results.
        try (Connection conn = ci.getConnection()) {

            // Check if orai18n exists if database version is 11g
            RunCommandChecker.checkOracleI18nExists(ci.getOracleDatabaseVersion(conn));

            // First of all do a compatibility check and fail-fast
            compatibilityProxy = checkFrameworkCompatibility(conn);
            reporterFactory = ReporterFactoryProvider.createReporterFactory(compatibilityProxy);

            reporterList = getReporterManager().initReporters(conn, reporterFactory, compatibilityProxy);

        } catch (SQLException e) {
            if ( e.getErrorCode() == 1017 || e.getErrorCode() == 12514 ) {
                throw new DatabaseConnectionFailed(e);
            }
            else {
                throw e;
            }
        }

        // Output a message if --failureExitCode is set but database framework is not capable of
        String msg = RunCommandChecker.getCheckFailOnErrorMessage(failureExitCode, compatibilityProxy.getDatabaseVersion());
        if ( msg != null ) {
            System.out.println(msg);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(1 + reporterList.size());

        // Run tests.
        executorService.submit(() -> {
            try (Connection conn = ci.getConnection()) {
                TestRunner testRunner = new TestRunner()
                        .addPathList(testPaths)
                        .addReporterList(reporterList)
                        .sourceMappingOptions(sourceMappingOptions[0])
                        .testMappingOptions(testMappingOptions[0])
                        .colorConsole(this.colorConsole)
                        .failOnErrors(true)
                        .skipCompatibilityCheck(skipCompatibilityCheck)
                        .includeObjects(finalIncludeObjectsList)
                        .excludeObjects(finalExcludeObjectsList);

                testRunner.run(conn);
            } catch (SomeTestsFailedException e) {
                returnCode[0] = this.failureExitCode;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                returnCode[0] = Cli.DEFAULT_ERROR_CODE;
                executorService.shutdownNow();
            }
        });

        // Gather each reporter results on a separate thread.
        getReporterManager().startReporterGatherers(executorService, ci, returnCode);

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.MINUTES);
        return returnCode[0];
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

    public FileMapperOptions getMapperOptions(List<String> mappingParams, List<String> filePaths) {
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

    /** Returns the version of the database framework if available
     *
     * @return
     */
    public Version getDatabaseVersion() {
        if ( compatibilityProxy != null )
            return compatibilityProxy.getDatabaseVersion();

        return null;
    }

    private ReporterManager getReporterManager() {
        if ( reporterManager == null )
            reporterManager = new ReporterManager(reporterParams);

        return reporterManager;
    }

    public List<ReporterOptions> getReporterOptionsList() {
        return getReporterManager().getReporterOptionsList();
    }
}
