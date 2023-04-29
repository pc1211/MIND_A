package com.example.pgyl.mind_a;

import com.example.pgyl.pekislib_a.StringDB;

import static com.example.pgyl.mind_a.StringDBTables.getMindTableDataFieldsCount;
import static com.example.pgyl.mind_a.StringDBTables.getPropsInits;
import static com.example.pgyl.mind_a.StringDBTables.getPropsTableName;
import static com.example.pgyl.mind_a.StringDBTables.getTempNumberColorsOrPegsInits;
import static com.example.pgyl.mind_a.StringDBTables.getTempScoreInits;
import static com.example.pgyl.pekislib_a.StringDBTables.getTempTableName;

public class StringDBUtils {

    //region TABLES
    public static void createMindTableIfNotExists(StringDB stringDB, String tableName) {
        stringDB.createTableIfNotExists(tableName, 1 + getMindTableDataFieldsCount(tableName));   //  Champ ID + Données;
    }
    //endregion

    //region PROPS
    public static void initializeTableProps(StringDB stringDB) {
        stringDB.insertOrReplaceRows(getPropsTableName(), getPropsInits());
    }

    public static String[] getDBPropById(StringDB stringDB, int idProp) {
        return stringDB.selectRowByIdOrCreate(getPropsTableName(), String.valueOf(idProp));
    }

    public static String[][] getDBProps(StringDB stringDB) {
        return stringDB.selectRows(getPropsTableName(), null);
    }

    public static void saveDBProp(StringDB stringDB, String[] values) {
        stringDB.insertOrReplaceRow(getPropsTableName(), values);
    }

    public static void saveDBProps(StringDB stringDB, String[][] values) {
        stringDB.deleteRows(getPropsTableName(), null);
        stringDB.insertOrReplaceRows(getPropsTableName(), values);
    }
    //endregion

    //region TEMP
    public static void initializeTableTempForNumberColorsOrPegs(StringDB stringDB) {
        stringDB.insertOrReplaceRows(getTempTableName(), getTempNumberColorsOrPegsInits());
    }

    public static void initializeTableTempForScore(StringDB stringDB) {
        stringDB.insertOrReplaceRows(getTempTableName(), getTempScoreInits());
    }
    //endregion
}
