package sql;

import java.sql.SQLException;

public class Scripts {

    static void dropEverything(){
        Queries.queryFromFile(FileUtil.getScriptFile("premade/drop.txt"));
    }

    static void generateEverything(){
        Queries.queryFromFile(FileUtil.getScriptFile("premade/generate.txt"));
    }

    static void runScript(){
        Queries.queryFromFile(FileUtil.getScriptFile("working.txt"));
    }

    static void endScript(){
        try {
            SqlConnection.connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Log.logSimple.accept("End of program");
        Log.saveToFile();
    }

    public static void main (String[] args) {
        ScriptCommands arg;

        arg = ScriptCommands.valueOf(args[0].toUpperCase());


        switch (arg){
            case DROP -> dropEverything();
            case GENERATE -> generateEverything();
            case RUN_SCRIPT -> runScript();
        }

        endScript();



    }



}
