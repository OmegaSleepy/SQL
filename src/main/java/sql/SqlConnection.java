package sql;

import common.CrashUtil;
import log.Log;
import log.LogFileHandler;
import sql.query.Query;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static log.Log.info;
import static sql.Credentials.*;

/**
 * Holder class for the SQL Connection. Use this class' {@code static final connection} to access the server
 * @see #connection
 * @see sql.query.Query
 * **/
@Deprecated
public class SqlConnection {

    private String url = "";
    private String username = "";
    private String password = "";

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Used to access the server
     * @see SqlConnection
     * @see Query
     * **/
    public java.sql.Connection connection;

    /**
     * Holds the exact time the Library initializes as {@code nanoTime}. Used for profiling
     * @see Quit#end(long start, long end)
     * **/
    public final long CONNECTION_START;

    public static final long LIBRARY_START = System.nanoTime();

    public SqlConnection(Path crdentialsPath) {

        initializeConnection(crdentialsPath);
        CONNECTION_START = System.nanoTime();

    }

    public void closeConnection(){
        try {
            Log.exec("Closed connection " + this);
            connection.close();
        } catch (SQLException e) {
            CrashUtil.crash(e);
        }

//        info("End of program");
//        info("Program took %f seconds to execute".formatted((CONNECTION_START - System.currentTimeMillis())*1e-9));
//        LogFileHandler.saveLogFiles();
    }

    /**
     * Connects the lib to the database and reads a credentials file.
     * @see Credentials
     * **/
    public void initializeConnection(Path credentialsPath){

        if(getUrl().isEmpty() || getUsername().isEmpty() || getPassword().isEmpty()){

            inputCredentialFile(credentialsPath);

        }

        try {
            connection = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
        } catch (SQLException e) {
            common.CrashUtil.crashViolently(e);
        }

    }

    /**
     * Prints the saved credentials to the console
     * @see #initializeConnection(Path path)
     * **/
    public void printCredentials(){
        String displayPassword = getPassword();
        if(getPassword().isBlank()) displayPassword = "NONE>";

        Log.info("url: %s, username: %s, password: %s".formatted(getUrl(), getUsername(), displayPassword));
    }

    /**
     * Returns the ping time in ms for the connection
     * @see #getInformation()
     * **/
    public int getPing(){
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
    public String getInformation() {
        try {
            if(connection.isValid(5)){
                return "Connection is stable and valid at URL %s ping %s ms".formatted(getUrl(), getPing());
            }
        } catch (SQLException e) {
            CrashUtil.catchError(e);
        }

        return "";
    }

    private static final String urlPlaceholder = "<URL> ex. jdbc:mysql://localhost:3306/";
    private static final String usernamePlaceholder = "<USERNAME> ex. root";
    private static final String passholderPlaceholder = "<PASSWORD> ex. password";


    /**
     * Used for overriding the credentials of the connection
     *
     **/
    public void inputCredentialFile(Path credentialsPath) {

        if (!Files.exists(credentialsPath)) {
            credentialsDoesNotExistError(credentialsPath);
        }

        try {
            var cred = Files.readAllLines(credentialsPath);
            url = cred.get(0);
            username = cred.get(1);
            //some connections are without password, we need to account for those by checking credentials.txt if it has a password or not
            password = "";
            if (cred.size() > 2) {
                password = cred.get(2);
            }
            log.Log.info("Loaded credentials from file.");

            checkValid();

        } catch (IOException e) {
            common.CrashUtil.crashViolently(e);
        }

    }

    /**
     * Used to check if the credential values are not the placeholder ones and if so it ends the project.
     *
     * @see #passholderPlaceholder
     * @see #usernamePlaceholder
     * @see #urlPlaceholder
     *
     **/
    private void checkValid() {

        boolean isSuitable = true;
        if (Objects.equals(urlPlaceholder, url)) {
            log.Log.error("URL is the placeholder value, please change!");
            isSuitable = false;
        }
        if (Objects.equals(usernamePlaceholder, username)) {
            log.Log.error("Username is the placeholder value, please change!");
            isSuitable = false;
        }
        if (Objects.equals(passholderPlaceholder, password)) {
            log.Log.error("Password is the placeholder value, please change!");
            isSuitable = false;
        }
        if (!isSuitable) {
            common.CrashUtil.crashViolently(new RuntimeException("Error in credentials! Check logs for the error!"));
        }
    }

    /**
     * Used when the credential file does not exist and creates a new one with placeholder values. Also ends the project.
     *
     * @see #inputCredentialFile(Path path)
     *
     **/
    private static void credentialsDoesNotExistError(Path path) {
        try {
            Log.error("Credentials.txt does not exist! Creating one...");
            Files.createFile(path);

            Files.write(path, List.of(urlPlaceholder, usernamePlaceholder, passholderPlaceholder));

            common.CrashUtil.crashViolently(
                    new RuntimeException(
                            "Credential file did not exist. Go to %s and fill in your information".formatted(path)
                    )
            );

        } catch (IOException e) {
            CrashUtil.crash(e);
        }
    }

    @Override
    public String toString () {
        return connection.toString();
    }
}
