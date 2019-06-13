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
class RunCommandTest {

    @Test
    void reporterOptions_Default() {
        IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString());

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
        assertNull(reporterOptions1.getOutputFileName());
        assertFalse(reporterOptions1.outputToFile());
        assertTrue(reporterOptions1.outputToScreen());
    }

    @Test
    void reporterOptions_OneReporter() {
        IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(), "-f=ut_documentation_reporter", "-o=output.txt");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
        assertEquals(reporterOptions1.getOutputFileName(), "output.txt");
        assertTrue(reporterOptions1.outputToFile());
        assertFalse(reporterOptions1.outputToScreen());
    }

    @Test
    void reporterOptions_OneReporterForceScreen() {
        IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(), "-f=ut_documentation_reporter", "-o=output.txt", "-s");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
        assertEquals(reporterOptions1.getOutputFileName(), "output.txt");
        assertTrue(reporterOptions1.outputToFile());
        assertTrue(reporterOptions1.outputToScreen());
    }

    @Test
    void reporterOptions_OneReporterForceScreenInverse() {
        IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(), "-f=ut_documentation_reporter", "-s", "-o=output.txt");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
        assertEquals(reporterOptions1.getOutputFileName(), "output.txt");
        assertTrue(reporterOptions1.outputToFile());
        assertTrue(reporterOptions1.outputToScreen());
    }

    @Test
    void reporterOptions_TwoReporters() {
        IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(),
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
    void randomOrder_default() throws Exception {
        IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString());

        TestRunnerOptions options = runCmd.newTestRunner(new ArrayList<>()).getOptions();
        assertThat(options.randomTestOrder, equalTo(false));
        assertThat(options.randomTestOrderSeed, nullValue());
    }

    @Test
    void randomOrder_withoutSeed() {
        IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(),
                "-r");

        TestRunnerOptions options = runCmd.newTestRunner(new ArrayList<>()).getOptions();
        assertThat(options.randomTestOrder, equalTo(true));
        assertThat(options.randomTestOrderSeed, nullValue());
    }

    @Test
    void randomOrder_withSeed() {
        IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(),
                "-seed=42");

        TestRunnerOptions options = runCmd.newTestRunner(new ArrayList<>()).getOptions();
        assertThat(options.randomTestOrder, equalTo(true));
        assertThat(options.randomTestOrderSeed, equalTo(42));
    }
}
