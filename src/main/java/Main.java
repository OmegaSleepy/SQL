import sql.Query;
import sql.Script;
import sql.SqlConnection;

import java.io.File;


public class Main {
    public static void main (String[] args) {

        Query.fromFile("print.txt");
        Query.fromSequence("som");

        Script.end(SqlConnection.LIBRARY_START, System.nanoTime());
    }
}
