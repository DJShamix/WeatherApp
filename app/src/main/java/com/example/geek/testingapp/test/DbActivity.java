package com.example.geek.testingapp.test;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.geek.testingapp.R;

import java.util.ArrayList;

public class DbActivity extends AppCompatActivity{
    TextView tv_db;
    Button btn_add;
    DBHelper mydb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);

        mydb = new DBHelper(this);
//        ArrayList array_list = mydb.getAllCotacts();

        tv_db = (TextView) findViewById(R.id.tv_db);
        btn_add = (Button) findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mydb.insertCity("Kazan',  Tatarstan Republic", "23", "5151515151", "Arbuzova", "Home", "North", "10 km/h", "15 %", "750 mm/hg", "20 km");
//                mydb.insertCity("Moscow,  Moscow Federal City", "30", "5151515151", "Arbuzova", "Home", "North", "10 km/h", "15 %", "750 mm/hg", "20 km");
//                mydb.insertCity("Sochi,  Krasnodar Krai", "95959", "25", "Arbuzova", "Home", "North", "10 km/h", "15 %", "750 mm/hg", "20 km");
//                mydb.insertCity("Los Angeles,  CA", "95959", "28", "Arbuzova", "Home", "North", "10 km/h", "15 %", "750 mm/hg", "20 km");
//                mydb.insertCity("Rostov-Na-Donu Raion,  Rostov Oblast", "15", "5151515151", "Arbuzova", "Home", "North", "10 km/h", "15 %", "750 mm/hg", "20 km");
            }
        });
    }
}
