package sql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class Credentials {

    private static String url;
    private static String username;
    private static String password;

    public static String getUrl() {
        return url;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    static {
        inputCredentialFile("credentials.txt");
    }

    private static final String urlPlaceholder = "<URL> ex. jdbc:mysql://localhost:3306/";
    private static final String usernamePlaceholder = "<USERNAME> ex. root";
    private static final String passholderPlaceholder = "<PASSWORD> ex. password";


    /**
     * Used for overriding the credentials of the connection
     *
     **/
    public static void inputCredentialFile(String path) {

        Path credentailsPath = Path.of(path);

        if (!Files.exists(credentailsPath)) {
            credentialsDoesNotExistError(credentailsPath);
        }

        try {
            var cred = Files.readAllLines(credentailsPath);
            url = cred.get(0);
            username = cred.get(1);
            //some connections are without password, we need to account for those by checking credentials.txt if it has a password or not
            password = "";
            if (cred.size() > 2) {
                password = cred.get(2);
            }
            Log.info("Loaded credentials from file.");

            checkValid();

        } catch (IOException e) {
            CrashUtil.crashViolently(e);
        }

    }

    /**
     * Used to check if the credential values are not the placeholder ones and if so it ends the project.
     *
     * @see #passholderPlaceholder
     * @see #usernamePlaceholder
     * @see #urlPlaceholder
     *
     **/
    private static void checkValid() {

        boolean isSuitable = true;
        if (Objects.equals(urlPlaceholder, url)) {
            Log.error("URL is the placeholder value, please change!");
            isSuitable = false;
        }
        if (Objects.equals(usernamePlaceholder, username)) {
            Log.error("Username is the placeholder value, please change!");
            isSuitable = false;
        }
        if (Objects.equals(passholderPlaceholder, password)) {
            Log.error("Password is the placeholder value, please change!");
            isSuitable = false;
        }
        if (!isSuitable) {
            CrashUtil.crashViolently(new RuntimeException("Error in credentials! Check logs for the error!"));
        }
    }

    /**
     * Used when the credential file does not exist and creates a new one with placeholder values. Also ends the project.
     *
     * @see #inputCredentialFile(String path)
     *
     **/
    private static void credentialsDoesNotExistError(Path path) {
        try {
            Log.error("Credentials.txt does not exist! Creating one...");
            Files.createFile(path);

            Files.write(path, List.of(urlPlaceholder, usernamePlaceholder, passholderPlaceholder));

            CrashUtil.crashViolently(
                    new RuntimeException(
                            "Credential file did not exist. Go to %s and fill in your information".formatted(path)
                    )
            );

        } catch (IOException e) {
            CrashUtil.crash(e);
        }
    }


}
