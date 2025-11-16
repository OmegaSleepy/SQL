package sql;

import java.sql.*;

import static sql.ConstantsKt.*;

public class SqlConnection {

    private SqlConnection(){}

    public static final java.sql.Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
