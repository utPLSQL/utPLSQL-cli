package org.utplsql.cli;

import org.junit.jupiter.api.Test;
import org.utplsql.cli.log.StringBlockFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringBlockFormatterTest {

    @Test
    void getBlockFormattedString() {

        String expected =
                "#### Headline ####\n" +
                "#                #\n" +
                "#   My value 1   #\n" +
                "#                #\n" +
                "##################";

        StringBlockFormatter formatter = new StringBlockFormatter("Headline");
        formatter.append("My value 1");

        assertEquals( expected, formatter.toString() );
    }

    @Test
    void getEncapsulatedLine() {

        String line = StringBlockFormatter.getEncapsulatedLine("val 1", 20);

        assertEquals("#   val 1                  #", line);
    }

    @Test
    void getEncapsulatedHeadline() {

        assertEquals("######### headline #########",
                StringBlockFormatter.getEncapsulatedHeadline("headline", 20));
        assertEquals("######### headline ##########",
                StringBlockFormatter.getEncapsulatedHeadline("headline", 21));
        assertEquals("######### headline1 #########",
                StringBlockFormatter.getEncapsulatedHeadline("headline1", 21));
        assertEquals("######## headline1 #########",
                StringBlockFormatter.getEncapsulatedHeadline("headline1", 20));
    }

    @Test
    void getEmptyEncapsulatedHeadline() {

        assertEquals("##################",
                StringBlockFormatter.getEncapsulatedHeadline("", 10));
    }
}
