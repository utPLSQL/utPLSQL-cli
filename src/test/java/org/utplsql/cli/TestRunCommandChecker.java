package org.utplsql.cli;

import org.junit.Assert;
import org.junit.Test;
import org.utplsql.api.Version;

public class TestRunCommandChecker {

    @Test
    public void getCheckFailOnErrorMessage()
    {
        // FailOnError option should work since 3.0.3+ framework
        Assert.assertNotNull(RunCommandChecker.getCheckFailOnErrorMessage(2, new Version("3.0.0")));
        Assert.assertNotNull(RunCommandChecker.getCheckFailOnErrorMessage(2, new Version("3.0.1")));
        Assert.assertNotNull(RunCommandChecker.getCheckFailOnErrorMessage(2, new Version("3.0.2")));
        Assert.assertNull(RunCommandChecker.getCheckFailOnErrorMessage(2, new Version("3.0.3")));
        Assert.assertNull(RunCommandChecker.getCheckFailOnErrorMessage(2, new Version("3.0.4")));
    }
}
