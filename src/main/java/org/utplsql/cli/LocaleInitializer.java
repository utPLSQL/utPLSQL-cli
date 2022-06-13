package org.utplsql.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utplsql.api.EnvironmentVariableUtil;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class makes sure the java locale is set according to the environment variables LC_ALL and LANG
 * We experienced that, in some cases, the locale was not set as expected, therefore this class implements some clear
 * rules:
 * 1. If environment variable LC_ALL is set, we try to parse its content and set locale according to its value if valid
 * 2. If environment variable LANG is set, we try to parse its content and set locale according to its value if valid
 * 3. Otherwise we use default locale
 *
 * @author pesse
 */
class LocaleInitializer {

    private static final Logger logger = LoggerFactory.getLogger(RunAction.class);

    private static final Pattern REGEX_LOCALE = Pattern.compile("^([a-zA-Z]+)[_-]([a-zA-Z]+)"); // We only need the very first part and are pretty forgiving in parsing

    /**
     * Sets the default locale according to the rules described above
     */
    static void initLocale() {

        boolean localeChanged = setDefaultLocale(EnvironmentVariableUtil.getEnvValue("LC_ALL"));
        if (!localeChanged) {
            localeChanged = setDefaultLocale(EnvironmentVariableUtil.getEnvValue("LANG"));
        }
        if ( !localeChanged ) {
            logger.debug("Java Locale not changed from LC_ALL or LANG environment variable");
        }
    }

    /**
     * Set the default locale from a given string like LC_ALL or LANG environment variable
     *
     * @param localeString Locale-string from LC_ALL or LANG, e.g "en_US.utf-8"
     * @return true if successful, false if not
     */
    private static boolean setDefaultLocale(String localeString) {
        if (localeString == null || localeString.isEmpty()) {
            return false;
        }

        try {
            Matcher m = REGEX_LOCALE.matcher(localeString);
            if (m.find()) {
                StringBuilder sb = new StringBuilder();
                sb.append(m.group(1));
                if (m.group(2) != null) {
                    sb.append("-").append(m.group(2));
                }

                Locale l = new Locale.Builder().setLanguageTag(sb.toString()).build();
                if (l != null) {
                    Locale.setDefault(l);
                    logger.debug("Java Locale changed to {}", l);
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Could not get locale from " + localeString);
        }

        return false;
    }
}
