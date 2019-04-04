package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.api.TestRunnerOptions;
import org.utplsql.api.reporter.CoreReporters;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for run command.
 */
class RunCommandTest {

    @Test
    void reporterOptions_Default() {
        RunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString());

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
        assertNull(reporterOptions1.getOutputFileName());
        assertFalse(reporterOptions1.outputToFile());
        assertTrue(reporterOptions1.outputToScreen());
    }

    @Test
    void reporterOptions_OneReporter() {
        RunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(), "-f=ut_documentation_reporter", "-o=output.txt");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
        assertEquals(reporterOptions1.getOutputFileName(), "output.txt");
        assertTrue(reporterOptions1.outputToFile());
        assertFalse(reporterOptions1.outputToScreen());
    }

    @Test
    void reporterOptions_OneReporterForceScreen() {
        RunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(), "-f=ut_documentation_reporter", "-o=output.txt", "-s");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
        assertEquals(reporterOptions1.getOutputFileName(), "output.txt");
        assertTrue(reporterOptions1.outputToFile());
        assertTrue(reporterOptions1.outputToScreen());
    }

    @Test
    void reporterOptions_OneReporterForceScreenInverse() {
        RunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(), "-f=ut_documentation_reporter", "-s", "-o=output.txt");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
        assertEquals(reporterOptions1.getOutputFileName(), "output.txt");
        assertTrue(reporterOptions1.outputToFile());
        assertTrue(reporterOptions1.outputToScreen());
    }

    @Test
    void reporterOptions_TwoReporters() {
        RunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(),
                "-f=ut_documentation_reporter",
                "-f=ut_coverage_html_reporter", "-o=coverage.html", "-s");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
        assertNull(reporterOptions1.getOutputFileName());
        assertFalse(reporterOptions1.outputToFile());
        assertTrue(reporterOptions1.outputToScreen());

        ReporterOptions reporterOptions2 = reporterOptionsList.get(1);
        assertEquals(CoreReporters.UT_COVERAGE_HTML_REPORTER.name(), reporterOptions2.getReporterName());
        assertEquals(reporterOptions2.getOutputFileName(), "coverage.html");
        assertTrue(reporterOptions2.outputToFile());
        assertTrue(reporterOptions2.outputToScreen());
    }

    @Test
    void connectionString_asSysdba() {
        RunCommand runCmd = TestHelper.createRunCommand("sys as sysdba/mypass@connectstring/service");

        assertEquals("sys as sysdba/mypass@connectstring/service",
                runCmd.getConnectionInfo().getConnectionString());
    }

    @Test
    void randomOrder_withoutSeed() {
        RunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(),
                "-random");

        TestRunnerOptions options = runCmd.newTestRunner(new ArrayList<>()).getOptions();
        assertThat(options.randomTestOrder, equalTo(true));
    }
}
