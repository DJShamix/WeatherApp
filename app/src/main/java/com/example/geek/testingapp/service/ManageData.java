package com.example.geek.testingapp.service;

import android.util.Log;
import android.widget.Toast;

import com.example.geek.testingapp.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ManageData extends File {
    private static File mFolder;
    private static File cityFile;
    FileOutputStream fop = null;
    private String[] cities;
    private String headOfMessage = "MSG from weather app: ManageData.class";

    public ManageData(File directory, String name) {
        super(directory, name);

        mFolder = new File(directory + "/");
        cityFile = new File(mFolder.getAbsolutePath() + "/" + name);    //путь к файлу кеша
    }

    public void write(ArrayList<String> cityName){
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(cityFile, true));

            for(int i = 0; i< cityName.size(); i++){
                output.append(cityName.get(i));
                output.append("\n");
            }
            output.close();

            Log.d(headOfMessage, "Успешно добавлено: " + output);
        } catch (Exception ex) {
            Log.d(headOfMessage, "Error:" + ex);
            return;
        }
    }

    public ArrayList read(){
        String[] cities;
        ArrayList<String> citiesOutput = new ArrayList<String>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(cityFile));
            StringBuilder buffer = new StringBuilder();
            String receiveString = "";

            while ((receiveString = bufferedReader.readLine()) != null) {
                buffer.append(receiveString);
                if (!receiveString.matches(""))
                    buffer.append("\n");
            }
            cities = buffer.toString().split("\n");
            bufferedReader.close();
            bufferedReader.close();

            for (int i = 0; i < cities.length; i++){
                citiesOutput.add(cities[i]);
            }
        }
        catch (IOException ex){
            Log.d(headOfMessage, "Error while try to read file");
            return null;
        }
        catch (Exception ex) {
            Log.d(headOfMessage, "Error: " + ex);
            return null;
        }
        return citiesOutput;
    }

    public void isFileExsists(){
        try {

            if (!mFolder.exists()) {
                mFolder.mkdir();
            }
            if (!cityFile.exists()) {
                cityFile.createNewFile();

                ArrayList<String> citiesNew = new ArrayList<String>();
                citiesNew.add("Kazan', Tatarstan Republic:22°C:Partly Cloudy:2130837588");
                citiesNew.add("Moscow,  Moscow Federal City:25°C:Mostly Cloudy:2130837590");
                citiesNew.add("Sochi,  Krasnodar Krai:27°C:Mostly Sunny:2130837588");
                citiesNew.add("Los Angeles,  CA:18°C:Partly Cloudy:2130837590");
                citiesNew.add("Rostov-na-Donu,  Rostov Oblast:30°C:Sunny:2130837590");

                this.write(citiesNew);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public void update(ArrayList cityName){
        try{
            fop = new FileOutputStream(cityFile);

            // if file doesnt exists, then create it
            if (!cityFile.exists()) {
                cityFile.createNewFile();
            }

            ArrayList<String> itemsToUpdate = new ArrayList<String>();
            if(cityName != null) {
                itemsToUpdate = cityName;

                String cache = "";
                for (int i = 0; i < itemsToUpdate.size(); i++) {
                    cache += itemsToUpdate.get(i) + "\n";
                }

                if (!cache.matches("") && !cache.matches(" ")) {
                    fop.write(cache.getBytes());
                    fop.flush();
                    fop.close();

                    cities = cache.split("\n");
                } else
                    Log.d(headOfMessage, "Error: cache is null!");
            }
            System.out.println("Done");
        }catch (IOException ex){
            Log.d(headOfMessage, String.valueOf(R.string.failedToUpdate));
            return;
        }
    }
}
