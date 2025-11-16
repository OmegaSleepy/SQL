package game;

import sql.Queries;

import java.util.List;

public class Holder {

    private Holder(){}

    static String[] weapons;
    static String[] elements;
    static String[] nations;

    public static void innitialize (){
        weapons = getValues("weapon");
        elements = getValues("element");
        nations = getValues("nation");
    }


    private static String[] getValues (String keyword){
        List<String[]> list = Queries.queryResult("select "+ keyword + "_name from " + keyword + "s");
        String[] values = new String[list.size()];

        for (int i = 0; i < list.size(); i++) {
            values[i] = list.get(i)[0];
        }

        return values;
    }






}
