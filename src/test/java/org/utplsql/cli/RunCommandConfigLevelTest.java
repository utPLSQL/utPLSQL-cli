package org.utplsql.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunCommandConfigLevelTest {

    private Logger getRootLogger() {
        return (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    }

    @Test
    void defaultIsInfo() {
        TestHelper.createRunCommand(TestHelper.getConnectionString())
                .init();

        assertEquals(Level.INFO, getRootLogger().getLevel());
    }

    @Test
    void silentModeSetsLoggerToOff() {
        TestHelper.createRunCommand(TestHelper.getConnectionString(), "-q")
                .init();

        assertEquals(Level.OFF, getRootLogger().getLevel());
    }

    @Test
    void debugModeSetsLoggerToDebug() {
        TestHelper.createRunCommand(TestHelper.getConnectionString(), "-d")
                .init();

        assertEquals(Level.DEBUG, getRootLogger().getLevel());
    }
}
