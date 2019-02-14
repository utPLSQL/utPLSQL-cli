package org.utplsql.cli;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

class LoggerConfiguration {

    private LoggerConfiguration() {
       throw new UnsupportedOperationException();
    }

    static void configureDefault() {
        Configurator.setRootLevel(Level.ERROR);
        Configurator.setLevel("org.utplsql.cli", Level.INFO);
    }

    static void configureSilent() {
        Configurator.setRootLevel(Level.ERROR);
        Configurator.setLevel("org.utplsql.cli", Level.OFF);
    }

    static void configureDebug() {
        Configurator.setRootLevel(Level.DEBUG);
        Configurator.setLevel("org.utplsql.cli", Level.INFO);
    }

}
