package sql;

import game.GenshinCharacter;
import game.Holder;

import java.sql.SQLException;

import static sql.ScriptCommands.*;

public class Scripts {

    static void dropEverything(){
        Queries.queryFromFile(FileUtil.getScriptFile("premade/drop.txt"));
    }

    static void generateEverything(){
        Queries.queryFromSequence(FileUtil.getLineFile("refresh"));
    }

    static void runScript(){
        Queries.queryFromFile(FileUtil.getScriptFile("default_database.txt"));
    }

    static void endScript(){
        try {
            SqlConnection.connection.close();
        } catch (SQLException e) {
            CrashUtil.crashHandler(e);
        }

        Log.info("End of program");
        Log.saveToFile();
    }

    static void clearLogs(){
        Log.error("Cleared logs");
        Log.clearLogs();
    }

    static void initialize (){
        GenshinCharacter.getAllCharacters();
        Holder.innitialize();
    }

    static void runSequence (){
        Queries.queryFromSequence(FileUtil.getLineFile("linked"));
    }


    public static void main (String[] args) {
        Queries.queryResult("");

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


        endScript();

    }



}
