package org.utplsql.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.LoggerFactory;

class LoggerConfiguration {

    public enum ConfigLevel {
        BASIC, NONE, DEBUG
    }

    private LoggerConfiguration() {
       throw new UnsupportedOperationException();
    }

    static void configure(ConfigLevel level) {
        switch ( level ) {
            case BASIC:
                configureInfo();
                break;
            case NONE:
                configureSilent();
                break;
            case DEBUG:
                configureDebug();
                break;
        }
    }

    private static void configureSilent() {
        setRootLoggerLevel(Level.OFF);
    }

    private static void configureInfo() {
        setRootLoggerLevel(Level.INFO);
        muteHikariLogger();
        setSingleConsoleAppenderWithLayout("%msg%n");
    }

    private static void configureDebug() {
        setRootLoggerLevel(Level.DEBUG);
        setSingleConsoleAppenderWithLayout("%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n");
    }

    private static void setRootLoggerLevel( Level level ) {
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }

    private static void muteHikariLogger() {
        ((Logger) LoggerFactory.getLogger(HikariDataSource.class)).setLevel(Level.OFF);
    }

    private static void setSingleConsoleAppenderWithLayout( String patternLayout ) {
        Logger logger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
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
