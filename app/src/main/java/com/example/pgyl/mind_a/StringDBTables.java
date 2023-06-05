package com.example.pgyl.mind_a;

import com.example.pgyl.pekislib_a.InputButtonsActivity;

import java.util.ArrayList;

import static com.example.pgyl.mind_a.Constants.MAX_COLORS;
import static com.example.pgyl.mind_a.Constants.MAX_PEGS;
import static com.example.pgyl.mind_a.MainActivity.pegs;
import static com.example.pgyl.pekislib_a.Constants.REGEXP_INTEGER_FROM_0;
import static com.example.pgyl.pekislib_a.Constants.REGEXP_INTEGER_FROM_0_ERROR_MESSAGE;
import static com.example.pgyl.pekislib_a.Constants.REGEXP_SIX_CHARS;
import static com.example.pgyl.pekislib_a.Constants.REGEXP_SIX_CHARS_ERROR_MESSAGE;
import static com.example.pgyl.pekislib_a.Constants.UNDEFINED;
import static com.example.pgyl.pekislib_a.StringDB.TABLE_ID_INDEX;
import static com.example.pgyl.pekislib_a.StringDBTables.TABLE_IDS;

public class StringDBTables {

    enum MIND_TABLES {   // Les tables, rattachées à leurs champs de data
        PROPS(MindTableDataFields.props.class, ""), PALETTE_COLORS(MindTableDataFields.paletteColors.class, ""), INPUT_PARAMS(MindTableDataFields.inputParams.class, "");   //  Table des propositions (chacune avec ses couleurs et son score)

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
            PROP_0, PROP_1, PROP_2, PROP_3, PROP_4, PROP_5, PROP_6, PROP_7, PROP_8, PROP_SCORE;   //  Avec un maximum de 9 pions (cad score avec max 9 dizaines)

            public int INDEX() {
                return ordinal() + 1;
            }   //  INDEX 0 pour identifiant utilisateur
        }

        enum paletteColors implements MindTableDataFields {   //  Les champs de data de la table PALETTE_COLORS
            PAL_0, PAL_1, PAL_2, PAL_3, PAL_4, PAL_5, PAL_6, PAL_7, PAL_8, PAL_9;   //  Avec un maximum de 10 (index de 0 à 9)

            private String valueLabel;

            paletteColors() {
                this.valueLabel = "Color #" + String.valueOf(INDEX());    //  Numérotation à partir de 1
            }

            public int INDEX() {
                return ordinal() + 1;
            }   //  INDEX 0 pour identifiant utilisateur

            public String LABEL() {
                return valueLabel;
            }
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

    //region PALETTE_COLORS
    public static String getPaletteColorsTableName() {
        return MIND_TABLES.PALETTE_COLORS.toString();
    }

    public static int getPaletteColorsAtIndex(int index) {
        return MindTableDataFields.paletteColors.PAL_0.INDEX() + index;
    }

    public static String[][] getPaletteColorsInits() {
        final String[][] PALETTE_COLORS_INITS = {   //  Pour entrer le nombre de pegs, de couleurs, ou le score d'une proposition d'Android
                {TABLE_IDS.LABEL.toString(), MindTableDataFields.paletteColors.PAL_0.LABEL(), MindTableDataFields.paletteColors.PAL_1.LABEL(), MindTableDataFields.paletteColors.PAL_2.LABEL(), MindTableDataFields.paletteColors.PAL_3.LABEL(), MindTableDataFields.paletteColors.PAL_4.LABEL(), MindTableDataFields.paletteColors.PAL_5.LABEL(), MindTableDataFields.paletteColors.PAL_6.LABEL(), MindTableDataFields.paletteColors.PAL_7.LABEL(), MindTableDataFields.paletteColors.PAL_8.LABEL(), MindTableDataFields.paletteColors.PAL_9.LABEL()},
                {TABLE_IDS.KEYBOARD.toString(), InputButtonsActivity.KEYBOARDS.HEX.toString(), InputButtonsActivity.KEYBOARDS.HEX.toString(), InputButtonsActivity.KEYBOARDS.HEX.toString(), InputButtonsActivity.KEYBOARDS.HEX.toString(), InputButtonsActivity.KEYBOARDS.HEX.toString(), InputButtonsActivity.KEYBOARDS.HEX.toString(), InputButtonsActivity.KEYBOARDS.HEX.toString(), InputButtonsActivity.KEYBOARDS.HEX.toString(), InputButtonsActivity.KEYBOARDS.HEX.toString(), InputButtonsActivity.KEYBOARDS.HEX.toString()},
                {TABLE_IDS.REGEXP.toString(), REGEXP_SIX_CHARS, REGEXP_SIX_CHARS, REGEXP_SIX_CHARS, REGEXP_SIX_CHARS, REGEXP_SIX_CHARS, REGEXP_SIX_CHARS, REGEXP_SIX_CHARS, REGEXP_SIX_CHARS, REGEXP_SIX_CHARS, REGEXP_SIX_CHARS},
                {TABLE_IDS.REGEXP_ERROR_MESSAGE.toString(), REGEXP_SIX_CHARS_ERROR_MESSAGE, REGEXP_SIX_CHARS_ERROR_MESSAGE, REGEXP_SIX_CHARS_ERROR_MESSAGE, REGEXP_SIX_CHARS_ERROR_MESSAGE, REGEXP_SIX_CHARS_ERROR_MESSAGE, REGEXP_SIX_CHARS_ERROR_MESSAGE, REGEXP_SIX_CHARS_ERROR_MESSAGE, REGEXP_SIX_CHARS_ERROR_MESSAGE, REGEXP_SIX_CHARS_ERROR_MESSAGE, REGEXP_SIX_CHARS_ERROR_MESSAGE},
                {TABLE_IDS.DEFAULT.toString(), "FF0000", "00FF00", "0000FF", "FFFF00", "A47449", "FF7F00", "FF00FF", "00FFFF", "000000", "FFFFFF"}   //  RED, GREEN, BLUE, YELLOW, BROWN, ORANGE, FUCHSIA, CYAN, BLACK, WHITE;
        };
        return PALETTE_COLORS_INITS;
    }

