package sql;

import java.io.File;
import java.util.Scanner;

public class Credentials {

    public static String url;
    public static String username;
    public static String password;

    static {
        File file = new File("credentials.txt");
        inputCredentialFile(file);
    }

    public static void inputCredentialFile(File file){
        try (Scanner scanner = new Scanner(file)){
            url = scanner.nextLine();
            username = scanner.nextLine();
            password = scanner.nextLine();
            Log.info(url);
            Log.info(username);
            Log.info(password);
            Log.info("Loaded credentials from file.");
        } catch (Exception e){
            CrashUtil.crash(e);
        }
    }





}
