package org.utplsql.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.utplsql.api.*;
import org.utplsql.api.exception.SomeTestsFailedException;
import org.utplsql.api.reporter.Reporter;
import org.utplsql.api.reporter.ReporterFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by vinicius.moreira on 19/04/2017.
 */
@Parameters(separators = "=", commandDescription = "run tests")
public class RunCommand {

    @Parameter(
            required = true, converter = ConnectionStringConverter.class,
            arity = 1,
            description = "user/pass@[[host][:port]/]db")
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

    public ConnectionInfo getConnectionInfo() {
        return connectionInfoList.get(0);
    }

    public List<String> getTestPaths() {
        return testPaths;
    }

    public int run() throws Exception {
        final ConnectionInfo ci = getConnectionInfo();

        final List<ReporterOptions> reporterOptionsList = getReporterOptionsList();
        final List<String> testPaths = getTestPaths();
        final List<Reporter> reporterList = new ArrayList<>();

        final File baseDir = new File("").getAbsoluteFile();
        final FileMapperOptions[] sourceMappingOptions = {null};
        final FileMapperOptions[] testMappingOptions = {null};

        final int[] returnCode = {0};

        if (!this.sourcePathParams.isEmpty()) {
            String sourcePath = this.sourcePathParams.get(0);
            List<String> sourceFiles = new FileWalker().getFileList(baseDir, sourcePath);
            sourceMappingOptions[0] = getMapperOptions(this.sourcePathParams, sourceFiles);
        }

        if (!this.testPathParams.isEmpty()) {
            String testPath = this.testPathParams.get(0);
            List<String> testFiles = new FileWalker().getFileList(baseDir, testPath);
            testMappingOptions[0] = getMapperOptions(this.testPathParams, testFiles);
        }

        if (testPaths.isEmpty()) testPaths.add(ci.getUser());

        // Do the reporters initialization, so we can use the id to run and gather results.
        try (Connection conn = ci.getConnection()) {
            for (ReporterOptions ro : reporterOptionsList) {
                Reporter reporter = ReporterFactory.createReporter(ro.getReporterName());
                reporter.init(conn);
                ro.setReporterObj(reporter);
                reporterList.add(reporter);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return Cli.DEFAULT_ERROR_CODE;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(1 + reporterList.size());

        // Run tests.
        executorService.submit(() -> {
            try (Connection conn = ci.getConnection()) {
                new TestRunner()
                        .addPathList(testPaths)
                        .addReporterList(reporterList)
                        .sourceMappingOptions(sourceMappingOptions[0])
                        .testMappingOptions(testMappingOptions[0])
                        .colorConsole(this.colorConsole)
                        .failOnErrors(true)
                        .run(conn);
            } catch (SomeTestsFailedException e) {
                returnCode[0] = this.failureExitCode;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                returnCode[0] = Cli.DEFAULT_ERROR_CODE;
                executorService.shutdownNow();
            }
        });

        // Gather each reporter results on a separate thread.
        for (ReporterOptions ro : reporterOptionsList) {
            executorService.submit(() -> {
                List<PrintStream> printStreams = new ArrayList<>();
                PrintStream fileOutStream = null;

                try (Connection conn = ci.getConnection()) {
                    if (ro.outputToScreen()) {
                        printStreams.add(System.out);
                    }

                    if (ro.outputToFile()) {
                        fileOutStream = new PrintStream(new FileOutputStream(ro.getOutputFileName()));
                        printStreams.add(fileOutStream);
                    }

                    new OutputBuffer(ro.getReporterObj()).printAvailable(conn, printStreams);
                } catch (SQLException | FileNotFoundException e) {
                    System.out.println(e.getMessage());
                    returnCode[0] = Cli.DEFAULT_ERROR_CODE;
                    executorService.shutdownNow();
                } finally {
                    if (fileOutStream != null)
                        fileOutStream.close();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.MINUTES);
        return returnCode[0];
    }

    public List<ReporterOptions> getReporterOptionsList() {
        List<ReporterOptions> reporterOptionsList = new ArrayList<>();
        ReporterOptions reporterOptions = null;

        for (String p : reporterParams) {
            if (reporterOptions == null || !p.startsWith("-")) {
                reporterOptions = new ReporterOptions(p);
                reporterOptionsList.add(reporterOptions);
            }
            else
            if (p.startsWith("-o=")) {
                reporterOptions.setOutputFileName(p.substring(3));
            }
            else
            if (p.equals("-s")) {
                reporterOptions.forceOutputToScreen(true);
            }
        }

        // If no reporter parameters were passed, use default reporter.
        if (reporterOptionsList.isEmpty()) {
            reporterOptionsList.add(new ReporterOptions(CustomTypes.UT_DOCUMENTATION_REPORTER));
        }

        return reporterOptionsList;
    }

    public FileMapperOptions getMapperOptions(List<String> mappingParams, List<String> filePaths) {
        FileMapperOptions mapperOptions = new FileMapperOptions(filePaths);

        for (String p : mappingParams) {
            if (p.startsWith("-object_owner=")) {
                mapperOptions.setObjectOwner(p.substring("-object_owner=".length()));
            }
            else
            if (p.startsWith("-regex_pattern=")) {
                mapperOptions.setRegexPattern(p.substring("-regex_pattern=".length()));
            }
            else
            if (p.startsWith("-type_mapping=")) {
                String typeMappingsParam = p.substring("-type_mapping=".length());

                List<KeyValuePair> typeMappings = new ArrayList<>();
                for (String mapping : typeMappingsParam.split("/")) {
                    String[] values = mapping.split("=");
                    typeMappings.add(new KeyValuePair(values[0], values[1]));
                }

                mapperOptions.setTypeMappings(typeMappings);
            }
            else
            if (p.startsWith("-owner_subexpression=")) {
                mapperOptions.setOwnerSubExpression(Integer.parseInt(p.substring("-owner_subexpression=".length())));
            }
            else
            if (p.startsWith("-name_subexpression=")) {
                mapperOptions.setNameSubExpression(Integer.parseInt(p.substring("-name_subexpression=".length())));
            }
            else
            if (p.startsWith("-type_subexpression=")) {
                mapperOptions.setTypeSubExpression(Integer.parseInt(p.substring("-type_subexpression=".length())));
            }
        }

        if (mapperOptions.getRegexPattern() == null || mapperOptions.getRegexPattern().isEmpty())
            return null;
        else
            return mapperOptions;
    }

}
