package com.example.pgyl.mind_a;

public class Constants {
    //region Constantes
    public enum MIND_ACTIVITIES {
        MAIN, SCORE;

        public int INDEX() {
            return ordinal();
        }
    }

    public static final int MIND_ACTIVITIES_REQUEST_CODE_MULTIPLIER = 100;   //  Pour se distinguer des requestCode de PEKISLIB
}
