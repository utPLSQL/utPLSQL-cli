package org.utplsql.cli.config;

import org.utplsql.api.reporter.CoreReporters;

import java.beans.ConstructorProperties;

public class ReporterConfig {

    private final String name;
    private final String output;
    private boolean forceToScreen = false;

    @ConstructorProperties({"name", "output", "forceToScreen"})
    public ReporterConfig(String name, String output, Boolean forceToScreen) {
        if ( name != null ) {
            this.name = name;
        } else {
            this.name = CoreReporters.UT_DOCUMENTATION_REPORTER.name();
        }
        this.output = output;
        if (forceToScreen != null) this.forceToScreen = forceToScreen;
    }

    public String getName() {
        return name;
    }

    public String getOutput() {
        return output;
    }

    public boolean isForceToScreen() {
        return forceToScreen;
    }
}
