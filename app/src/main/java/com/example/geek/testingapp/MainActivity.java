package com.example.geek.testingapp;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.geek.testingapp.listener.WeatherServiceListener;
import com.shamanland.fab.ShowHideOnScroll;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static String PACKAGE_NAME;

    public ArrayList<City> citiesArray = new ArrayList<City>(); //сюда кидаем инфу для отображения в ListView
    public ArrayList<String> citiesList; //тут мы храним всю инфу о городах
    BoxAdapter boxAdapter;

    private EditText txtInput;
    ListView listView;

    SwipeRefreshLayout swiperefresh;

    public final String fileName = "cacheInfo.txt"; //название файла кеша.
    public static String[] cities;
    static File mFolder;
    static File cityFile;

    FileOutputStream fop = null;

    public static ProgressDialog dialog;

    AlertDialog.Builder ad;
    Context context;
    String nameToRemove = "";   //часть удаления переделать потом.
    int indexToRemove = 9999;

    //вызывается автоматически при создании Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //мщем компоненты для работы с GUI
        LayoutInflater inflater = getLayoutInflater();
        setContentView(inflater.inflate(R.layout.activity_main, null));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        listView = (ListView) findViewById(R.id.lwMain);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        mFolder = new File(getFilesDir() + "/");
        cityFile = new File(mFolder.getAbsolutePath() + "/" + fileName);    //путь к файлу кеша

        ArrayList<String> na = new ArrayList<String>(); //этот костыль потом переделать!
        manageData("isFileExist", na);
        manageData("read", na); //получаем данные из файла и записываем в cities


        //создаем ListView
        citiesList = new ArrayList<>(Arrays.asList(cities));
        fillData(citiesList);
        boxAdapter = new BoxAdapter(this, citiesArray);
        listView.setAdapter(boxAdapter);


        //устанавливаем swipeToUpdate
        swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateInfo();
                        swiperefresh.setRefreshing(false);
                    }
                }
        );


        //инициализируем диалог для подтверждения удаления элемента списка
        context = MainActivity.this;
        ad = new AlertDialog.Builder(context);
        ad.setTitle("Сделай выбор");  // заголовок
        ad.setMessage("Вы уверены, что хотите удалить?"); // сообщение
        final String btn_yes = "Да";
        final String btn_no = "Нет";

        ad.setPositiveButton(btn_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                removeItem(indexToRemove);

                Toast.makeText(getApplicationContext(), "Удалено",
                        Toast.LENGTH_LONG).show();
            }
        });
        ad.setNegativeButton(btn_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });
        ad.setCancelable(true);


        //устанавливаем обработчик нажатия на элементы
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int arg2, long arg3) {
                String[] itemName = arg0.getItemAtPosition(arg2).toString().split("\n");
                nameToRemove = itemName[0];
                indexToRemove = arg2;

                ad.show();

                return true;
            }
        });
        listView.setOnItemClickListener(this);
