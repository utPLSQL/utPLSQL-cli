package org.utplsql.cli;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.utplsql.api.TestRunnerOptions;
import org.utplsql.api.reporter.CoreReporters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for run command.
 */
class RunCommandTest {

    @Nested
    class A_reporter {

        List<ReporterOptions> getReporterOptionsWithArgs(String... args) {
            ArrayList<String> newArgs = new ArrayList<>(args.length + 1);
            newArgs.add(TestHelper.getConnectionString());
            newArgs.addAll(Arrays.asList(args));
            IRunCommand cmd = TestHelper.createRunCommand(newArgs.toArray(new String[0]));
            return cmd.getReporterOptionsList();
        }

        @Nested
        class Is_output_to_screen {

            @Test
            void by_default() {
                List<ReporterOptions> options = getReporterOptionsWithArgs();

                assertTrue(options.get(0).outputToScreen());
            }

            @Test
            void when_only_reporter_without_output_specified() {
                List<ReporterOptions> options = getReporterOptionsWithArgs("-f=ut_documentation_reporter");

                assertTrue(options.get(0).outputToScreen());
            }

            @Test
            void when_without_output_and_no_other_reporter_has_forceToScreen_flag() {
                List<ReporterOptions> options = getReporterOptionsWithArgs(
                        "-f=ut_coverage_sonar_reporter", "-o=output.txt",
                        "-f=ut_coverage_html_reporter");

                assertTrue(options.get(1).outputToScreen());
            }

            @Test
            void when_only_reporter_with_forceToScreen_flag() {
                List<ReporterOptions> options = getReporterOptionsWithArgs(
                        "-f=ut_coverage_sonar_reporter", "-o=output.txt",
                        "-f=ut_coverage_html_reporter", "-o=output.html", "-s",
                        "-f=ut_coverage_cobertura_reporter", "-o=cobertura.html");

                assertTrue(options.get(1).outputToScreen());
            }
        }

        @Nested
        class Is_not_output_to_screen {

            @Test
            void when_it_has_output_specified_but_no_forceToScreen() {
                List<ReporterOptions> options = getReporterOptionsWithArgs(
                        "-f=ut_documentation_reporter", "-o=output.txt",
                        "-f=ut_coverage_html_reporter", "-o=output.html"
                );

                assertFalse(options.get(0).outputToScreen());
                assertFalse(options.get(1).outputToScreen());
            }
        }

        @Nested
        class Cannot_be_run {

            @Test
            void when_more_than_one_forceToScreen_flags() {
                assertThrows(IllegalArgumentException.class, () -> {
                    getReporterOptionsWithArgs(
                            "-f=ut_coverage_sonar_reporter", "-o=output.txt", "-s",
                            "-f=ut_coverage_html_reporter", "-o=output.html", "-s");
                });
            }

            @Test
            void when_more_than_one_reporters_without_output() {
                assertThrows(IllegalArgumentException.class, () -> {
                    getReporterOptionsWithArgs(
                            "-f=ut_coverage_sonar_reporter",
                            "-f=ut_coverage_html_reporter");
                });
            }
            @Test
            void when_one_reporter_without_output_and_one_with_forceToScreen() {
                assertThrows(IllegalArgumentException.class, () -> {
                    getReporterOptionsWithArgs(
                            "-f=ut_coverage_sonar_reporter",
                            "-f=ut_coverage_html_reporter", "-o=output.html", "-s");
                });
            }
        }

        @Test
        void reporterOptions_Default() {
            IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString());

            List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

            ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
            assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
            assertNull(reporterOptions1.getOutputFileName());
            assertFalse(reporterOptions1.outputToFile());
            assertTrue(reporterOptions1.outputToScreen());
        }

        @Test
        void reporterOptions_OneReporter() {
            IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(), "-f=ut_documentation_reporter", "-o=output.txt");

            List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

            ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
            assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
            assertEquals("output.txt", reporterOptions1.getOutputFileName());
            assertTrue(reporterOptions1.outputToFile());
            assertFalse(reporterOptions1.outputToScreen());
        }

        @Test
        void reporterOptions_OneReporter_DefaultToScreen() {
            IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(), "-f=ut_documentation_reporter");

            List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

            ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
            assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
            assertNull(reporterOptions1.getOutputFileName());
            assertFalse(reporterOptions1.outputToFile());
            assertTrue(reporterOptions1.outputToScreen());
        }

        @Test
        void reporterOptions_OneReporterForceScreen() {
            IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(), "-f=ut_documentation_reporter", "-o=output.txt", "-s");

            List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

            ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
            assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
            assertEquals("output.txt", reporterOptions1.getOutputFileName());
            assertTrue(reporterOptions1.outputToFile());
            assertTrue(reporterOptions1.outputToScreen());
        }

        @Test
        void reporterOptions_OneReporterForceScreenInverse() {
            IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(), "-f=ut_documentation_reporter", "-s", "-o=output.txt");

            List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

            ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
            assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
            assertEquals("output.txt", reporterOptions1.getOutputFileName());
            assertTrue(reporterOptions1.outputToFile());
            assertTrue(reporterOptions1.outputToScreen());
        }

        @Test
        void reporterOptions_TwoReporters() {
            IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(),
                    "-f=ut_documentation_reporter", "-o=output.txt",
                    "-f=ut_coverage_html_reporter", "-o=coverage.html", "-s");

            List<ReporterOptions> reporterOptionsList = runCmd.getReporterOptionsList();

            ReporterOptions reporterOptions1 = reporterOptionsList.get(0);
            assertEquals(CoreReporters.UT_DOCUMENTATION_REPORTER.name(), reporterOptions1.getReporterName());
            assertEquals("output.txt", reporterOptions1.getOutputFileName());
            assertTrue(reporterOptions1.outputToFile());
            assertFalse(reporterOptions1.outputToScreen());

            ReporterOptions reporterOptions2 = reporterOptionsList.get(1);
            assertEquals(CoreReporters.UT_COVERAGE_HTML_REPORTER.name(), reporterOptions2.getReporterName());
            assertEquals("coverage.html", reporterOptions2.getOutputFileName());
            assertTrue(reporterOptions2.outputToFile());
            assertTrue(reporterOptions2.outputToScreen());
        }
    }

    @Test
    void randomOrder_default() throws Exception {
        IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString());

        TestRunnerOptions options = runCmd.newTestRunner(new ArrayList<>()).getOptions();
        assertThat(options.randomTestOrder, equalTo(false));
        assertThat(options.randomTestOrderSeed, nullValue());
    }

    @Test
    void randomOrder_withoutSeed() {
        IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(),
                "-r");

        TestRunnerOptions options = runCmd.newTestRunner(new ArrayList<>()).getOptions();
        assertThat(options.randomTestOrder, equalTo(true));
        assertThat(options.randomTestOrderSeed, nullValue());
    }

    @Test
    void randomOrder_withSeed() {
        IRunCommand runCmd = TestHelper.createRunCommand(TestHelper.getConnectionString(),
                "-seed=42");

        TestRunnerOptions options = runCmd.newTestRunner(new ArrayList<>()).getOptions();
        assertThat(options.randomTestOrder, equalTo(true));
        assertThat(options.randomTestOrderSeed, equalTo(42));
    }
}
