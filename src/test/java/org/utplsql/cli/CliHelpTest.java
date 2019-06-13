package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.cli.util.SystemCapturer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliHelpTest {

    private SystemCapturer capturer;

    @Test
    void show_basic_help_on_help_command() {
        capturer = new SystemCapturer.SystemOutCapturer();
        capturer.start();
        TestHelper.runApp("help");
        String output = capturer.stop();

        assertTrue(output.contains("Usage:"));
    }

    @Test
    void write_help_to_error_out_on_unknown_command() {
        capturer = new SystemCapturer.SystemErrCapturer();
        capturer.start();
        int exitCode = TestHelper.runApp("wtfhappens");
        String output = capturer.stop();

        assertTrue(output.contains("Usage:"));
        assertEquals(1, exitCode);
    }
}
