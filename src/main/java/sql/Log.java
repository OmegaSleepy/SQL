package sql;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import java.io.File;
import java.io.FileInputStream;
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

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss:ms");
    private static final DateTimeFormatter FILE = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private static final List<String> buffer = new ArrayList<>();

    public static void clearLogs(){
        File folder = new File(LOG_DIR);

        if(!folder.exists() || !folder.isDirectory()) return;

        for (File file: Objects.requireNonNull(folder.listFiles())){
            if (file == null) {
                return;
            }
            if(file.isFile()){
                file.delete();
            }
        }

    }


    public static void info(String message){
        log(message, GREEN);
    }
    public static void exec(String message){
        log(message, BLUE);
    }
    public static void error(String message){
        log(message, RED);
    }
    public static void special(String message){
        log(message, YELLOW);
    }

    public static void saveToFile () {
        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) dir.mkdir();

            String filename = LocalDateTime.now().format(FILE) + ".log";
            File file = new File(dir, filename);
            file.createNewFile();

            try (FileWriter writer = new FileWriter(file)) {
                for (String line : buffer) {
                    writer.write(stripAnsi(line) + System.lineSeparator());
                }
            }

        } catch (IOException e) {
            CrashUtil.crashHandler(e);
        }
    }

    public static String stripAnsi(String message) {

        message = message.replace(RED, "ERROR ");
        message = message.replace(BLUE, "EXEC ");
        message = message.replace(GREEN, "INFO ");
        message = message.replace(YELLOW, "IMPORTANT ");

        return message;
    }

    private static void log(String message, String color) {
        String timestamp = "[" + LocalDateTime.now().format(TIME) + "] ";

        // Console (colored)
        System.out.println(color + timestamp + RESET + message);

        // Save raw (no ANSI)
        buffer.add(color + timestamp + message);
    }


    public static final Consumer<List<String[]>> logSelect = rows -> {

        if (rows == null) {
            error(" (no results)");
            return;
        }

        if (rows.isEmpty()) {
            error(" (no results)");
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
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < columns; i++) {
                String cell = row[i] != null ? row[i] : "null";
                stringBuilder.append(String.format("%-" + maxWidths[i] + "s", cell)); // left-align
                if (i < columns - 1) stringBuilder.append(" | ");
            }
            info(stringBuilder.toString());
        }
    };

    // Consumer for SQL code execution (CREATE, INSERT, UPDATE, DELETE)
    public static final Consumer<String> logSQL = Log::exec;
}
