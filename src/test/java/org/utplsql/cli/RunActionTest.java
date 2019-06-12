package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.api.TestRunnerOptions;
import org.utplsql.api.reporter.CoreReporters;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for run command.
 */
class RunActionTest {

    @Test
    void reporterOptions_Default() throws Exception {
        RunAction runCmd = TestHelper.createRunAction(TestHelper.getConnectionString());

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
        assertNull(reporterOptions1.getOutputFileName());
        assertFalse(reporterOptions1.outputToFile());
        assertTrue(reporterOptions1.outputToScreen());
    }

    @Test
    void reporterOptions_OneReporter() throws Exception {
        RunAction runCmd = TestHelper.createRunAction(TestHelper.getConnectionString(), "-f=ut_documentation_reporter", "-o=output.txt");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
        assertEquals(reporterOptions1.getOutputFileName(), "output.txt");
        assertTrue(reporterOptions1.outputToFile());
        assertFalse(reporterOptions1.outputToScreen());
    }

    @Test
    void reporterOptions_OneReporterForceScreen() throws Exception {
        RunAction runCmd = TestHelper.createRunAction(TestHelper.getConnectionString(), "-f=ut_documentation_reporter", "-o=output.txt", "-s");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
        assertEquals(reporterOptions1.getOutputFileName(), "output.txt");
        assertTrue(reporterOptions1.outputToFile());
        assertTrue(reporterOptions1.outputToScreen());
    }

    @Test
    void reporterOptions_OneReporterForceScreenInverse() throws Exception {
        RunAction runCmd = TestHelper.createRunAction(TestHelper.getConnectionString(), "-f=ut_documentation_reporter", "-s", "-o=output.txt");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
        assertEquals(reporterOptions1.getOutputFileName(), "output.txt");
        assertTrue(reporterOptions1.outputToFile());
        assertTrue(reporterOptions1.outputToScreen());
    }

    @Test
    void reporterOptions_TwoReporters() throws Exception {
        RunAction runCmd = TestHelper.createRunAction(TestHelper.getConnectionString(),
                "-f=ut_documentation_reporter",
                "-f=ut_coverage_html_reporter", "-o=coverage.html", "-s");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
        assertNull(reporterOptions1.getOutputFileName());
        assertFalse(reporterOptions1.outputToFile());
        assertFalse(reporterOptions1.outputToScreen());

        ReporterOptions reporterOptions2 = reporterOptionsList.get(1);
        assertEquals(CoreReporters.UT_COVERAGE_HTML_REPORTER.name(), reporterOptions2.getReporterName());
        assertEquals(reporterOptions2.getOutputFileName(), "coverage.html");
        assertTrue(reporterOptions2.outputToFile());
        assertTrue(reporterOptions2.outputToScreen());
    }

    @Test
    void connectionString_asSysdba() throws Exception {
        RunAction runCmd = TestHelper.createRunAction("sys as sysdba/mypass@connectstring/service");

        assertEquals("sys as sysdba/mypass@connectstring/service",
                runCmd.getConfig().getConnectString());
    }

    @Test
    void randomOrder_default() throws Exception {
        RunAction runCmd = TestHelper.createRunAction(TestHelper.getConnectionString());

        TestRunnerOptions options = runCmd.newTestRunner(new ArrayList<>()).getOptions();
        assertThat(options.randomTestOrder, equalTo(false));
        assertThat(options.randomTestOrderSeed, nullValue());
    }

    @Test
    void randomOrder_withoutSeed() throws Exception {
        RunAction runCmd = TestHelper.createRunAction(TestHelper.getConnectionString(),
                "-r");

        TestRunnerOptions options = runCmd.newTestRunner(new ArrayList<>()).getOptions();
        assertThat(options.randomTestOrder, equalTo(true));
        assertThat(options.randomTestOrderSeed, nullValue());
    }

    @Test
    void randomOrder_withSeed() throws Exception {
        RunAction runCmd = TestHelper.createRunAction(TestHelper.getConnectionString(),
                "-seed=42");

        TestRunnerOptions options = runCmd.newTestRunner(new ArrayList<>()).getOptions();
        assertThat(options.randomTestOrder, equalTo(true));
        assertThat(options.randomTestOrderSeed, equalTo(42));
    }
}
