package com.example.geek.testingapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geek.testingapp.data.Atmosphere;
import com.example.geek.testingapp.data.Channel;
import com.example.geek.testingapp.data.Condition;
import com.example.geek.testingapp.data.LocationResult;
import com.example.geek.testingapp.data.Units;
import com.example.geek.testingapp.data.Wind;
import com.example.geek.testingapp.listener.GeocodingServiceListener;
import com.example.geek.testingapp.listener.WeatherServiceListener;
import com.example.geek.testingapp.service.GoogleMapsGeocodingService;
import com.example.geek.testingapp.service.WeatherCacheService;
import com.example.geek.testingapp.service.WeatherService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class WeatherActivity extends MainActivity implements WeatherServiceListener, GeocodingServiceListener, LocationListener {

    private TextView city_tv;
    private TextView county_tv;
    private ImageView weatherIconImageView;
    private TextView temperatureTextView;
    private TextView conditionTextView;
    private TextView chill;
    private TextView direction;
    private TextView speed;
    private TextView humidity;
    private TextView pressure;
    private TextView visibility;

    ArrayList<String> arrayToUpdate = new ArrayList<>();
    private WeatherService weatherService;
    private GoogleMapsGeocodingService geocodingService;
    private WeatherCacheService cacheService;
    private ProgressDialog dialog;

    // weather service fail flag
    private boolean weatherServicesHasFailed = false;

    private SharedPreferences preferences = null;

    String location = null;

    public static String temperatureUnit = "C";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_view_list);

        weatherIconImageView = (ImageView) findViewById(R.id.cv_weather_icon);
        city_tv = (TextView) findViewById(R.id.cv_city);
        county_tv = (TextView) findViewById(R.id.cv_country);
        temperatureTextView = (TextView) findViewById(R.id.cv_mTemperature);
        conditionTextView = (TextView) findViewById(R.id.cv_description);
        chill = (TextView) findViewById(R.id.cv_mChill);
        direction = (TextView) findViewById(R.id.cv_mDirection);
        speed = (TextView) findViewById(R.id.cv_mSpeed);
        humidity = (TextView) findViewById(R.id.cv_mHumidity);
        pressure = (TextView) findViewById(R.id.cv_mPressure);
//        visibility = (TextView) findViewById(R.id.visibility);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        weatherService = new WeatherService(this);
        weatherService.setTemperatureUnit(preferences.getString(getString(R.string.pref_temperature_unit), null));

        geocodingService = new GoogleMapsGeocodingService(this);
        cacheService = new WeatherCacheService(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //этот костыль убрать потом. Пофиксить обновление инфы при пересоздании activity

        if(isOnline() == true){

            dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.loading));
            dialog.setCancelable(false);
            dialog.show();
            Intent intent = getIntent();
            location = intent.getStringExtra("location");

            if(location != null) {
                Log.d("MSG FROM APP", location);
                weatherService.refreshWeather(location);
            }
        }
        else {
            Intent intent = getIntent();
            location = intent.getStringExtra("location");
            String[] mLocation = location.split(",");
            String temperature = intent.getStringExtra("temperature");
            String description = intent.getStringExtra("description");
            String imageCode = intent.getStringExtra("image");
            String chillInfo = intent.getStringExtra("chill");
            String directionInfo = intent.getStringExtra("direction");
            String speedInfo = intent.getStringExtra("speed");
            String humidityInfo = intent.getStringExtra("humidity");
            String pressureInfo = intent.getStringExtra("pressure");
            String visibilityInfo = intent.getStringExtra("visibility");


            weatherIconImageView.setImageResource(Integer.parseInt(imageCode));
            city_tv.setText(mLocation[0].trim());
            county_tv.setText(mLocation[1].trim());
            temperatureTextView.setText(temperature);
            conditionTextView.setText(description);
            chill.setText(chillInfo + "");
            direction.setText(directionInfo + "");
            speed.setText(speedInfo + "");
            humidity.setText(humidityInfo + " %");
            pressure.setText(pressureInfo);
            visibility.setText("Visibility: " + visibilityInfo);

            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    //сохраняем вид
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String temperature = temperatureTextView.getText().toString();
        String description = conditionTextView.getText().toString();
        String location = city_tv.getText().toString();
//        Drawable icon = weatherIconImageView.getDrawable();

        outState.putString("temperature", temperature);
        outState.putString("description", description);
        outState.putString("location", location);
//        outState.putParcelable("weatherIconDrawable", (Parcelable) icon);

        Log.d("Weather App", "onSaveInstanceState");
    }

    //восстанавливаем сохраненный вид
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

