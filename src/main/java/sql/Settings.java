package sql;

import java.time.format.DateTimeFormatter;

public class Settings {
    public static String RESET = "\u001B[0m";
    public static String GREEN = "\u001B[32m";
    public static String BLUE = "\u001B[34m";
    public static String RED = "\u001B[31m";
    public static String YELLOW = "\u001B[33m";

    public static DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss:ms");
    public static DateTimeFormatter FILE = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static boolean logQueries = true;
    public static boolean logResults = true;


}
