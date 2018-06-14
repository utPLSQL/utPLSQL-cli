package org.utplsql.cli.config;

import java.beans.ConstructorProperties;

public class ReporterConfig {

    private final String name;
    private final String output;
    private boolean screen = false;

    @ConstructorProperties({"name", "output", "screen"})
    public ReporterConfig( String name, String output, boolean screen ) {
        this.name = name;
        this.output = output;
        this.screen = screen;
    }

    public String getName() {
        return name;
    }

    public String getOutput() {
        return output;
    }

    public boolean isScreen() {
        return screen;
    }
}
