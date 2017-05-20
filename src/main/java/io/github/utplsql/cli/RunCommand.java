package io.github.utplsql.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.github.utplsql.api.OutputBuffer;
import io.github.utplsql.api.TestRunner;
import io.github.utplsql.api.types.BaseReporter;
import io.github.utplsql.api.types.CustomTypes;

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
                    "-p schema or schema:[suite ...][.test] or schema[.suite ...][.test]")
    private List<String> testPaths = new ArrayList<>();

    @Parameter(
            names = {"-f", "--format"},
            variableArity = true,
            description = "output reporter format: \n" +
                    "-f reporter_name [output_file] [console_output]")
    private List<String> reporterParams = new ArrayList<>();

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

    public void run() throws Exception {
        final ConnectionInfo ci = getConnectionInfo();
        System.out.println("Running Tests For: " + ci.toString());

        final List<ReporterOptions> reporterOptionsList = getReporterOptionsList();
        final List<BaseReporter> reporterList = new ArrayList<>();
        final List<String> testPaths = getTestPaths();

        if (testPaths.isEmpty()) testPaths.add(ci.getUser());

        // Do the reporters initialization, so we can use the id to run and gather results.
        try (Connection conn = ci.getConnection()) {
            for (ReporterOptions ro : reporterOptionsList) {
                BaseReporter reporter = CustomTypes.createReporter(ro.getReporterName());
                reporter.init(conn);
                ro.setReporterObj(reporter);
                reporterList.add(reporter);
            }
        } catch (SQLException e) {
            // TODO
            e.printStackTrace();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(1 + reporterList.size());

        executorService.submit(() -> {
            try (Connection conn = ci.getConnection()){
                new TestRunner().run(conn, testPaths, reporterList);
            } catch (SQLException e) {
                // TODO
                e.printStackTrace();
            }
        });


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
    }

}
