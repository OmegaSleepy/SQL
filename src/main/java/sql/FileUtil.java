package sql;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Utility class for quick access to preformed paths to resource files
 * @see #getResourceReader(String resourcePath)
 * @see #readResourceFile(String resourcePath)
 * */
public class FileUtil {

    private FileUtil(){}

    public static BufferedReader getResourceReader (String resourcePath){
        InputStream in = FileUtil.class.getClassLoader().getResourceAsStream(resourcePath);
        if (in == null) CrashUtil.crash(new IllegalArgumentException("SQL resource not found: " + resourcePath));

        return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    public static String readResourceFile (String resourcePath){
        try (BufferedReader reader = getResourceReader(resourcePath)){
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read SQL resource: " + resourcePath, e);
        }
    }

}
