package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.api.TestRunner;
import org.utplsql.cli.config.RunCommandConfig;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class RunCommandConfigParamsArePassedToTestRunnerTest {

    @Test
    void tags() {
        RunCommandConfig config = new RunCommandConfig.Builder()
                .tags(new String[]{"tag1", "tag2"})
                .create();
        TestRunner testRunner = new RunAction(config).newTestRunner(new ArrayList<>());
        assertThat( testRunner.getOptions().tags, contains("tag1", "tag2") );
    }

    @Test
    void coverageSchemes() {
        RunCommandConfig config = new RunCommandConfig.Builder()
                .coverageSchemes(new String[]{"schema1", "another_schema", "and-another-one"})
                .create();
        TestRunner testRunner = new RunAction(config).newTestRunner(new ArrayList<>());
        assertThat( testRunner.getOptions().coverageSchemes, contains("schema1", "another_schema", "and-another-one") );
    }
}
