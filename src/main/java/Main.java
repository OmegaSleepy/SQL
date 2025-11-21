import sql.Credentials;
import sql.Queries;

public class Main {
    public static void main (String[] args) {
        System.out.println(Credentials.url);
        System.out.println(Credentials.username);
        System.out.println(Credentials.password);
        Queries.queryResult("use musicIndex; select * from musicFiles;");
    }
}
