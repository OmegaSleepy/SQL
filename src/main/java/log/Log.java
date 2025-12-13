package log;

import sql.query.Query;
import common.Settings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static common.Settings.*;

/**
 * Simple class aimed at logging all activities of the program, holds an internal buffer, saves all logged actions during program execution, displays select queries,
 * clears old logs
 *
 * @see #buffer
 * @see #log(String, String)
 * @see #logSelect
 * @see LogFileHandler#saveLogFiles()
 * @see LogFileHandler#cleanUp()
 * @see LogFileHandler#clearAllLogs()
 *
 */
public class Log {

    public static String LOG_VERSION = "1.5.0";
    public static int MAX_LOGS = 16;
    public static String LOG_DIR = "logs";
    public static String CRASH_DIR = "crash";
    public static String SUCCESSFUL_DIR = "regular";


    private Log () {
    }

    /**
     * Holds all logged information for {@code saveLogFiles} to write to a log and the latest log
     *
     * @see #log(String, String)
     * @see LogFileHandler#saveLogFiles()
     *
     */
    private static final List<String> buffer = new ArrayList<>();

    public static List<String> getBuffer () {
        return buffer;
    }



    /**
     * Logs info action with {@code GREEN} color, check the constants .kt file for the value
     *
     * @see Settings#GREEN
     * @see #log(String message, String color)
     *
     */
    public static void info (String message) {
        log(message, GREEN);
        infoCount++;
    }

    /**
     * Logs execution action with {@code BLUE} color, check the constants .kt file for the value
     *
     * @see Settings#BLUE
     * @see #log(String message, String color)
     *
     */
    public static void exec (String message) {
        log(message, BLUE);
        execCount++;
    }

    /**
     * Logs warn action with {@code YELLOW} color, check the constants .kt file for the value
     *
     * @see Settings#YELLOW
     * @see #log(String message, String color)
     *
     */

    public static void warn (String message) {
        log(message, YELLOW);
        warnCount++;
    }

    /**
     * Logs error action with {@code RED} color, check the constants .kt file for the value
     *
     * @see Settings#RED
     * @see #log(String message, String color)
     *
     */
    public static void error (String message) {
        log(message, RED);
        errorCount++;
    }


    static int infoCount;
    static int execCount;
    static int warnCount;
    static int errorCount;



    /**
     * Returns the log version in a neat format from the constants .kt file
     *
     * @see #LOG_VERSION
     *
     */
    public static String getLogVersion () {
        return "LOG VERSION=%s | LOG DIR=%s"
                .formatted(LOG_VERSION, LOG_DIR);
    }

    /**
     * Returns the total amount of all log lines by type in a neat format
     *
     * @return String logCount
     * @see LogFileHandler#saveLogFiles()
     * @see #info(String message)
     * @see #exec(String message)
     * @see #error(String message)
     * @see #warn(String message)
     *
     */
    public static String getLogCount () {
        return "INFO=%d | EXEC=%d | WARN=%d | ERROR=%d"
                .formatted(infoCount, execCount, warnCount, errorCount);
    }

    /**
     * Used in {@code .log} file creation. Removes Ansi values and replaces them with {@code String} values
     *
     * @see LogFileHandler#saveLogFiles()
     * @see #info(String message)
     * @see #exec(String message)
     * @see #error(String message)
     * @see #warn(String message)
     *
     */

    public static String stripAnsi (String message) {

        //No need for more log message types
        message = message.replace(GREEN, "[INFO] ");
        message = message.replace(BLUE, "[EXEC] ");
        message = message.replace(YELLOW, "[WARN] ");
        message = message.replace(RED, "[ERROR] ");

        return message;
    }

    /**
     * Saves an action {@code String} with a specific colored timestamp. Methods bellow are use-cases with specific color timestamps
     *
     * @see #info(String message)
     * @see #exec(String message)
     * @see #error(String message)
     * @see #warn(String message)
     *
     */
    private static void log (String message, String color) {
        String timestamp = "[" + LocalDateTime.now().format(Objects.requireNonNull(TIME)) + "] ";

        // Print to console (colored)
        System.out.println(color + timestamp + RESET + message);
        //Saving without RESET ensures we don't have to remove it later when saving to a file
        //Still adding color so we can replace that with capitalized MESSAGE
        //Yes OOP can be used here to replace the color value, but that will cause speed problems and will not benefit the program in any way
        buffer.add(color + timestamp + message);
    }

    /**
     * Method used for quick and pretty display printing of {@code SELECT} type queries, usually directly called from {@code selectOperation}
     *
     * @see Query#fromString(String SQL)
     *
     */
    public static final Consumer<List<String[]>> logSelect = rows -> {

        if (rows == null || rows.isEmpty()) return;


        //Sometimes queryResult can return malformed data with inconsistent column count,
        // this code block ensures that the absolute max is found

        Predicate<String[]> checkNullTail = strings -> {
            if (strings[strings.length - 1] == null) return true;
            return !strings[strings.length - 1].isBlank();
        };

        int columns = rows.stream()
                .filter(checkNullTail)
                .mapToInt(r -> r.length)
                .max()
                .orElse(0);


        int[] maxWidthPerCell = new int[columns];

        // compute max width for each column
        for (String[] row : rows) {
            for (int i = 0; i < columns; i++) {
                //if the row cell is null or is out of scope for the row then make it null, otherwise get the value
                String cell = (i < row.length && row[i] != null) ? row[i] : " ";
                maxWidthPerCell[i] = Math.max(maxWidthPerCell[i], cell.length());
            }
        }

        // print rows with proper alignment
        for (String[] row : rows) {
            StringBuilder formattedRow = getSpaceFormatedString(row, columns, maxWidthPerCell);
            info(formattedRow.toString());
        }
    };

    /**
     * Generates a well formated view for a row with some size
     *
     * @see #logSelect
     *
     */
    private static StringBuilder getSpaceFormatedString (String[] row, int columns, int[] maxWidthPerCell) {
        StringBuilder formattedRow = new StringBuilder();


        for (int i = 0; i < columns; i++) {
            String cell = (i < row.length && row[i] != null) ? row[i] : " ";

            formattedRow.append(" ");

            int width = maxWidthPerCell[i];

            formattedRow.append(String.format("%-" + (width) + "s", cell));


            formattedRow.append(" ");
            if (i < columns - 1) formattedRow.append(" | ");
        }
        return formattedRow;
    }


    /**
     * Consumer for SQL code execution (CREATE, INSERT, UPDATE, DELETE).
     *
     * @see Query#executeExpression(String query)
     */
    public static final Consumer<String> logSQL = Log::exec;

}
