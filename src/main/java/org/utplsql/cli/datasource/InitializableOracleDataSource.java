package org.utplsql.cli.datasource;

import oracle.jdbc.pool.OracleDataSource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class InitializableOracleDataSource implements DataSource {

    private final OracleDataSource delegate;
    private String initSql;

    public InitializableOracleDataSource() throws SQLException {
        this.delegate = new OracleDataSource();
    }

    // --- URL / credentials passthrough (mirrors what callers used before) ---

    public void setURL(String url) throws SQLException {
        delegate.setURL(url);
    }

    public void setUser(String user) {
        delegate.setUser(user);
    }

    public void setPassword(String password) {
        delegate.setPassword(password);
    }

    // --- Core DataSource methods ---

    @Override
    public Connection getConnection() throws SQLException {
        Connection con = delegate.getConnection();
        runInitSql(con);
        return con;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection con = delegate.getConnection(username, password);
        runInitSql(con);
        return con;
    }

    public void setConnectionInitSql(String sql) {
        this.initSql = sql;
    }

    // --- DataSource boilerplate ---

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(delegate)) return iface.cast(delegate);
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(delegate) || delegate.isWrapperFor(iface);
    }

    // --- Private helpers ---

    private void runInitSql(Connection con) throws SQLException {
        if (initSql != null && !initSql.isEmpty()) {
            try (CallableStatement stmt = con.prepareCall(initSql)) {
                stmt.execute();
            }
        }
    }
}