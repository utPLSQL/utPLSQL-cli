package org.utplsql.cli;

import com.beust.jcommander.JCommander;
import org.junit.Assert;
import org.junit.Test;
import org.utplsql.api.CustomTypes;
import org.utplsql.api.compatibility.OptionalFeatures;

import java.util.List;

/**
 * System tests for run command.
 */
public class RunCommandSystemTest {


    @Test
    public void run_Default() {
        RunCommand runCmd = RunCommandTestHelper.createRunCommand(RunCommandTestHelper.getConnectionString(),
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
