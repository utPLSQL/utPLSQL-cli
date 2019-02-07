package org.utplsql.cli.log;

public class StringBlockFormatter {

    private String headline;
    private StringBuilder content = new StringBuilder();

    public StringBlockFormatter() {}

    public StringBlockFormatter(String headline) {
        setHeadline(headline);
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getHeadline() {
        return headline;
    }

    public void append( CharSequence seq ) {
        content.append(seq);
    }

    public void appendLine( CharSequence seq ) {
        content.append(seq).append("\n");
    }

    private int getMaxLength( String[] lines ) {
        int len = 0;
        for ( String line : lines ) {
            if (line.length() > len)
                len = line.length();
        }

        if ( headline.length() > (len+6))
            len = headline.length();

        return len;
    }

    public static String getEncapsulatedLine( String line, int maxLength ) {
        return String.format("#   %-" + maxLength + "s   #", line);
    }

    public static String getEncapsulatedHeadline( String headline, int maxLength ) {
        String content = new String(new char[maxLength+8]).replace("\0", "#");
        if ( headline == null || headline.isEmpty() )
            return content;

        headline = " " + headline + " ";
        int start = (int)Math.floor(
                 (float)content.length()/2f
                -(float)headline.length()/2f
        );
        int end = start + headline.length();

        return content.substring(0, start)
                + headline
                + content.substring(end);
    }

    public String toString() {

        String[] lines = content.toString().split("\n");
        int maxLen = getMaxLength(lines);

        StringBuilder sb = new StringBuilder();

        sb.append(getEncapsulatedHeadline(headline, maxLen)).append("\n");
        sb.append(getEncapsulatedLine("", maxLen)).append("\n");
        for ( String line : lines ) {
            sb.append(getEncapsulatedLine(line, maxLen)).append("\n");
        }
        sb.append(getEncapsulatedLine("", maxLen)).append("\n");
        sb.append(getEncapsulatedHeadline("", maxLen));

        return sb.toString();
    }
}
