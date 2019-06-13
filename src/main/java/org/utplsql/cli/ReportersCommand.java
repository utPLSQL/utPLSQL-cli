package org.utplsql.cli;

import org.utplsql.api.exception.DatabaseNotCompatibleException;
import org.utplsql.api.exception.UtPLSQLNotInstalledException;
import org.utplsql.api.reporter.ReporterFactory;
import org.utplsql.api.reporter.inspect.ReporterInfo;
import org.utplsql.api.reporter.inspect.ReporterInspector;
import org.utplsql.cli.exception.DatabaseConnectionFailed;
import picocli.CommandLine.*;

import javax.sql.DataSource;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.*;

@Command( name = "reporters", description = "prints a list of reporters available in the specified database")
public class ReportersCommand implements ICommand {

    @Parameters(description = ConnectionInfo.COMMANDLINE_PARAM_DESCRIPTION, arity = "1")
    private String connectionString;

    @Override
    public int run() {

        try {
            DataSource ds = DataSourceProvider.getDataSource(connectionString, 1);
            try (Connection con = ds.getConnection()) {

                ReporterFactory reporterFactory = ReporterFactoryProvider.createReporterFactory(con);

                writeReporters(ReporterInspector.create(reporterFactory, con).getReporterInfos(), System.out);
            }
        }
        catch ( DatabaseNotCompatibleException | UtPLSQLNotInstalledException | DatabaseConnectionFailed | IllegalArgumentException e ) {
            System.out.println(e.getMessage());
            return 1;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    @Override
    public String getCommand() {
        return "reporters";
    }

    private void writeReporters(List<ReporterInfo> reporterInfos, PrintStream out) {
        reporterInfos.stream()
                .sorted(Comparator.comparing(ReporterInfo::getName))
                .forEach(info -> writeReporter(info, 4, out));
    }

    private void writeReporter(ReporterInfo info, int padding, PrintStream out) {

        writeReporterName(info, padding, out);
        writeReporterDescription(info, padding, out);

        out.println();
    }

    private void writeReporterName( ReporterInfo info, int paddingRight, PrintStream out ) {
        out.println(info.getName()+":");

    }

    private void writeReporterDescription( ReporterInfo info, int paddingLeft, PrintStream out ) {
        String[] lines = info.getDescription().split("\n");
        String paddingLeftStr = String.format("%1$"+paddingLeft+"s", "");
        Arrays.stream(lines).forEach(line -> out.println(paddingLeftStr+line.trim()));
    }
}
