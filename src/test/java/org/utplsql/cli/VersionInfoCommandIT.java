package org.utplsql.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.utplsql.cli.util.SystemOutCapturer;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionInfoCommandIT {

    private SystemOutCapturer capturer;

    @BeforeEach
    void setupCaptureSystemOut() {
        capturer = new SystemOutCapturer();
    }

    private int getNonEmptyLines(String content) {
        return (int) Arrays.stream(content.split("[\n|\r]"))
                .filter(line -> !line.isEmpty())
                .count();
    }

    private void assertNumberOfLines( int expected, String content ) {
        int numOfLines = getNonEmptyLines(content);
        assertEquals(expected, numOfLines, String.format("Expected output to have %n lines, but got %n", expected, numOfLines));
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