    //region INPUT_PARAMS
    public static String getInputParamsTableName() {
        return MIND_TABLES.INPUT_PARAMS.toString();
    }

    public static String[][] getInputParamsInits() {
        final String[][] INPUT_PARAMS_INITS = {   //  Pour entrer le nombre de pegs, de couleurs, ou le score d'une proposition d'Android; Le score est entré via ScoreActivity et non InputButtonsActivity
                {TABLE_IDS.LABEL.toString(), MindTableDataFields.inputParams.PEGS.LABEL(), MindTableDataFields.inputParams.COLORS.LABEL(), null},
                {TABLE_IDS.KEYBOARD.toString(), InputButtonsActivity.KEYBOARDS.INTEGER.toString(), InputButtonsActivity.KEYBOARDS.INTEGER.toString(), null},
                {TABLE_IDS.REGEXP.toString(), REGEXP_INTEGER_FROM_0, REGEXP_INTEGER_FROM_0, null},
                {TABLE_IDS.REGEXP_ERROR_MESSAGE.toString(), REGEXP_INTEGER_FROM_0_ERROR_MESSAGE, REGEXP_INTEGER_FROM_0_ERROR_MESSAGE, null},
                {TABLE_IDS.MIN.toString(), "1", "1", null},
                {TABLE_IDS.MAX.toString(), String.valueOf(MAX_PEGS), String.valueOf(MAX_COLORS), null},
                {TABLE_IDS.DEFAULT.toString(), "4", "6", null}   //  4 pegs, 6 colors, Pas pertinent pour le score
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

    public static int getPropsCombAtIndex(int index) {
        return MindTableDataFields.props.PROP_0.INDEX() + index;
    }

    public static int getPropsScoreIndex() {
        return MindTableDataFields.props.PROP_SCORE.INDEX();
    }

    public static final String COMB_NAME_PREFIX = "PROP_";
    public static final int CURRENT_PROP_ID = 0;
    public static final int SECR_PROP_ID = 1;

    public static ArrayList<PropRecord> propRowsToPropRecords(String[][] propRows) {
        ArrayList<PropRecord> propRecords = new ArrayList<PropRecord>();
        if (propRows != null) {
            for (int i = 0; i <= (propRows.length - 1); i = i + 1) {
                propRecords.add(propRowToPropRecord(propRows[i]));
            }
        }
        return propRecords;
    }

    public static PropRecord propRowToPropRecord(String[] propRow) {
        int id = Integer.parseInt(propRow[TABLE_ID_INDEX]);
        int[] comb = new int[pegs];
        for (int i = 0; i <= (pegs - 1); i = i + 1) {   //  Les pions, uniquement la partie utilisée (via pegs)
            comb[i] = Integer.parseInt(propRow[MindTableDataFields.props.valueOf(COMB_NAME_PREFIX + i).INDEX()]);
        }
        int score = Integer.parseInt(propRow[MindTableDataFields.props.PROP_SCORE.INDEX()]);

        PropRecord propRecord = new PropRecord();
        propRecord.setId(id);
        propRecord.setComb(comb);
        propRecord.setScore(score);
        return propRecord;
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

    public static String[] propRecordToPropRow(PropRecord propRecord) {
        String[] propRow = new String[1 + MindTableDataFields.props.values().length];  //  Champ ID + Données (9 pions + Score)

        propRow[TABLE_ID_INDEX] = String.valueOf(propRecord.getId());
        for (int i = 0; i <= (MAX_PEGS - 1); i = i + 1) {    //  Les pions, partie utilisée (via getcomb()) et partie non utilisée (via couleur vide)
            propRow[MindTableDataFields.props.valueOf(COMB_NAME_PREFIX + i).INDEX()] = String.valueOf((i < propRecord.getComb().length) ? propRecord.getComb()[i] : UNDEFINED);
        }
        propRow[MindTableDataFields.props.PROP_SCORE.INDEX()] = String.valueOf(propRecord.getScore());   //  Le score

        return propRow;
    }
    //endregion
}
