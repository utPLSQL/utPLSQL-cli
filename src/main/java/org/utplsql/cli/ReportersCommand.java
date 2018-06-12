package org.utplsql.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.utplsql.api.reporter.ReporterFactory;
import org.utplsql.api.reporter.inspect.ReporterInfo;
import org.utplsql.api.reporter.inspect.ReporterInspector;

import javax.sql.DataSource;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.*;

@Parameters(separators = "=", commandDescription = "prints a list of reporters available in the specified database")
public class ReportersCommand implements ICommand {

    @Parameter(
            converter = ConnectionInfo.ConnectionStringConverter.class,
            arity = 1,
            description = ConnectionInfo.COMMANDLINE_PARAM_DESCRIPTION)
    private List<ConnectionInfo> connectionInfoList = new ArrayList<>();

    public ConnectionInfo getConnectionInfo() {
        if ( connectionInfoList != null && connectionInfoList.size() > 0 )
            return connectionInfoList.get(0);
        else
            return null;
    }

    @Override
    public int run() {

        DataSource ds = DataSourceProvider.getDataSource(getConnectionInfo(), 1);
        try (Connection con = ds.getConnection() ) {

            ReporterFactory reporterFactory = ReporterFactoryProvider.createReporterFactory(con);

            writeReporters(ReporterInspector.create(reporterFactory, con).getReporterInfos(), System.out);
        }
        catch ( Exception e ) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    @Override
    public String getCommand() {
        return "reporters";
    }

    private int getMaxNameLength(List<ReporterInfo> reporterInfos) {
        return reporterInfos.stream()
                .mapToInt(info -> info.getName().length())
                .max()
                .orElse(0);
    }

    private void writeReporters(List<ReporterInfo> reporterInfos, PrintStream out) {
        int padding = getMaxNameLength(reporterInfos)+1;
        reporterInfos.stream()
                .sorted(Comparator.comparing(ReporterInfo::getName))
                .forEach(info -> writeReporter(info, padding, out));
    }

    private void writeReporter(ReporterInfo info, int padding, PrintStream out) {

        writeReporterName(info, padding, out);
        writeReporterDescription(info, padding, out);

        out.println();
    }

    private void writeReporterName( ReporterInfo info, int paddingRight, PrintStream out ) {
        out.print(String.format("%1$-" + paddingRight + "s", info.getName()));
    }

    private void writeReporterDescription( ReporterInfo info, int paddingLeft, PrintStream out ) {
        String[] lines = info.getDescription().split("\n");

        boolean firstLine = true;
        for ( String line : lines ) {

            line = line.trim();

            if ( !firstLine )
                out.print(String.format("%1$" + paddingLeft + "s", ""));

            out.println(line);
            firstLine = false;
        }

    }
}
