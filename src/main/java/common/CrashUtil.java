package common;

import log.Log;
import sql.Credentials;
import sql.Quit;
import sql.SqlConnection;

import static sql.SqlConnection.LIBRARY_START;

/**
 * Simple class with methods aimed at logging errors and crashes so the Logger can safely record all events
 * @see log.Log
 * */
public class CrashUtil {

    public static boolean crashed = false;

    private CrashUtil(){}

    /**
     * Logging errors with this method will end the program, due to it saving and throwing the error. Will also close the connection safely
     * @see log.Log#error(String)
     * @see #catchError(Exception)
     * **/
    public static void crash (Exception e){
        crashed = true;
        log.Log.error(e.getMessage());
        log.Log.error("LIBRARY CRASHED");
        Quit.end(LIBRARY_START, System.nanoTime());
        throw new RuntimeException(e);
    }
    /**
     * Logging errors with this method will not end the program, this is a wrapper for Log.error
     * @see log.Log#error(String)
     * @see #crash(Exception)  
     * */
    public static void catchError (Exception e){
        log.Log.error(e.getMessage());
    }

    /**
     * THIS will not close the connection. Logging errors with this method will end the program, due to it saving and throwing the error.
     * @see log.Log#error(String)
     * @see Credentials#inputCredentialFile(String path)
     * **/
    public static void crashViolently(Exception e){
        crashed = true;
        log.Log.error(e.getMessage());
        Log.error("LIBRARY CRASHED");
        Quit.forceEnd(LIBRARY_START, System.nanoTime());
        throw new RuntimeException(e);
    }
}
