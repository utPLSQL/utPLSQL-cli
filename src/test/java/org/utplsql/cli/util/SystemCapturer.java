package org.utplsql.cli.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/** All credit to Manasjyoti Sharma: https://stackoverflow.com/a/30665299
 */
public abstract class SystemCapturer {

    private ByteArrayOutputStream baos;
    private PrintStream previous;
    private boolean capturing;

    protected abstract PrintStream getOriginalStream();

    protected abstract void setSystemStream( PrintStream stream );

    public void start() {
        if (capturing) {
            return;
        }

        capturing = true;
        previous = getOriginalStream();
        baos = new ByteArrayOutputStream();

        OutputStream outputStreamCombiner =
                new OutputStreamCombiner(Arrays.asList(previous, baos));
        PrintStream custom = new PrintStream(outputStreamCombiner);

        setSystemStream(custom);
    }

    public String stop() {
        if (!capturing) {
            return "";
        }

        setSystemStream(previous);

        String capturedValue = baos.toString();

        baos = null;
        previous = null;
        capturing = false;

        return capturedValue;
    }

    private static class OutputStreamCombiner extends OutputStream {
        private List<OutputStream> outputStreams;

        public OutputStreamCombiner(List<OutputStream> outputStreams) {
            this.outputStreams = outputStreams;
        }

        public void write(int b) throws IOException {
            for (OutputStream os : outputStreams) {
                os.write(b);
            }
        }

        public void flush() throws IOException {
            for (OutputStream os : outputStreams) {
                os.flush();
            }
        }

        public void close() throws IOException {
            for (OutputStream os : outputStreams) {
                os.close();
            }
        }
    }

    public static class SystemOutCapturer extends SystemCapturer {

        @Override
        protected PrintStream getOriginalStream() {
            return System.out;
        }

        @Override
        protected void setSystemStream(PrintStream stream) {
            System.setOut(stream);
        }
    }

    public static class SystemErrCapturer extends SystemCapturer {

        @Override
        protected PrintStream getOriginalStream() {
            return System.err;
        }

        @Override
        protected void setSystemStream(PrintStream stream) {
            System.setErr(stream);
        }
    }
}
