package com.example.pgyl.mind_a;

import java.util.ArrayList;

import static com.example.pgyl.mind_a.MainActivity.colors;
import static com.example.pgyl.mind_a.MainActivity.pegs;
import static com.example.pgyl.pekislib_a.Constants.UNDEFINED;

public class CandRecordsHandler {

    //region Variables
    private CandRecord[] candRecords;
    //endregion

    public CandRecordsHandler() {
        init();
    }

    private void init() {
        setupCandRecords();
    }

    public void close() {
        for (int i = 0; i <= (candRecords.length - 1); i = i + 1) {
            candRecords[i].close();
        }
        candRecords = null;
    }

    public void updateCandRecordsToPropRecords(PropRecordsHandler propRecordsHandler) {
        ArrayList<PropRecord> propRecords = propRecordsHandler.getPropRecords();
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

    public int[] getCombAtIndex(int index) {
        return candRecords[index].getComb();
    }

    public int getScoreByComparing(int[] comb, int[] secrComb) {
        int blacks = 0;
        int whites = 0;
        for (int i = 0; i <= (pegs - 1); i = i + 1) {
            if (comb[i] == secrComb[i]) {
                blacks = blacks + 1;
            }
        }
        if (blacks != pegs) {
            whites = -blacks;
            for (int i = 0; i <= (colors - 1); i = i + 1) {
                int wc = 0;
                int ws = 0;
                for (int j = 0; j <= (pegs - 1); j = j + 1) {
                    if (comb[j] == i) {
                        wc = wc + 1;
                    }
                    if (secrComb[j] == i) {
                        ws = ws + 1;
                    }
                }
                whites = whites + (Math.min(wc, ws));
            }
        }
        return 10 * blacks + whites;
    }

    public int getSolutionCandRecordsIndex(int[] comb, int score) {
        int count = 0;
        int solIndex = UNDEFINED;
        for (int i = 0; i <= (candRecords.length - 1); i = i + 1) {
            if (candRecords[i].isSelected()) {
                if (getScoreByComparing(candRecords[i].getComb(), comb) != score) {
                    candRecords[i].setSelected(false);
                } else {   //  Score OK
                    solIndex = i;
                    count = count + 1;
                }
            }
        }
        if (count != 1) {
            solIndex = UNDEFINED;
        }
        return solIndex;
    }

    private int getMiniMaxCandRecordsIndex() {
        int miniMaxCandRecordsIndex = UNDEFINED;
        int minNbScores = Integer.MAX_VALUE;
        for (int i = 0; i <= (candRecords.length - 1); i = i + 1) {
            int maxNbScores = -Integer.MAX_VALUE;
            candRecords[i].resetNbScores();
            for (int j = 0; j <= (candRecords.length - 1); j = j + 1) {
                if (candRecords[j].isSelected()) {
                    int nbScoresIndex = getScoreByComparing(candRecords[i].getComb(), candRecords[j].getComb());   //  Le score lui-même est utilisé comme index dans nbScores; Si pegs = 4 => les nombres de score de chaque candidat seront stockés dans candRecords().nbScores aux index 00,01,02,03,04,10,11,12,13,20,21,22,30,40
                    candRecords[i].incNbScoresAtIndex(nbScoresIndex);
                }
            }
            for (int j = 0; j <= (10 * pegs); j = j + 1) {
                if (candRecords[i].getNbScoresAtIndex(j) > maxNbScores) {
                    maxNbScores = candRecords[i].getNbScoresAtIndex(j);
                }
            }
            if (maxNbScores < minNbScores) {
                minNbScores = maxNbScores;
                miniMaxCandRecordsIndex = i;
            }
        }
        return miniMaxCandRecordsIndex;
    }

    private void setupCandRecords() {   // Enumérer toutes les solutions possibles pour pegs pions et colors couleurs   (Utilisées dans le cas Android Guess)
        candRecords = new CandRecord[(int) Math.pow(colors, pegs)];
        int[] comb = new int[pegs];
        for (int i = 0; i <= (candRecords.length - 1); i = i + 1) {
            candRecords[i] = new CandRecord(pegs, colors);
            if (i != 0) {
                int j = pegs - 1;
                do {
                    comb[j] = comb[j] + 1;   //   L'array comb (de longueur pegs) est calculé an Base couleurs (p.ex. en base 6 si couleurs = 6), de 0 à (colors ^ pegs - 1)
                    if (comb[j] >= colors) {
                        comb[j] = 0;
                        j = j - 1;
                    } else {
                        break;
                    }
                } while (j >= 0);
            }
            candRecords[i].setComb(comb);   //  Le candidat a maintenant sa combinaison de couleurs, réparties sur ses pegs pions
            candRecords[i].setSelected(true);
        }
    }
}
