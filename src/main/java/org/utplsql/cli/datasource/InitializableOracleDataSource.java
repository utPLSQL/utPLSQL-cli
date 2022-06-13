package org.utplsql.cli.datasource;

import oracle.jdbc.pool.OracleDataSource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class InitializableOracleDataSource extends OracleDataSource {

    private String initSql;

    public InitializableOracleDataSource() throws SQLException {
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection con = super.getConnection();

        if ( initSql != null && !initSql.isEmpty() ) {
            try (CallableStatement stmt = con.prepareCall(initSql)) {
                stmt.execute();
            }
        }

        return con;
    }

    public void setConnectionInitSql( String sql ) {
        this.initSql = sql;
    }
}
