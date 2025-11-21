package sql;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public static java.sql.Connection connection;

    static long systemStart = System.nanoTime();

    public static void initializeConnection(){
//        String url = Credentials.url;
//        String user = Credentials.url;
//        String password = Credentials.url;

        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String password = "password";
        try {
            connection = DriverManager.getConnection(url, user, password);
            Logger.getLogger("sqlite-jdbc").setLevel(Level.OFF);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        initializeConnection();
    }
}
