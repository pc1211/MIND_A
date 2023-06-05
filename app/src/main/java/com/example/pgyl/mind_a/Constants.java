package com.example.pgyl.mind_a;

public class Constants {
    //region Constantes
    public enum MIND_ACTIVITIES {
        MAIN, SCORE;

        public int INDEX() {
            return ordinal();
        }
    }

    public static final int MAX_PEGS = 9;   //  Maximum théorique, OK pour des scores jusque 99 (9 noirs; 9 blancs); table PROPS, main.xml et mainproplistitem à ajuster
    public static final int MAX_COLORS = 10;   //  Maximum théorique, OK pour des index de 0 à 9; table PALETTE_COLORS, main.xml à ajuster
    public static final int MIND_ACTIVITIES_REQUEST_CODE_MULTIPLIER = 100;   //  Pour se distinguer des requestCode de PEKISLIB
}
