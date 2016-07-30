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
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

import java.util.ArrayList;

public class WeatherActivity extends MainActivity implements WeatherServiceListener, GeocodingServiceListener, LocationListener {

    private TextView city_tv;
    private TextView county_tv;
    private ImageView weatherIconImageView;
    private TextView temperatureTextView;
    private TextView descriptionTextView;
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
    CollapsingToolbarLayout collapsingToolbar = null;
    private CoordinatorLayout mRoot;

    // weather service fail flag
    private boolean weatherServicesHasFailed = false;

    private SharedPreferences preferences = null;

    String location = null;
    String description = "";
    public static String temperatureUnit = "C";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //заместо этого сделать отдельное landscape activity .
        setContentView(R.layout.card_view_list);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_weather));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

//        weatherIconImageView = (ImageView) findViewById(R.id.cv_weather_icon);
        county_tv = (TextView) findViewById(R.id.cv_country);
        temperatureTextView = (TextView) findViewById(R.id.cv_mTemperature);
        descriptionTextView = (TextView) findViewById(R.id.cv_description);
        chill = (TextView) findViewById(R.id.cv_mChill);
        direction = (TextView) findViewById(R.id.cv_mDirection);
        speed = (TextView) findViewById(R.id.cv_mSpeed);
        humidity = (TextView) findViewById(R.id.cv_mHumidity);
        pressure = (TextView) findViewById(R.id.cv_mPressure);
        mRoot = (CoordinatorLayout) findViewById(R.id.coord_layout);
//        visibility = (TextView) findViewById(R.id.visibility);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        weatherService = new WeatherService(this);
        weatherService.setTemperatureUnit(preferences.getString(getString(R.string.pref_temperature_unit), null));

        geocodingService = new GoogleMapsGeocodingService(this);
        cacheService = new WeatherCacheService(this);

        update();
    }


    //обновление инфы
    private void update(){
        if(isOnline()){

            Intent intent = getIntent();
            location = intent.getStringExtra("location");

            collapsingToolbar.setTitle(location);

            if(location != null) {
                dialog = new ProgressDialog(this);
                dialog.setMessage(getString(R.string.loading));
                dialog.setCancelable(false);
                dialog.show();

//                String[] request = location.split(",");
//                final GetWIKI asyncTask = (GetWIKI) new GetWIKI(this){
//                    @Override
//                    public void processFinish(String output) {
//                        if(output != null) {
//                            Log.d("My log", "Результат поиска:\n'" + output + "'");
//                            description = output;
//                        }
//                    }
//                };
//                asyncTask.execute(request);

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

//            weatherIconImageView.setImageResource(Integer.parseInt(imageCode));
            collapsingToolbar.setTitle(mLocation[0].trim());
            county_tv.setText(mLocation[1].trim());
            temperatureTextView.setText(temperature);
            descriptionTextView.setText(description);
            chill.setText(chillInfo + "");
            direction.setText(directionInfo + "");
            speed.setText(speedInfo + "");
            humidity.setText(humidityInfo + " %");
            pressure.setText(pressureInfo);
//            visibility.setText("Visibility: " + visibilityInfo);

            final Snackbar snackbar = Snackbar.make(mRoot, R.string.no_internet, Snackbar.LENGTH_SHORT);
            snackbar.setActionTextColor(getResources().getColor(R.color.accent_color));
            snackbar.show();
        }
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

        int resourceId = getResources()
                .getIdentifier("drawable/icon_" + condition.getCode(), null, getPackageName());

        @SuppressWarnings("deprecation")
        Drawable weatherIconDrawable = getResources().getDrawable(resourceId);
//        weatherIconImageView.setImageDrawable(weatherIconDrawable);

        String temperatureLabel = getString(R.string.temperature_output, condition.getTemperature(), "C");

        String recLocation = channel.getLocation().toString();

        String[] mLocation = recLocation.split(",");

        String str_wind = wind.getChill() + ":" +
                wind.getDirection() + ":" + wind.getSpeed() + " " + units.getSpeed();

        String str_atmosphere = atmosphere.getHumidity() + ":" +
                atmosphere.getPressure() + " " + units.getPressure() + ":" +
                            atmosphere.getVisibility() + " " + units.getDistance();

        collapsingToolbar.setTitle(mLocation[0].trim());
        county_tv.setText(mLocation[1].trim());
        temperatureTextView.setText(temperatureLabel);
        descriptionTextView.setText(condition.getDescription());
        chill.setText(wind.getChill() + "");
        direction.setText(wind.getDirection() + "");
        speed.setText(wind.getSpeed() + " " + units.getSpeed());
        humidity.setText(atmosphere.getHumidity() + " %");
        pressure.setText(atmosphere.getPressure() + " " + units.getPressure());
        //        descriptionTextView.setText(condition.getDescription());
//        visibility.setText(atmosphere.getVisibility() + " " + units.getDistance());

        final ArrayList<String> arrayOfCities = adapter.getCity();
        for(int i = 0; i< arrayOfCities.size(); i++){
            String[] strToCheck = arrayOfCities.get(i).split(":");
            if (strToCheck[0].matches(location)) {
                String result = recLocation + ":" +
                        temperatureLabel + ":" + condition.getDescription() + ":" +
                                        resourceId + ":" + str_wind + ":" + str_atmosphere;

                arrayOfCities.remove(i);
                arrayOfCities.add(i, result);

                arrayToUpdate = arrayOfCities;
                manageData.update(arrayToUpdate);
            }
        }
        Log.d("Weather APP", "Weather service success from WeatherActivity");
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
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(this, "Now app works offline!", Toast.LENGTH_SHORT).show();
    }


    //для работы меню
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater Menuinflater = getMenuInflater();
//        Menuinflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(WeatherActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.update:
                if(isOnline()){
                    update();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("passed_item", arrayToUpdate);
        setResult(2560, getIntent().putExtra("passed_item", arrayToUpdate));
        super.finish();
    }
}
