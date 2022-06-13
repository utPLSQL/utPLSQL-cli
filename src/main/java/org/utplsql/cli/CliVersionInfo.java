package org.utplsql.cli;

import org.utplsql.api.JavaApiVersionInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class is getting updated automatically by the build process.
 * Please do not update its constants manually cause they will be overwritten.
 *
 * @author pesse
 */
public class CliVersionInfo {

    private static final String MAVEN_PROJECT_NAME = "utPLSQL-cli";
    private static String MAVEN_PROJECT_VERSION = "unknown";

    static {
        try {
            try (InputStream in = JavaApiVersionInfo.class.getClassLoader().getResourceAsStream("utplsql-cli.version");
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                MAVEN_PROJECT_VERSION = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println("WARNING: Could not get Version information!");
        }
    }

    public static String getVersion() {
        return MAVEN_PROJECT_VERSION;
    }

    public static String getInfo() {
        return MAVEN_PROJECT_NAME + " " + getVersion();
    }


}
