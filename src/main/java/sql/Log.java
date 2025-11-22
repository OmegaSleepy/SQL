package sql;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static sql.Settings.*;


/**
 * Simple class aimed at logging all activities of the program, holds an internal buffer, saves all logged actions during program execution, displays select queries,
 * clears old logs
 * @see #buffer
 * @see #log(String, String)
 * @see #logSelect
 * @see #saveLogFiles()
 * @see #cleanUp()
 * @see #clearAllLogs()
 * */
public class Log {

    public static final String LOG_VERSION = "1.2.0";
    public static final int MAX_LOGS = 32;
    public static final String LOG_DIR = "logs";

    private Log(){}

    /**
     * Holds all logged information for {@code saveLogFiles} to write to a log and the latest log
     * @see #writeToFile(File)
     * @see #log(String, String)
     * */
    private static final List<String> buffer = new ArrayList<>();

    /**
     * Deletes all logs in the {@code LOG_DIR} folder. For chronological deletion check {@code cleanUP}
     * @see #LOG_DIR
     * @see #cleanUp()
     * */
    public static void clearAllLogs () {
        File folder = new File(LOG_DIR);

        if (!folder.exists() || !folder.isDirectory()) return;

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file == null) {
                return;
            }
            if (file.isFile()) {
                file.delete();
            }
        }
        Log.warn("Cleared logs");
    }
    /**
     * Deletes all logs in the {@code LOG_DIR} folder based on how old they are. It will delete enough files so there are less or equal to {@code MAX_LOGS}.
     * For full clean-up, check {@code clearAllLogs}
     * @see #clearAllLogs()
     * @see #LOG_DIR
     * @see #MAX_LOGS
     * */
    public static void cleanUp () {
        File folder = new File(LOG_DIR);

        if (!folder.exists() || !folder.isDirectory()) return;

        int logCount = folder.listFiles().length;

        File[] logs = folder.listFiles();

        info("There are %d logs in memory".formatted(logCount));

        if (logCount > MAX_LOGS) {
            error("There are over %d logs, deleting %d oldest".formatted(MAX_LOGS, logCount - MAX_LOGS));

            for (int i = 0; i < logCount - MAX_LOGS; i++) {
                assert logs != null;
                warn("Deleting %s".formatted(logs[i].getName()));
                logs[i].delete();
            }

        }

    }

    /**
     * Logs info action with {@code GREEN} color, check the constants .kt file for the value
     * @see Settings#GREEN
     * @see #log(String message, String color)
     * */
    public static void info (String message) {
        log(message, GREEN);
        infoCount++;
    }

    /**
        * Logs execution action with {@code BLUE} color, check the constants .kt file for the value
        * @see Settings#BLUE
        * @see #log(String message, String color)
     * */
    public static void exec (String message) {
        log(message, BLUE);
        execCount++;
    }

    /**
     * Logs warn action with {@code YELLOW} color, check the constants .kt file for the value
     * @see Settings#YELLOW
     * @see #log(String message, String color)
     * */

    public static void warn (String message) {
        log(message, YELLOW);
        warnCount++;
    }

    /**
     * Logs error action with {@code RED} color, check the constants .kt file for the value
     * @see Settings#RED
     * @see #log(String message, String color)
     * */
    public static void error (String message) {
        log(message, RED);
        errorCount++;
    }


    static int infoCount;
    static int execCount;
    static int warnCount;
    static int errorCount;

    /**
     * Saves {@code buffer} to two .log files. One is named with a timestamp and the second is latest.log.
     * @see #buffer
     * @see #writeToFile
     * */
    public static void saveLogFiles () {

        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) {
                if (dir.mkdir()) {
                    info("Log folder generated at %s".formatted(dir.getAbsolutePath()));
                }
            }

            String filename = LocalDateTime.now().format(Objects.requireNonNull(FILE)) + ".log";
            File file = new File(dir, filename);
            File latest = new File(dir, "latest.log");

            if(file.createNewFile()) {
                info("Created log file at %s".formatted(file.getAbsolutePath()));
            }
            if(latest.createNewFile()) {
                info("Created latest log file at %s".formatted(file.getAbsolutePath()));
            }

            info(getLogVersion());
            info(getLogCount());

            writeToFile(file).createNewFile();
            writeToFile(latest).createNewFile();

            info("Saved log to %s".formatted(dir + File.separator + filename));
            info("Saved latest to %s".formatted(latest + File.separator + filename));


        } catch (IOException e) {
            CrashUtil.crash(e);
        }

    }

    /**
     * Writes the {@code buffer} values into a {@code .log} file and returns the file. It replaces the ansi color codes with words
     * @return File {@code .log}
     * @see #stripAnsi(String message)
     * @see #saveLogFiles()
     * */
    private static File writeToFile(File file){
        try (FileWriter writer = new FileWriter(file)) {
            for (String line : buffer) {
                writer.write(stripAnsi(line) + System.lineSeparator());
            }
        } catch (IOException e) {
            CrashUtil.crash(e);
        }
        return file;
    }

    /**
     * Returns the log version in a neat format from the constants .kt file
     * @see #LOG_VERSION
     * */
    private static String getLogVersion () {
        return "LOG VERSION=%s | LOG DIR=%s "
                .formatted(LOG_VERSION, LOG_DIR);
    }

    /**
     * Returns the total amount of all log lines by type in a neat format
     * @return String logCount
     * @see #saveLogFiles()
     * @see #info(String message)
     * @see #exec(String message)
     * @see #error(String message)
     * @see #warn(String message)
          * */
    private static String getLogCount () {
        return "INFO=%d | EXEC=%d | WARN=%d | ERROR=%d"
                .formatted(infoCount, execCount, errorCount, warnCount);
    }

    /**
     * Used in {@code .log} file creation. Removes Ansi values and replaces them with {@code String} values
     * @see #saveLogFiles()
     * @see #info(String message)
     * @see #exec(String message)
     * @see #error(String message)
     * @see #warn(String message)
     * */

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
     * @see #info(String message)
     * @see #exec(String message)
     * @see #error(String message)
     * @see #warn(String message)
     * */
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
     * @see Query#getResult(String SQL)
     * */
    public static final Consumer<List<String[]>> logSelect = rows -> {

        if (rows == null || rows.isEmpty()) return;


        //Sometimes queryResult can return malformed data with inconsistent column count,
        // this code block ensures that the absolute max is found

        int columns = rows.stream()
                .mapToInt(r -> r.length)
                .max()
                .orElse(0);


        int[] maxWidthPerCell = new int[columns];

        // compute max width for each column
        for (String[] row : rows) {
            for (int i = 0; i < columns; i++) {
                //if the row cell is null or is out of scope for the row then make it null, otherwise get the value
                String cell = (i < row.length && row[i] != null) ? row[i] : "null";
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
     * @see #logSelect
     * */
    private static StringBuilder getSpaceFormatedString (String[] row, int columns, int[] maxWidthPerCell) {
        StringBuilder formattedRow = new StringBuilder();

        for (int i = 0; i < columns; i++) {
            //if the row cell is null or is out of scope for the row then make it null, otherwise get the value
            String cell = (i < row.length && row[i] != null) ? row[i] : "null";
            //Add spaces to fill the printed cell with enough white space so the vertical lines match
            formattedRow.append(String.format("%-" + maxWidthPerCell[i] + "s", cell));
            //if it is not the last cell, append a vertical line
            if (i < columns - 1) formattedRow.append(" | ");
        }
        return formattedRow;
    }


    /**
     * Consumer for SQL code execution (CREATE, INSERT, UPDATE, DELETE).
     * @see Query#executeExpression(String query)
     * */
    public static final Consumer<String> logSQL = Log::exec;
}
