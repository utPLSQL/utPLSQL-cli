package org.utplsql.cli.config;


import org.utplsql.cli.LoggerConfiguration.ConfigLevel;

import java.beans.ConstructorProperties;

public class RunCommandConfig extends ConnectionConfig {

    private final String[] suitePaths;
    private final ReporterConfig[] reporters;
    private boolean outputAnsiColor = false;
    private final Integer failureExitCode;
    private boolean skipCompatibilityCheck = false;
    private final String[] includePackages;
    private final String[] excludePackages;
    private final FileMapperConfig sourceMapping;
    private final FileMapperConfig testMapping;
    private final ConfigLevel logConfigLevel;
    private final Integer timeoutInMinutes;
    private boolean dbmsOutput = false;
    private boolean randomTestOrder = false;
    private final Integer randomTestOrderSeed;

    @ConstructorProperties({"connectString", "suitePaths", "reporters", "outputAnsiColor", "failureExitCode", "skipCompatibilityCheck", "includePackages", "excludePackages", "sourceMapping", "testMapping", "logConfigLevel", "timeoutInMinutes", "dbmsOutput", "randomTestOrder", "randomTestOrderSeed"})
    public RunCommandConfig(String connectString, String[] suitePaths, ReporterConfig[] reporters, boolean outputAnsiColor, Integer failureExitCode, boolean skipCompatibilityCheck, String[] includePackages, String[] excludePackages, FileMapperConfig sourceMapping, FileMapperConfig testMapping, ConfigLevel logConfigLevel, Integer timeoutInMinutes, boolean dbmsOutput, boolean randomTestOrder, Integer randomTestOrderSeed) {
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
        this.logConfigLevel = logConfigLevel;
        this.timeoutInMinutes = timeoutInMinutes;
        this.dbmsOutput = dbmsOutput;
        this.randomTestOrder = randomTestOrder;
        this.randomTestOrderSeed = randomTestOrderSeed;
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

    public ConfigLevel getLogConfigLevel() {
        return logConfigLevel;
    }

    public Integer getTimeoutInMinutes() {
        return timeoutInMinutes;
    }

    public boolean isDbmsOutput() {
        return dbmsOutput;
    }

    public boolean isRandomTestOrder() {
        return randomTestOrder;
    }

    public Integer getRandomTestOrderSeed() {
        return randomTestOrderSeed;
    }
}
