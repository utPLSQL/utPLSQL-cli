# utPLSQL-cli
Java command-line client for [utPLSQL v3](https://github.com/utPLSQL/utPLSQL/).

Provides an easy way of invoking utPLSQL from command-line. Main features:

* Ability to run tests with multiple reporters simultaneously.
* Ability to save output from every individual reporter to a separate output file.
* Allows execution of selected suites, subset of suite.
* Maps project and test files to database objects for reporting purposes. (Comming Soon)

## Downloading
You can download development versions on [Bintray](https://bintray.com/viniciusam/utPLSQL-cli/utPLSQL-cli-develop#files).


## Requirements
* [Java SE Runtime Environment 8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
* When using reporters for Sonar or Coveralls client needs to be invoked from project's root directory.
* Due to Oracle license we can't ship the necessary oracle libraries directly with utPLSQL-cli. <b>Please download the libraries directly from oracle website and put the jars into the "lib" folder of your utPLSQL-cli installation</b>
  * Oracle JDBC driver: http://www.oracle.com/technetwork/database/features/jdbc/jdbc-ucp-122-3110062.html
  * If you are on a 11g database you might need the orai18n library, too: http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html

## Compatibility
The latest CLI is always compatible with all database frameworks of the same major version.
For example CLI-3.0.4 is compatible with database framework 3.0.0-3.0.4 but not with database framework 2.x.

## Usage
utplsql run \<ConnectionURL\> [-p=(ut_path|ut_paths)] [-f=format [-o=output_file] [-s] ...]

```
<ConnectionURL>     - <user>/<password>@//<host>[:<port>]/<service> OR <user>/<password>@<TNSName> OR <user>/<password>@<host>:<port>:<SID>
                          To connect using TNS, you need to have the ORACLE_HOME environment variable set.
-p=suite_path(s)    - A suite path or a comma separated list of suite paths for unit test to be executed.     
                      The path(s) can be in one of the following formats:
                          schema[.package[.procedure]]
                          schema:suite[.suite[.suite][...]][.procedure]
                      Both formats can be mixed in the list.
                      If only schema is provided, then all suites owner by that schema are executed.
                      If -p is omitted, the current schema is used.
-f=format           - A reporter to be used for reporting.
                      If no -f option is provided, the default ut_documentation_reporter is used.
                      Available options:
                          -f=ut_documentation_reporter
                             A textual pretty-print of unit test results (usually use for console output)
                          -f=ut_teamcity_reporter
                             For reporting live progress of test execution with Teamcity CI. 
                          -f=ut_xunit_reporter
                             Used for reporting test results with CI servers like Jenkins/Hudson/Teamcity.
                          -f=ut_coverage_html_reporter
                             Generates a HTML coverage report with summary and line by line information on code coverage.
                             Based on open-source simplecov-html coverage reporter for Ruby.
                             Includes source code in the report.
                          -f=ut_coveralls_reporter
                             Generates a JSON coverage report providing information on code coverage with line numbers.
                             Designed for [Coveralls](https://coveralls.io/).
                          -f=ut_coverage_sonar_reporter
                             Generates a JSON coverage report providing information on code coverage with line numbers.
                             Designed for [SonarQube](https://about.sonarqube.com/) to report coverage.
                          -f=ut_sonar_test_reporter
                             Generates a JSON report providing detailed information on test execution.
                             Designed for [SonarQube](https://about.sonarqube.com/) to report test execution.
  
    -o=output       - Defines file name to save the output from the specified reporter.
                      If defined, the output is not displayed on screen by default. This can be changed with the -s parameter.
                      If not defined, then output will be displayed on screen, even if the parameter -s is not specified.
                      If more than one -o parameter is specified for one -f parameter, the last one is taken into consideration.
    -s              - Forces putting output to to screen for a given -f parameter.
-source_path=source - path to project source files, use the following options to enable custom type mappings:
    -owner="app"
    -regex_expression="pattern"
    -type_mapping="matched_string=TYPE[/matched_string=TYPE]*"
    -owner_subexpression=subexpression_number
    -type_subexpression=subexpression_number
    -name_subexpression=subexpression_number
-test_path=test     - path to project test files, use the following options to enable custom type mappings:
    -owner="app"
    -regex_expression="pattern"
    -type_mapping="matched_string=TYPE[/matched_string=TYPE]*"
    -owner_subexpression=subexpression_number
    -type_subexpression=subexpression_number
    -name_subexpression=subexpression_number
-c                  - If specified, enables printing of test results in colors as defined by ANSICONSOLE standards. 
                      Works only on reporeters that support colors (ut_documentation_reporter).
--failure-exit-code - Override the exit code on failure, defaults to 1. You can set it to 0 to always exit with a success status.
-scc                - If specified, skips the compatibility-check with the version of the database framework.
                      If you skip compatibility-check, CLI will expect the most actual framework version
```

Parameters -f, -o, -s are correlated. That is parameters -o and -s are controlling outputs for reporter specified by the preceding -f parameter.

Sonar and Coveralls reporter will only provide valid reports, when source_path and/or test_path are provided, and ut_run is executed from your project's root path.

Examples:

```
utplsql run hr/hr@xe -p=hr_test -f=ut_documentation_reporter -o=run.log -s -f=ut_coverage_html_reporter -o=coverage.html -source_path=source
```

Invokes all Unit tests from schema/package "hr_test" with two reporters:

* ut_documentation_reporter - will output to screen and save output to file "run.log"
* ut_coverage_html_reporter - will report only on database objects that are mapping to file structure from "source" folder and save output to file "coverage.html"

```
utplsql run hr/hr@xe
```

Invokes all unit test suites from schema "hr". Results are displayed to screen using default ut_documentation_reporter.

##### Enabling Color Outputs on Windows

To enable color outputs on Windows cmd you need to install an open-source utility called [ANSICON](http://adoxa.altervista.org/ansicon/).
