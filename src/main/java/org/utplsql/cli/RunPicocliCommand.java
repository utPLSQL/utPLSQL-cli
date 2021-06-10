package org.utplsql.cli;

import org.utplsql.api.TestRunner;
import org.utplsql.api.reporter.Reporter;
import org.utplsql.cli.config.FileMapperConfig;
import org.utplsql.cli.config.ReporterConfig;
import org.utplsql.cli.config.RunCommandConfig;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.*;

@Command(name = "run", description = "run tests")
public class RunPicocliCommand implements IRunCommand {

    @Parameters(description = UtplsqlPicocliCommand.COMMANDLINE_PARAM_DESCRIPTION)
    private String connectionString;

    @Option(names = {"-p", "--path"},
            description = "run suites/tests by path, format: " +
                    "-p=[schema|schema:[suite ...][.test]|schema[.suite ...][.test]")
    private List<String> paths = new ArrayList<>();

    @Option(names = {"--tags"},
            description = "comma-separated list of tags to run",
            split = ",")
    private List<String> tags = new ArrayList<>();

    @Option(names = {"--coverage-schemes"},
            description = "comma-separated list of schemas on which coverage should be gathered",
            split = ",")
    private List<String> coverageSchemes = new ArrayList<>();


    @Option(
            names = {"-c", "--color"},
            description = "enables printing of test results in colors as defined by ANSICONSOLE standards")
    private boolean colorConsole = false;

    @Option(
            names = {"-d", "--debug"},
            description = "Outputs a load of debug information to console")
    private boolean logDebug = false;

    @Option(
            names = {"-q", "--quiet"},
            description = "Does not output the informational messages normally printed to console")
    private boolean logSilent = false;

    @Option(
            names = {"--failure-exit-code"},
            description = "override the exit code on failure, default = 1")
    private int failureExitCode = 1;


    @Option(
            names = {"-scc", "--skip-compatibility-check"},
            description = "Skips the check for compatibility with database framework. CLI expects the framework to be " +
                    "most actual. Use this if you use CLI with a development version of utPLSQL-framework")
    private boolean skipCompatibilityCheck = false;

    @Option(
            names = {"-t", "--timeout"},
            description = "Sets the timeout in minutes after which the cli will abort. Default 60")
    private int timeoutInMinutes = 60;

    @Option(
            names = {"-include"},
            description = "Comma-separated object list to include in the coverage report. " +
                    "Format: [schema.]package[,[schema.]package ...]. See coverage reporting options in framework documentation"
    )
    private String includeObjects = null;

    @Option(
            names = {"-exclude"},
            description = "Comma-separated object list to exclude from the coverage report. " +
                    "Format: [schema.]package[,[schema.]package ...]. See coverage reporting options in framework documentation"
    )
    private String excludeObjects = null;


    @Option(
            names = {"-D", "--dbms_output"},
            description = "Enables DBMS_OUTPUT for the TestRunner (default: DISABLED)"
    )
    private boolean enableDbmsOutput = false;

    // RandomTestOrder
    @Option(
            names = {"-r", "--random-test-order"},
            description = "Enables random order of test executions (default: DISABLED)"
    )
    private boolean randomTestOrder = false;

    @Option(
            names = {"-seed", "--random-test-order-seed"},
            description = "Sets the seed to use for random test execution order. If set, it sets --random-test-order to true"
    )
    private Integer randomTestOrderSeed;

    @ArgGroup(exclusive = false, multiplicity = "0..*")
    private List<Format> reporters = new ArrayList<>();

    static class Format {
        @Option(names = {"-f", "--format"}, required = true, description = "Enables specified format reporting")
        String format;
        @Option(names = {"-o"}, description = "Outputs format to file")
        String outputFile;
        @Option(names = {"-s"}, description = "Outputs to screen even when an output file is specified")
        boolean outputToScreen = false;
    }

    // FileMappings
    @ArgGroup(exclusive = false, multiplicity = "0..2")
    private List<FileMappingComposite> fileMappings;

    static class FileMappingComposite {

        @ArgGroup(exclusive = true, multiplicity = "1")
        TestOrSourcePath testOrSourcePath;

        @ArgGroup(exclusive = false, multiplicity = "0..1")
        FileMapping mapping;

        static class TestOrSourcePath {
            @Option(names = "-source_path", required = true)
            String sourcePath;
            @Option(names = "-test_path", required = true)
            String testPath;

            String getPath() {
                return (isSourcePath()) ? sourcePath : testPath;
            }

            boolean isSourcePath() {
                return sourcePath != null;
            }
        }

