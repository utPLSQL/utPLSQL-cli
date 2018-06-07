package org.utplsql.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.utplsql.api.compatibility.CompatibilityProxy;
import org.utplsql.api.reporter.ReporterFactory;
import org.utplsql.api.reporter.inspect.ReporterInfo;
import org.utplsql.api.reporter.inspect.ReporterInspector;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

            ReporterInspector.create(reporterFactory, con).getReporterInfos().stream()
                    .sorted(Comparator.comparing(ReporterInfo::getName))
                    .forEach(r -> {
                        System.out.println(r.getName() + " (" + r.getType().name() + "): " + r.getDescription());
                        System.out.println();
                    });
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
}
