package sql;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import static sql.Log.logSQL;
import static sql.SqlConnection.connection;

public class Queries {

    public static ArrayList<String[]> queryResult (String fullSql) {

        String[] statements = fullSql.split(";");

        ArrayList<String[]> result = new ArrayList<>();

        for (String query : statements) {
            query = query.trim();

            if (query.isEmpty()) continue;

            result = executeExpression(query);

        }

        Log.logSelect.accept(result);
        return result;
    }

    private static ArrayList<String[]> executeExpression (String query) {

        logSQL.accept(query);

        try (PreparedStatement statement = connection.prepareStatement(query)) {

            if (query.toLowerCase().startsWith("select")) {
                return selectOperation(statement);
            } else {
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            CrashUtil.crashHandler(e);
        }

        return null;

    }

    private static ArrayList<String[]> selectOperation (PreparedStatement statement) throws SQLException {
        var result = new ArrayList<String[]>();

        try (ResultSet resultSet = statement.executeQuery()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // loop through all rows
            while (resultSet.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    Object value = resultSet.getObject(i);
                    row[i - 1] = (value != null) ? value.toString() : null;
                }
                result.add(row);
            }
        }
        return result;
    }

    public static ArrayList<String[]> queryFromFile (File file) {

        StringBuilder query = new StringBuilder();
        Scanner scanner = null;

        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            CrashUtil.crashHandler(e);
        }

        while (true) {
            assert scanner != null;
            if (!scanner.hasNext()) break;
            query.append(scanner.next()).append(" ");
        }
        return queryResult(query.toString());

    }
}
