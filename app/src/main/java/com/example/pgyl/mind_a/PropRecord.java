package com.example.pgyl.mind_a;

public class PropRecord {
    //region Constantes
    public static final int COLOR_NUM_EMPTY = -1;
    private static final String SEPARATOR = "-";
    public static final String MAX_SCORE_DISPLAY_SIZE = "9" + SEPARATOR + "9";
    //endregion
    //region Variables
    private int id;             //  Identifiant de la proposition (1, 2, 3, ...)   (0 pour current Prop)
    private int[] comb;         //  Numéros de couleur (0..9) de la proposition (cf PALETTE_COLORS[])  (-1 si pas de couleur attribuée (COLOR_NUM_EMPTY))
    private int score;          //  Score de la proposition p.ex. 2N1B => 21
    //endregion

    public PropRecord() {
        init();
    }

    private void init() {
        score = 0;
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

    public int getCombAtIndex(int index) {
        return comb[index];
    }

    public void setComb(int[] comb) {
        this.comb = new int[comb.length];
        for (int i = 0; i <= (comb.length - 1); i = i + 1) {
            this.comb[i] = comb[i];
        }
    }

    public void setCombAtIndex(int index, int value) {
        comb[index] = value;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void resetComb() {
        for (int i = 0; i <= (comb.length - 1); i = i + 1) {
            this.comb[i] = COLOR_NUM_EMPTY;
        }
    }

    public boolean hasValidComb(int pegs) {
        boolean ret = true;
        for (int i = 0; i <= (pegs - 1); i = i + 1) {
            if (comb[i] == COLOR_NUM_EMPTY) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    public void setRandomComb(int pegs, int colors) {
        for (int i = 0; i <= (pegs - 1); i = i + 1) {
            comb[i] = (int) (colors * Math.random());
        }
    }

    public String getSeparator() {
        return SEPARATOR;
    }

    public String getStringScore() {
        return (score / 10) + SEPARATOR + (score % 10);
    }
}
