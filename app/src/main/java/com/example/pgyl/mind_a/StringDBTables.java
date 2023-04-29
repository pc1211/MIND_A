package com.example.pgyl.mind_a;

import com.example.pgyl.pekislib_a.InputButtonsActivity;

import java.util.ArrayList;

import static com.example.pgyl.mind_a.Constants.MAX_PEGS;
import static com.example.pgyl.mind_a.PropRecord.COLOR_NUM_EMPTY;
import static com.example.pgyl.mind_a.PropRecord.CURRENT_PROP_ID;
import static com.example.pgyl.pekislib_a.Constants.REG_EXP_INTEGER_FROM_0;
import static com.example.pgyl.pekislib_a.Constants.REG_EXP_INTEGER_FROM_0_ERROR_MESSAGE;
import static com.example.pgyl.pekislib_a.StringDB.TABLE_ID_INDEX;
import static com.example.pgyl.pekislib_a.StringDBTables.TABLE_IDS;

public class StringDBTables {

    enum MIND_TABLES {   // Les tables, rattachées à leurs champs de data
        PROPS(MindTableDataFields.props.class, "");   //  Table des propositions (chacune avec ses couleurs et son score)

        private int dataFieldsCount;
        private String description;

        MIND_TABLES(Class<? extends MindTableDataFields> mindTableFields, String description) {
            dataFieldsCount = mindTableFields.getEnumConstants().length;
            this.description = description;
        }

        public String DESCRIPTION() {
            return description;
        }

        public int INDEX() {
            return ordinal();
        }

        public int getDataFieldsCount() {
            return dataFieldsCount;
        }
    }

    private interface MindTableDataFields {  //  Les champs de data, par table

        enum props implements MindTableDataFields {   //  Les champs de data de la table PROPS
            COL_NUM_0, COL_NUM_1, COL_NUM_2, COL_NUM_3, COL_NUM_4, COL_NUM_5, COL_NUM_6, COL_NUM_7, COL_NUM_8, COL_NUM_9, SCORE;

            public int INDEX() {
                return ordinal() + 1;
            }   //  INDEX 0 pour identifiant utilisateur
        }
    }

    //region MIND_TABLES
    public static int getMindTableDataFieldsCount(String tableName) {
        return MIND_TABLES.valueOf(tableName).getDataFieldsCount();
    }

    public static int getMindTableIndex(String tableName) {
        return MIND_TABLES.valueOf(tableName).INDEX();
    }

    public static String getMindTableDescription(String tableName) {
        return MIND_TABLES.valueOf(tableName).DESCRIPTION();
    }

    //region TEMP
    public static String[][] getTempNumberColorsOrPegsInits() {
        final String[][] TEMP_NUMBER_COLORS_OR_PEGS = {
                {TABLE_IDS.KEYBOARD.toString(), InputButtonsActivity.KEYBOARDS.INTEGER.toString()},
                {TABLE_IDS.REGEXP.toString(), REG_EXP_INTEGER_FROM_0},
                {TABLE_IDS.REGEXP_ERROR_MESSAGE.toString(), REG_EXP_INTEGER_FROM_0_ERROR_MESSAGE},
                {TABLE_IDS.MIN.toString(), "1"},
                {TABLE_IDS.MAX.toString(), "10"}
        };
        return TEMP_NUMBER_COLORS_OR_PEGS;
    }

    public static String[][] getTempScoreInits() {
        final String[][] TEMP_SCORE_INITS = {
                {TABLE_IDS.KEYBOARD.toString(), InputButtonsActivity.KEYBOARDS.INTEGER.toString()},
                {TABLE_IDS.REGEXP.toString(), REG_EXP_INTEGER_FROM_0},
                {TABLE_IDS.REGEXP_ERROR_MESSAGE.toString(), REG_EXP_INTEGER_FROM_0_ERROR_MESSAGE},
                {TABLE_IDS.MIN.toString(), "0"},
                {TABLE_IDS.MAX.toString(), "10"}
        };
        return TEMP_SCORE_INITS;
    }

    //region PROPS
    public static String getPropsTableName() {
        return MIND_TABLES.PROPS.toString();
    }
    public static String[][] getPropsInits() {   //  Créer un enregistrement d'office pour currentPropRecord avec couleurs vides et score 0
        final String[][] TABLE_PROPS_INITS = {
                {String.valueOf(CURRENT_PROP_ID),
                        String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY),
                        String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY),
                        String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY),
                        String.valueOf(0)}
        };
        return TABLE_PROPS_INITS;
    }

    public static int getPropsColorNumIndex(int colNum) {
        return MindTableDataFields.props.COL_NUM_0.INDEX() + colNum;
    }

    public static int getPropsScoreIndex() {
        return MindTableDataFields.props.SCORE.INDEX();
    }

    public static final String PROPS_COLOR_FIELD_NAME_PREFIX = "COL_NUM_";

    public static PropRecord propRowToPropRecord(String[] propRow) {
        PropRecord propRecord = new PropRecord();

        int[] colorNums = new int[MAX_PEGS];
        for (int i = 0; i <= (MAX_PEGS - 1); i = i + 1) {
            colorNums[i] = Integer.parseInt(propRow[MindTableDataFields.props.valueOf(PROPS_COLOR_FIELD_NAME_PREFIX + i).INDEX()]);
        }
        propRecord.setId(Integer.parseInt(propRow[TABLE_ID_INDEX]));
        propRecord.setColorNums(colorNums);
        propRecord.setScore(Integer.parseInt(propRow[MindTableDataFields.props.SCORE.INDEX()]));
        return propRecord;
    }

    public static String[] propRecordToPropRow(PropRecord propRecord) {
        String[] propRow = new String[1 + MindTableDataFields.props.values().length];  //  Champ ID + Données

        propRow[TABLE_ID_INDEX] = String.valueOf(propRecord.getId());
        for (int i = 0; i <= (MAX_PEGS - 1); i = i + 1) {
            propRow[MindTableDataFields.props.valueOf(PROPS_COLOR_FIELD_NAME_PREFIX + i).INDEX()] = String.valueOf(propRecord.getColorNums()[i]);
        }
        propRow[MindTableDataFields.props.SCORE.INDEX()] = String.valueOf(propRecord.getScore());

        return propRow;
    }

    public static ArrayList<PropRecord> propRowsToPropRecords(String[][] propRows) {
        ArrayList<PropRecord> propRecords = new ArrayList<PropRecord>();
        if (propRows != null) {
            for (int i = 0; i <= (propRows.length - 1); i = i + 1) {
                propRecords.add(propRowToPropRecord(propRows[i]));
            }
        }
        return propRecords;
    }

    public static String[][] propRecordsToPropRows(ArrayList<PropRecord> propRecords) {
        String[][] propRows = null;
        if (!propRecords.isEmpty()) {
            propRows = new String[propRecords.size()][];
            for (int i = 0; i <= (propRecords.size() - 1); i = i + 1) {
                propRows[i] = propRecordToPropRow(propRecords.get(i));
            }
        }
        return propRows;
    }
    //endregion
}
