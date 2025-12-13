package log;

import common.CrashUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static common.Settings.FILE;
import static log.Log.*;

public class LogFileHandler {

    //some architectural thinking here - I will not be needing to write and clean up log files constantly. Like every 2 to 4 hours will be fine,
    // which means I won't really need to clear often, so that means that not using a file channel is fine, right?

    private LogFileHandler () {}

    /**
     * Deletes all logs in the {@code LOG_DIR} folder. For chronological deletion check {@code cleanUp}
     *
     * @see Log#LOG_DIR
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
            Files.walk(Paths.get(LOG_DIR, CRASH_DIR)).forEach(t -> {
                try {
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
     * Deletes logs in the {@link Log#LOG_DIR} folder based on how old they are.
     * It will delete enough files so there are less or equal to {@link Log#MAX_LOGS}.
     * For full cleanup, check {@link  #clearAllLogs}
     *
     */
    public static void cleanUp () {

        if (MAX_LOGS <= 0) {
            error("MAX_LOGS is set to %s, will not clean up files.".formatted(MAX_LOGS));
            error("Change value to a positive int.");
            return;
        }

        clearDir(Path.of(LOG_DIR, SUCCESSFUL_DIR));
        clearDir(Path.of(LOG_DIR, CRASH_DIR));

    }

    private static void clearDir (Path logDir) {
        List<Path> pathList = new ArrayList<>();

        Path base = logDir.toAbsolutePath().normalize();
        try{
            Files.walk(logDir).forEach(t -> {
                Path abs = t.toAbsolutePath().normalize();
                if (abs.startsWith(base) && !t.endsWith("latest.log")) {
                    pathList.add(t);
                } else {
                    // Skip anything that resolves outside the target directory (symlink traversal protection)
                    warn("Skipped path outside of logDir: %s".formatted(t));
                }
            });
        } catch (IOException e) {
            CrashUtil.crash(e);
        }


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
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        CrashUtil.crash(e);
                    }
                }

                warn("Deleted %s".formatted(path));
            }

        }
    }

    /**
     * Saves {@code buffer} to two .log files. One is named with a timestamp and the second is latest.log.
     *
     * @see Log#buffer
     *
     */
    public static void saveLogFiles () {

        String fileName = LocalDateTime.now().format(Objects.requireNonNull(FILE)) + ".log";
        String latest = "latest.log";

        Path workingDir = CrashUtil.crashed ? Path.of(LOG_DIR, CRASH_DIR) : Path.of(LOG_DIR, SUCCESSFUL_DIR);

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

        getBuffer().stream()
                .map(Log::stripAnsi)
                .forEach(log::add);

        try {
            Files.write(logFile, log);
            if (Files.exists(logLatest)) Files.delete(logLatest);
            Files.copy(logFile, logLatest);

        } catch (IOException e) {
            CrashUtil.crash(e);
        }
    }

    private static boolean checkPlural (int i) {
        return i > 1;
    }
}
