package org.utplsql.cli.reporters;

import org.utplsql.api.compatibility.CompatibilityProxy;
import org.utplsql.api.reporter.CoverageHTMLReporter;
import org.utplsql.api.reporter.Reporter;
import org.utplsql.api.reporter.ReporterFactory;
import org.utplsql.cli.ReporterOptions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

/** Simple replacement of the CoverageHTMLReporter which writes the necessary assets to a folder
 * named after the Output File's name.
 *
 * @author pesse
 */
public class LocalAssetsCoverageHTMLReporter extends CoverageHTMLReporter implements ReporterOptionsAware {

    private ReporterOptions options;

    public LocalAssetsCoverageHTMLReporter(String selfType, Object[] attributes) {
        super(selfType, attributes);
    }

    @Override
    public Reporter init(Connection con, CompatibilityProxy compatibilityProxy, ReporterFactory reporterFactory) throws SQLException {
        super.init(con, compatibilityProxy, reporterFactory);

        if ( hasOutputToFile() ) {
            writeReportAssetsTo(getPhysicalAssetPath());
        }

        return this;
    }

    private String getNameOfOutputFile() {
        Path outputPath = Paths.get(options.getOutputFileName());
        return outputPath.getName(outputPath.getNameCount()-1).toString();
    }

    private Path getPhysicalAssetPath() {
        Path outputPath = Paths.get(options.getOutputFileName());
        if ( outputPath.getNameCount() > 1 )
            return outputPath.getParent().resolve(getAssetsPath());
        else
            return Paths.get(getAssetsPath());
    }

    private void setAssetsPathFromOptions() {
        if ( hasOutputToFile() ) {
            setAssetsPath(getNameOfOutputFile() + "_assets/");
        }
    }

    private boolean hasOutputToFile() {
        return (options != null && options.outputToFile());
    }

    @Override
    public void setReporterOptions(ReporterOptions options) {
        this.options = options;
        setAssetsPathFromOptions();
    }

    @Override
    protected void setAttributes(Object[] attributes) {
        super.setAttributes(attributes);
        setAssetsPathFromOptions();
    }
}
