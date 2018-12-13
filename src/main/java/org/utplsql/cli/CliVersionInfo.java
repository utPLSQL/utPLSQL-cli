package org.utplsql.cli;

import org.utplsql.api.JavaApiVersionInfo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/** This class is getting updated automatically by the build process.
 * Please do not update its constants manually cause they will be overwritten.
 *
 * @author pesse
 */
public class CliVersionInfo {

    private static final String MAVEN_PROJECT_NAME = "utPLSL-cli";
    private static String MAVEN_PROJECT_VERSION = "unknown";

    static {
        try {
            MAVEN_PROJECT_VERSION = Files.readAllLines(
                    Paths.get(CliVersionInfo.class.getClassLoader().getResource("utplsql-cli.version").toURI())
                    , Charset.defaultCharset())
                    .get(0);
        }
        catch ( IOException | URISyntaxException e ) {
            System.out.println("WARNING: Could not get Version information!");
        }
    }

    public static String getVersion() { return MAVEN_PROJECT_VERSION; }
    public static String getInfo() { return MAVEN_PROJECT_NAME + " " + getVersion(); }


}
