package com.example.pgyl.mind_a;

import static com.example.pgyl.mind_a.MainActivity.colors;
import static com.example.pgyl.mind_a.MainActivity.pegs;
import static com.example.pgyl.pekislib_a.Constants.UNDEFINED;

public class PropRecord {
    //region Variables
    private int id;             //  Identifiant de la proposition (0, 1, 2, 3, ...)
    private int[] comb;         //  Numéros de couleur (0..9) de la proposition (cf PALETTE_COLORS[])  (-1 si pas de couleur attribuée (UNDEFINED))
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

    public int getScoreBlacks() {
        return (score / 10);
    }

    public int getScoreWhites() {
        return (score % 10);
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void resetScore() {
        score = UNDEFINED;
    }

    public int getCombAtIndex(int index) {
        return comb[index];
    }

    public void setCombAtIndex(int index, int value) {
        comb[index] = value;
    }

    public void resetCombAtIndex(int index) {
        comb[index] = UNDEFINED;
    }

    public void resetComb() {
        comb = new int[pegs];
        for (int i = 0; i <= (pegs - 1); i = i + 1) {
            comb[i] = UNDEFINED;
        }
    }

    public boolean hasValidComb() {
        boolean ret = true;
        for (int i = 0; i <= (pegs - 1); i = i + 1) {
            if (comb[i] == UNDEFINED) {
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
}
