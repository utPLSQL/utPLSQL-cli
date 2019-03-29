package org.utplsql.cli;

import org.utplsql.api.TestRunner;

import java.sql.Connection;
import java.util.concurrent.ExecutorService;

class TestRunnerTask implements Runnable {

    private Connection con;
    private TestRunner testRunner;
    private ExecutorService executorService;

    TestRunnerTask(Connection con, TestRunner testRunner, ExecutorService executorService ) {
        this.con = con;
        this.testRunner = testRunner;
        this.executorService = executorService;
    }

    @Override
    public void run() {

    }
}
