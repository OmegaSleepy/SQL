package sql;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class FileUtil {

    private FileUtil(){}

    public static File getFile (String fileName) {
        URL url = SqlConnection.class.getClassLoader().getResource(fileName);
        assert url != null;
        File file;

        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return file;
    }

    public static File getLineFile (String fileName){
        fileName = "scripts/line/" + fileName;

        return getFile(fileName);
    }

    public static File getScriptFile (String fileName){
        fileName = "scripts/" + fileName;

        return getFile(fileName);

    }

}
