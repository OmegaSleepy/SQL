package sql;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static sql.Credentials.*;

/**
 * Holder class for the SQL Connection. Use this class' {@code static final connection} to access the server
 * @see #connection
 * @see Query
 * **/
public class SqlConnection {

    private SqlConnection(){}

    /**
     * Used to access the server
     * @see SqlConnection
     * @see Query
     * **/
    public static java.sql.Connection connection;

    public static final long LIBRARY_START = System.nanoTime();

    public static void initializeConnection(){
        try {
            connection = DriverManager.getConnection(url, username, password);
            Logger.getLogger("sqlite-jdbc").setLevel(Level.OFF);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        initializeConnection();
    }
}
