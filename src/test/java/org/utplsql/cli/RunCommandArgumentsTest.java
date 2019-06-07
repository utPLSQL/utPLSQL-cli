package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.api.TestRunner;

import java.util.ArrayList;

public class RunCommandArgumentsTest {

    @Test
    public void allArgumentsAreRecognized() {
        RunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(),
                "-p=app",
                "-f=ut_sonar_test_reporter",
                    "-o=sonar_result.xml",
                    "-s",
                "-d",
                "-c",
                "--failure-exit-code=10",
                "-scc",
                "-t=60",
                "-exclude=app.betwnstr",
                "-include=app.betwnstr",
                "-source_path=src/test/resources/plsql/source",
                    "-owner=app",
                    "-regex_expression=\"*\"",
                    "-type_mapping=\"sql=PACKAGE BODY\"",
                    "-owner_subexpression=0",
                    "-type_subexpression=0",
                    "-name_subexpression=0",
                "-test_path=src/test/resources/plsql/test",
                    "-owner=app",
                    "-regex_expression=\"*\"",
                    "-type_mapping=\"sql=PACKAGE BODY\"",
                    "-owner_subexpression=0",
                    "-type_subexpression=0",
                    "-name_subexpression=0"
        );

        TestRunner testRunner = runCmd.newTestRunner(new ArrayList<>());
    }
}
