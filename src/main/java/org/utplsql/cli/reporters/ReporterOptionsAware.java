package org.utplsql.cli.reporters;

import org.utplsql.cli.ReporterOptions;

/**
 * Reporters implementing this interface will get their specific ReporterOptions before initialization
 *
 * @author pesse
 */
public interface ReporterOptionsAware {
    void setReporterOptions(ReporterOptions options);
}
