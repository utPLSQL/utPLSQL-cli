package io.github.utplsql.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.github.utplsql.api.OutputBuffer;
import io.github.utplsql.api.TestRunner;
import io.github.utplsql.api.types.BaseReporter;
import io.github.utplsql.api.types.CustomTypes;
import io.github.utplsql.api.types.DocumentationReporter;

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

    public String getTestPaths() {
//        if (testPaths != null && testPaths.size() > 1)
//            throw new RuntimeException("Multiple test paths not supported yet.");

        return (testPaths == null) ? null : String.join(",", testPaths);
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
            reporterOptionsList.add(new ReporterOptions(CustomTypes.UT_DOCUMENTATION_REPORTER.getName()));
        }

        return reporterOptionsList;
    }

    public void run() throws Exception {
        final ConnectionInfo ci = getConnectionInfo();
        System.out.println("Running Tests For: " + ci.toString());

        String tempTestPaths = getTestPaths();
        if (tempTestPaths == null) tempTestPaths = ci.getUser();

        final BaseReporter reporter = new DocumentationReporter();
        final String testPaths = tempTestPaths;

        try (Connection conn = ci.getConnection()) {
            reporter.init(conn);
        } catch (SQLException e) {
            // TODO
            e.printStackTrace();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            try (Connection conn = ci.getConnection()){
                new TestRunner().run(conn, testPaths, reporter);
            } catch (SQLException e) {
                // TODO
                e.printStackTrace();
            }
        });

        executorService.submit(() -> {
            try (Connection conn = ci.getConnection()){
                new OutputBuffer(reporter).printAvailable(conn, System.out);
            } catch (SQLException e) {
                // TODO
                e.printStackTrace();
            }
        });

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.MINUTES);
    }

}
