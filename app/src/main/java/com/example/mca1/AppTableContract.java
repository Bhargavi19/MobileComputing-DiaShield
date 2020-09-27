package com.example.mca1;

import android.provider.BaseColumns;

public final class AppTableContract {
    private AppTableContract() {}
    public static class AppTable implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_1 = "heart_rate";
        public static final String COLUMN_2 = "respiratory_rate";
        public static final String COLUMN_3 = "Nausea";
        public static final String COLUMN_4 = "Headache";
        public static final String COLUMN_5 = "Diarrhea";
        public static final String COLUMN_6 = "Sore_Throat";
        public static final String COLUMN_7 = "Fever";
        public static final String COLUMN_8 = "Muscle_Ache";
        public static final String COLUMN_9 = "Loss_of_Smell_Taste";
        public static final String COLUMN_10 = "Cough";
        public static final String COLUMN_11 = "Shortness_of_Breath";
        public static final String COLUMN_12 = "Feeling_Tired";
    }
}
