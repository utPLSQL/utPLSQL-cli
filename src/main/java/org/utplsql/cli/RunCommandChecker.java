package org.utplsql.cli;

import org.utplsql.api.Version;
import org.utplsql.api.compatibility.OptionalFeatures;

/** Helper class to check several circumstances with RunCommand. Might need refactoring.
 *
 * @author pesse
 */
class RunCommandChecker {

    /** Checks that ojdbc library exists
     *
     */
    static void checkOracleJDBCExists()
    {
        if ( !OracleLibraryChecker.checkOjdbcExists() )
        {
            System.out.println("Could not find Oracle JDBC driver in classpath. Please download the jar from Oracle website" +
                    " and copy it to the 'lib' folder of your utPLSQL-cli installation.");
            System.out.println("Download from http://www.oracle.com/technetwork/database/features/jdbc/jdbc-ucp-122-3110062.html");

            throw new RuntimeException("Can't run utPLSQL-cli without Oracle JDBC driver");
        }
    }

    /** Checks that orai18n library exists if database is an oracle 11
     *
     */
    static void checkOracleI18nExists(String oracleDatabaseVersion )
    {
        if ( oracleDatabaseVersion.startsWith("11.") && !OracleLibraryChecker.checkOrai18nExists() )
        {
            System.out.println("Warning: Could not find Oracle i18n driver in classpath. Depending on the database charset " +
                    "utPLSQL-cli might not run properly. It is recommended you download " +
                    "the i18n driver from the Oracle website and copy it to the 'lib' folder of your utPLSQL-cli installation.");
            System.out.println("Download from http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html");
        }
    }

    /** Returns a warning message if failureExitCode is specified but database version is too low
     *
     * @param failureExitCode
     * @param databaseVersion
     */
    static String getCheckFailOnErrorMessage(int failureExitCode, Version databaseVersion) {
        if ( failureExitCode != 1 && !OptionalFeatures.FAIL_ON_ERROR.isAvailableFor(databaseVersion)) {
            return "Your database framework version (" + databaseVersion.getNormalizedString() + ") is not able to " +
                    "redirect failureCodes. Please upgrade to a newer version if you want to use that feature.";
        }

        return null;
    }
}
