package sql;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static sql.Log.info;
import static sql.Log.logSQL;
import static sql.SqlConnection.connection;

/**
 * Holds {@code static} methods for easy and safe querying
 * @see #queryResult(String)
 *
 * @see #executeExpression(String)
 * @see #selectOperation(PreparedStatement)
 *
 * @see #queryFromFile(File)

 * @see #queryFromSequence(File)
 * @see #queryFromLine(File)
 * */
public class Queries {

    private Queries(){}

    /**
     * Splits a SQL query to single line queries and parses them to another method. This is in line and should not be changed.
     * @see #executeExpression(String query)
     * @see SqlConnection
     * @see Log#logSelect
     * */
    @Nullable
    public static ArrayList<String[]> queryResult (String fullSql) {

        String[] statements = fullSql.split(";");

        ArrayList<String[]> result = new ArrayList<>();

        for (String query : statements) {
            query = query.trim();

            if (query.isEmpty()) continue;

            result = executeExpression(query);
            Log.logSelect.accept(result);

        }

        return result;
    }

    /**
     * Used to execute sql queries from a file in the resource folder. Should be used in combination with {@code FileUtil}.
     * @see FileUtil#getResourceFile(String fileName)
     * @see #queryResult(String fullSQL)
     * **/
    public static ArrayList<String[]> queryFromFile (File file) {

        StringBuilder query = new StringBuilder();
        Scanner scanner = null;

        info("Running query from " + file);

        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            CrashUtil.crash(e);
        }

        while (true) {
            assert scanner != null;
            if (!scanner.hasNext()) break;
            query.append(scanner.next()).append(" ");
        }
        return queryResult(query.toString());

    }

    /**
     * Used to execute multiple .txt files containing sql scripts. Imputed dir should be in {@code resources/scripts/line/}
     * All sequence line folders should contain sequence.txt that must follow this format:
     * <div style="margin:0px">
     *     <p>{@code <\Script1\>.txt, <\Script2\>.txt...}</p>
     * </div>
     * Files can be named anything, but they must be a .txt file.
     * @see #queryFromFile(File file)
     * @see #isValid(File dir) 
     * **/
    public static ArrayList<ArrayList<String[]>> queryFromSequence (File dir){
        if (!isValid(dir)) return null;

        List<File> files = Arrays.asList(Objects.requireNonNull(dir.listFiles()));

        File sequenceTXT = new File(dir + "/sequence.txt");
        
        if(!files.contains(sequenceTXT)){
            CrashUtil.crash(new RuntimeException("You must have a sequence.txt in a sequence line"));
        }

        try (Scanner scanner = new Scanner(sequenceTXT)){
            StringBuilder sequence = new StringBuilder();
            while (scanner.hasNextLine()){
                sequence.append(scanner.nextLine());
            }

            String[] sequenceLine = Arrays.stream(sequence.toString().split(","))
                    .map(s -> s.trim().toLowerCase())
                    .toArray(String[]::new);

            Map<String, File> filesMap = Arrays.stream(Objects.requireNonNull(dir.listFiles()))
                    .collect(Collectors.toMap(f -> f.getName().toLowerCase(), f -> f));

            for (int i = 0; i != sequenceLine.length; i++){
                filesMap.put(sequenceLine[i],null);
            }

            var answers = new ArrayList<ArrayList<String[]>>();


            for (String fileName : sequenceLine) {
                File file = filesMap.get(fileName + ".txt");
                if (file == null) {
                    CrashUtil.crash(new RuntimeException("File not found: " + fileName + ".txt"));
                }
                answers.add(queryFromFile(file));
            }

            return answers;

        } catch (FileNotFoundException e) {
            CrashUtil.crash(e);
        }

        return null;
    }

    public static ArrayList<ArrayList<String[]>> queryFromLine (File dir){

        if (!isValid(dir)) return null;

        List<File> files = Arrays.asList(Objects.requireNonNull(dir.listFiles()));

        //Sort by alphabetical order
        files.sort((o1, o2) -> {
            var first = o1.getName().toCharArray();
            var second = o2.getName().toCharArray();

            for (int i = 0; i < Math.max(first.length, second.length); i++) {
                if(first[i] > second[i]){
                    return 1;
                } else if (second[i] > first[i]) {
                    return -1;
                }
            }

            if(first.length > second.length){
                return 1;
            } else if (first.length < second.length) {
                return -1;
            }
            CrashUtil.crash(new RuntimeException("You cannot have files with the same name in a sequence: " + o1.getName()));
            return 0;

        });

        var answers = new ArrayList<ArrayList<String[]>>();

        for (File queryFile: files){
            answers.add(queryFromFile(queryFile));
        }

        return answers;


    }

    /**
     * Used to determine if the sequence folder has a {@code sequence.txt} file or not.
     * @see #queryFromSequence(File dir) 
     * **/
    private static boolean isValid (File dir) {
        if(!dir.exists()) {
            CrashUtil.crash(new RuntimeException("A sequence, with this name \"" + dir + "\" , does not exist"));
            return false;
        }

        if(!dir.isDirectory()){
            CrashUtil.crash(new RuntimeException("This is not a directory"));
            return false;
        }

        return true;
    }

    /**
     * Used to connect to the DB and decide which operation should be executed. Either {@code selectOperation} or {@code executeUpdate}.
     * The select operation should be used only for select type operations.
     * The execute update method for anything else.
     * @see #queryResult(String fullSQL)
     * @see #selectOperation(PreparedStatement statement)
     * @see PreparedStatement
     * **/
    private static ArrayList<String[]> executeExpression (String query) {

        logSQL.accept(query);

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
}
