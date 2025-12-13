package sql;

import common.CrashUtil;
import log.Log;
import sql.query.Query;

import java.sql.DriverManager;
import java.sql.SQLException;

import static sql.Credentials.*;

/**
 * Holder class for the SQL Connection. Use this class' {@code static final connection} to access the server
 * @see #connection
 * @see sql.query.Query
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

        if(getUrl().isEmpty() || getUsername().isEmpty() || getPassword().isEmpty()){

            Credentials.inputCredentialFile("credentials.txt");

        }

        try {
            connection = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
        } catch (SQLException e) {
            common.CrashUtil.crashViolently(e);
        }

    }

    /**
     * Prints the saved credentials to the console
     * @see #initializeConnection()
     * **/
    public static void printCredentials(){
        String displayPassword = getPassword();
        if(getPassword().isBlank()) displayPassword = "NONE>";

        Log.info("url: %s, username: %s, password: %s".formatted(getUrl(), getUsername(), displayPassword));
    }

    /**
     * Returns the ping time in ms for the connection
     * @see #getInformation()
     * **/
    public static int getPing(){
        try {
            return connection.getNetworkTimeout();
        } catch (SQLException e) {
            common.CrashUtil.catchError(e);
        }
        return -1;
    }

    /**
     * Prints basic information about the connection, fails when the connection is invalid/not initialized.
     * Also fails if the ping is over 5 seconds
     * @see #getPing()
     * @see #printCredentials()
     * **/
    public static String getInformation() {
        try {
            if(connection.isValid(5)){
                return "Connection is stable and valid at URL %s ping %s ms".formatted(getUrl(), getPing());
            }
        } catch (SQLException e) {
            CrashUtil.catchError(e);
        }

        return "";
    }

    //Initializing the connection by default at standard class initialization
    static {
        initializeConnection();
    }
}
