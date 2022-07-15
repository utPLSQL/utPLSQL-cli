[![latest-release](https://img.shields.io/github/release/utPLSQL/utPLSQL-cli.svg)](https://github.com/utPLSQL/utPLSQL-cli/releases)
[![license](https://img.shields.io/github/license/utPLSQL/utPLSQL-cli.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Build status](https://github.com/utPLSQL/utPLSQL-cli/actions/workflows/build.yml/badge.svg)](https://github.com/utPLSQL/utPLSQL-cli/actions/workflows/build.yml)

----------
# utPLSQL-cli
Java command-line client for [utPLSQL v3](https://github.com/utPLSQL/utPLSQL/).

Provides an easy way of invoking utPLSQL from command-line. Main features:

* Ability to run tests with multiple reporters simultaneously.
* Realtime reporting during test-run
* Ability to save output from every individual reporter to a separate output file.
* Allows execution of selected suites, subset of suite.
* Maps project and test files to database objects for reporting purposes.

## Downloading

Published releases are available for download on the [utPLSQL-cli GitHub Releases Page.](https://github.com/utPLSQL/utPLSQL-cli/releases)

You can also download all development versions from [Bintray](https://bintray.com/utplsql/utPLSQL-cli/utPLSQL-cli-develop#files).


## Requirements
* [Java SE Runtime Environment 8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) or newer
* When using reporters for Sonar or Coveralls client needs to be invoked from project's root directory.

## Compatibility
The latest CLI is always compatible with all database frameworks of the same major version.
For example CLI-3.1.0 is compatible with database framework 3.0.0-3.1.* but not with database framework 2.x.

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

## Charset

Java will use the default charset of your system for any string output.  
You can change this by passing the `-Dfile.encoding` property to the JVM when running a java-application.  
To avoid changing the utPLSQL-cli shell- or batchscript, you can define `-Dfile.encoding` in the environment variable `JAVA_TOOL_OPTIONS`. 
This environment variable will be picked up and interpreted by the JVM:

```
export JAVA_TOOL_OPTIONS='-Dfile.encoding=utf8'
utplsql run user/pw@connecstring

> Picked up JAVA_TOOL_OPTIONS: -Dfile.encoding=utf8
``` 

Make sure that the defined charset matches with the codepage your console is using.

## Usage
Currently, utPLSQL-cli supports the following sub-commands:
- run
- info
- reporters
- help

To get more info about a command, use 
```
utplsql <sub-command> -h
```
Example:
```
utplsql run -h
```

#### \<ConnectionURL>

This is used in all commands as first parameter (though it's optional for `info`).

Accepted formats:

- `<user>/<password>@//<host>[:<port>]/<service>`
- `<user>/<password>@<host>:<port>:<SID>` 
- `<user>/<password>@<TNSName>`
                         
To connect using TNS, you need to have the ORACLE_HOME environment variable set.
The file tnsnames.ora must exist in path %ORACLE_HOME%/network/admin
The file tnsnames.ora must contain valid TNS entries. 

In case you use a username containing `/` or a password containing `@` you should encapsulate it with double quotes `"`:
```
utplsql run "my/Username"/"myP@ssword"@connectstring
```

### run
`utplsql run <ConnectionURL> [<options>]`
                                                                 

#### Options
```                       
-p=suite_path(s)    - A suite path or a comma separated list of suite paths for unit test to be executed.     
(--path)              The path(s) can be in one of the following formats:
                          schema[.package[.procedure]]
                          schema:suite[.suite[.suite][...]][.procedure]
                      Both formats can be mixed in the list.
                      If only schema is provided, then all suites owner by that schema are executed.
                      If -p is omitted, the current schema is used.

--tags=tags         - A comma separated list of tags to include or exclude in the run. 
                      The excluded tags must be preceeded by a `-` (minus) sign and the entire expression must be surrounded by escaped doubleqotes in command line.
                      Format: --tags=tag1[,tag2,...,tagN]
                      or
                      Format: --tags=\"-tag1\"[,\"-tag2\",...,tagN]
-f=format           - A reporter to be used for reporting.
(--format)            If no -f option is provided, the default ut_documentation_reporter is used.
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
(--color)             Works only on reporeters that support colors (ut_documentation_reporter).
                      
-fcode=code         - Override the exit code on failure, defaults to 1. You can set it to 0 to always exit with a success status.
(--failure-exit-code)

-scc                - If specified, skips the compatibility-check with the version of the database framework.
(--skip-              If you skip compatibility-check, CLI will expect the most actual framework version
 compatibility-check) 
                      
-include=pckg_list  - Comma-separated object list to include in the coverage report.
                      Format: [schema.]package[,[schema.]package ...].
                      See coverage reporting options in framework documentation.
                      
-exclude=pckg_list  - Comma-separated object list to exclude from the coverage report.
                      Format: [schema.]package[,[schema.]package ...].
                      See coverage reporting options in framework documentation.
                      
-q                  - Does not output the informational messages normally printed to console.
(--quiet)             Default: false
                      
-d                  - Outputs a load of debug information to console
(--debug)             Default: false

-t=timeInMinutes   - Sets the timeout in minutes after which the cli will abort. 
(--timeout)          Default 60
                      
-D                 - Enables DBMS_OUTPUT in the TestRunner-Session
(--dbms_output)      Default: false

-r                 - Enables random order of test executions
(--random-test-order) Default: false

-seed              - Sets the seed to use for random test execution order. If set, it sets -random to true
(--random-test-order-seed)

--coverage-schemes - A comma separated list of schemas on which coverage should be gathered
                     Format: --coverage-schemes=schema1[,schema2[,schema3]]
                     
--ora-stuck-timeout - Sets a timeout around Reporter creation and retries when not ready after a while. 0 = no timeout.
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

UT_DEBUG_REPORTER:
    No description available

UT_DOCUMENTATION_REPORTER:
    A textual pretty-print of unit test results (usually use for console output)
    Provides additional properties lvl and failed

UT_JUNIT_REPORTER:
    Provides outcomes in a format conforming with JUnit 4 and above as defined in: https://gist.github.com/kuzuha/232902acab1344d6b578

UT_REALTIME_REPORTER:
    Provides test results in a XML format, for clients such as SQL Developer interested in showing progressing details.

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

## Using utPLSQL-cli as sysdba

Since 3.1.6 it is possible to run utPLSQL-cli as sysdba by running

```
utplsql run "sys as sysdba"/pw@connectstring
```

It is, however, __not recommended__ to run utPLSQL with sysdba privileges.

## Enabling Color Outputs on Windows

To enable color outputs on Windows cmd you need to install an open-source utility called [ANSICON](http://adoxa.altervista.org/ansicon/).

## Custom Reporters

Since v3.1.0 you can call custom reporters (PL/SQL) via cli, too. Just call the name of the custom reporter as you would do for the core reporters.

```
utplsql run hr/hr@xe -p=hr_test -f=my_custom_reporter -o=run.log -s
```
