package com.example.geek.testingapp.test;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDBName.db";
    public static final String CONTACTS_TABLE_NAME = "cities";
    public static final String CITIES_COLUMN_ID = "id";
    public static final String CITIES_COLUMN_NAME = "location";
    public static final String CITIES_COLUMN_TEMPERATURE = "temperature";
    public static final String CITIES_COLUMN_DESCRIPTION = "description";
    public static final String CITIES_COLUMN_RID = "resource_id";
    public static final String CITIES_COLUMN_CHILL = "wind_chill";
    public static final String CITIES_COLUMN_DIRECTION = "wind_direction";
    public static final String CITIES_COLUMN_SPEED = "wind_speed";
    public static final String CITIES_COLUMN_HUMIDITY = "humidity";
    public static final String CITIES_COLUMN_PRESSURE = "pressure";
    public static final String CITIES_COLUMN_VISIBILITY = "visibility";
    private HashMap hp;

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table cities " +
                        "(id integer primary key, location text, temperature text, " +
                        "description text, resource_id integer,wind_chill integer, " +
                        "wind_direction integer, wind_speed integer, humidity integer, " +
                        "pressure text, visibility integer)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS cities");
        onCreate(db);
    }

    public boolean insertCity(String input)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] mInput = input.split(":");

        ContentValues contentValues = new ContentValues();
        contentValues.put("location", mInput[0]);
        contentValues.put("temperature", mInput[1]);
        contentValues.put("description", mInput[2]);
        contentValues.put("resource_id", mInput[3]);
        contentValues.put("wind_chill", mInput[4]);
        contentValues.put("wind_direction", mInput[5]);
        contentValues.put("wind_speed", mInput[6]);
        contentValues.put("humidity", mInput[7]);
        contentValues.put("pressure", mInput[8]);
        contentValues.put("visibility", mInput[9]);
        db.insert("cities", null, contentValues);
        return true;
    }

    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from cities where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME);
        return numRows;
    }

    public boolean updateContact (Integer id, String location, String temperature, String description, String resource_id, String wind_chill, String wind_direction, String wind_speed, String humidity, String pressure, String visibility)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("location", location);
        contentValues.put("temperature", temperature);
        contentValues.put("description", description);
        contentValues.put("resource_id", resource_id);
        contentValues.put("wind_chill", wind_chill);
        contentValues.put("wind_direction", wind_direction);
        contentValues.put("wind_speed", wind_speed);
        contentValues.put("humidity", humidity);
        contentValues.put("pressure", pressure);
        contentValues.put("visibility", visibility);
        db.update("cities", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteContact (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("cities",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getPreviewInfo()
    {
        ArrayList<String> arrayList = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from cities", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            String id = res.getString(res.getColumnIndex(CITIES_COLUMN_ID));
            String name = res.getString(res.getColumnIndex(CITIES_COLUMN_NAME));
            String resId = res.getString(res.getColumnIndex(CITIES_COLUMN_RID));
            String description = res.getString(res.getColumnIndex(CITIES_COLUMN_DESCRIPTION));
            String temperature = res.getString(res.getColumnIndex(CITIES_COLUMN_TEMPERATURE));
            arrayList.add(name + ":" + temperature + ":" + description + ":" + resId);
            res.moveToNext();
        }
        return arrayList;
    }
}