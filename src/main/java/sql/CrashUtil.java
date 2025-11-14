package sql;

public class CrashUtil {
    public static void crashHandler(Exception e){
        Log.error(e.getMessage());
        Log.error("PROJECT CRASHED");
        Scripts.endScript();
        throw new RuntimeException(e);
    }
}
