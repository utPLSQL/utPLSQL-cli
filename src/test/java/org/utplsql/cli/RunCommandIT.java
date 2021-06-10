package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.api.compatibility.OptionalFeatures;
import org.utplsql.api.reporter.CoreReporters;

import java.nio.file.Paths;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * System tests for run command.
 */
class RunCommandIT extends AbstractFileOutputTest {

    private void assertValidReturnCode(int returnCode) throws SQLException {
        // Only expect failure-exit-code to work on several framework versions
        if (OptionalFeatures.FAIL_ON_ERROR.isAvailableFor(TestHelper.getFrameworkVersion()))
            assertEquals(2, returnCode);
        else
            assertEquals(0, returnCode);
    }

    @Test
    void run_Default() throws Exception {

        int result = TestHelper.runApp("run",
                TestHelper.getConnectionString(),
                "-f=ut_documentation_reporter",
                "-s",
                "-c",
                "--failure-exit-code=2");

        assertValidReturnCode(result);
    }

    @Test
    void run_Debug() throws Exception {

        int result = TestHelper.runApp("run",
                TestHelper.getConnectionString(),
                "--debug",
                "--failure-exit-code=2");

        assertValidReturnCode(result);
    }

    @Test
    void run_MultipleReporters() throws Exception {

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

        assertValidReturnCode(result);
    }


    @Test
    void run_withDbmsOutputEnabled() throws Exception {

        int result = TestHelper.runApp("run",
                TestHelper.getConnectionString(),
                "-D",
                "--failure-exit-code=2");

        assertValidReturnCode(result);
    }

    @Test
    void run_withOutputButNoReporterDefined() throws Exception {

        String outputFileName = "output_" + System.currentTimeMillis() + ".xml";
        addTempPath(Paths.get(outputFileName));

        int result = TestHelper.runApp("run",
                TestHelper.getConnectionString(),
                "-o=" + outputFileName,
                "--failure-exit-code=2");

        assertValidReturnCode(result);
    }

    @Test
    void run_withCatchOraStuck() throws Exception {
        int result = TestHelper.runApp("run",
                TestHelper.getConnectionString(),
                "--catch-ora-stuck",
                "--failure-exit-code=2");

        assertValidReturnCode(result);
    }
}
