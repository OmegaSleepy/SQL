package sql;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import static sql.Log.info;
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
            Log.logSelect.accept(result);

        }


        return result;
    }

    public static ArrayList<String[]> queryFromFile (File file) {

        StringBuilder query = new StringBuilder();
        Scanner scanner = null;

        info("Running query from " + file);

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

    public static ArrayList<ArrayList<String[]>> queryFromSequence (File dir){
        if (!isValid(dir)) return null;

        List<File> files = Arrays.asList(Objects.requireNonNull(dir.listFiles()));

        File sequenceTXT = new File(dir + "/sequence.txt");
        
        if(!files.contains(sequenceTXT)){
            CrashUtil.crashHandler(new RuntimeException("You must have a sequence.txt in a sequence line"));
        }
        Scanner scanner = null;

        try {
            scanner = new Scanner(sequenceTXT);
        } catch (FileNotFoundException e) {
            CrashUtil.crashHandler(e);
        }

        String sequence = "";

        while (scanner.hasNextLine()){
            sequence += scanner.nextLine();
        }

        String[] sequenceLine = sequence.split(",");

        for (int i = 0; i < sequenceLine.length; i++) {
            sequenceLine[i] = sequenceLine[i].trim().toLowerCase();
        }

        ArrayList<File> fileSequence = new ArrayList<>();

        for (String fileName: sequenceLine){

            File tempFileList = null;

            for(File file: files){
                var fileURL = dir.getAbsolutePath() + "\\" + fileName + ".txt";
                var checkFileURL = file.getAbsolutePath();

                if(checkFileURL.equals(fileURL)){
                    tempFileList = (file);
                    break;
                }
            }

            fileSequence.add(tempFileList);

        }

        var answers = new ArrayList<ArrayList<String[]>>();

        for (File queryFile: fileSequence){
            answers.add(queryFromFile(queryFile));
        }

        return answers;

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
            CrashUtil.crashHandler(new RuntimeException("You cannot have files with the same name in a sequence: " + o1.getName()));
            return 0;

        });

        var answers = new ArrayList<ArrayList<String[]>>();

        for (File queryFile: files){
            answers.add(queryFromFile(queryFile));
        }

        return answers;


    }

    @Nullable
    private static boolean isValid (File dir) {
        if(!dir.exists()) {
            CrashUtil.crashHandler(new RuntimeException("A sequence, with this name \"" + dir + "\" , does not exist"));
            return false;
        }

        if(!dir.isDirectory()){
            CrashUtil.crashHandler(new RuntimeException("This is not a directory"));
            return false;
        }

        return true;
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
