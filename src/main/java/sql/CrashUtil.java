package sql;

public class CrashUtil {
    public static void crash (Exception e){
        Log.error(e.getMessage());
        Log.error("PROJECT CRASHED");
        Scripts.endScript(System.nanoTime());
        throw new RuntimeException(e);
    }
    public static void catchError (Exception e){
        Log.error(e.getMessage());
    }
}
