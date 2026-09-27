package org.utplsql.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The command line arguments are logged at debug level; the credentials of the connect string must not be (issue #172).
 */
class CliArgsLoggingTest {

    private final Logger cliLogger = (Logger) LoggerFactory.getLogger(Cli.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Level originalLevel;

    @BeforeEach
    void captureCliLog() {
        originalLevel = cliLogger.getLevel();
        cliLogger.setLevel(Level.DEBUG);
        appender.start();
        cliLogger.addAppender(appender);
    }

    @AfterEach
    void restoreCliLog() {
        cliLogger.detachAppender(appender);
        cliLogger.setLevel(originalLevel);
    }

    @Test
    void maskedArgs() {
        assertEquals("run, ****/****@//localhost:1521/FREEPDB1, --debug",
                Cli.maskedArgs("run", "app/Sup3rSecretPw@//localhost:1521/FREEPDB1", "--debug"));
    }

    @Test
    void passwordIsNotLogged() {
        // "run -h" only prints the usage, so no database is needed
        Cli.runPicocliWithExitCode(new String[]{"run", "app/Sup3rSecretPw@//localhost:1521/FREEPDB1", "-h"});

        List<String> messages = appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();

        assertTrue(messages.contains("Args: run, ****/****@//localhost:1521/FREEPDB1, -h"), () -> "Logged: " + messages);
        assertFalse(messages.stream().anyMatch(message -> message.contains("Sup3rSecretPw")), () -> "Logged: " + messages);
    }
}
