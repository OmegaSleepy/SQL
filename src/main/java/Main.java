import sql.Queries;

public class Main {
    public static void main (String[] args) {
        Queries.queryResult("use musicIndex;" +
                "select title from musicFiles where title != \"\";");
    }
}
