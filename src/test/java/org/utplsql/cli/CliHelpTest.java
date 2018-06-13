package org.utplsql.cli;

import org.junit.jupiter.api.Test;

public class CliHelpTest {

    @Test
    public void showBasicHelp() {
        TestHelper.runApp("help");
    }
}
