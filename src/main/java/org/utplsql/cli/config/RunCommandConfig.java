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
    private final String[] tags;
    private final String[] coverageSchemes;

    @ConstructorProperties({"connectString", "suitePaths", "reporters", "outputAnsiColor", "failureExitCode", "skipCompatibilityCheck", "includePackages", "excludePackages", "sourceMapping", "testMapping", "logConfigLevel", "timeoutInMinutes", "dbmsOutput", "randomTestOrder", "randomTestOrderSeed", "tags", "coverageSchemes"})
    public RunCommandConfig(String connectString, String[] suitePaths, ReporterConfig[] reporters, boolean outputAnsiColor, Integer failureExitCode, boolean skipCompatibilityCheck, String[] includePackages, String[] excludePackages, FileMapperConfig sourceMapping, FileMapperConfig testMapping, ConfigLevel logConfigLevel, Integer timeoutInMinutes, boolean dbmsOutput, boolean randomTestOrder, Integer randomTestOrderSeed, String[] tags, String[] coverageSchemes) {
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
        this.tags = tags;
        this.coverageSchemes = coverageSchemes;
    }

    public String[] getSuitePaths() {
        return suitePaths;
    }

    public String[] getTags() {
        return tags;
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

    public String[] getCoverageSchemes() {
        return coverageSchemes;
    }

    public static class Builder {

        private String connectString;
        private String[] suitePaths;
        private ReporterConfig[] reporters;
        private boolean outputAnsiColor;
        private Integer failureExitCode;
        private boolean skipCompatibilityCheck;
        private String[] includePackages;
        private String[] excludePackages;
        private FileMapperConfig sourceMapping;
        private FileMapperConfig testMapping;
        private ConfigLevel logConfigLevel;
        private Integer timeoutInMinutes;
        private boolean dbmsOutput;
        private boolean randomTestOrder;
        private Integer randomTestOrderSeed;
        private String[] tags;
        private String[] coverageSchemes;

        public Builder connectString(String connectString) {
            this.connectString = connectString;
            return this;
        }

        public Builder suitePaths(String[] suitePaths) {
            this.suitePaths = suitePaths;
            return this;
        }

        public Builder reporters(ReporterConfig[] reporters) {
            this.reporters = reporters;
            return this;
        }

        public Builder outputAnsiColor(boolean outputAnsiColor) {
            this.outputAnsiColor = outputAnsiColor;
            return this;
        }

        public Builder failureExitCode(Integer failureExitCode) {
            this.failureExitCode = failureExitCode;
            return this;
        }

        public Builder skipCompatibilityCheck(boolean skipCompatibilityCheck) {
            this.skipCompatibilityCheck = skipCompatibilityCheck;
            return this;
        }

        public Builder includePackages(String[] includePackages) {
            this.includePackages = includePackages;
            return this;
        }

        public Builder excludePackages(String[] excludePackages) {
            this.excludePackages = excludePackages;
            return this;
        }

        public Builder sourceMapping(FileMapperConfig sourceMapping) {
            this.sourceMapping = sourceMapping;
            return this;
        }

        public Builder testMapping(FileMapperConfig testMapping) {
            this.testMapping = testMapping;
            return this;
        }

        public Builder logConfigLevel(ConfigLevel logConfigLevel) {
            this.logConfigLevel = logConfigLevel;
            return this;
        }

        public Builder timeoutInMinutes(Integer timeoutInMinutes) {
            this.timeoutInMinutes = timeoutInMinutes;
            return this;
        }

        public Builder dbmsOutput(boolean dbmsOutput) {
            this.dbmsOutput = dbmsOutput;
            return this;
        }

        public Builder randomTestOrder(boolean randomTestOrder) {
            this.randomTestOrder = randomTestOrder;
            return this;
        }

        public Builder randomTestOrderSeed(Integer randomTestOrderSeed) {
            this.randomTestOrderSeed = randomTestOrderSeed;
            return this;
        }

        public Builder tags(String[] tags) {
            this.tags = tags;
            return this;
        }

        public Builder coverageSchemes(String[] coverageSchemes) {
            this.coverageSchemes = coverageSchemes;
            return this;
        }

        public RunCommandConfig create() {
            return new RunCommandConfig(connectString, suitePaths, reporters, outputAnsiColor, failureExitCode, skipCompatibilityCheck, includePackages, excludePackages, sourceMapping, testMapping, logConfigLevel, timeoutInMinutes, dbmsOutput, randomTestOrder, randomTestOrderSeed, tags, coverageSchemes);
        }
    }
}
