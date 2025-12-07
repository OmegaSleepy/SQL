package sql;

import java.sql.PreparedStatement;
import java.time.format.DateTimeFormatter;

/**
 * Holds lookup values for the rest of the library which can be used its scope too.
 * **/
public class Settings {

    private Settings(){}

    /**
     * Used to stop coloring everything past the timestamp
     * @see Log#log(String message, String color)
     * **/
    public static String RESET = "\u001B[0m";
    /**
     * Used for coloring the timestamp of info operations
     * @see Log#info(String message)
     * **/
    public static String GREEN = "\u001B[32m";
    /**
     * Used for coloring the timestamp of execution operations
     * @see Log#exec(String message)
     * **/
    public static String BLUE = "\u001B[34m";
    /**
     * Used for coloring the timestamp of warn operations
     * @see Log#warn(String message)
     * **/
    public static String YELLOW = "\u001B[33m";
    /**
     * Used for coloring the timestamp of error operations
     * @see Log#error(String message)
     * **/
    public static String RED = "\u001B[31m";


    public static DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss:ms");
    public static DateTimeFormatter FILE = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Used to control the printing of queries
     * @see Query#executeExpression(String SQL)
     * @see Log#logSQL
     * **/
    public static boolean logQueries = true;
    /**
     * Used to control auto displaying of query results to the console.
     * This DOES NOT disable printing to the console
     * @see Query#selectOperation(PreparedStatement)
     * @see Log#logSelect
     * **/
    public static boolean logResults = true;


}
