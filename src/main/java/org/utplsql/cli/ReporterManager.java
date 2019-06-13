package org.utplsql.cli;

import org.utplsql.api.compatibility.CompatibilityProxy;
import org.utplsql.api.reporter.CoreReporters;
import org.utplsql.api.reporter.Reporter;
import org.utplsql.api.reporter.ReporterFactory;
import org.utplsql.cli.config.ReporterConfig;
import org.utplsql.cli.reporters.ReporterOptionsAware;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

class ReporterManager {

    private List<ReporterOptions> reporterOptionsList;
    private List<Throwable> reporterGatherErrors;
    private ExecutorService executorService;

    ReporterManager(ReporterConfig[] reporterConfigs ) {
        reporterOptionsList = new ArrayList<>();
        if ( reporterConfigs != null && reporterConfigs.length > 0 ) {
            loadOptionsFromConfigs( reporterConfigs );
        }
        else {
            reporterOptionsList.add(getDefaultReporterOption());
        }
    }

    private void loadOptionsFromConfigs( ReporterConfig[] reporterConfigs ) {
        boolean printToScreen = false;
        for (ReporterConfig reporterConfig : reporterConfigs) {
            ReporterOptions option = new ReporterOptions(
                    reporterConfig.getName(),
                    reporterConfig.getOutput());

            option.forceOutputToScreen(reporterConfig.isForceToScreen());
            reporterOptionsList.add(option);

            // Check printToScreen validity
            if (option.outputToScreen() && printToScreen)
                throw new IllegalArgumentException("You cannot configure more than one reporter to output to screen");
            printToScreen = option.outputToScreen();
        }
    }

    private ReporterOptions getDefaultReporterOption() {
        return new ReporterOptions(CoreReporters.UT_DOCUMENTATION_REPORTER.name());
    }

    private void abortGathering(Throwable e) {
        addGatherError(e);
        executorService.shutdownNow();
    }

    private void addGatherError( Throwable e ) {
        if ( reporterGatherErrors == null ) {
            reporterGatherErrors = new ArrayList<>();
        }
        reporterGatherErrors.add(e);
    }

    boolean haveGatherErrorsOccured() {
        return reporterGatherErrors != null && !reporterGatherErrors.isEmpty();
    }

    List<Throwable> getGatherErrors() {
        return reporterGatherErrors;
    }

    /** Initializes the reporters so we can use the id to gather results
     *
     * @param conn Active Connection
     * @return List of Reporters
     * @throws SQLException
     */
    List<Reporter> initReporters(Connection conn, ReporterFactory reporterFactory, CompatibilityProxy compatibilityProxy) throws SQLException
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
     * @param dataSource
     */
    void startReporterGatherers(ExecutorService executorService, final DataSource dataSource)
    {
        if ( this.executorService != null && !this.executorService.isShutdown())
            throw new IllegalStateException("There is already a running executor service!");

        this.executorService = executorService;

        reporterOptionsList.forEach((reporterOption) -> executorService.submit(
                new GatherReporterOutputTask(dataSource, reporterOption, this::abortGathering)
        ));
    }

    List<ReporterOptions> getReporterOptionsList() {
        return reporterOptionsList;
    }

    int getNumberOfReporters() { return reporterOptionsList.size(); }

    /** Gathers Reporter Output based on ReporterOptions and prints it to a file, System.out or both
     */
    private static class GatherReporterOutputTask implements Runnable {

        private DataSource dataSource;
        private ReporterOptions option;
        private Consumer<Throwable> abortFunction;

        GatherReporterOutputTask( DataSource dataSource, ReporterOptions reporterOption, Consumer<Throwable> abortFunction ) {

            if ( reporterOption.getReporterObj() == null )
                throw new IllegalArgumentException("Reporter " + reporterOption.getReporterName() + " is not initialized");

            this.dataSource = dataSource;
            this.option = reporterOption;
            this.abortFunction = abortFunction;
        }

        @Override
        public void run() {
            List<PrintStream> printStreams = new ArrayList<>();
            PrintStream fileOutStream = null;

            try (Connection conn = dataSource.getConnection()) {
                if (option.outputToScreen()) {
                    printStreams.add(System.out);
                    option.getReporterObj().getOutputBuffer().setFetchSize(1);
                }

                if (option.outputToFile()) {
                    fileOutStream = new PrintStream(new FileOutputStream(option.getOutputFileName()));
                    printStreams.add(fileOutStream);
                }

                option.getReporterObj().getOutputBuffer().printAvailable(conn, printStreams);
            } catch (SQLException | FileNotFoundException e) {
                abortFunction.accept(e);
            } finally {
                if (fileOutStream != null)
                    fileOutStream.close();
            }
        }
    }
}
