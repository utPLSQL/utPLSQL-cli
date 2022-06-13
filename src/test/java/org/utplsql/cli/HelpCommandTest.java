package org.utplsql.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.utplsql.cli.util.SystemCapturer;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelpCommandTest {


    private SystemCapturer capturer;

    @BeforeEach
    void setupCaptureSystemOut() {
        capturer = new SystemCapturer.SystemOutCapturer();
    }

    @Test
    void callHelp() {

        capturer.start();
        int result = TestHelper.runApp("-h");
        String output = capturer.stop();

        assertEquals(0, result);
        assertTrue(output.contains("Usage:"));
    }

    @Test
    void callRunHelp() {

        capturer.start();
        int result = TestHelper.runApp("run", "-h");
        String output = capturer.stop();

        assertEquals(0, result);
        assertTrue(output.contains("Usage:"));
    }

    @Test
    void callWithNoArgs() {

        capturer.start();
        int result = TestHelper.runApp();
        String output = capturer.stop();

        assertEquals(1, result);
        assertTrue(output.contains("Usage:"));
    }

    @AfterEach
    void cleanupCaptureSystemOut() throws IOException {
        capturer.stop();
    }
}
