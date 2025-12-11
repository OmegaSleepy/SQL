import sql.Log;
import sql.Query;


public class Main {
    public static void main (String[] args) {

        for (int i = 0; i < 320; i++) {
            Log.warn("I want to test this code");
        }
        Query.getResult("Select title from musicindex.musicspy");
        Log.saveLogFiles();

    }
}
