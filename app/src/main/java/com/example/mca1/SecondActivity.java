package com.example.mca1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class SecondActivity extends AppCompatActivity {
    Spinner spin;
    Button upload;
    RatingBar ratings;
    TextView ack;
    TextView ratingValue;
    long rowid;
    SQLiteDatabase dbwrite;
    Map column_map;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Intent intent = getIntent();
        appDbHelper dbHelper = new appDbHelper(getApplicationContext());
        dbwrite = dbHelper.getWritableDatabase();

        rowid = intent.getLongExtra(MainActivity.EXTRA_MESSAGE, 0);
        //savedInstanceState.

        upload = (Button) findViewById(R.id.uploadsymptoms);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRatingValues();
            }
        });

        spin = (Spinner) findViewById(R.id.symptomlist);
        ratings = (RatingBar) findViewById(R.id.ratingBar);


        ack = (TextView) findViewById(R.id.successmsg);
        //ratingValue = (TextView) findViewById(R.id.rating);
        //ack = (TextView) findViewById(R.id.su);


        column_map=new HashMap();
        String symlist[] = {"Nausea",
                            "Headache",
                            "Diarrhea",
                            "Sore Throat",
                            "Fever",
                            "Muscle Ache",
                            "Loss of Smell or Taste",
                            "Cough",
                            "Shortness of Breath",
                            "Feeling Tired"};

        String collist[] = {"Nausea",
                "Headache",
                "Diarrhea",
                "Sore_Throat",
                "Fever",
                "Muscle_Ache",
                "Loss_of_Smell_Taste",
                "Cough",
                "Shortness_of_Breath",
                "Feeling_Tired"};

        for(int i = 0; i < 10 ; i++){
            //s = ""
            column_map.put(symlist[i], collist[i]);

        }
        //column_map.put()

    }

    public void getRatingValues() {
        String sym = spin.getSelectedItem().toString();
        float rat = ratings.getRating();
        ratings.setRating(0.0f);

        ContentValues values = new ContentValues();
        //values.put(FeedEntry.COLUMN_NAME_TITLE, title);
        String selection = AppTableContract.AppTable._ID + " = ?";
        String[] selectionArgs = { Long.toString(rowid) };
        values.put((String)column_map.get(sym), rat);

        int count = dbwrite.update(
                AppTableContract.AppTable.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        if(count != 0){
            ack.setText(sym + " updated!");
        }
        //values.put(sym, rat);
    }
}