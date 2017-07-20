package io.github.utplsql.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.github.utplsql.api.CustomTypes;
import io.github.utplsql.api.OutputBuffer;
import io.github.utplsql.api.TestRunner;
import io.github.utplsql.api.exception.SomeTestsFailedException;
import io.github.utplsql.api.reporter.Reporter;
import io.github.utplsql.api.reporter.ReporterFactory;

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
            description = "run suites/tests by path, format: \n" +
                    "-p=[schema|schema:[suite ...][.test]|schema[.suite ...][.test]")
    private List<String> testPaths = new ArrayList<>();

    @Parameter(
            names = {"-f", "--format"},
            variableArity = true,
            description = "output reporter format: \n" +
                    "enables specified format reporting to specified output file (-o) and to screen (-s)\n" +
                    "-f=reporter_name [-o=output_file [-s]]")
    private List<String> reporterParams = new ArrayList<>();

    @Parameter(
            names = {"-c", "--color"},
            description = "enables printing of test results in colors as defined by ANSICONSOLE standards")
    private boolean colorConsole = false;

    @Parameter(
            names = {"--no-failure"},
            description = "will exit with 0 even if tests failed, default false")
    private boolean noFailure = false;

    @Parameter(names = {"-source_path"}, description = "path to project source files")
    private String sourcePath;

    @Parameter(names = {"-test_path"}, description = "path to project test files")
    private String testPath;

    public ConnectionInfo getConnectionInfo() {
        return connectionInfoList.get(0);
    }

    public List<String> getTestPaths() {
        return testPaths;
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

    public int run() throws Exception {
        final ConnectionInfo ci = getConnectionInfo();

        final List<ReporterOptions> reporterOptionsList = getReporterOptionsList();
        final List<String> testPaths = getTestPaths();
        final List<Reporter> reporterList = new ArrayList<>();

        final File baseDir = new File("").getAbsoluteFile();
        List<String> sourceFilesTmp = null;
        List<String> testFilesTmp = null;

        if (this.sourcePath != null)
            sourceFilesTmp = new FileWalker().getFileList(baseDir, this.sourcePath);

        if (this.testPath != null)
            testFilesTmp = new FileWalker().getFileList(baseDir, this.testPath);

        final List<String> sourceFiles = sourceFilesTmp;
        final List<String> testFiles = testFilesTmp;
        final int[] returnCode = {0};

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
            // TODO
            e.printStackTrace();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(1 + reporterList.size());

        // Run tests.
        executorService.submit(() -> {
            try (Connection conn = ci.getConnection()) {
                new TestRunner()
                        .addPathList(testPaths)
                        .addReporterList(reporterList)
                        .withSourceFiles(sourceFiles)
                        .withTestFiles(testFiles)
                        .colorConsole(this.colorConsole)
                        .failOnErrors(!this.noFailure)
                        .run(conn);
            } catch (SomeTestsFailedException e) {
                returnCode[0] = -1;
            } catch (SQLException e) {
                // TODO
                e.printStackTrace();
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
                    // TODO
                    e.printStackTrace();
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

}
