package com.example.mca1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.security.AccessControlContext;

public class appDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "bhargavi.db";
    //private static final String DB_PATH = String.format("%s/%s",
           // Environment.getExternalStorageDirectory().getAbsolutePath(), DATABASE_NAME);

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + AppTableContract.AppTable.TABLE_NAME + " (" +
                    AppTableContract.AppTable._ID + " INTEGER PRIMARY KEY," +
                    AppTableContract.AppTable.COLUMN_1 + " DOUBLE DEFAULT 0," +
                    AppTableContract.AppTable.COLUMN_2 + " DOUBLE DEFAULT 0 ," +
                    AppTableContract.AppTable.COLUMN_3 + " INTEGER DEFAULT 0," +
                    AppTableContract.AppTable.COLUMN_4 + " INTEGER DEFAULT 0," +
                    AppTableContract.AppTable.COLUMN_5 + " INTEGER DEFAULT 0," +
                    AppTableContract.AppTable.COLUMN_6 + " INTEGER DEFAULT 0," +
                    AppTableContract.AppTable.COLUMN_7 + " INTEGER DEFAULT 0," +
                    AppTableContract.AppTable.COLUMN_8 + " INTEGER DEFAULT 0," +
                    AppTableContract.AppTable.COLUMN_9 + " INTEGER DEFAULT 0," +
                    AppTableContract.AppTable.COLUMN_10 + " INTEGER DEFAULT 0," +
                    AppTableContract.AppTable.COLUMN_11 + " INTEGER DEFAULT 0," +
                    AppTableContract.AppTable.COLUMN_12 + " INTEGER DEFAULT 0)" ;

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AppTableContract.AppTable.TABLE_NAME;

        public appDbHelper(Context context) {
            //s = context.getExternalFilesDir(null);
            //s + File.separator +
            //System.out.println(context.getExternalFilesDir(null) + File.separator  + DATABASE_NAME );
            super(context,  "/sdcard/Android/data/com.example.mca1/files" + File.separator  + DATABASE_NAME , null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            //System.out.println(context.getExternalFilesDir(null) + File.separator  + DATABASE_NAME );
            db.execSQL(SQL_CREATE_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

