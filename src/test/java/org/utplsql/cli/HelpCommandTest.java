package org.utplsql.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.utplsql.cli.util.SystemOutCapturer;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelpCommandTest {


    private SystemOutCapturer capturer;

    @BeforeEach
    public void setupCaptureSystemOut() {
        capturer = new SystemOutCapturer();
    }

    @Test
    public void callHelp() {

        capturer.start();
        int result = TestHelper.runApp("-h");
        String output = capturer.stop();

        assertEquals(0, result);
        assertTrue(output.contains("Usage:"));
    }

    @Test
    public void callWithNoArgs() {

        capturer.start();
        int result = TestHelper.runApp();
        String output = capturer.stop();

        assertEquals(1, result);
        assertTrue(output.contains("Usage:"));
    }

    @AfterEach
    public void cleanupCaptureSystemOut() throws IOException {
        capturer.stop();
    }
}
