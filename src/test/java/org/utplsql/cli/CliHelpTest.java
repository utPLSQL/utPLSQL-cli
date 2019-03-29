package org.utplsql.cli;

import org.junit.jupiter.api.Test;

class CliHelpTest {

    @Test
    void showBasicHelp() {
        TestHelper.runApp("help");
    }
}
