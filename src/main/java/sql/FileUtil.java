package sql;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class FileUtil {

    private FileUtil(){}

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

    public static File getLineFile (String fileName){
        fileName = "scripts/line/" + fileName;

        return getResourceFile(fileName);
    }

    public static File getScriptFile (String fileName){
        fileName = "scripts/" + fileName;

        return getResourceFile(fileName);

    }

}
