package org.utplsql.cli;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunCommandConfigLevelTest {

    private Logger getRootLogger() {
        return LogManager.getRootLogger();
    }

    private Logger getCliLogger() {
        return LogManager.getLogger("org.utplsql.cli");
    }

    @Test
    void defaultIsInfo() {
        TestHelper.createRunCommand(TestHelper.getConnectionString())
                .init();

        assertEquals(Level.ERROR, getRootLogger().getLevel());
        assertEquals(Level.INFO, getCliLogger().getLevel());
    }

    @Test
    void silentModeSetsLoggerToOff() {
        TestHelper.createRunCommand(TestHelper.getConnectionString(), "-q")
                .init();

        assertEquals(Level.ERROR, getRootLogger().getLevel());
        assertEquals(Level.OFF, getCliLogger().getLevel());
    }

    @Test
    void debugModeSetsLoggerToDebug() {
        TestHelper.createRunCommand(TestHelper.getConnectionString(), "-d")
                .init();

        assertEquals(Level.DEBUG, getRootLogger().getLevel());
        assertEquals(Level.INFO, getCliLogger().getLevel());
    }
}
