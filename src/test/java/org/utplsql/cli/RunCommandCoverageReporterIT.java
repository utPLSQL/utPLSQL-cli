package org.utplsql.cli;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * System tests for Code Coverage Reporter
 *
 * @author pesse
 */
public class RunCommandCoverageReporterIT {

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

        while (Files.exists(p) && i < 100)
            p = Paths.get(getTempCoverageFileName(i++));

        if (i >= 100)
            throw new IllegalStateException("Could not get temporary file for coverage output");

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

        RunCommand runCmd = RunCommandTestHelper.createRunCommand(RunCommandTestHelper.getConnectionString(),
                "-f=ut_coverage_html_reporter", "-o=" + coveragePath, "-s", "-exclude=app.award_bonus,app.betwnstr");

        try {
            int result = runCmd.run();

            String content = new Scanner(coveragePath).useDelimiter("\\Z").next();

            assertEquals(true, hasCoverageListed(content, "app.remove_rooms_by_name"));
            assertEquals(false, hasCoverageListed(content, "app.award_bonus"));
            assertEquals(false, hasCoverageListed(content, "app.betwnstr"));

        } finally {
            Files.delete(coveragePath);
        }

    }

    @Test
    public void coverageReporterWriteAssetsToOutput() throws Exception {
        Path coveragePath = getTempCoverageFilePath();

        RunCommand runCmd = RunCommandTestHelper.createRunCommand(RunCommandTestHelper.getConnectionString(),
                "-f=ut_coverage_html_reporter", "-o=" + coveragePath, "-s");
        try {
            int result = runCmd.run();

            List<ReporterOptions> reporterOptions = runCmd.getReporterOptionsList();
            File applicationJs = coveragePath.resolve(Paths.get("assets", "application.js")).toFile();

            assertTrue(applicationJs.exists());

        } finally {
            if ( Files.exists(coveragePath))
                Files.delete(coveragePath);
        }

    }
}
