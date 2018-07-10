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
    private final FileMapperConfig sourceMapping;
    private final FileMapperConfig testMapping;

    @ConstructorProperties({"connectString", "suitePaths", "reporters", "outputAnsiColor", "failureExitCode", "skipCompatibilityCheck", "includePackages", "excludePackages", "sourceMapping", "testMapping"})
    public TestRunnerConfig(String connectString, String[] suitePaths, ReporterConfig[] reporters, boolean outputAnsiColor, Integer failureExitCode, boolean skipCompatibilityCheck, String[] includePackages, String[] excludePackages, FileMapperConfig sourceMapping, FileMapperConfig testMapping) {
        super(connectString);
        this.suitePaths = suitePaths;
        this.reporters = reporters;
        this.outputAnsiColor = outputAnsiColor;
        this.failureExitCode = failureExitCode;
        this.skipCompatibilityCheck = skipCompatibilityCheck;
        this.includePackages = includePackages;
        this.excludePackages = excludePackages;
        this.sourceMapping = sourceMapping;
        this.testMapping = testMapping;
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

    public FileMapperConfig getSourceMapping() {
        return sourceMapping;
    }

    public FileMapperConfig getTestMapping() {
        return testMapping;
    }
}
