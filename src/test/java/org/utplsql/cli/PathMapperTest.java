package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.api.TestRunner;

import java.util.ArrayList;

public class PathMapperTest {

    @Test
    void checkPathMapperOutput() {
        RunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(),
                "-f=ut_sonar_test_reporter",
                "-o=sonar_result.xml",
                "-s",
                "-d",
                "-source_path=src/test/resources/plsql/source",
                "-owner=app",
                "-regex_expression=\"*\"",
                "-type_mapping=\"sql=PACKAGE BODY\"",
                "-test_path=src/test/resources/plsql/test"
                );

        TestRunner testRunner = runCmd.newTestRunner(new ArrayList<>());;
    }
}
