package com.example.pgyl.mind_a;

import com.example.pgyl.mind_a.MainActivity.CURRENT_PROP_PEGS;
import com.example.pgyl.mind_a.MainActivity.PALETTE_COLORS;
import com.example.pgyl.pekislib_a.InputButtonsActivity;

import java.util.ArrayList;

import static com.example.pgyl.mind_a.PropRecord.COLOR_NUM_EMPTY;
import static com.example.pgyl.pekislib_a.Constants.REG_EXP_INTEGER_FROM_0;
import static com.example.pgyl.pekislib_a.Constants.REG_EXP_INTEGER_FROM_0_ERROR_MESSAGE;
import static com.example.pgyl.pekislib_a.StringDB.TABLE_ID_INDEX;
import static com.example.pgyl.pekislib_a.StringDBTables.TABLE_IDS;

public class StringDBTables {

    enum MIND_TABLES {   // Les tables, rattachées à leurs champs de data
        PROPS(MindTableDataFields.props.class, ""), INPUT_PARAMS(MindTableDataFields.inputParams.class, "");   //  Table des propositions (chacune avec ses couleurs et son score)

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
            PROP_0, PROP_1, PROP_2, PROP_3, PROP_4, PROP_5, PROP_6, PROP_7, PROP_8, SCORE;

            public int INDEX() {
                return ordinal() + 1;
            }   //  INDEX 0 pour identifiant utilisateur
        }

        enum inputParams implements MindTableDataFields {   //  Les champs de data de la table INPUT_PARAMS
            PEGS("Number of Pegs"), COLORS("Number of Colors"), SCORE("Score");

            private String valueLabel;

            inputParams(String valueLabel) {
                this.valueLabel = valueLabel;
            }

            public int INDEX() {
                return ordinal() + 1;
            }   //  INDEX 0 pour identifiant utilisateur

            public String LABEL() {
                return valueLabel;
            }
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

    //region INPUT_PARAMS
    public static String getInputParamsTableName() {
        return MIND_TABLES.INPUT_PARAMS.toString();
    }

    public static String[][] getInputParamsInits() {
        final String[][] INPUT_PARAMS_INITS = {   //  Pour entrer le nombre de pegs, de couleurs, ou le score d'une proposition d'Android
                {TABLE_IDS.LABEL.toString(), MindTableDataFields.inputParams.PEGS.LABEL(), MindTableDataFields.inputParams.COLORS.LABEL(), MindTableDataFields.inputParams.SCORE.LABEL()},
                {TABLE_IDS.KEYBOARD.toString(), InputButtonsActivity.KEYBOARDS.INTEGER.toString(), InputButtonsActivity.KEYBOARDS.INTEGER.toString(), InputButtonsActivity.KEYBOARDS.INTEGER.toString()},
                {TABLE_IDS.REGEXP.toString(), REG_EXP_INTEGER_FROM_0, REG_EXP_INTEGER_FROM_0, REG_EXP_INTEGER_FROM_0},
                {TABLE_IDS.REGEXP_ERROR_MESSAGE.toString(), REG_EXP_INTEGER_FROM_0_ERROR_MESSAGE, REG_EXP_INTEGER_FROM_0_ERROR_MESSAGE, REG_EXP_INTEGER_FROM_0_ERROR_MESSAGE},
                {TABLE_IDS.MIN.toString(), "1", "1", "0"},
                {TABLE_IDS.MAX.toString(), String.valueOf(CURRENT_PROP_PEGS.values().length), String.valueOf(PALETTE_COLORS.values().length), "99"},
                {TABLE_IDS.DEFAULT.toString(), "4", "6", "0"}   //  4 pegs, 6 colors, Pas pertinent pour le score
        };
        return INPUT_PARAMS_INITS;
    }

    public static int getInputParamsPegsIndex() {
        return MindTableDataFields.inputParams.PEGS.INDEX();
    }

    public static int getInputParamsColorsIndex() {
        return MindTableDataFields.inputParams.COLORS.INDEX();
    }

    public static int getInputParamsScoreIndex() {
        return MindTableDataFields.inputParams.SCORE.INDEX();
    }

    //region PROPS
    public static String getPropsTableName() {
        return MIND_TABLES.PROPS.toString();
    }

    public static String[][] getPropsInits() {   //  Créer un enregistrement d'office pour currentPropRecord et SecrPropRecord avec couleurs vides et score 0
        final String[][] TABLE_PROPS_INITS = {
                {String.valueOf(CURRENT_PROP_ID),
                        String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY),
                        String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY),
                        String.valueOf(COLOR_NUM_EMPTY),
                        String.valueOf(0)},
                {String.valueOf(SECR_PROP_ID),
                        String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY),
                        String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY), String.valueOf(COLOR_NUM_EMPTY),
                        String.valueOf(COLOR_NUM_EMPTY),
                        String.valueOf(0)}
        };
        return TABLE_PROPS_INITS;
    }

    public static int getPropsCombIndex(int index) {
        return MindTableDataFields.props.PROP_0.INDEX() + index;
    }

    public static int getPropsScoreIndex() {
        return MindTableDataFields.props.SCORE.INDEX();
    }

    public static final String PROPS_NAME_PREFIX = "PROP_";
    public static final int CURRENT_PROP_ID = 0;
    public static final int SECR_PROP_ID = 1;

    public static PropRecord propRowToPropRecord(String[] propRow) {
        PropRecord propRecord = new PropRecord();

        int[] comb = new int[CURRENT_PROP_PEGS.values().length];
        for (int i = 0; i <= (CURRENT_PROP_PEGS.values().length - 1); i = i + 1) {
            comb[i] = Integer.parseInt(propRow[MindTableDataFields.props.valueOf(PROPS_NAME_PREFIX + i).INDEX()]);
        }
        propRecord.setId(Integer.parseInt(propRow[TABLE_ID_INDEX]));
        propRecord.setComb(comb);
        propRecord.setScore(Integer.parseInt(propRow[MindTableDataFields.props.SCORE.INDEX()]));
        return propRecord;
    }

    public static String[] propRecordToPropRow(PropRecord propRecord) {
        String[] propRow = new String[1 + MindTableDataFields.props.values().length];  //  Champ ID + Données

        propRow[TABLE_ID_INDEX] = String.valueOf(propRecord.getId());
        int[] propComb = propRecord.getComb();
        for (int i = 0; i <= (CURRENT_PROP_PEGS.values().length - 1); i = i + 1) {
            propRow[MindTableDataFields.props.valueOf(PROPS_NAME_PREFIX + i).INDEX()] = String.valueOf((i < propComb.length) ? propRecord.getComb()[i] : COLOR_NUM_EMPTY);
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
