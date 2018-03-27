package org.utplsql.cli;

import org.utplsql.api.compatibility.CompatibilityProxy;
import org.utplsql.api.reporter.CoreReporters;
import org.utplsql.api.reporter.Reporter;
import org.utplsql.api.reporter.ReporterFactory;
import org.utplsql.cli.reporters.ReporterOptionsAware;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

class ReporterManager {

    private List<ReporterOptions> reporterOptionsList;

    ReporterManager(List<String> reporterParams ) {
        initReporterOptionsList(reporterParams);
    }

    private void initReporterOptionsList( List<String> reporterParams ) {
        reporterOptionsList = new ArrayList<>();
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
            reporterOptionsList.add(new ReporterOptions(CoreReporters.UT_DOCUMENTATION_REPORTER.name()));
        }
    }


    /** Initializes the reporters so we can use the id to gather results
     *
     * @param conn Active Connection
     * @return List of Reporters
     * @throws SQLException
     */
    public List<Reporter> initReporters(Connection conn, ReporterFactory reporterFactory, CompatibilityProxy compatibilityProxy) throws SQLException
    {
        final List<Reporter> reporterList = new ArrayList<>();

        for (ReporterOptions ro : reporterOptionsList) {
            Reporter reporter = reporterFactory.createReporter(ro.getReporterName());

            if ( reporter instanceof ReporterOptionsAware)
                ((ReporterOptionsAware) reporter).setReporterOptions(ro);

            reporter.init(conn, compatibilityProxy, reporterFactory);

            ro.setReporterObj(reporter);
            reporterList.add(reporter);
        }

        return reporterList;
    }

    /** Starts a separate thread for each Reporter to gather its results
     *
     * @param executorService
     * @param ci
     * @param returnCode
     */
    public void startReporterGatherers(ExecutorService executorService, final ConnectionInfo ci, final int[] returnCode)
    {
        // TODO: Implement Init-check
        // Gather each reporter results on a separate thread.
        for (ReporterOptions ro : reporterOptionsList) {
            executorService.submit(() -> {
                List<PrintStream> printStreams = new ArrayList<>();
                PrintStream fileOutStream = null;

                try (Connection conn = ci.getConnection()) {
                    if (ro.outputToScreen()) {
                        printStreams.add(System.out);
                        ro.getReporterObj().getOutputBuffer().setFetchSize(1);
                    }

                    if (ro.outputToFile()) {
                        fileOutStream = new PrintStream(new FileOutputStream(ro.getOutputFileName()));
                        printStreams.add(fileOutStream);
                    }

                    ro.getReporterObj().getOutputBuffer().printAvailable(conn, printStreams);
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
    }

    public List<ReporterOptions> getReporterOptionsList() {
        return reporterOptionsList;
    }
}
