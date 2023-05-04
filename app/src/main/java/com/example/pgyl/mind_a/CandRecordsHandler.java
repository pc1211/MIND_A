package com.example.pgyl.mind_a;

import java.util.ArrayList;

import static com.example.pgyl.pekislib_a.Constants.UNDEFINED;

public class CandRecordsHandler {

    //region Variables
    private CandRecord[] candRecords;
    private int pegs;
    private int colors;
    private int maxScore;
    //endregion

    public CandRecordsHandler(int pegs, int colors) {
        this.pegs = pegs;
        this.colors = colors;
        setupCandRecords();
        init();
    }

    private void init() {
        maxScore = 10 * pegs;
    }

    public void close() {
        for (int i = 0; i <= (candRecords.length - 1); i = i + 1) {
            candRecords[i].close();
        }
        candRecords = null;
    }

    public CandRecord[] getCandRecords() {
        return candRecords;
    }

    public void updateCandRecordsToPropRecords(ArrayList<PropRecord> propRecords) {
        for (int i = 0; i <= (candRecords.length - 1); i = i + 1) {
            candRecords[i].setSelected(true);   //  Comme au début
        }
        if (!propRecords.isEmpty()) {
            for (int i = 0; i <= (propRecords.size() - 1); i = i + 1) {
                int solIndex = getSolutionCandRecordsIndex(propRecords.get(i).getComb(), propRecords.get(i).getScore());   //  Pour adapter le Selected des candRecords
            }
        }
    }

    public int[] getGuessComb() {
        return candRecords[getMiniMaxCandRecordsIndex()].getComb();
    }

    public int getMiniMaxCandRecordsIndex() {
        int ret = UNDEFINED;
        int minNbScores = Integer.MAX_VALUE;
        for (int i = 0; i <= (candRecords.length - 1); i = i + 1) {
            int maxNbScores = -Integer.MAX_VALUE;
            candRecords[i].resetNbScores();
            for (int j = 0; j <= (candRecords.length - 1); j = j + 1) {
                if (candRecords[i].isSelected()) {
                    int nbScoresIndex = getScore(candRecords[i].getComb(), candRecords[j].getComb());   //  Le score lui-même est utilisé comme index dans nbScores; Si pegs = 4 => les nombres de score de chaque candidat seront stockés dans candRecords().nbScores aux index 00,01,02,03,04,10,11,12,13,20,21,22,30,40
                    candRecords[i].incNbScores(nbScoresIndex);
                }
            }
            for (int j = 0; j <= maxScore; j = j + 1) {
                if (candRecords[i].getNbScores(j) > maxNbScores) {
                    maxNbScores = candRecords[i].getNbScores(j);
                }
            }
            if (maxNbScores < minNbScores) {
                minNbScores = maxNbScores;
                ret = i;
            }
        }
        return ret;
    }

    public int getScore(int[] comb, int[] secrComb) {
        final int FREE = 0;
        final int BLACK = 1;
        final int WHITE = 2;


        int blacks = 0;
        int whites = 0;
        int[] tempComb = new int[pegs];
        for (int i = 0; i <= (pegs - 1); i = i + 1) {
            if (comb[i] == secrComb[i]) {
                tempComb[i] = BLACK;
                blacks = blacks + 1;
            } else {
                tempComb[i] = FREE;
            }
        }
        for (int i = 0; i <= (pegs - 1); i = i + 1) {
            for (int j = 0; j <= (pegs - 1); j = j + 1) {
                if ((tempComb[j] == FREE) && (tempComb[i] != BLACK)) {
                    if (comb[i] == secrComb[j]) {
                        tempComb[j] = WHITE;
                        whites = whites + 1;
                        break;
                    }
                }
            }
        }
        return 10 * blacks + whites;
    }

    public int getSolutionCandRecordsIndex(int[] comb, int score) {
        int count = 0;
        int ret = UNDEFINED;
        for (int i = 0; i <= (candRecords.length - 1); i = i + 1) {
            if (candRecords[i].isSelected()) {
                if (getScore(candRecords[i].getComb(), comb) != score) {
                    candRecords[i].setSelected(false);
                } else {   //  Score OK
                    ret = i;
                    count = count + 1;
                }
            }
        }
        if (count > 1) {
            ret = UNDEFINED;
        }
        return ret;
    }

    private void setupCandRecords() {
        candRecords = new CandRecord[(int) Math.pow(colors, pegs)];
        int[] comb = new int[pegs];
        for (int i = 0; i <= (candRecords.length - 1); i = i + 1) {
            candRecords[i] = new CandRecord(pegs);
            if (i != 0) {
                int j = pegs - 1;
                do {
                    comb[j] = comb[j] + 1;   //   L'array comb (de longueur pegs) est calculé an base couleurs (p.ex. en base 6 si couleurs = 6), de 0 à (colors ^ pegs - 1)
                    if (comb[j] >= colors) {
                        comb[j] = 0;
                        j = j - 1;
                    } else {
                        break;
                    }
                } while (j >= 0);
            }
            candRecords[i].setComb(comb);   //  Le candidat a sa combinaison de couleurs, réparties sur ses pegs dont le nombre est pegs
            candRecords[i].setSelected(true);
        }
    }
}