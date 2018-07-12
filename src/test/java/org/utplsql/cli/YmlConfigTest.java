package org.utplsql.cli;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.jupiter.api.Test;
import org.utplsql.cli.config.TestRunnerConfig;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class YmlConfigTest {

    @Test
    public void letsPlayAround() throws IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        String testConfigFile = "src/test/resources/test-config.yml";

        TestRunnerConfig config = mapper.readValue(new File(testConfigFile), TestRunnerConfig.class);

        System.out.println(ReflectionToStringBuilder.toString(config, ToStringStyle.MULTI_LINE_STYLE));
    }

    @Test
    public void configTest() {
        JCommander jc = new JCommander();
        jc.setProgramName("utplsql");

        CommandProvider cmdProvider = new CommandProvider();

        cmdProvider.commands().forEach(cmd -> jc.addCommand(cmd.getCommand(), cmd));

        jc.parse("run", TestHelper.getConnectionString(),
                "-f=ut_coverage_html_reporter", "-o=test.html",
                "-f=ut_documentation_reporter", "-s",
                "-exclude=app.award_bonus,app.betwnstr");

        RunCommand cmd = (RunCommand) cmdProvider.getCommand(jc.getParsedCommand());

        TestRunnerConfig config = cmd.getConfig();

        assertEquals(TestHelper.getConnectionString(), config.getConnectString());
        assertEquals(2, config.getReporters().length);
        assertEquals("ut_coverage_html_reporter", config.getReporters()[0].getName());
        assertEquals("test.html", config.getReporters()[0].getOutput());
        assertEquals(false, config.getReporters()[0].isScreen());
        assertEquals("ut_documentation_reporter", config.getReporters()[1].getName());
        assertNull(config.getReporters()[1].getOutput());
        assertEquals(true, config.getReporters()[1].isScreen());
        assertEquals("app.award_bonus", config.getExcludePackages()[0]);
        assertEquals("app.betwnstr", config.getExcludePackages()[1]);

    }


}
