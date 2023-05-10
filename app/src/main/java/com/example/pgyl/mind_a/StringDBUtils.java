package com.example.pgyl.mind_a;

import com.example.pgyl.pekislib_a.StringDB;

import static com.example.pgyl.mind_a.StringDBTables.getInputParamsInits;
import static com.example.pgyl.mind_a.StringDBTables.getInputParamsTableName;
import static com.example.pgyl.mind_a.StringDBTables.getMindTableDataFieldsCount;
import static com.example.pgyl.mind_a.StringDBTables.getPaletteColorsInits;
import static com.example.pgyl.mind_a.StringDBTables.getPaletteColorsTableName;
import static com.example.pgyl.mind_a.StringDBTables.getPropsTableName;

public class StringDBUtils {

    //region TABLES
    public static void createMindTableIfNotExists(StringDB stringDB, String tableName) {
        stringDB.createTableIfNotExists(tableName, 1 + getMindTableDataFieldsCount(tableName));   //  Champ ID + Données;
    }
    //endregion

    //region PALETTE_COLORS
    public static void initializeTablePaletteColors(StringDB stringDB) {
        stringDB.insertOrReplaceRows(getPaletteColorsTableName(), getPaletteColorsInits());
    }
    //endregion

    //region INPUT_PARAMS
    public static void initializeTableInputParams(StringDB stringDB) {
        stringDB.insertOrReplaceRows(getInputParamsTableName(), getInputParamsInits());
    }
    //endregion

    //region PROPS
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

}
