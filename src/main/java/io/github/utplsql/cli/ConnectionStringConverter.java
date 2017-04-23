package io.github.utplsql.cli;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

/**
 * Created by Vinicius on 21/04/2017.
 */
public class ConnectionStringConverter implements IStringConverter<ConnectionInfo> {

    @Override
    public ConnectionInfo convert(String s) {
        try {
            return new ConnectionInfo().parseConnectionString(s);
        } catch (ParameterException ignored) {
            return null;
        }
    }
}