//        weatherIconImageView.setImageDrawable((Drawable) savedInstanceState.getParcelable("weatherIconDrawable"));

        temperatureTextView.setText(savedInstanceState.getString("temperature"));
        conditionTextView.setText(savedInstanceState.getString("description"));
        city_tv.setText(savedInstanceState.getString("location"));
        Log.d("Weather App", "onRestoreInstanceState");
    }

    //получаем погоду для текущего местоположения
    private void getWeatherFromCurrentLocation() {
        // system's LocationManager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // medium accuracy for weather, good for 100 - 500 meters
        Criteria locationCriteria = new Criteria();
        locationCriteria.setAccuracy(Criteria.ACCURACY_MEDIUM);

        String provider = locationManager.getBestProvider(locationCriteria, true);

        // single location update
        locationManager.requestSingleUpdate(provider, this, null);
    }

    //это для работы меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.currentLocation:
//                dialog.show();
//                getWeatherFromCurrentLocation();
//                return true;
//            case R.id.settings:
//                startSettingsActivity();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getTemperatureUnit() {
        return temperatureUnit;
    }

    //если запрос на погоду успешен, вызывается этот метод
    @Override
    public void serviceSuccess(Channel channel) {
        dialog.dismiss();

        Atmosphere atmosphere = channel.getAtmosphere();
        Condition condition = channel.getItem().getCondition();
        Wind wind = channel.getWind();
        Units units = channel.getUnits();

        int resourceId = getResources().getIdentifier("drawable/icon_" + condition.getCode(), null, getPackageName());

        @SuppressWarnings("deprecation")
        Drawable weatherIconDrawable = getResources().getDrawable(resourceId);

        weatherIconImageView.setImageDrawable(weatherIconDrawable);

        String temperatureLabel = getString(R.string.temperature_output, condition.getTemperature(), "C");
        String recLocation = channel.getLocation().toString();
        String[] mLocation = recLocation.split(",");

        String str_wind = wind.getChill() + ":" + wind.getDirection() + ":" + wind.getSpeed() + " " + units.getSpeed();
        String str_atmosphere = atmosphere.getHumidity() + ":" + atmosphere.getPressure() + " " + units.getPressure() + ":" + atmosphere.getVisibility() + " " + units.getDistance();

        temperatureTextView.setText(temperatureLabel);
        conditionTextView.setText(condition.getDescription());
        city_tv.setText(mLocation[0].trim());
        county_tv.setText(mLocation[1].trim());
        chill.setText(wind.getChill() + "");
        direction.setText(wind.getDirection() + "");
        speed.setText(wind.getSpeed() + " " + units.getSpeed());
        humidity.setText(atmosphere.getHumidity() + " %");
        pressure.setText(atmosphere.getPressure() + " " + units.getPressure());
//        visibility.setText(atmosphere.getVisibility() + " " + units.getDistance());


        File cityFileWeather = new File(mFolder.getAbsolutePath() + "/cacheInfo.txt");

        try {
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }
            if (!cityFileWeather.exists()) {
                cityFileWeather.createNewFile();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("Weather APP", "Weather service success from WeatherActivity");
        int index = 0;
        for (String str : cities) {
            String[] strToCheck = str.split(":");
            if (strToCheck[0].matches(location) == true) {
                try {
                    String result = recLocation + ":" + temperatureLabel + ":" + condition.getDescription() + ":" + resourceId + ":" + str_wind + ":" + str_atmosphere;
                    cities[index] = result;
                    arrayToUpdate = new ArrayList<>(Arrays.asList(cities));

                    FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
                    for (String st : cities) {
                        try {
                            String cache = st + "\n";
                            outputStream.write(cache.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    outputStream.close();
                    return;
                }catch (Exception ex){
                    Log.e("Weather APP", ex.toString());
                    return;
                }
            }
            index++;
        }
    }

    @Override
    public void serviceFailure(Exception exception) {
        // display error if this is the second failure
        if (weatherServicesHasFailed) {
            dialog.hide();
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            // error doing reverse geocoding, load weather data from cache
            weatherServicesHasFailed = true;
            // OPTIONAL: let the user know an error has occurred then fallback to the cached data
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();

            cacheService.load(this);
        }
    }

    @Override
    public void geocodeSuccess(LocationResult location) {
        // completed geocoding successfully
        weatherService.refreshWeather(location.getAddress());

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.pref_cached_location), location.getAddress());
        editor.apply();
    }

    @Override
    public void geocodeFailure(Exception exception) {
        // GeoCoding failed, try loading weather data from the cache
        cacheService.load(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        geocodingService.refreshLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        // OPTIONAL: implement your custom logic here
    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(this, "Now app works online!", Toast.LENGTH_SHORT).show();
//        Snackbar.make(getCurrentFocus(), "Now app works online", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(this, "Now app works offline!", Toast.LENGTH_SHORT).show();
//        Snackbar.make(getCurrentFocus(), "Now app works offline", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show();
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("passed_item", arrayToUpdate);
        setResult(2560, getIntent().putExtra("passed_item", arrayToUpdate));
        super.finish();
    }
}
