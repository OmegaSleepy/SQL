package sql;

/**
 * Simple class with methods aimed at logging erros and crashes so the Logger can safely record all events
 * @see Log
 * */
public class CrashUtil {
    
    /**
     * Logging errors with this method will end the program, due to it saving and throwing the error.
     * @see Log#error(String) 
     * @see #catchError(Exception)
     * **/
    public static void crash (Exception e){
        Log.error(e.getMessage());
        Log.error("PROJECT CRASHED");
        Scripts.endScript(System.nanoTime());
        throw new RuntimeException(e);
    }
    /**
     * Logging errors with this method will not end the program, this is a wrapper for Log.error
     * @see Log#error(String)
     * @see #crash(Exception)  
     * */
    public static void catchError (Exception e){
        Log.error(e.getMessage());
    }
}
