package org.utplsql.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.jupiter.api.Test;
import org.utplsql.cli.config.TestRunnerConfig;

import java.io.File;
import java.io.IOException;

public class YmlConfigTest {

    @Test
    public void letsPlayAround() throws IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        String testConfigFile = "src/test/resources/test-config.yml";

        TestRunnerConfig config = mapper.readValue(new File(testConfigFile), TestRunnerConfig.class);

        System.out.println(ReflectionToStringBuilder.toString(config,ToStringStyle.MULTI_LINE_STYLE));
    }


}
