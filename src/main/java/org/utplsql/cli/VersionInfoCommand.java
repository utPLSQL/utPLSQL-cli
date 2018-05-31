package org.utplsql.cli;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.utplsql.api.DBHelper;
import org.utplsql.api.JavaApiVersionInfo;
import org.utplsql.api.Version;
import org.utplsql.api.exception.UtPLSQLNotInstalledException;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@Parameters(separators = "=", commandDescription = "prints version information of cli, java-api and - if connection is given - database utPLSQL framework")
public class VersionInfoCommand {

    @Parameter(
            converter = ConnectionInfo.ConnectionStringConverter.class,
            variableArity = true,
            description = "<user>/<password>@//<host>[:<port>]/<service> OR <user>/<password>@<TNSName> OR <user>/<password>@<host>:<port>:<SID>")
    private List<ConnectionInfo> connectionInfoList = new ArrayList<>();

    public ConnectionInfo getConnectionInfo() {
        if ( connectionInfoList != null && connectionInfoList.size() > 0 )
            return connectionInfoList.get(0);
        else
            return null;
    }

    public int run() throws Exception {

        System.out.println(CliVersionInfo.getInfo());
        System.out.println("Java-API " + JavaApiVersionInfo.getVersion());

        ConnectionInfo ci = getConnectionInfo();
        if ( ci != null ) {
            // TODO: Ora-check
            ci.setMaxConnections(1);
            try (Connection con = ci.getConnection()) {
                Version v = DBHelper.getDatabaseFrameworkVersion( con );
                System.out.println("utPLSQL " + v.getNormalizedString());
            }
            catch ( UtPLSQLNotInstalledException e ) {
                System.out.println("utPLSQL framework is not installed in database.");
            }
            catch ( Exception e ) {
                e.printStackTrace();
                return 1;
            }
        }

        return 0;
    }
}
