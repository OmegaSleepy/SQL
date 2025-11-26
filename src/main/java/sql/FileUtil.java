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


    /**
     * Used in combination with {@code readResourceFile}. Used to safely get resource files inside of the .jar
     * @see #readResourceFile(String resourcePath)
     * **/
    public static BufferedReader getResourceReader (String resourcePath){

        InputStream in = FileUtil.class.getClassLoader().getResourceAsStream(resourcePath);

        if (in == null) CrashUtil.crash(new IllegalArgumentException("SQL resource not found: " + resourcePath));

        return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    /**
     * Used in combination with {@code getResourceReader}. Safely returns a {@code String} containing the contence of the file referenced.
     * @see #getResourceReader(String resourcePath)  
     * **/
    public static String readResourceFile (String resourcePath){
        try (BufferedReader reader = getResourceReader(resourcePath)){
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read SQL resource: " + resourcePath, e);
        }
    }

}
