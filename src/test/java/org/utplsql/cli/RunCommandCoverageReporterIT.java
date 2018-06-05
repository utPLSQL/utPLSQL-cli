package org.utplsql.cli;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * System tests for Code Coverage Reporter
 *
 * @author pesse
 */
public class RunCommandCoverageReporterIT extends AbstractFileOutputTest {

    private static final Pattern REGEX_COVERAGE_TITLE = Pattern.compile("<a href=\"[a-zA-Z0-9#]+\" class=\"src_link\" title=\"[a-zA-Z\\._]+\">([a-zA-Z0-9\\._]+)<\\/a>");


    private String getTempCoverageFileName(int counter) {

        return "tmpCoverage_" + String.valueOf(System.currentTimeMillis()) + "_" + String.valueOf(counter) + ".html";
    }

    /**
     * Returns a random filename which does not yet exist on the local path
     *
     * @return
     */
    private Path getTempCoverageFilePath() {

        int i = 1;
        Path p = Paths.get(getTempCoverageFileName(i));

        while ((Files.exists(p) || tempPathExists(p)) && i < 100)
            p = Paths.get(getTempCoverageFileName(i++));

        if (i >= 100)
            throw new IllegalStateException("Could not get temporary file for coverage output");

        addTempPath(p);
        addTempPath(Paths.get(p.toString()+"_assets"));

        return p;
    }

    /**
     * Checks Coverage HTML Output if a given packageName is listed
     *
     * @param content
     * @param packageName
     * @return
     */
    private boolean hasCoverageListed(String content, String packageName) {
        Matcher m = REGEX_COVERAGE_TITLE.matcher(content);

        while (m.find()) {
            if (packageName.equals(m.group(1)))
                return true;
        }

        return false;
    }

    @Test
    public void run_CodeCoverageWithIncludeAndExclude() throws Exception {

        Path coveragePath = getTempCoverageFilePath();

        int result = TestHelper.runApp("run", TestHelper.getConnectionString(),
                "-f=ut_coverage_html_reporter", "-o=" + coveragePath, "-s", "-exclude=app.award_bonus,app.betwnstr");


        String content = new String(Files.readAllBytes(coveragePath));

        assertEquals(true, hasCoverageListed(content, "app.remove_rooms_by_name"));
        assertEquals(false, hasCoverageListed(content, "app.award_bonus"));
        assertEquals(false, hasCoverageListed(content, "app.betwnstr"));

    }

    @Test
    public void coverageReporterWriteAssetsToOutput() throws Exception {
        Path coveragePath = getTempCoverageFilePath();
        Path coverageAssetsPath = Paths.get(coveragePath.toString() + "_assets");

        TestHelper.runApp("run", TestHelper.getConnectionString(),
                "-f=ut_coverage_html_reporter", "-o=" + coveragePath, "-s");

        // Run twice to test overriding of assets
        TestHelper.runApp("run", TestHelper.getConnectionString(),
                "-f=ut_coverage_html_reporter", "-o=" + coveragePath, "-s");


        // Check application file exists
        File applicationJs = coverageAssetsPath.resolve(Paths.get("application.js")).toFile();
        assertTrue(applicationJs.exists());

        // Check correct script-part in HTML source exists
        String content = new String(Files.readAllBytes(coveragePath));
        assertTrue(content.contains("<script src='" + coverageAssetsPath.toString() + "/application.js' type='text/javascript'>"));

        // Check correct title exists
        assertTrue(content.contains("<title>Code coverage</title>"));
    }

}
