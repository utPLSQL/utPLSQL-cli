package org.utplsql.cli;

import org.utplsql.api.reporter.Reporter;

/**
 * Created by Vinicius on 20/05/2017.
 */
public class ReporterOptions {

    private String reporterName;
    private String outputFileName;
    private boolean outputToScreen;
    private boolean forceOutputToScreen;

    private Reporter reporterObj = null;

    public ReporterOptions(String reporterName, String outputFileName) {
        setReporterName(reporterName);
        setOutputFileName(outputFileName);
        this.outputToScreen = (outputFileName == null); // If outputFileName is null we assume it should be sent to screen
        this.forceOutputToScreen = false;
    }

    public ReporterOptions(String reporterName) {
        this(reporterName, null);
    }

    public Reporter getReporterObj() {
        return reporterObj;
    }

    public void setReporterObj(Reporter reporterObj) {
        this.reporterObj = reporterObj;
    }

    public String getReporterName() {
        return reporterName.toUpperCase();
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
        this.outputToScreen = false;
    }

    public boolean outputToFile() {
        return outputFileName != null && !outputFileName.isEmpty();
    }

    public boolean outputToScreen() {
        return outputToScreen || forceOutputToScreen;
    }

    public void forceOutputToScreen(boolean outputToScreen) {
        this.forceOutputToScreen = outputToScreen;
    }

}
