package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.api.compatibility.OptionalFeatures;
import org.utplsql.api.reporter.CoreReporters;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * System tests for run command.
 */
public class RunCommandIT extends AbstractFileOutputTest {

    @Test
    public void run_Default() throws Exception {

        int result = TestHelper.runApp("run",
                TestHelper.getConnectionString(),
                "-f=ut_documentation_reporter",
                "-s",
                "-c",
                "--failure-exit-code=2");

        // Only expect failure-exit-code to work on several framework versions
        if (OptionalFeatures.FAIL_ON_ERROR.isAvailableFor(TestHelper.getFrameworkVersion()))
            assertEquals(2, result);
        else
            assertEquals(0, result);
    }

    @Test
    public void run_Debug() throws Exception {

        int result = TestHelper.runApp("run",
                TestHelper.getConnectionString(),
                "--debug");

        assertEquals(1, result);
    }

    @Test
    public void run_MultipleReporters() throws Exception {

        String outputFileName = "output_" + System.currentTimeMillis() + ".xml";
        addTempPath(Paths.get(outputFileName));

        int result = TestHelper.runApp("run",
                TestHelper.getConnectionString(),
                "-f=ut_documentation_reporter",
                "-s",
                "-f=" + CoreReporters.UT_SONAR_TEST_REPORTER.name(),
                "-o=" + outputFileName,
                "-c",
                "--failure-exit-code=2");

        // Only expect failure-exit-code to work on several framework versions
        if (OptionalFeatures.FAIL_ON_ERROR.isAvailableFor(TestHelper.getFrameworkVersion()))
            assertEquals(2, result);
        else
            assertEquals(0, result);
    }


}
