package io.github.utplsql.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.github.utplsql.api.OutputBuffer;
import io.github.utplsql.api.OutputBufferLines;
import io.github.utplsql.api.TestRunner;
import io.github.utplsql.api.types.BaseReporter;
import io.github.utplsql.api.types.DocumentationReporter;
import io.github.utplsql.api.utPLSQL;

import java.sql.Connection;
import java.sql.SQLException;
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
            description = "user/pass@[[host][:port]/]db")
    private List<ConnectionInfo> connectionInfoList;

    @Parameter(
            names = {"-p", "--path"},
            description = "run suites/tests by path, format: \n" +
                    "schema or schema:[suite ...][.test] or schema[.suite ...][.test]")
    private List<String> testPaths;

    public ConnectionInfo getConnectionInfo() {
        return connectionInfoList.get(0);
    }

    public String getTestPaths() {
//        if (testPaths != null && testPaths.size() > 1)
//            throw new RuntimeException("Multiple test paths not supported yet.");

        return (testPaths == null) ? null : String.join(",", testPaths);
    }

    public void run() throws Exception {
        ConnectionInfo ci = getConnectionInfo();
        System.out.println("Running Tests For: " + ci.toString());

        utPLSQL.init(ci.getConnectionUrl(), ci.getUser(), ci.getPassword());

        String tempTestPaths = getTestPaths();
        if (tempTestPaths == null) tempTestPaths = ci.getUser();

        final BaseReporter reporter = createDocumentationReporter();
        final String testPaths = tempTestPaths;

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            Connection conn = null;
            try {
                conn = utPLSQL.getConnection();
                new TestRunner().run(conn, testPaths, reporter);

                OutputBufferLines outputLines = new OutputBuffer(reporter.getReporterId())
                        .fetchAll(conn);

                if (outputLines.getLines().size() > 0)
                    System.out.println(outputLines.toString());
            } catch (SQLException e) {
                // TODO
                e.printStackTrace();
            } finally {
                if (conn != null)
                    try { conn.close(); } catch (SQLException ignored) {}
            }
        });

//        executorService.submit(() -> {
//            Connection conn = null;
//            try {
//                conn = utPLSQL.getConnection();
//                OutputBufferLines outputLines;
//                do {
//                    outputLines = new OutputBuffer(reporter.getReporterId())
//                            .fetchAvailable(conn);
//
//                    Thread.sleep(500);
//
//                    if (outputLines.getLines().size() > 0)
//                        System.out.println(outputLines.toString());
//                } while (!outputLines.isFinished());
//            } catch (SQLException | InterruptedException e) {
//                // TODO
//                e.printStackTrace();
//            } finally {
//                if (conn != null)
//                    try { conn.close(); } catch (SQLException ignored) {}
//            }
//        });

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.MINUTES);
    }

    private BaseReporter createDocumentationReporter() throws SQLException {
        Connection conn = null;
        try {
            conn = utPLSQL.getConnection();
            BaseReporter reporter = new DocumentationReporter();
            reporter.setReporterId(utPLSQL.newSysGuid(conn));
            return reporter;
        } finally {
            if (conn != null)
                try { conn.close(); } catch (SQLException ignored) {}
        }
    }

}
