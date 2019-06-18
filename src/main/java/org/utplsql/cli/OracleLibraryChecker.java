package org.utplsql.cli;

/**
 * Simple class to check whether needed Oracle libraries are on classpath or not
 *
 * @author pesse
 */
class OracleLibraryChecker {

    private static boolean classExists(String classFullName) {
        try {
            Class.forName(classFullName);

            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks if OJDBC library is on the classpath by searching for oracle.jdbc.OracleDriver class
     *
     * @return true or false
     */
    public static boolean checkOjdbcExists() {
        return classExists("oracle.jdbc.OracleDriver");
    }

    /**
     * Checks if Orai18n library is on the classpath by searching for oracle.i18n.text.OraCharset
     *
     * @return true or false
     */
    public static boolean checkOrai18nExists() {
        return classExists("oracle.i18n.text.OraCharset");
    }


}
