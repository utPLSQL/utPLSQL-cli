[![latest-release](https://img.shields.io/github/release/utPLSQL/utPLSQL-cli.svg)](https://github.com/utPLSQL/utPLSQLc-li/releases)
[![license](https://img.shields.io/github/license/utPLSQL/utPLSQL-cli.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![build](https://img.shields.io/travis/utPLSQL/utPLSQL-cli/develop.svg?label=develop%20branch)](https://travis-ci.org/utPLSQL/utPLSQL-cli)

----------
# utPLSQL-cli
Java command-line client for [utPLSQL v3](https://github.com/utPLSQL/utPLSQL/).

Provides an easy way of invoking utPLSQL from command-line. Main features:

* Ability to run tests with multiple reporters simultaneously.
* Ability to save output from every individual reporter to a separate output file.
* Allows execution of selected suites, subset of suite.
* Maps project and test files to database objects for reporting purposes. (Comming Soon)

## Downloading

Published releases are available for download on the [utPLSQL-cli GitHub Releases Page.](https://github.com/utPLSQL/utPLSQL-cli/releases)

You can also download all development versions from [Bintray](https://bintray.com/utplsql/utPLSQL-cli/utPLSQL-cli-develop#files).


## Requirements
* [Java SE Runtime Environment 8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
* When using reporters for Sonar or Coveralls client needs to be invoked from project's root directory.
* Due to Oracle license we can't ship the necessary oracle libraries directly with utPLSQL-cli. <b>Please download the libraries directly from oracle website and put the jars into the "lib" folder of your utPLSQL-cli installation</b>
  * Oracle `ojdbc8` driver
  * If you are on a 11g database with non standard NLS settings, you will also need the `orai18n` library.
  * All of the above can be downloaded from [Oracle download site](http://www.oracle.com/technetwork/database/features/jdbc/jdbc-ucp-122-3110062.html)

## Compatibility
The latest CLI is always compatible with all database frameworks of the same major version.
For example CLI-3.1.0 is compatible with database framework 3.0.0-3.1.2 but not with database framework 2.x.

## Localization and NLS settings
utPLSQL-cli will use the environment variables "LC_ALL" or "LANG" to change the locale and therefore the client NLS settings.
If neither environment variable is available, it will use the JVM default locale.

Example: to change the NLS-settings to English American, you can do the following:
```
export LC_ALL=en_US.utf-8
```

The charset-part of LC_ALL is ignored.

In addition, utPLSQL-cli will use an existing "NLS_LANG" environment variable to create corresponding 
`ALTER SESSION`-statements during initialization of the connection.

The variable is parsed according to the [Oracle globalization documentation](https://www.oracle.com/technetwork/database/database-technologies/globalization/nls-lang-099431.html#_Toc110410543)

Example: "NLS_LANG" of `AMERICAN_AMERICA.UTF8` will lead to the following statements:
```sql
ALTER SESSION SET NLS_LANGUAGE='AMERICAN';
ALTER SESSION SET NLS_TERRITORY='AMERICA';
```

## Usage
Currently, utPLSQL-cli supports the following commands:
- run
- info
- reporters

#### \<ConnectionURL>

This is used in all commands as first parameter (though it's optional for `info`).

Accepted formats:

- `<user>/<password>@//<host>[:<port>]/<service>`
- `<user>/<password>@<host>:<port>:<SID>` 
- `<user>/<password>@<TNSName>`
                         
To connect using TNS, you need to have the ORACLE_HOME environment variable set.
The file tnsnames.ora must exist in path %ORACLE_HOME%/network/admin
The file tnsnames.ora must contain valid TNS entries. 

### run
`utplsql run <ConnectionURL> [<options>]`
                                                                 

#### Options
```                       
-p=suite_path(s)    - A suite path or a comma separated list of suite paths for unit test to be executed.     
                      The path(s) can be in one of the following formats:
                          schema[.package[.procedure]]
                          schema:suite[.suite[.suite][...]][.procedure]
                      Both formats can be mixed in the list.
                      If only schema is provided, then all suites owner by that schema are executed.
                      If -p is omitted, the current schema is used.
                      
-f=format           - A reporter to be used for reporting.
                      If no -f option is provided, the default ut_documentation_reporter is used.
                      See reporters command for possible values
  -o=output         - Defines file name to save the output from the specified reporter.
                      If defined, the output is not displayed on screen by default. This can be changed with the -s parameter.
                      If not defined, then output will be displayed on screen, even if the parameter -s is not specified.
                      If more than one -o parameter is specified for one -f parameter, the last one is taken into consideration.
  -s                - Forces putting output to to screen for a given -f parameter.
  
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
                      
-include=pckg_list  - Comma-separated object list to include in the coverage report.
                      Format: [schema.]package[,[schema.]package ...].
                      See coverage reporting options in framework documentation.
                      
-exclude=pckg_list  - Comma-separated object list to exclude from the coverage report.
                      Format: [schema.]package[,[schema.]package ...].
                      See coverage reporting options in framework documentation.
                      
-q                  - Does not output the informational messages normally printed to console.
                      Default: false
                      
-d                  - Outputs a load of debug information to console
                      Default: false

-t                  - Sets the timeout in minutes after which the cli will abort. 
                      Default 60
```

Parameters -f, -o, -s are correlated. That is parameters -o and -s are controlling outputs for reporter specified by the preceding -f parameter.

Sonar and Coveralls reporter will only provide valid reports, when source_path and/or test_path are provided, and ut_run is executed from your project's root path.

#### Examples

```
> utplsql run hr/hr@xe -p=hr_test -f=ut_documentation_reporter -o=run.log -s -f=ut_coverage_html_reporter -o=coverage.html -source_path=source
```

Invokes all Unit tests from schema/package "hr_test" with two reporters:

* ut_documentation_reporter - will output to screen and save output to file "run.log"
* ut_coverage_html_reporter - will report only on database objects that are mapping to file structure from "source" folder and save output to file "coverage.html"

```
> utplsql run hr/hr@xe
```

Invokes all unit test suites from schema "hr". Results are displayed to screen using default ut_documentation_reporter.

### info
`utplsql info [<ConnectionURL>]`


#### Examples

```
> utplsql info

cli 3.1.1-SNAPSHOT.local
utPLSQL-java-api 3.1.1-SNAPSHOT.123
```
```
> utplsql info app/app@localhost:1521/ORCLPDB1

cli 3.1.1-SNAPSHOT.local
utPLSQL-java-api 3.1.1-SNAPSHOT.123
utPLSQL 3.1.2.1913
```

### reporters
`utplsql reporters <ConnectionURL>`

#### Examples
```
> utplsql reporters app/app@localhost:1521/ORCLPDB1

UT_COVERAGE_COBERTURA_REPORTER:
    Generates a Cobertura coverage report providing information on code coverage with line numbers.
    Designed for Jenkins and TFS to report coverage.
    Cobertura Document Type Definition can be found: http://cobertura.sourceforge.net/xml/coverage-04.dtd.
    Sample file: https://github.com/leobalter/testing-examples/blob/master/solutions/3/report/cobertura-coverage.xml.

UT_COVERAGE_HTML_REPORTER:
    Generates a HTML coverage report with summary and line by line information on code coverage.
    Based on open-source simplecov-html coverage reporter for Ruby.
    Includes source code in the report.
    Will copy all necessary assets to a folder named after the Output-File

UT_COVERAGE_SONAR_REPORTER:
    Generates a JSON coverage report providing information on code coverage with line numbers.
    Designed for [SonarQube](https://about.sonarqube.com/) to report coverage.
    JSON format returned conforms with the Sonar specification: https://docs.sonarqube.org/display/SONAR/Generic+Test+Data

UT_COVERALLS_REPORTER:
    Generates a JSON coverage report providing information on code coverage with line numbers.
    Designed for [Coveralls](https://coveralls.io/).
    JSON format conforms with specification: https://docs.coveralls.io/api-introduction

UT_DOCUMENTATION_REPORTER:
    A textual pretty-print of unit test results (usually use for console output)
    Provides additional properties lvl and failed

UT_JUNIT_REPORTER:
    Provides outcomes in a format conforming with JUnit 4 and above as defined in: https://gist.github.com/kuzuha/232902acab1344d6b578

UT_SONAR_TEST_REPORTER:
    Generates a JSON report providing detailed information on test execution.
    Designed for [SonarQube](https://about.sonarqube.com/) to report test execution.
    JSON format returned conforms with the Sonar specification: https://docs.sonarqube.org/display/SONAR/Generic+Test+Data

UT_TEAMCITY_REPORTER:
    Provides the TeamCity (a CI server by jetbrains) reporting-format that allows tracking of progress of a CI step/task as it executes.
    https://confluence.jetbrains.com/display/TCD9/Build+Script+Interaction+with+TeamCity

UT_TFS_JUNIT_REPORTER:
    Provides outcomes in a format conforming with JUnit version for TFS / VSTS.
    As defined by specs :https://docs.microsoft.com/en-us/vsts/build-release/tasks/test/publish-test-results?view=vsts
    Version is based on windy road junit https://github.com/windyroad/JUnit-Schema/blob/master/JUnit.xsd.

UT_XUNIT_REPORTER:
    Depracated reporter. Please use Junit.
    Provides outcomes in a format conforming with JUnit 4 and above as defined in: https://gist.github.com/kuzuha/232902acab1344d6b578
```

## Enabling Color Outputs on Windows

To enable color outputs on Windows cmd you need to install an open-source utility called [ANSICON](http://adoxa.altervista.org/ansicon/).

## Custom Reporters

Since v3.1.0 you can call custom reporters (PL/SQL) via cli, too. Just call the name of the custom reporter as you would do for the core reporters.

```
utplsql run hr/hr@xe -p=hr_test -f=my_custom_reporter -o=run.log -s
```
