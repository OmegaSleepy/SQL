package sql;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import static sql.Log.info;
import static sql.Log.logSQL;
import static sql.Settings.logQueries;
import static sql.Settings.logResults;
import static sql.SqlConnection.connection;

/**
 * Holds {@code static} methods for easy and safe querying
 * @see #getResult(String)
 *
 * @see #executeExpression(String)
 * @see #selectOperation(PreparedStatement)
 *
 * @see #fromFile(String resourcePath)
 * @see #fromSequence(String resourcePath)
 * */
public class Query {

    private Query (){}

    /**
     * Splits a SQL query to single line queries and parses them to another method. This is in line and should not be changed.
     * @see #executeExpression(String query)
     * @see SqlConnection
     * @see Log#logSelect
     * */
    @Nullable
    public static ArrayList<String[]> getResult (String fullSql) {

        String[] statements = fullSql.split(";");

        ArrayList<String[]> result = new ArrayList<>();

        for (String query : statements) {
            query = query.trim();

            if (query.isEmpty()) continue;

            result = executeExpression(query);

            if(logResults) Log.logSelect.accept(result);

        }

        return result;
    }

    /**
     * Used to execute sql queries from a file in the resource folder. Should be used in combination with {@code FileUtil}.
     * @see FileUtil#readResourceFile(String resourcePath)
     * @see #getResult(String fullSQL)
     * **/
    public static ArrayList<String[]> fromFile (String resourcePath) {

        if (logQueries) info("Running query from " + resourcePath);

        return getResult(FileUtil.readResourceFile(resourcePath));

    }

    /**
     * Used to execute multiple .txt files containing sql scripts. Imputed dir should be in {@code resources/scripts/line/}
     * All sequence line folders should contain sequence.txt that must follow this format:
     * <div style="margin:0px">
     *     <p>{@code <\Script1\>.txt, <\Script2\>.txt...}</p>
     * </div>
     * Files can be named anything, but they must be a .txt file.
     * @see #fromFile(String resourcePath) 
     * **/
    public static ArrayList<ArrayList<String[]>> fromSequence (String sequenceFolder){

        String sequenceContent = FileUtil.readResourceFile(
                sequenceFolder + File.separator + "sequence.txt");

        var sequence = Arrays.stream(sequenceContent.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        ArrayList<ArrayList<String[]>> results = new ArrayList<>();

        for(String script: sequence){
            if(!script.endsWith(".txt")) continue;
            String sql = FileUtil.readResourceFile(sequenceFolder + File.separator + script);
            results.add(getResult(sql));
        }

        return results;

    }


    /**
     * Used to connect to the DB and decide which operation should be executed. Either {@code selectOperation} or {@code executeUpdate}.
     * The select operation should be used only for select type operations.
     * The execute update method for anything else.
     * @see #getResult(String fullSQL)
     * @see #selectOperation(PreparedStatement statement)
     * @see PreparedStatement
     * **/
    private static ArrayList<String[]> executeExpression (String query) {

        if(logQueries) logSQL.accept(query);

        try (PreparedStatement statement = connection.prepareStatement(query)) {

            if (query.toLowerCase().startsWith("select")) {
                return selectOperation(statement);
            } else {
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            CrashUtil.crash(e);
        }

        return null;

    }

    /**
     * This method is used to obtain all values from a table and puts them into a {@code ArrayList} for each row. All value
     * are saved as {@code String}
     * @see #executeExpression(String query)
     * @see ResultSet
     * **/
    private static ArrayList<String[]> selectOperation (PreparedStatement statement) throws SQLException {
        var result = new ArrayList<String[]>();

        try (ResultSet resultSet = statement.executeQuery()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            String[] columnNames = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = (metaData.getColumnName(i+1));
            }

            result.add(0,columnNames);
            result.add(1,new String[]{"","",""});

            // loop through all rows
            while (resultSet.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    Object value = resultSet.getObject(i);
                    row[i-1] = (value != null) ? value.toString() : null;
                }
                result.add(row);
            }
        }
        return result;
    }

    /**
     * This method is used to obtain a single column of a well-structured ArrayList result. 
     * @see #extractColumns(ArrayList input, int[] columns) 
     * **/
    public static String[] extractColumn (ArrayList<String[]> input, int column){
        ArrayList<String> result = new ArrayList<>();

        for(String[] row: input){
            result.add(row[column]);
        }
        //removing field name and an empty roll
        result.removeFirst();
        result.removeFirst();

        return result.toArray(new String[0]);
    }

    /**
     * This method is used to obtain multiple columns of a well-structured ArrayList result. 
     * @see #extractColumn(ArrayList input, int column) 
     * **/
    public static String[][] extractColumns (ArrayList<String[]> input, int[] columns){
        ArrayList<String[]> result = new ArrayList<>();

        for(int i: columns){
            result.add(extractColumn(input,i));
        }

        return result.toArray(new String[0][]);
    }

}
