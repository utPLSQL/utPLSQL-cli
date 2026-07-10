package org.utplsql.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.utplsql.cli.util.SystemCapturer;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionInfoCommandIT {

    private SystemCapturer capturer;

    @BeforeEach
    void setupCaptureSystemOut() {
        capturer = new SystemCapturer.SystemOutCapturer();
    }

    private String[] getNonEmptyLineArray(String content) {
        return Arrays.stream(content.split("[\r\n]+"))
                .filter(line -> !line.isEmpty())
                .toArray(String[]::new);
    }

    private void assertNumberOfLines( int expected, String content ) {
        String[] lines = getNonEmptyLineArray(content);
        assertEquals(expected, lines.length, () -> {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Expected output to have %d lines, but got %d.%n", expected, lines.length));
            sb.append("Actual lines were:").append(System.lineSeparator());
            for (int i = 0; i < lines.length; i++) {
                sb.append(String.format("  [%d] %s%n", i, lines[i]));
            }
            return sb.toString();
        });
    }
    @Test
    void infoCommandRunsWithoutConnection() {

        capturer.start();

        int result = TestHelper.runApp("info");

        String output = capturer.stop();

        assertEquals(0, result);
        assertNumberOfLines(2, output);
    }
    @Test
    void infoCommandRunsWithConnection() {

        capturer.start();

        int result = TestHelper.runApp("info", TestHelper.getConnectionString());

        String output = capturer.stop();

        assertEquals(0, result);
        assertNumberOfLines(3, output);
    }

    @AfterEach
    void cleanupCaptureSystemOut() {
        capturer.stop();
    }
}
