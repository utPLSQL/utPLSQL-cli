package io.github.utplsql.cli;

import com.beust.jcommander.IStringConverter;

/**
 * Created by Vinicius on 21/04/2017.
 */
public class ConnectionStringConverter implements IStringConverter<ConnectionInfo> {

    @Override
    public ConnectionInfo convert(String s) {
        return new ConnectionInfo().parseConnectionString(s);
    }
}