        static class FileMapping {
            @Option(names = "-owner")
            String owner;
            @Option(names = "-regex_expression")
            String regexExpression;
            @Option(names = "-type_mapping")
            String typeMapping;
            @Option(names = "-owner_subexpression")
            Integer ownerSubExpression;
            @Option(names = "-type_subexpression")
            Integer typeSubExpression;
            @Option(names = "-name_subexpression")
            Integer nameSubExpression;
        }

        FileMapperConfig toFileMapperConfig() {
            if (mapping == null) {
                mapping = new FileMapping();
            }

            Map<String, String> typeMap = new HashMap<>();

            if (mapping.typeMapping != null && !mapping.typeMapping.isEmpty()) {
                for (String keyVal : mapping.typeMapping.split("/")) {
                    String[] values = keyVal.split("=");
                    typeMap.put(values[1], values[0]);
                }
            }

            return new FileMapperConfig(
                    testOrSourcePath.getPath(),
                    mapping.owner,
                    mapping.regexExpression,
                    typeMap,
                    mapping.ownerSubExpression,
                    mapping.nameSubExpression,
                    mapping.typeSubExpression
            );
        }
    }

    @Option(names = "-h", usageHelp = true, description = "display this help and exit")
    boolean help;

    @Option(names = "--catch-ora-stuck", description = "Sets a timeout around Reporter creation and retries when not ready after a while")
    boolean catchOraStuck = false;

    private RunAction runAction;

    private String[] splitOrEmpty(String value) {
        if (value == null || value.isEmpty()) {
            return new String[0];
        } else {
            return value.split(",");
        }
    }

    public RunCommandConfig getRunCommandConfig() {
        // Prepare path elements
        ArrayList<String> suitePaths = new ArrayList<>();
        for (String pathElem : paths) {
            suitePaths.addAll(Arrays.asList(pathElem.split(",")));
        }

        // Prepare LogLevelConfig
        LoggerConfiguration.ConfigLevel loggerConfigLevel = LoggerConfiguration.ConfigLevel.BASIC;
        if (logSilent) {
            loggerConfigLevel = LoggerConfiguration.ConfigLevel.NONE;
        } else if (logDebug) {
            loggerConfigLevel = LoggerConfiguration.ConfigLevel.DEBUG;
        }

        // Prepare Reporter configs
        List<ReporterConfig> reporterConfigs = new ArrayList<>();
        for (Format format : reporters) {
            reporterConfigs.add(new ReporterConfig(format.format, format.outputFile, format.outputToScreen));
        }

        // Prepare TypeMappings
        FileMapperConfig sourceFileMapping = null;
        FileMapperConfig testFileMapping = null;
        if (fileMappings != null) {
            for (FileMappingComposite fmc : fileMappings) {
                if (fmc.testOrSourcePath.isSourcePath()) {
                    sourceFileMapping = fmc.toFileMapperConfig();
                } else {
                    testFileMapping = fmc.toFileMapperConfig();
                }
            }
        }

        return new RunCommandConfig.Builder()
                .connectString(connectionString)
                .suitePaths(suitePaths.toArray(new String[0]))
                .reporters(reporterConfigs.toArray(new ReporterConfig[0]))
                .outputAnsiColor(colorConsole)
                .failureExitCode(failureExitCode)
                .skipCompatibilityCheck(skipCompatibilityCheck)
                .includePackages(splitOrEmpty(includeObjects))
                .excludePackages(splitOrEmpty(excludeObjects))
                .sourceMapping(sourceFileMapping)
                .testMapping(testFileMapping)
                .logConfigLevel(loggerConfigLevel)
                .timeoutInMinutes(timeoutInMinutes)
                .dbmsOutput(enableDbmsOutput)
                .randomTestOrder(randomTestOrder)
                .randomTestOrderSeed(randomTestOrderSeed)
                .tags(tags.toArray(new String[0]))
                .coverageSchemes(coverageSchemes.toArray(new String[0]))
                .catchOraStuck(catchOraStuck)
                .create();
    }

    private RunAction getRunAction() {
        if (runAction == null) {
            runAction = new RunAction(getRunCommandConfig());
        }

        return runAction;
    }

    @Override
    public int run() {
        return getRunAction().run();
    }

    @Override
    public TestRunner newTestRunner(List<Reporter> reporterList) {
        return getRunAction().newTestRunner(reporterList);
    }

    @Override
    public List<ReporterOptions> getReporterOptionsList() {
        return getRunAction().getReporterOptionsList();
    }

    @Override
    public void initLogger() {
        getRunAction().init();
    }
}
