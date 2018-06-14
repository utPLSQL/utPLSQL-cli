package org.utplsql.cli.config;


import java.beans.ConstructorProperties;

public class TestRunnerConfig extends ConnectionConfig {

    private final String[] suitePaths;
    private final ReporterConfig[] reporters;
    private boolean outputAnsiColor = false;
    private final Integer failureExitCode;
    private boolean skipCompatibilityCheck = false;
    private final String[] includePackages;
    private final String[] excludePackages;
    private final String sourcePath;
    private final String testPath;

    @ConstructorProperties({"connectString", "suitePaths", "reporters", "outputAnsiColor", "failureExitCode", "skipCompatibilityCheck", "includePackages", "excludePackages", "sourcePath", "testPath"})
    public TestRunnerConfig(String connectString, String[] suitePaths, ReporterConfig[] reporters, boolean outputAnsiColor, Integer failureExitCode, boolean skipCompatibilityCheck, String[] includePackages, String[] excludePackages, String sourcePath, String testPath) {
        super(connectString);
        this.suitePaths = suitePaths;
        this.reporters = reporters;
        this.outputAnsiColor = outputAnsiColor;
        this.failureExitCode = failureExitCode;
        this.skipCompatibilityCheck = skipCompatibilityCheck;
        this.includePackages = includePackages;
        this.excludePackages = excludePackages;
        this.sourcePath = sourcePath;
        this.testPath = testPath;
    }

    public String[] getSuitePaths() {
        return suitePaths;
    }

    public ReporterConfig[] getReporters() {
        return reporters;
    }

    public boolean isOutputAnsiColor() {
        return outputAnsiColor;
    }

    public Integer getFailureExitCode() {
        return failureExitCode;
    }

    public boolean isSkipCompatibilityCheck() {
        return skipCompatibilityCheck;
    }

    public String[] getIncludePackages() {
        return includePackages;
    }

    public String[] getExcludePackages() {
        return excludePackages;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getTestPath() {
        return testPath;
    }
}
