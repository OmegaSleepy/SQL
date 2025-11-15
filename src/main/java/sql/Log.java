package sql;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static sql.ConstantsKt.*;

public class Log {



    private static final List<String> buffer = new ArrayList<>();

    public static void deleteLogs () {
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
            if (!dir.exists()) dir.mkdir();

            String filename = LocalDateTime.now().format(Objects.requireNonNull(getFILE())) + ".log";
            File file = new File(dir, filename);
            File latest = new File(dir, "latest.log");
            file.createNewFile();
            latest.createNewFile();

            info(getLogVersion());
            info(getLogCount());

            writeToFile(file).createNewFile();
            writeToFile(latest).createNewFile();

            info("Saved log to %s".formatted(dir + File.separator + filename));


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
