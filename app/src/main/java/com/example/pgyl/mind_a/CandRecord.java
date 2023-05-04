package com.example.pgyl.mind_a;

public class CandRecord {
    //region Constantes
    //endregion
    //region Variables
    private int[] comb;         //  Chacune des couleurs du candidat
    private boolean selected;   //  Candidat possible
    private int[] nbScores;     //  Chacun des nombres de score obtenus par rapport aux autres candidats; un score obtenu est utilisé comm index dans nbScores
    private int pegs;
    //endregion

    public CandRecord(int pegs) {
        this.pegs = pegs;
        init();
    }

    private void init() {
        comb = new int[pegs];
        nbScores = new int[10 * pegs + 1];   //  Pour aller jusque l'index (10*pegs) compris
    }

    public void close() {
        comb = null;
        nbScores = null;
    }

    public int[] getComb() {
        return comb;
    }

    public void setComb(int[] comb) {
        for (int i = 0; i <= (comb.length - 1); i = i + 1) {
            this.comb[i] = comb[i];
        }
    }

    public void setCombIndex(int index, int value) {
        comb[index] = value;
    }

    public boolean isSelected() {
        return selected;
    }

    public int getNbScores(int nbScoresIndex) {
        return nbScores[nbScoresIndex];
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void resetNbScores() {
        for (int i = 0; i <= (nbScores.length - 1); i = i + 1) {
            nbScores[i] = 0;
        }
    }

    public void incNbScores(int nbScoresIndex) {
        this.nbScores[nbScoresIndex] = this.nbScores[nbScoresIndex] + 1;
    }
}