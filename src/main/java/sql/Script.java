package sql;

import java.sql.SQLException;

import static sql.Log.info;

public class Script {
    public static void endScript(long start, long end){
        try {
            SqlConnection.connection.close();
        } catch (SQLException e) {
            CrashUtil.crash(e);
        }

        info("End of program");
        info("Program took %f seconds to execute".formatted((end - start)*1e-9));
        Log.saveLogFiles();

    }
}
