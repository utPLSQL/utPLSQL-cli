package org.utplsql.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utplsql.api.DBHelper;
import org.utplsql.api.TestRunner;
import org.utplsql.api.exception.OracleCreateStatmenetStuckException;
import org.utplsql.api.exception.SomeTestsFailedException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * Runs the utPLSQL Test-Runner
 * <p>
 * Takes care of its connection.
 * In case of an OracleCreateStatementStuckException it will abort the connection, otherwise close it.
 *
 * @author pesse
 */
public class RunTestRunnerTask implements Callable<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(RunTestRunnerTask.class);
    private DataSource dataSource;
    private TestRunner testRunner;
    private boolean enableDmbsOutput;

    RunTestRunnerTask(DataSource dataSource, TestRunner testRunner, boolean enableDmbsOutput) {
        this.dataSource = dataSource;
        this.testRunner = testRunner;
        this.enableDmbsOutput = enableDmbsOutput;
    }

    @Override
    public Boolean call() throws Exception {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            if (enableDmbsOutput) DBHelper.enableDBMSOutput(conn);
            logger.info("Running tests now.");
            logger.info("--------------------------------------");
            testRunner.run(conn);
        } catch (SomeTestsFailedException e) {
            throw e;
        } catch (OracleCreateStatmenetStuckException e) {
            try {
                conn.abort(Executors.newSingleThreadExecutor());
                conn = null;
            } catch (SQLException e1) {
                logger.error(e1.getMessage(), e1);
            }
            throw e;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try {
                    if (enableDmbsOutput) DBHelper.disableDBMSOutput(conn);
                    conn.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return true;
    }
}
