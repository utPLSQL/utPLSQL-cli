package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.api.CustomTypes;
import org.utplsql.api.reporter.CoreReporters;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for run command.
 */
public class RunCommandTest {

    @Test
    public void reporterOptions_Default() {
        RunCommand runCmd = RunCommandTestHelper.createRunCommand(RunCommandTestHelper.getConnectionString());

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER, reporterOptions1.getReporterName());
        assertNull(reporterOptions1.getOutputFileName());
        assertFalse(reporterOptions1.outputToFile());
        assertTrue(reporterOptions1.outputToScreen());
    }

    @Test
    public void reporterOptions_OneReporter() {
        RunCommand runCmd = RunCommandTestHelper.createRunCommand(RunCommandTestHelper.getConnectionString(), "-f=ut_documentation_reporter", "-o=output.txt");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER, reporterOptions1.getReporterName());
        assertEquals(reporterOptions1.getOutputFileName(), "output.txt");
        assertTrue(reporterOptions1.outputToFile());
        assertFalse(reporterOptions1.outputToScreen());
    }

    @Test
    public void reporterOptions_OneReporterForceScreen() {
        RunCommand runCmd = RunCommandTestHelper.createRunCommand(RunCommandTestHelper.getConnectionString(), "-f=ut_documentation_reporter", "-o=output.txt", "-s");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER, reporterOptions1.getReporterName());
        assertEquals(reporterOptions1.getOutputFileName(), "output.txt");
        assertTrue(reporterOptions1.outputToFile());
        assertTrue(reporterOptions1.outputToScreen());
    }

    @Test
    public void reporterOptions_OneReporterForceScreenInverse() {
        RunCommand runCmd = RunCommandTestHelper.createRunCommand(RunCommandTestHelper.getConnectionString(), "-f=ut_documentation_reporter", "-s", "-o=output.txt");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER, reporterOptions1.getReporterName());
        assertEquals(reporterOptions1.getOutputFileName(), "output.txt");
        assertTrue(reporterOptions1.outputToFile());
        assertTrue(reporterOptions1.outputToScreen());
    }

    @Test
    public void reporterOptions_TwoReporters() {
        RunCommand runCmd = RunCommandTestHelper.createRunCommand(RunCommandTestHelper.getConnectionString(),
                "-f=ut_documentation_reporter",
                "-f=ut_coverage_html_reporter", "-o=coverage.html", "-s");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER, reporterOptions1.getReporterName());
        assertNull(reporterOptions1.getOutputFileName());
        assertFalse(reporterOptions1.outputToFile());
        assertTrue(reporterOptions1.outputToScreen());

        ReporterOptions reporterOptions2 = reporterOptionsList.get(1);
        assertEquals(CoreReporters.UT_COVERAGE_HTML_REPORTER, reporterOptions2.getReporterName());
        assertEquals(reporterOptions2.getOutputFileName(), "coverage.html");
        assertTrue(reporterOptions2.outputToFile());
        assertTrue(reporterOptions2.outputToScreen());
    }

}
