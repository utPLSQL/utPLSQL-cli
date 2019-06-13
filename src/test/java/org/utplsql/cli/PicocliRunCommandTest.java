package org.utplsql.cli;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.utplsql.cli.config.FileMapperConfig;
import org.utplsql.cli.config.ReporterConfig;
import org.utplsql.cli.config.RunCommandConfig;
import picocli.CommandLine;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class PicocliRunCommandTest {

    private RunCommandConfig parseForConfig( String... args ) throws Exception {
        return TestHelper.parseRunConfig(args);
    }

    @Test
    void runCommandAllArguments() throws Exception {
        RunCommandConfig config = parseForConfig("run",
                TestHelper.getConnectionString(),
                "-p=app.betwnstr,app.basic",
                "-d",
                "-c",
                "-q",
                "--failure-exit-code=10",
                "-scc",
                "-t=60",
                "-exclude=app.exclude1,app.exclude2",
                "-include=app.include1,app.include2",
                "-D",
                "-r",
                "-seed=123",
                "-f=ut_sonar_test_reporter",
                    "-o=sonar_result.xml",
                    "-s",
                "-source_path=src/test/resources/plsql/source",
                    "-owner=app",
                    "-regex_expression=\"*\"",
                    "-type_mapping=\"sql=PACKAGE BODY\"",
                    "-owner_subexpression=0",
                    "-type_subexpression=0",
                    "-name_subexpression=0",
                "-test_path=src/test/resources/plsql/test",
                    "-owner=test_app",
                    "-regex_expression=\"test_regex\"",
                    "-type_mapping=\"tsql=PACKAGE BODY\"",
                    "-owner_subexpression=1",
                    "-type_subexpression=2",
                    "-name_subexpression=3");

        assertNotNull(config.getConnectString());
        assertThat( config.getSuitePaths(), is(new String[]{"app.betwnstr", "app.basic"}));
        assertTrue( config.isOutputAnsiColor() );
        assertEquals( LoggerConfiguration.ConfigLevel.NONE, config.getLogConfigLevel());
        assertEquals( 10, config.getFailureExitCode());
        assertTrue( config.isSkipCompatibilityCheck() );
        assertEquals( 60, config.getTimeoutInMinutes());
        assertThat( config.getExcludePackages(), is(new String[]{"app.exclude1", "app.exclude2"}));
        assertThat( config.getIncludePackages(), is(new String[]{"app.include1", "app.include2"}));
        assertTrue( config.isDbmsOutput() );
        assertTrue( config.isRandomTestOrder() );
        assertEquals( 123, config.getRandomTestOrderSeed() );
        assertNotNull( config.getReporters() );
        assertEquals( 1, config.getReporters().length );

        // Source FileMapping
        assertNotNull(config.getSourceMapping());
        assertNotNull(config.getTestMapping());
    }

    @Test
    void commaSeparatedPath() throws Exception {
        RunCommandConfig config = parseForConfig("run",
                TestHelper.getConnectionString(),
                "-p=app.betwnstr,app.basic");

        assertThat( config.getSuitePaths(), is(new String[]{"app.betwnstr", "app.basic"}));
    }

    @Test
    void multiplePaths() throws Exception {
        RunCommandConfig config = parseForConfig("run",
                TestHelper.getConnectionString(),
                "-p=app.betwnstr",
                "-p=app.basic");

        assertThat( config.getSuitePaths(), is(new String[]{"app.betwnstr", "app.basic"}));
    }

    @Test
    void combinedOptions() throws Exception {
        RunCommandConfig config = parseForConfig("run",
                TestHelper.getConnectionString(),
                "-cdq");

        assertTrue( config.isOutputAnsiColor() );
        assertEquals( LoggerConfiguration.ConfigLevel.NONE, config.getLogConfigLevel());
    }

    @Test
    void debugLevelOptionsDebug() throws Exception {
        RunCommandConfig config = parseForConfig("run",
                TestHelper.getConnectionString(),
                "-d");

        assertEquals( LoggerConfiguration.ConfigLevel.DEBUG, config.getLogConfigLevel());
    }

    @Test
    void debugLevelOptionsBasic() throws Exception {
        RunCommandConfig config = parseForConfig("run",
                TestHelper.getConnectionString());

        assertEquals( LoggerConfiguration.ConfigLevel.BASIC, config.getLogConfigLevel());
    }

    @Test
    void debugLevelOptionsNone() throws Exception {
        RunCommandConfig config = parseForConfig("run",
                TestHelper.getConnectionString(),
                "-q");

        assertEquals( LoggerConfiguration.ConfigLevel.NONE, config.getLogConfigLevel());
    }

    @Test
    void singleReporter() throws Exception {
        RunCommandConfig config = parseForConfig("run",
                TestHelper.getConnectionString(),
                "-f=ut_documentation_reporter");

        assertNotNull( config.getReporters() );

        ReporterConfig reporterConfig = config.getReporters()[0];
        assertEquals("ut_documentation_reporter", reporterConfig.getName());
        assertNull(reporterConfig.getOutput());
        assertFalse(reporterConfig.isScreen());
    }

    @Test
    void multipleReporters() throws Exception {
        RunCommandConfig config = parseForConfig("run",
                TestHelper.getConnectionString(),
                "-f=ut_documentation_reporter",
                    "-o=output1.txt",
                "-f=ut_coverage_html",
                    "-o=output2.html",
                    "-s");

        assertNotNull( config.getReporters() );

        ReporterConfig reporterConfig = config.getReporters()[0];
        assertEquals("ut_documentation_reporter", reporterConfig.getName());
        assertEquals("output1.txt", reporterConfig.getOutput());
        assertFalse(reporterConfig.isScreen());

        reporterConfig = config.getReporters()[1];
        assertEquals("ut_coverage_html", reporterConfig.getName());
        assertEquals("output2.html", reporterConfig.getOutput());
        assertTrue(reporterConfig.isScreen());
    }

    @Test
    void sourceFileMapping() throws Exception {
        RunCommandConfig config = parseForConfig("run",
                TestHelper.getConnectionString(),
                "-source_path=src/test/resources/plsql/source",
                    "-owner=app",
                    "-regex_expression=\"[a-Z+]\"",
                    "-type_mapping=\"sql=PACKAGE BODY/pks=PACKAGE\"",
                    "-owner_subexpression=0",
                    "-type_subexpression=1",
                    "-name_subexpression=3");

        FileMapperConfig sourceMapperConfig = config.getSourceMapping();
        assertEquals( "src/test/resources/plsql/source", sourceMapperConfig.getPath());
        assertEquals( "app", sourceMapperConfig.getOwner());
        assertEquals( "[a-Z+]", sourceMapperConfig.getRegexExpression());
        assertThat( sourceMapperConfig.getTypeMapping(), hasEntry("PACKAGE BODY", "sql"));
        assertThat( sourceMapperConfig.getTypeMapping(), hasEntry("PACKAGE", "pks"));
        assertEquals( 0, sourceMapperConfig.getOwnerSubexpression());
        assertEquals( 1, sourceMapperConfig.getTypeSubexpression());
        assertEquals( 3, sourceMapperConfig.getNameSubexpression());
    }

    @Test
    void testFileMapping() throws Exception {
        RunCommandConfig config = parseForConfig("run",
                TestHelper.getConnectionString(),
                "-test_path=src/test/resources/plsql/test",
                    "-owner=test_app",
                    "-regex_expression=\"test_regex\"",
                    "-type_mapping=\"tsql=PACKAGE BODY/tsql=FUNCTION\"",
                    "-owner_subexpression=4",
                    "-type_subexpression=5",
                    "-name_subexpression=6");

        FileMapperConfig testMapperConfig = config.getTestMapping();
        assertEquals( "src/test/resources/plsql/test", testMapperConfig.getPath());
        assertEquals( "test_app", testMapperConfig.getOwner());
        assertEquals( "test_regex", testMapperConfig.getRegexExpression());
        assertThat( testMapperConfig.getTypeMapping(), hasEntry("PACKAGE BODY", "tsql"));
        assertThat( testMapperConfig.getTypeMapping(), hasEntry("FUNCTION", "tsql"));
        assertEquals( 4, testMapperConfig.getOwnerSubexpression());
        assertEquals( 5, testMapperConfig.getTypeSubexpression());
        assertEquals( 6, testMapperConfig.getNameSubexpression());
    }

    @Test
    void testFileMappingWithoutDetails() throws Exception {
        RunCommandConfig config = parseForConfig("run",
                TestHelper.getConnectionString(),
                "-test_path=src/test/resources/plsql/test");

        FileMapperConfig testMapperConfig = config.getTestMapping();
        assertEquals( "src/test/resources/plsql/test", testMapperConfig.getPath());
        assertNull( testMapperConfig.getOwner());
        assertNull( testMapperConfig.getRegexExpression());
        assertThat( testMapperConfig.getTypeMapping(), anEmptyMap());
        assertNull( testMapperConfig.getOwnerSubexpression());
        assertNull( testMapperConfig.getTypeSubexpression());
        assertNull( testMapperConfig.getNameSubexpression());
    }
}
