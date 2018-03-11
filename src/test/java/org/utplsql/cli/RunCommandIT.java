package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.api.compatibility.OptionalFeatures;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * System tests for run command.
 */
public class RunCommandIT {

    @Test
    public void run_Default() throws Exception {
        RunCommand runCmd = RunCommandTestHelper.createRunCommand(RunCommandTestHelper.getConnectionString(),
                "-f=ut_documentation_reporter",
                "-c",
                "--failure-exit-code=2");

        int result = runCmd.run();

        // Only expect failure-exit-code to work on several framework versions
        if (OptionalFeatures.FAIL_ON_ERROR.isAvailableFor(runCmd.getDatabaseVersion()))
            assertEquals(2, result);
        else
            assertEquals(0, result);
    }


}