//        listView.setOnTouchListener(new ShowHideOnScroll(fab));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputDialog();
            }
        });


        //этот костыль убрать потом. Пофиксить обновление инфы при пересоздании activity
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        PACKAGE_NAME = getApplicationContext().getPackageName();
        toolbar.setClickable(true);
        setSupportActionBar(toolbar);
    }


    //ф-я для редактирования listView
    void fillData(ArrayList<String> cityInput) {
        if(cityInput.get(0).toString().split(":").length >= 3) {
            for (int i = 0; i < cityInput.size(); i++) {
                String[] current_city = cityInput.get(i).split(":");
                citiesArray.add(new City(current_city[0], current_city[2],
                        Integer.parseInt(current_city[3]), current_city[1]));
            }
        }else{
            cityFile.delete();
            ArrayList<String> na = new ArrayList<String>();
            manageData("isFileExist", na);
            manageData("read", na); //получаем данные из файла и записываем в cities
            citiesList = new ArrayList<>(Arrays.asList(cities));
            fillData(citiesList);
        }
    }


    //удаляет элемент из БД
    private void removeItem(int index){
        citiesArray.remove(index);
        citiesList.remove(index);
        boxAdapter.notifyDataSetChanged();
        manageData("update", citiesList);
        nameToRemove = "";
        indexToRemove = 9999;
        return;
    }


    //показывает диалог для ввода города в базу данных
    protected void showInputDialog() {
        // get prompts.xml view

        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.info_add, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.editText);

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isOnline() == true) {
                            String newCity = editText.getText().toString();
                             if (newCity.matches("") != true && newCity.matches(" ") != true) {
                                 Log.d("My log", "Внесено:'" + newCity + "'");
                                 showDialog();
                                 addInfoToList(newCity);
//                                     weatherService.refreshWeather(newCity);
                             }
                        }else Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    //показывает WaitDialog, чтобы у юзера не было возможности "понажимать" во время обновления
    public void showDialog(){
        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.loading));
        dialog.setCancelable(false);
        dialog.show();
    }


    //обрабатывает нажатия на элемент списка (город) и передает данные в WeatherActivity с последующим его запуском
    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        try {

            Intent intent = new Intent(this, WeatherActivity.class);

            String[] itemName = citiesList.get(position).split(":");    //делим строку на массим и кидаем в Intent
            intent.putExtra("location", itemName[0]);

            if (!isOnline()) {
                intent.putExtra("temperature", itemName[1]);
                intent.putExtra("description", itemName[2]);
                intent.putExtra("image", itemName[3]);
                intent.putExtra("chill", itemName[4]);
                intent.putExtra("direction", itemName[5]);
                intent.putExtra("speed", itemName[6]);
                intent.putExtra("humidity", itemName[7]);
                intent.putExtra("pressure", itemName[8]);
                intent.putExtra("visibility", itemName[9]);
            }

            setResult(RESULT_OK, intent);
            startActivity(intent);
        }catch (Exception ex){
            return;
        }
    }


    //для работы с файлом
    public void manageData(String state, ArrayList<String> cityName) {
        switch (state) {
            case "write":
                try {
                    BufferedWriter output = new BufferedWriter(new FileWriter(cityFile, true));

                    for(int i = 0; i< cityName.size(); i++){
                        output.append(cityName.get(i));
                        output.append("\n");
                    }
                    output.close();

                    Toast.makeText(this, "Успешно добавлено", Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Toast.makeText(this, "Error:" + ex, Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case "read":
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(cityFile));
                    String receiveString = "";
                    StringBuilder buffer = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        buffer.append(receiveString);
                        if (!receiveString.matches(""))
                            buffer.append("\n");
                    }
                    cities = buffer.toString().split("\n");
                    bufferedReader.close();
                    bufferedReader.close();
                }
                catch (IOException ex){
                    Toast.makeText(this, "Error while try to read file", Toast.LENGTH_SHORT).show();
                    return;
                }
                catch (Exception ex) {
                    Toast.makeText(this, "Error:" + ex, Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case "isFileExist":
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

                        manageData("write", citiesNew);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                break;
            case "update":
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
                            Toast.makeText(this, "cache is null!", Toast.LENGTH_SHORT).show();
                    }
                    System.out.println("Done");
                }catch (IOException ex){
                    Toast.makeText(this, "Error: Failed to update info!", Toast.LENGTH_SHORT).show();
                    return;
                }
            default:
                break;
        }
    }

    public static void forManageData(ArrayList<String> cityName){
        MainActivity main = new MainActivity();
        main.manageData("update", cityName);
        return;
    }


    //добавляет новый город в БД
    private void addInfoToList(String input){

        final ArrayList arrayList = new ArrayList<String>(Arrays.asList(input));

        myTask asyncTask = (myTask) new myTask(this){
            @Override
            public void processFinish(ArrayList<String> output) {
                if(output != null) {
                    File cityFileWeather = new File(mFolder.getAbsolutePath() + "/" + fileName);

                    try {
                        if (!mFolder.exists()) {
                            mFolder.mkdir();
                        }
                        if (!cityFileWeather.exists()) {
                            cityFileWeather.createNewFile();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        Log.d("My log", "Результат поиска:\n'" + output.get(0).toString() + "'");
                        BufferedWriter outputFile = new BufferedWriter(new FileWriter(cityFileWeather, true));
                        outputFile.append(output.get(0).toString());
                        outputFile.append("\n");
                        outputFile.close();

                        citiesList.add(output.get(0));
                        citiesArray.clear();
                        fillData(citiesList);
                        boxAdapter.notifyDataSetChanged();
                    } catch (Exception ex) {
                        Log.d("My log", "Ошибка " + ex);
                        return;
                    }

                }else Toast.makeText(getApplicationContext(), "Error: search output is NULL", Toast.LENGTH_SHORT).show();
            }
        };
        asyncTask.execute(arrayList);
    }


    //ф-я обновления информации
    public void updateInfo(){
        if(isOnline() == true) {

            showDialog();

            ArrayList<String> arrayList = new ArrayList<String>();

            for (int i = 0; i < citiesList.size(); i++) {
                String[] cityData = citiesList.get(i).split(":");
                arrayList.add(cityData[0]);
            }

            myTask asyncTask = (myTask) new myTask(this){
                @Override
                public void processFinish(ArrayList<String> output) {
                    if(output != null) {
                        citiesList.clear();
                        citiesArray.clear();
                        citiesList.addAll(output);

                        fillData(citiesList);
                        boxAdapter.notifyDataSetChanged();

                        manageData("update", citiesList);
//                        Toast.makeText(getApplicationContext(), output.get(0), Toast.LENGTH_SHORT).show();
                    }else Toast.makeText(getApplicationContext(), "Error: search output is NULL", Toast.LENGTH_SHORT).show();
                }
            };
                asyncTask.execute(arrayList);
        }
        else Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
//            case R.id.action_settings:
//                return true;

//            case R.id.currentLocation:
//                return true;

            case R.id.update:
                updateInfo();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    protected void onResume() {
//        citiesList = new ArrayList<>(Arrays.asList(cities));
//        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, citiesList);
//        listView.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    protected void onStart() {
//        citiesList = new ArrayList<>(Arrays.asList(cities));
//        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, citiesList);
//        listView.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
        super.onStart();
    }


    //ф-я проверки "Подключены ли мы к интернету?"
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}


//тут мы получаем погоду с сервера
class myTask extends AsyncTask<ArrayList<String>, Void, ArrayList<String>>{

    ArrayList<String> arr = new ArrayList<String>();
    AppCompatActivity activity;

    private WeatherServiceListener listener;
    private Exception error;
    public static String temperatureUnit = "C";

    public myTask(AppCompatActivity activity) {
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
                    error = new LocationWeatherException("No weather information found for " + location.get(i));
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
//                Time lastTime = Time.valueOf(dataResult.optString("lastBuildDate"));
//  "°C"

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
        return;
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