package sql;

import java.sql.*;

import static sql.CredentialsKt.*;

/**
 * Holder class for the SQL Connection. Use this class' {@code static final connection} to access the server
 * @see #connection
 * @see Queries
 * **/
public class SqlConnection {

    private SqlConnection(){}

    /**
     * Used to access the server
     * @see SqlConnection
     * @see Queries
     * **/
    public static final java.sql.Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
