package org.utplsql.cli;

import org.utplsql.api.TestRunner;
import org.utplsql.api.reporter.Reporter;

import java.util.List;

public interface IRunCommand extends ICommand {

    void initLogger();

    TestRunner newTestRunner(List<Reporter> reporterList);

    List<ReporterOptions> getReporterOptionsList();
}
