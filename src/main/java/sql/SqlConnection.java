package sql;

import java.sql.*;

import static sql.FileUtil.getScriptFile;
import static sql.Queries.queryFromFile;
import static sql.Log.*;
import static sql.ConstantsKt.*;
import static sql.Queries.queryResult;

public class SqlConnection {

    public static java.sql.Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
