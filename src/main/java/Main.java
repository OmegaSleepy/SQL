import common.CrashUtil;
import log.Log;
import log.LogFileHandler;
import sql.SqlConnection;
import sql.query.Query;

import java.nio.file.Path;

public class Main {
    public static void main (String[] args) {

        Log.MAX_LOGS = 6;

        LogFileHandler.cleanUp();

        for (int i = 0; i < 10; i++) {
            Log.warn("I want to test this code");
        }

        SqlConnection connection = new SqlConnection(Path.of("credentials.txt"));

        Query.fromString("select title, artist from musicindex.musicspy", connection);
        connection.closeConnection();


        CrashUtil.crash(new RuntimeException("uwu"));

    }
}
