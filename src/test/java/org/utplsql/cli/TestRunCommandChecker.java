package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.api.Version;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestRunCommandChecker {

    @Test
    public void getCheckFailOnErrorMessage()
    {
        // FailOnError option should work since 3.0.3+ framework
        assertNotNull(RunCommandChecker.getCheckFailOnErrorMessage(2, new Version("3.0.0")));
        assertNotNull(RunCommandChecker.getCheckFailOnErrorMessage(2, new Version("3.0.1")));
        assertNotNull(RunCommandChecker.getCheckFailOnErrorMessage(2, new Version("3.0.2")));
        assertNull(RunCommandChecker.getCheckFailOnErrorMessage(2, new Version("3.0.3.1266")));
        assertNull(RunCommandChecker.getCheckFailOnErrorMessage(2, new Version("3.0.4")));
    }
}
