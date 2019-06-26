package org.utplsql.cli.config;

import java.beans.ConstructorProperties;

public class ReporterConfig {

    private final String name;
    private final String output;
    private boolean forceToScreen = false;

    @ConstructorProperties({"name", "output", "forceToScreen"})
    public ReporterConfig(String name, String output, Boolean forceToScreen) {
        this.name = name;
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
