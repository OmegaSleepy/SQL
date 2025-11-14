package game;

import org.jetbrains.annotations.NotNull;
import sql.FileUtil;
import sql.Queries;

import java.util.ArrayList;
import java.util.List;

public record GenshinCharacter(String name, double version, String element, String weapon, String nation) {

    @NotNull
    @Override
    public String toString () {
        return "GenshinCharacter{" +
                "name='" + name + '\'' +
                ", version=" + version +
                ", weapon='" + weapon + '\'' +
                ", nation='" + nation + '\'' +
                ", element='" + element + '\'' +
                '}';
    }


    static final List<GenshinCharacter> characterList = new ArrayList<>();

    public static void getAllCharacters () {

        List<String[]> values = Queries.queryFromFile(FileUtil.getScriptFile("premade/specificStuff.txt"));

        for (String[] strings : values) {
            GenshinCharacter character = new GenshinCharacter(
                    strings[0], Double.parseDouble(strings[1]), strings[2], strings[3], strings[4]);
            characterList.add(character);
        }

//        Log.info(characterList.toString());


    }

}
