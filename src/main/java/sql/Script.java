package sql;

import java.sql.SQLException;

import static sql.Log.info;

/**
 * Holds simple scripts to help in runtime management.
 * @see #end(long start, long end)
 * **/
public class Script {

    private Script(){}

    /**
     * Safely closes the SQL connection, displays total runtime and begins the process of saving the log. This will also execute System.exit(0)
     * @see Log#saveLogFiles()
     * **/
    public static void end (long start, long end){
        try {
            SqlConnection.connection.close();
        } catch (SQLException e) {
            CrashUtil.crash(e);
        }

        info("End of program");
        info("Program took %f seconds to execute".formatted((end - start)*1e-9));
        Log.saveLogFiles();
    }

    /**
     * Does not close the SQL connection, displays total runtime and begins the process of saving the log. This will also execute System.exit(-1).
     * Should be used when SQL connection could not be established
     * @see SqlConnection
     * @see Log#saveLogFiles()
     * **/
    public static void forceEnd(long start, long end){
        info("End of program");
        info("Program took %f seconds to execute".formatted((end - start)*1e-9));
        Log.saveLogFiles();
        System.exit(-1);
    }


}
