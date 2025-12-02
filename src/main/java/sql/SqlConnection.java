package sql;

import java.sql.DriverManager;
import java.sql.SQLException;

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
    
    /**
     * Holds the exact time the Library initializes as {@code nanoTime}. Used for profiling
     * @see Script#end(long start, long end) 
     * **/
    public static final long LIBRARY_START = System.nanoTime();

    /**
     * Connects the lib to the database and reads a credentials file.
     * @see Credentials
     * **/
    public static void initializeConnection(){
        Log.info("url: %s, username: %s, password: %s".formatted(getUrl(), getUsername(), getPassword()));

        if(getUrl().isEmpty() || getUsername().isEmpty() || getPassword().isEmpty()){

            Credentials.inputCredentialFile("credentials.txt");

        }

        try {
            connection = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
//            Logger.getLogger("sqlite-jdbc").setLevel(Level.OFF);
        } catch (SQLException e) {
            CrashUtil.crashViolently(e);
        }

    }

    //Initializing the connection by default at standard class initialization
    static {
        initializeConnection();
    }
}
