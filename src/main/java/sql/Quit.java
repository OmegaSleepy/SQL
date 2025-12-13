package sql;

import common.CrashUtil;
import log.LogFileHandler;

import java.sql.SQLException;

import static log.Log.info;

/**
 * Holds simple scripts to help in runtime management.
 * @see #end(long start, long end)
 * **/
public class Quit {

    private Quit(){}

    /**
     * Safely closes the SQL connection, displays total runtime and begins the process of saving the log. This will also execute System.exit(0)
     * @see LogFileHandler#saveLogFiles()
     * **/
    public static void end (long start, long end){
        try {
            SqlConnection.connection.close();
        } catch (SQLException e) {
            CrashUtil.crash(e);
        }

        info("End of program");
        info("Program took %f seconds to execute".formatted((end - start)*1e-9));
        LogFileHandler.saveLogFiles();
    }

    /**
     * Does not close the SQL connection, displays total runtime and begins the process of saving the log. This will also execute System.exit(-1).
     * Should be used when SQL connection could not be established
     * @see SqlConnection
     * @see LogFileHandler#saveLogFiles()
     * **/
    public static void forceEnd(long start, long end){
        info("End of program");
        info("Program took %f seconds to execute".formatted((end - start)*1e-9));
        LogFileHandler.saveLogFiles();
        System.exit(-1);
    }


}
