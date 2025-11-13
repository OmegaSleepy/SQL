package sql;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Log {

    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String RED = "\u001B[31m";

    private static final String RESET = "\u001B[0m";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("MM-dd_HH-mm-ss");

    static List<String> logs = new ArrayList<>();

    public static void saveToFile () {
        File logsDir = new File("logs");
        if (!logsDir.exists()) logsDir.mkdir();

        String fileName = "logs/" + LocalDateTime.now().format(FILE_FORMAT) + ".txt";

        File logFile = new File(fileName);

        try (FileWriter fileWriter = new FileWriter(logFile)) {
            logFile.createNewFile();

            for (String message : logs) {
                fileWriter.write(stripAnsi(message) + System.lineSeparator());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String stripAnsi(String message) {
        return message.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    public static void log (String message) {
        System.out.println(message);
        logs.add(message);
    }

    public static Consumer<String> logSimple = string -> {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String timeTag = RED + "[" + timestamp + "]" + RESET;

        log(timeTag + " " + string);

    };

    public static Consumer<List<String[]>> logSelect = rows -> {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String timeTag = GREEN + "[" + timestamp + "]" + RESET;

        if (rows == null) {
            log(timeTag + " (no results)");
            return;
        }

        if (rows.isEmpty()) {
            log(timeTag + " (no results)");
            return;
        }

        int columns = rows.getFirst().length;
        int[] maxWidths = new int[columns];

        // compute max width for each column
        for (String[] row : rows) {
            for (int i = 0; i < columns; i++) {
                int len = row[i] != null ? row[i].length() : 4; // for null
                maxWidths[i] = Math.max(maxWidths[i], len);
            }
        }

        // print rows with proper alignment
        for (String[] row : rows) {
            StringBuilder stringBuilder = new StringBuilder(timeTag + " ");
            for (int i = 0; i < columns; i++) {
                String cell = row[i] != null ? row[i] : "null";
                stringBuilder.append(String.format("%-" + maxWidths[i] + "s", cell)); // left-align
                if (i < columns - 1) stringBuilder.append(" | ");
            }
            log(stringBuilder.toString());
        }
    };

    // Consumer for SQL code execution (CREATE, INSERT, UPDATE, DELETE)
    public static Consumer<String> logSQL = sql -> {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String timeTag = BLUE + "[" + timestamp + "]" + RESET;
        log(timeTag + " " + sql);
    };
}
