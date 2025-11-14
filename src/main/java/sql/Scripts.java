package sql;

import game.GenshinCharacter;
import game.Holder;

import java.sql.SQLException;

public class Scripts {

    static void dropEverything(){
        Queries.queryFromFile(FileUtil.getScriptFile("premade/drop.txt"));
    }

    static void generateEverything(){
        Queries.queryFromSequence(FileUtil.getLineFile("refresh"));
    }

    static void runScript(){
        Queries.queryFromFile(FileUtil.getScriptFile("working.txt"));
    }

    static void endScript(long end){
        try {
            SqlConnection.connection.close();
        } catch (SQLException e) {
            CrashUtil.crashHandler(e);
        }

        Log.info("End of program");
        Log.info("Program took %f seconds to execute".formatted((end - start)*1e-9));
        Log.saveToFile();

    }

    static void clearLogs(){
        Log.deleteLogs();
    }

    static void initialize (){
        GenshinCharacter.getAllCharacters();
        Holder.innitialize();
    }

    static void runSequence (){
        Queries.queryFromSequence(FileUtil.getLineFile("linked"));
    }


    public static long start;

    public static void main (String[] args) {
        Queries.queryResult("");
        start = System.nanoTime();

        Log.cleanUp();

        for (String arg: args){
            switch (ScriptCommands.valueOf(arg)){
                case DROP -> dropEverything();
                case GENERATE -> generateEverything();
                case RUN_SCRIPT -> runScript();
                case CLEAR_LOGS -> clearLogs();
                case INITIALIZATION -> initialize();
                case SEQUENCE -> runSequence();
            }
        }

        long end = System.nanoTime();
        endScript(end);

    }



}
