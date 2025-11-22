import sql.Query;
import sql.Script;
import sql.SqlConnection;

import java.util.Arrays;

public class Main {
    public static void main (String[] args) {
        var result = Query.getResult("use musicIndex;" +
                "select title from musicFiles where title != \"\";");

        assert result != null;
        System.out.println((Arrays.toString(Query.extractColumn(result, 0))));

        Script.endScript(SqlConnection.SYSTEM_START, System.nanoTime());
    }
}
