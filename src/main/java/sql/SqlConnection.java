package sql;

import java.io.File;
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

    /**
     * Connects the lib to the database and reads a credentials file.
     * @see Credentials
     * **/
    public static void initializeConnection(){
        if(getUrl().isEmpty() || getUsername().isEmpty() || getPassword().isEmpty()){
            Credentials.inputCredentialFile(new File("credentials.txt"));
        }

        try {
            connection = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
            Logger.getLogger("sqlite-jdbc").setLevel(Level.OFF);
        } catch (SQLException e) {
            CrashUtil.crashViolently(e);
        }
    }

    //Initializing the connection by default at standard class initialization
    static {
        initializeConnection();
    }
}
