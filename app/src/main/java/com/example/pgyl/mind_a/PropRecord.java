package com.example.pgyl.mind_a;

public class PropRecord {
    //region Constantes
    public static final int COLOR_NUM_EMPTY = -1;
    public static final int CURRENT_PROP_ID = 0;
    private static final String SEPARATOR = "-";
    public static final String MAX_SCORE_DISPLAY_SIZE = "9" + SEPARATOR + "9";
    //endregion
    //region Variables
    private int id;             //  Identifiant de la proposition (1, 2, 3, ...)   (0 pour current Prop)
    private int[] colorNums;    //  Numéros de couleur (0..9) de la proposition (cf PALETTE_COLORS[])  (-1 si pas de couleur attribuée (COLOR_NUM_EMPTY))
    private int score;          //  Score de la proposition p.ex. 2N1B => 21
    //endregion

    public PropRecord() {
        init();
    }

    private void init() {
        //
    }

    public int getId() {
        return id;
    }

    public void setId(int newIdProp) {
        id = newIdProp;
    }

    public int[] getColorNums() {
        return colorNums;
    }

    public void setColorNums(int[] colorNums) {
        this.colorNums = new int[colorNums.length];
        for (int i = 0; i <= (colorNums.length - 1); i = i + 1) {
            this.colorNums[i] = colorNums[i];
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getSeparator() {
        return SEPARATOR;
    }

    public String getStringScore() {
        return (score / 10) + SEPARATOR + (score % 10);
    }
}
