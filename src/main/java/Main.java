import sql.Log;


public class Main {
    public static void main (String[] args) {

        for (int i = 0; i < 320; i++) {
            Log.warn("I want to test this code");
        }
        Log.saveLogFiles();

    }
}
