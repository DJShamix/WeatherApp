package com.example.geek.testingapp.test;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class GetWIKI extends AsyncTask<String, Void, String>{

    AppCompatActivity activity;
    String strToReturn;

    public GetWIKI(AppCompatActivity activity){
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... city) {

        String endpoint = String.format("https://en.wikipedia.org/w/api.php?" +
                "format=json&action=query&prop=extracts&exintro=&explaintext=&titles=%s", city);

        try {
            URL url = new URL(endpoint);

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);

            InputStream inputStream = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            JSONObject data = new JSONObject(result.toString());

            String batchcomplete = data.optString("batchcomplete");
            if(batchcomplete != null) {
                JSONObject pages = data.optJSONObject("query").optJSONObject("pages");
                String extract = pages.toString().substring(pages.toString().indexOf("extract"));

                //TOdo Пофиксить извлечение информации из extract. Посмотреть, как получить pageId
                extract = extract.substring(pages.toString().indexOf(":"));
                strToReturn = extract;
            }
        }catch (Exception ex){}
            return strToReturn;
    }

    @Override
    protected void onPostExecute(String strToReturn) {
        processFinish(strToReturn);
    }

    public void processFinish(String output) {
        //вызвав его, мы передаем инфу из Asinktask в место его вызова
    }
}
