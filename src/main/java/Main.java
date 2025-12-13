import common.CrashUtil;
import log.Log;
import log.LogFileHandler;
import sql.query.Query;

public class Main {
    public static void main (String[] args) {

        Log.MAX_LOGS = 6;

        LogFileHandler.cleanUp();

        for (int i = 0; i < 320; i++) {
            Log.warn("I want to test this code");
        }
        Query.fromString("Select title from musicindex.musicspy");


        LogFileHandler.saveLogFiles();
        CrashUtil.crash(new RuntimeException("uwu"));

    }
}
