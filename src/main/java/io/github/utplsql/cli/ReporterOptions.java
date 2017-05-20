package io.github.utplsql.cli;

import com.sun.istack.internal.NotNull;

/**
 * Created by Vinicius on 20/05/2017.
 */
public class ReporterOptions {

    private String reporterName;
    private String outputFileName;
    private boolean outputToScreen;
    private boolean forceOutputToScreen;

    public ReporterOptions(String reporterName, String outputFileName, boolean outputToScreen) {
        setReporterName(reporterName);
        setOutputFileName(outputFileName);
        this.outputToScreen = outputToScreen;
        this.forceOutputToScreen = false;
    }

    public ReporterOptions(String reporterName) {
        this(reporterName, null, true);
    }

    public String getReporterName() {
        return reporterName;
    }

    @NotNull
    public void setReporterName(String reporterName) {
        this.reporterName = reporterName.toUpperCase();
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
