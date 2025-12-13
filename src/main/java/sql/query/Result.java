package sql.query;

import java.util.ArrayList;

public class Result {
    //no need to touch for the thread safe rewrite

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

    public static String[] getRow (ArrayList<String[]> input, int column){
        return input.get(column);
    }

    public static String[] getFirstRow (ArrayList<String[]> input){
        return input.get(2);
    }
}
