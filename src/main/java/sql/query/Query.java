package sql.query;

import common.CrashUtil;
import common.FileUtil;
import log.Log;
import org.jetbrains.annotations.Nullable;
import sql.SqlConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import static log.Log.info;
import static log.Log.logSQL;
import static common.Settings.logQueries;
import static common.Settings.logResults;

//TODO add all the Query stuff to the SqlConnection under the name "quick prepareStatementCreation", the actual execution should be still here

/**
 * Holds {@code static} methods for easy and safe querying
 * @see #fromString(String fullSQL, SqlConnection con)
 *
 * @see #executeExpression(String expression, SqlConnection con)
 * @see #selectOperation(PreparedStatement)
 *
 * @see #fromResourceFile(String resourcePath, SqlConnection con)
 * @see #fromSequence(String resourcePath, SqlConnection con)
 * */
public class Query {

    private Query (){}

    /**
     * Splits a SQL query to single line queries and parses them to another method. This is in line and should not be changed.
     * @see #executeExpression(String query, SqlConnection con)
     * @see SqlConnection
     * @see log.Log#logSelect
     * */
    @Nullable
    public static ArrayList<String[]> fromString (String fullSql, SqlConnection con) {

        String[] statements = fullSql.split(";");

        ArrayList<String[]> result = new ArrayList<>();

        for (String query : statements) {
            query = query.trim();

            if (query.isEmpty()) continue;

            result = executeExpression(query,con);

            if(logResults) Log.logSelect.accept(result);

        }

        return result;
    }

    /**
     * Used to execute SQL queries from a file in the resource folder.
     * Can't query from files that are outside the resource dir, use {@link #fromFile(String path, SqlConnection con)} instead
     * @see common.FileUtil#readResourceFile(String resourcePath)
     * @see #fromString(String fullSQL, SqlConnection con)
     * @see #fromFile(String path, SqlConnection con)
     * **/
    public static ArrayList<String[]> fromResourceFile(String resourcePath, SqlConnection con) {

        if (logQueries) info("Running query from " + resourcePath);

        return fromString(common.FileUtil.readResourceFile(resourcePath), con);

    }

    /**
     * Used to execute SQL queries from a file anywhere in the project. It is discouraged
     * to query from resource files with this method, check {@link #fromSequence(String resourcePath, SqlConnection con)}
     * for these use cases
     * @see #fromResourceFile(String path, SqlConnection con)
     * */
    public static ArrayList<String[]> fromFile(String path, SqlConnection con) {

        if (logQueries) info("Running query from " + path);

        try {
            StringBuilder builder = new StringBuilder();
            for(String s: Files.readAllLines(Path.of(path)))
            {
                builder.append(s).append(" ");
            }
            return fromString(builder.toString(), con);

        } catch (IOException e) {
            common.CrashUtil.crash(e);
        }

        return null;
    }

    /**
     * Used to execute multiple .txt files containing SQL scripts. Imputed dir should be in {@code resources/scripts/line/}
     * All sequence line folders should contain sequence.txt that must follow this format:
     * <div style="margin:0px">
     *     <p>{@code <\Script1\>.txt, <\Script2\>.txt...}</p>
     * </div>
     * Files can be named anything, but they must be a .txt file.
     * @see #fromResourceFile(String resourcePath, SqlConnection con)
     * **/
    @Deprecated
    public static ArrayList<ArrayList<String[]>> fromSequence (String sequenceFolder, SqlConnection con) {

        String sequenceContent = common.FileUtil.readResourceFile(
                sequenceFolder + File.separator + "sequence.txt");

        var sequence = Arrays.stream(sequenceContent.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        ArrayList<ArrayList<String[]>> results = new ArrayList<>();

        for(String script: sequence){
            if(!script.endsWith(".txt")) continue;
            String sql = FileUtil.readResourceFile(sequenceFolder + File.separator + script);
            results.add(fromString(sql, con));
        }

        return results;

    }

    public static ArrayList<String[]> fromPreparedStatement(PreparedStatement preparedStatement){

        var result = executeExpression(preparedStatement);

        if(logResults) Log.logSelect.accept(result);

        return result;

    }

    /**
     * Used to connect to the DB and decide which operation should be executed. Either {@code selectOperation} or {@code executeUpdate}.
     * The select operation should be used only for select type operations.
     * The execute update method for anything else.
     * @see #fromString(String fullSQL, SqlConnection con)
     * @see #selectOperation(PreparedStatement statement)
     * @see PreparedStatement
     * **/
    private static ArrayList<String[]> executeExpression (String query, SqlConnection con) {

        if(logQueries) logSQL.accept(query);

        try (PreparedStatement statement = con.connection.prepareStatement(query)) {

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

    private static ArrayList<String[]> executeExpression (PreparedStatement statement) {

        if(logQueries) logSQL.accept(statement.toString());

        try {
            if (statement.toString().toLowerCase().startsWith("select")) {
                selectOperation(statement);
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
     * @see #executeExpression(String query, SqlConnection con)
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

            result.addFirst(columnNames);

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



}
