package com.example.pgyl.mind_a;

import static com.example.pgyl.mind_a.MainActivity.colors;
import static com.example.pgyl.mind_a.MainActivity.pegs;

public class PropRecord {
    //region Constantes
    public static final int COLOR_NUM_EMPTY = -1;
    private static final String SEPARATOR = "-";
    public static final String MAX_SCORE_DISPLAY_SIZE = "9" + SEPARATOR + "9";
    //endregion

    //region Variables
    private int id;             //  Identifiant de la proposition (0, 1, 2, 3, ...)
    private int[] comb;         //  Numéros de couleur (0..9) de la proposition (cf PALETTE_COLORS[])  (-1 si pas de couleur attribuée (COLOR_NUM_EMPTY))
    private int score;          //  Score de la proposition p.ex. 2N1B => 21
    //endregion

    public PropRecord() {
        init();
    }

    private void init() {
    }

    public int getId() {
        return id;
    }

    public void setId(int newIdProp) {
        id = newIdProp;
    }

    public int[] getComb() {
        return comb;
    }

    public void setComb(int[] comb) {
        this.comb = new int[comb.length];
        for (int i = 0; i <= (comb.length - 1); i = i + 1) {
            this.comb[i] = comb[i];
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCombAtIndex(int index) {
        return comb[index];
    }

    public void setCombAtIndex(int index, int value) {
        comb[index] = value;
    }

    public void resetComb() {
        this.comb = new int[pegs];
        for (int i = 0; i <= (pegs - 1); i = i + 1) {
            this.comb[i] = COLOR_NUM_EMPTY;
        }
    }

    public boolean hasValidComb() {
        boolean ret = true;
        for (int i = 0; i <= (pegs - 1); i = i + 1) {
            if (comb[i] == COLOR_NUM_EMPTY) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    public void setRandomComb() {
        for (int i = 0; i <= (pegs - 1); i = i + 1) {
            comb[i] = (int) (colors * Math.random());
        }
    }

    public String getSeparator() {
        return SEPARATOR;
    }

    public String getStringScore() {
        return String.valueOf(score / 10) + SEPARATOR + String.valueOf(score % 10);
    }
}
