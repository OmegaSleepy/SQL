package sql;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static sql.Settings.*;

/**
 * Simple class aimed at logging all activities of the program, holds an internal buffer, saves all logged actions during program execution, displays select queries,
 * clears old logs
 *
 * @see #buffer
 * @see #log(String, String)
 * @see #logSelect
 * @see #saveLogFiles()
 * @see #cleanUp()
 * @see #clearAllLogs()
 *
 */
public class Log {

    public static String LOG_VERSION = "1.4.0";
    public static int MAX_LOGS = 2;
    public static String LOG_DIR = "logs";
    public static String CRASH_DIR = "crash";


    private Log () {
    }

    /**
     * Holds all logged information for {@code saveLogFiles} to write to a log and the latest log
     *
     * @see #log(String, String)
     * @see #saveLogFiles()
     *
     */
    private static final List<String> buffer = new ArrayList<>();

    /**
     * Deletes all logs in the {@code LOG_DIR} folder. For chronological deletion check {@code cleanUp}
     *
     * @see #LOG_DIR
     * @see #cleanUp()
     *
     */
    public static void clearAllLogs () {

        try {
            Files.walk(Paths.get(LOG_DIR)).forEach(t -> {
                try {
                    if (!Files.isDirectory(t))
                        Files.delete(t);
                } catch (IOException e) {
                    CrashUtil.crash(e);
                }
            });
            Files.walk(Paths.get(LOG_DIR,CRASH_DIR)).forEach(t -> {
                try{
                    if (!Files.isDirectory(t))
                        Files.delete(t);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            CrashUtil.crash(e);
        }

        Log.warn("Cleared logs");
    }

    /**
     * Deletes logs in the {@code LOG_DIR} folder based on how old they are.
     * It will delete enough files so there are less or equal to {@code MAX_LOGS}.
     * For full clean-up, check {@code clearAllLogs}
     *
     * @see #clearAllLogs()
     * @see #LOG_DIR
     * @see #MAX_LOGS
     *
     */
    public static void cleanUp () {

        if (MAX_LOGS <= 0) {
            error("MAX_LOGS is set to %s, will not clean up files.".formatted(MAX_LOGS));
            error("Change value to a positive int.");
            return;
        }

        try {
            clearDir(Path.of(LOG_DIR));
            clearDir(Path.of(LOG_DIR,CRASH_DIR));

        } catch (IOException e) {
            CrashUtil.crash(e);
        }

    }

    private static void clearDir(Path logDir) throws IOException {
        List<Path> pathList = new ArrayList<>();
        //TODO implement checking if the file in actually in the logDir folder
        Files.walk(logDir).forEach(t -> {
            pathList.add(t);
        });

        int logCount = pathList.size();

        Map<Boolean, String> logTranslate = Map.of(false, "log", true, "logs");

        String logForm = logTranslate.get(checkPlural(logCount));

        if (logCount > 0)
            info("There are %d %s in memory".formatted(logCount, logForm));

        if (logCount > MAX_LOGS) {
            int difference = logCount - MAX_LOGS;

            Log.error("There are over %d %s, deleting %d oldest"
                    .formatted(MAX_LOGS, logForm, difference));

            for (int i = 0; i < difference; i++) {

                Path path = pathList.get(i);

                if (Files.isRegularFile(path)) {
                    Files.delete(path);
                }

                warn("Deleted %s".formatted(path));
            }

        }
    }

    private static boolean checkPlural (int i) {
        return i > 1;
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
     * Saves {@code buffer} to two .log files. One is named with a timestamp and the second is latest.log.
     *
     * @see #buffer
     *
     */
    public static void saveLogFiles () {

        String fileName = LocalDateTime.now().format(Objects.requireNonNull(FILE)) + ".log";
        String latest = "latest.log";

        Path workingDir = CrashUtil.crashed ? Path.of(LOG_DIR, CRASH_DIR) : Path.of(LOG_DIR);

        Path logFile = Path.of(workingDir.toString(), fileName);
        Path logLatest = Path.of(workingDir.toString(), latest);

        info(getLogVersion());
        info(getLogCount());
        info("Created log file at %s.".formatted(logFile));

        if (!Files.exists(workingDir)) {
            try {
                Files.createDirectory(workingDir);
            } catch (IOException e) {
                CrashUtil.crash(e);
            }
        }

        List<String> log = new ArrayList<>();

        buffer.stream()
                .map(Log::stripAnsi)
                .forEach(log::add);


        try {
            Files.write(logFile, log);
            if(Files.exists(logLatest)) Files.delete(logLatest);
            Files.copy(logFile, logLatest);

        } catch (IOException e) {
            CrashUtil.crash(e);
        }
    }

    /**
     * Returns the log version in a neat format from the constants .kt file
     *
     * @see #LOG_VERSION
     *
     */
    private static String getLogVersion () {
        return "LOG VERSION=%s | LOG DIR=%s"
                .formatted(LOG_VERSION, LOG_DIR);
    }

    /**
     * Returns the total amount of all log lines by type in a neat format
     *
     * @return String logCount
     * @see #saveLogFiles()
     * @see #info(String message)
     * @see #exec(String message)
     * @see #error(String message)
     * @see #warn(String message)
     *
     */
    private static String getLogCount () {
        return "INFO=%d | EXEC=%d | WARN=%d | ERROR=%d"
                .formatted(infoCount, execCount, warnCount, errorCount);
    }

    /**
     * Used in {@code .log} file creation. Removes Ansi values and replaces them with {@code String} values
     *
     * @see #saveLogFiles()
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
     * @see Query#getResult(String SQL)
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
     * @see Query#executeExpression(String query)
     */
    public static final Consumer<String> logSQL = Log::exec;

}
