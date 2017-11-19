package org.utplsql.cli;

import com.beust.jcommander.JCommander;
import org.junit.Assert;
import org.junit.Test;
import org.utplsql.api.CustomTypes;
import org.utplsql.api.compatibility.OptionalFeatures;

import java.util.List;

/**
 * Unit test for run command.
 */
public class RunCommandTest {

    private static String sUrl;
    private static String sUser;
    private static String sPass;

    static {
        sUrl  = System.getenv("DB_URL")  != null ? System.getenv("DB_URL")  : "192.168.99.100:1521:XE";
        sUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "app";
        sPass = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : "app";
    }

    private RunCommand createRunCommand(String... args) {
        RunCommand runCmd = new RunCommand();

        JCommander.newBuilder()
                .addObject(runCmd)
                .args(args)
                .build();

        return runCmd;
    }

    private String getConnectionString() {
        return sUser + "/" + sPass + "@" + sUrl;
    }

    @Test
    public void reporterOptions_Default() {
        RunCommand runCmd = createRunCommand(getConnectionString());

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        Assert.assertEquals(CustomTypes.UT_DOCUMENTATION_REPORTER, reporterOptions1.getReporterName());
        Assert.assertNull(reporterOptions1.getOutputFileName());
        Assert.assertFalse(reporterOptions1.outputToFile());
        Assert.assertTrue(reporterOptions1.outputToScreen());
    }

    @Test
    public void reporterOptions_OneReporter() {
        RunCommand runCmd = createRunCommand(getConnectionString(), "-f=ut_documentation_reporter", "-o=output.txt");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        Assert.assertEquals(CustomTypes.UT_DOCUMENTATION_REPORTER, reporterOptions1.getReporterName());
        Assert.assertEquals(reporterOptions1.getOutputFileName(), "output.txt");
        Assert.assertTrue(reporterOptions1.outputToFile());
        Assert.assertFalse(reporterOptions1.outputToScreen());
    }

    @Test
    public void reporterOptions_OneReporterForceScreen() {
        RunCommand runCmd = createRunCommand(getConnectionString(), "-f=ut_documentation_reporter", "-o=output.txt", "-s");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        Assert.assertEquals(CustomTypes.UT_DOCUMENTATION_REPORTER, reporterOptions1.getReporterName());
        Assert.assertEquals(reporterOptions1.getOutputFileName(), "output.txt");
        Assert.assertTrue(reporterOptions1.outputToFile());
        Assert.assertTrue(reporterOptions1.outputToScreen());
    }

    @Test
    public void reporterOptions_OneReporterForceScreenInverse() {
        RunCommand runCmd = createRunCommand(getConnectionString(), "-f=ut_documentation_reporter", "-s", "-o=output.txt");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        Assert.assertEquals(CustomTypes.UT_DOCUMENTATION_REPORTER, reporterOptions1.getReporterName());
        Assert.assertEquals(reporterOptions1.getOutputFileName(), "output.txt");
        Assert.assertTrue(reporterOptions1.outputToFile());
        Assert.assertTrue(reporterOptions1.outputToScreen());
    }

    @Test
    public void reporterOptions_TwoReporters() {
        RunCommand runCmd = createRunCommand(getConnectionString(),
                "-f=ut_documentation_reporter",
                "-f=ut_coverage_html_reporter", "-o=coverage.html", "-s");

        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        Assert.assertEquals(CustomTypes.UT_DOCUMENTATION_REPORTER, reporterOptions1.getReporterName());
        Assert.assertNull(reporterOptions1.getOutputFileName());
        Assert.assertFalse(reporterOptions1.outputToFile());
        Assert.assertTrue(reporterOptions1.outputToScreen());

        ReporterOptions reporterOptions2 = reporterOptionsList.get(1);
        Assert.assertEquals(CustomTypes.UT_COVERAGE_HTML_REPORTER, reporterOptions2.getReporterName());
        Assert.assertEquals(reporterOptions2.getOutputFileName(), "coverage.html");
        Assert.assertTrue(reporterOptions2.outputToFile());
        Assert.assertTrue(reporterOptions2.outputToScreen());
    }

    @Test
    public void run_Default() {
        RunCommand runCmd = createRunCommand(getConnectionString(),
                "-f=ut_documentation_reporter",
                "-c",
                "--failure-exit-code=2");

        try {
            int result = runCmd.run();

            // Only expect failure-exit-code to work on several framework versions
            if (OptionalFeatures.FAIL_ON_ERROR.isAvailableFor(runCmd.getDatabaseVersion()) )
                Assert.assertEquals(2, result);
            else
                Assert.assertEquals(0, result);
        }
        catch ( Exception e ) {
            Assert.fail(e.getMessage());
        }
    }

}
