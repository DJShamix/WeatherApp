package com.example.geek.testingapp.service;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import com.example.geek.testingapp.MainActivity;
import com.example.geek.testingapp.listener.WeatherServiceListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

//тут мы получаем погоду с сервера
public class GetWeather extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {

    ArrayList<String> arr = new ArrayList<String>();
    AppCompatActivity activity;

    private WeatherServiceListener listener;
    private Exception error;
    public static String temperatureUnit = "C";

    public GetWeather(AppCompatActivity activity) {
        this.listener = listener;
        this.activity = activity;
    }

    public String getTemperatureUnit() {
        return temperatureUnit;
    }

    @Override
    protected void onPreExecute() {
    }

    protected ArrayList<String> doInBackground(ArrayList<String>... passing) {
        ArrayList<String> location = passing[0];

        for (int i = 0; i < location.size(); i++) {

            String unit = getTemperatureUnit().equalsIgnoreCase("f") ? "f" : "c";

            String YQL = String.format("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\") and u='" + unit + "'", location.get(i));

            String endpoint = String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(YQL));

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

                JSONObject queryResults = data.optJSONObject("query");

                int count = queryResults.optInt("count");

                if (count == 0) {
                    error = new GetWeather.LocationWeatherException("No weather information found for " + location.get(i));
                    return null;
                }

                JSONObject dataResult = queryResults.optJSONObject("results").optJSONObject("channel");

                JSONObject unitsData = dataResult.optJSONObject("units");
                String distanceUnit = unitsData.optString("distance").toString();
                String pressureUnit = unitsData.optString("pressure").toString();
                String speedUnit = unitsData.optString("speed").toString();
                String temperatureUnit = unitsData.optString("temperature").toString();
                String units = distanceUnit + ":" + pressureUnit + ":" + speedUnit + ":" + temperatureUnit;

                int temperatureData = dataResult.optJSONObject("item").optJSONObject("condition").optInt("temp");
                String temperature = temperatureData + " °" + temperatureUnit;
                String description = dataResult.optJSONObject("item").optJSONObject("condition").optString("text");
                int code = dataResult.optJSONObject("item").optJSONObject("condition").optInt("code");
                int resourceId = activity.getResources().getIdentifier("drawable/icon_" + code, null, activity.getPackageName());

                JSONObject windData = dataResult.optJSONObject("wind");
                int chill = windData.optInt("chill");
                int direction = windData.optInt("direction");
                double speed = windData.optDouble("speed");
                String wind = chill + ":" + direction + ":" + speed + " " +  speedUnit;

                JSONObject atmosphereData = dataResult.optJSONObject("atmosphere");
                int humidity = atmosphereData.optInt("humidity");
                int pressure = atmosphereData.optInt("pressure");
                double visibility = atmosphereData.optDouble("visibility");
                String atmosphere = humidity + ":" + pressure + " " + pressureUnit + ":" + visibility + " " + distanceUnit;

//                JSONObject astronomy = dataResult.optJSONObject("atmosphere");
//                String sunrise = astronomy.optString("sunrise");
//                String sunset = astronomy.optString("sunset");

                JSONObject locationData = dataResult.optJSONObject("location");
                String region = locationData.optString("region");
                String country = locationData.optString("country");
                String locationInfo = String.format("%s, %s", locationData.optString("city"), (region.length() != 0 ? region : country));

                String output = locationInfo + ":" + temperature + ":" + description + ":" + resourceId + ":" + wind + ":" + atmosphere;
                arr.add(output);
            } catch (Exception e) {
                error = e;
            }
        }
        return arr;
    }

    @Override
    protected void onPostExecute(ArrayList<String> arr) {
        if(MainActivity.dialog.isShowing()){
            MainActivity.dialog.dismiss();
        }
        processFinish(arr);
//        return;
//        super.onPostExecute(arr);
    }

    public void processFinish(ArrayList<String> output) {
        //вызвав его, мы передаем инфу из Asinktask в место его вызова
    }

    public class LocationWeatherException extends Exception {
        public LocationWeatherException(String detailMessage) {
            super(detailMessage);
        }
    }
}
