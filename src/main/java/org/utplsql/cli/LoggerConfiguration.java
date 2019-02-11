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

class LoggerConfiguration {
    private LoggerConfiguration() {
       throw new UnsupportedOperationException();
    }
    static void configure(boolean silent, boolean debug) {
        if ( silent )
            configureSilent();
        else if ( debug )
            configureDebug();
        else
            configureDefault();
    }

    private static void configureSilent() {
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.OFF);
    }

    private static void configureDefault() {
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        ((Logger) LoggerFactory.getLogger(HikariDataSource.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(TestRunner.class)).setLevel(Level.ERROR);

        setSingleConsoleAppenderWithLayout(root, "%msg%n");
    }

    private static void configureDebug() {
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);

        setSingleConsoleAppenderWithLayout(root, "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n");
    }

    private static void setSingleConsoleAppenderWithLayout( Logger logger, String patternLayout ) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern(patternLayout);

        ple.setContext(lc);
        ple.start();

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setEncoder(ple);
        consoleAppender.setContext(lc);
        consoleAppender.start();

        logger.detachAndStopAllAppenders();
        logger.setAdditive(false);
        logger.addAppender(consoleAppender);
    }
}
