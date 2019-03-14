package org.utplsql.cli;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utplsql.api.reporter.CoreReporters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for run command.
 *
 * @author philipp salivsberg
 */
class RunCommandIssue20Test {

    private static final Logger logger = LoggerFactory.getLogger(RunCommandIssue20Test.class);

    @Test
    void runLoop() {
        RunCommand runCmd = TestHelper.createRunCommand(
                TestHelper.getConnectionString(),
                "-p=TEST_BETWNSTR.normal_case",
                "-f=ut_documentation_reporter");
        List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();
        ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
        assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
        assertTrue(reporterOptions1.outputToScreen());
        // Loop in same JVM, uses a lot of new connections without closing existing ones, this might lead to 
        //   "Could not establish connection to database. Reason: IO Error: Got minus one from a read call"
        // before hitting the hanger at oracle.jdbc.driver.OracleStruct.getOracleAttributes(OracleStruct.java:347)
        // You may increase processes and implicitly sessions by "alter system set processes=1024 scope=spfile;"
        for (int i=0; i <= 120; i++) {
            logger.info("=======================");
            logger.info("Loop number " + i);
            logger.info("=======================");
            int result = runCmd.run();
            if (result != 0) {
                logger.error("Got an error during run. Return Code was " + result + "." );
                break;
            }
        }
    }

}