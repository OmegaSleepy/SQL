package sql;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Utility class for quick access to preformed paths to resource files
 * @see #getResourceFile(String fileName)
 * @see #getScriptFile(String fileName) 
 * @see #getScriptFile(String fileName) 
 * */
public class FileUtil {

    private FileUtil(){}

    /**
     * Used for quick and safe access of root resources files.
     * @param fileName String
     * @return {@code resourceFile} - String
     * @see #getLineFile(String fileName) 
     * @see #getScriptFile(String fileName) 
     * */
    public static File getResourceFile (String fileName) {
        URL url = SqlConnection.class.getClassLoader().getResource(fileName);
        assert url != null;
        File file = null;

        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            CrashUtil.crash(e);
        }

        return file;
    }

    /**
     * Used for quick and safe access to line folders in the {@code resource/script/line} folder.
     * @param folderName String
     * @return {@code folder} - File
     * @see #getResourceFile(String fileName)
     * @see #getScriptFile(String fileName) 
     **/
    public static File getLineFile (String folderName){
        
        folderName = "scripts/line/" + folderName;

        File folder = getResourceFile(folderName);
        
        if (!folder.isDirectory()) CrashUtil.crash(new RuntimeException("Imputed file %s is not a folder!".formatted(folderName)));
        
        return folder;
        
    }

    /**
     * Used for quick and safe access to script files in the {@code resource/script} folder.
     * @param fileName String
     * @return {@code file} - File
     * @see #getResourceFile(String fileName)
     * @see #getScriptFile(String fileName)
     **/
    public static File getScriptFile (String fileName){
        fileName = "scripts/" + fileName;

        return getResourceFile(fileName);

    }

}
