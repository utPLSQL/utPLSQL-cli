package org.utplsql.cli;

import org.utplsql.api.compatibility.CompatibilityProxy;
import org.utplsql.api.reporter.CoreReporters;
import org.utplsql.api.reporter.ReporterFactory;
import org.utplsql.cli.reporters.LocalAssetsCoverageHTMLReporter;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A simple class to provide a ReporterFactory for the RunCommand
 *
 * @author pesse
 */
class ReporterFactoryProvider {

    public static ReporterFactory createReporterFactory(CompatibilityProxy proxy) {
        ReporterFactory reporterFactory = ReporterFactory.createDefault(proxy);
        reporterFactory.registerReporterFactoryMethod(CoreReporters.UT_COVERAGE_HTML_REPORTER.name(), LocalAssetsCoverageHTMLReporter::new, "Will copy all necessary assets to a folder named after the Output-File");

        return reporterFactory;
    }

    public static ReporterFactory createReporterFactory(Connection con) throws SQLException {
        return createReporterFactory(new CompatibilityProxy(con));
    }
}
