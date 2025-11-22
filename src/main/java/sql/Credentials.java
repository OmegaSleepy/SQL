package sql;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

public class Credentials {

    public static String url;
    public static String username;
    public static String password;

    static {
        File file = new File("credentials.txt");
        inputCredentialFile(file);
    }

    private static final String urlPlaceholder = "<URL> ex. jdbc:mysql://localhost:3306/";
    private static final String usernamePlaceholder = "<USERNAME> ex. root";
    private static final String passholderPlaceholder = "<PASSWORD> ex. password";


    /**
     * Used for overriding the credentials of the connection
     * **/
    public static void inputCredentialFile(File file){

        if(!file.exists()) {
            credentialsDoesNotExistError(file);
        }

        try (Scanner scanner = new Scanner(file)){
            url = scanner.nextLine();
            username = scanner.nextLine();
            password = scanner.nextLine();
            Log.info("Loaded credentials from file.");

            checkValid();

        } catch (Exception e){
            CrashUtil.crashViolently(e);
        }
    }

    /**
     * Used to check if the credential values are not the placeholder ones and if so it ends the project.
     * @see #passholderPlaceholder
     * @see #usernamePlaceholder
     * @see #urlPlaceholder
     * **/
    private static void checkValid () {

        boolean isSuitable = true;
        if(Objects.equals(urlPlaceholder, url)){
            Log.error("URL is the placeholder value, please change!");
            isSuitable = false;
        }
        if(Objects.equals(usernamePlaceholder, username)){
            Log.error("Username is the placeholder value, please change!");
            isSuitable = false;
        }
        if(Objects.equals(passholderPlaceholder, password)){
            Log.error("Password is the placeholder value, please change!");
            isSuitable = false;
        }
        if(!isSuitable){
            CrashUtil.crashViolently(new RuntimeException("Error in credentials! Check logs for the error!"));
        }
    }
    /**
     * Used when the credential file does not exist and creates a new one with placeholder values. Also ends the project.
     * @see #inputCredentialFile(File)
     * **/
    private static void credentialsDoesNotExistError(File file){
        try {
            Log.error("Credentials.txt does not exist! Creating one...");
            file.createNewFile();
            try (FileWriter writer = new FileWriter(file)){
                writer.write("%s\n%s\n%s".formatted(urlPlaceholder,usernamePlaceholder,passholderPlaceholder));
            }

            CrashUtil.crashViolently(new RuntimeException("Credential file did not exist. Go to %s and fill in your information".formatted(file.getAbsolutePath())));

        } catch (IOException e) {
            CrashUtil.crash(e);
        }
    }





}
