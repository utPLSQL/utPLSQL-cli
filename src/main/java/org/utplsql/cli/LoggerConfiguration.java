package org.utplsql.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.LoggerFactory;
import org.utplsql.api.TestRunner;

public class LoggerConfiguration {

    static void configureDefault() {
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        Logger hikariLogger = (Logger) LoggerFactory.getLogger(HikariDataSource.class);
        hikariLogger.setLevel(Level.OFF);

        ((Logger) LoggerFactory.getLogger(TestRunner.class)).setLevel(Level.ERROR);

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%msg%n");

        ple.setContext(lc);
        ple.start();

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<ILoggingEvent>();
        consoleAppender.setEncoder(ple);
        consoleAppender.setContext(lc);
        consoleAppender.start();

        root.detachAndStopAllAppenders();
        root.setAdditive(false);
        root.addAppender(consoleAppender);
    }
}
