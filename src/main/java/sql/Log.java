package sql;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static sql.ConstantsKt.*;


/**
 * Simple class aimed at logging all activities of the program, holds an internal buffer, saves all logged actions during program execution, displays select queries,
 * clears old logs
 * @see #buffer
 * @see #log(String, String)
 * @see #logSelect
 * @see #saveToFile()
 * @see #cleanUp()
 * @see #clearAllLogs()
 * */
public class Log {

    private Log(){}


    /**
     * Holds all logged information for {@code saveToFile} to write to a log and the latest log
     * @see #writeToFile(File)
     * @see #log(String, String)
     * */
    private static final List<String> buffer = new ArrayList<>();

    /**
     * Deletes all logs in the {@code LOG_DIR} folder. For chronological deletion check {@code cleanUP}
     * @see ConstantsKt#LOG_DIR
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
     * @see ConstantsKt#LOG_DIR
     * @see ConstantsKt#MAX_LOGS
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

    public static void info (String message) {
        log(message, GREEN);
        infoCount++;
    }

    public static void exec (String message) {
        log(message, BLUE);
        execCount++;
    }

    public static void error (String message) {
        log(message, RED);
        errorCount++;
    }

    public static void warn (String message) {
        log(message, YELLOW);
        warnCount++;
    }

    static int infoCount;
    static int execCount;
    static int errorCount;
    static int warnCount;
    static int specialCount;

    public static void saveToFile () {

        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) {
                if (dir.mkdir()) {
                    info("Log folder generated at %s".formatted(dir.getAbsolutePath()));
                }
            }

            String filename = LocalDateTime.now().format(Objects.requireNonNull(getFILE())) + ".log";
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

    private static String getLogVersion () {
        return "LOG VERSION=%s | LOG DIR=%s "
                .formatted(LOG_VERSION, LOG_DIR);
    }

    private static String getLogCount () {
        return "INFO=%d | EXEC=%d | ERROR=%d | SPECIAL=%d"
                .formatted(infoCount, execCount, errorCount, specialCount);
    }


    public static String stripAnsi (String message) {

        message = message.replace(RED, "ERROR ");
        message = message.replace(BLUE, "EXEC ");
        message = message.replace(GREEN, "INFO ");
        message = message.replace(YELLOW, "IMPORTANT ");

        return message;
    }

    private static void log (String message, String color) {
        String timestamp = "[" + LocalDateTime.now().format(Objects.requireNonNull(getTIME())) + "] ";

        // Console (colored)
        System.out.println(color + timestamp + RESET + message);

        buffer.add(color + timestamp + message);
    }


    public static final Consumer<List<String[]>> logSelect = rows -> {

        if (rows == null || rows.isEmpty()) return;

        int columns = rows.stream()
                .mapToInt(r -> r.length)
                .max()
                .orElse(0);

        int[] maxWidths = new int[columns];

        // compute max width for each column
        for (String[] row : rows) {
            for (int i = 0; i < columns; i++) {
                String cell = (i < row.length && row[i] != null) ? row[i] : "null";
                maxWidths[i] = Math.max(maxWidths[i], cell.length());
            }
        }

        // print rows with proper alignment
        for (String[] row : rows) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < columns; i++) {
                String cell = (i < row.length && row[i] != null) ? row[i] : "null";
                sb.append(String.format("%-" + maxWidths[i] + "s", cell));
                if (i < columns - 1) sb.append(" | ");
            }
            info(sb.toString());
        }
    };


    // Consumer for SQL code execution (CREATE, INSERT, UPDATE, DELETE)
    public static final Consumer<String> logSQL = Log::exec;
}
