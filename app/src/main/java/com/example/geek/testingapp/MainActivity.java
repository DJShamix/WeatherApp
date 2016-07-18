package com.example.geek.testingapp;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.geek.testingapp.service.GetWeather;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity{

    static final int PICK_WEATHER_REQUEST = 1;  // The request code

    RecyclerView recyclerView;
    RecyclerAdapter adapter;

    private EditText txtInput;

    SwipeRefreshLayout swiperefresh;

    public final String fileName = "cacheInfo.txt"; //название файла кеша.
    public static String[] cities;
    FileOutputStream fop = null;
    static File mFolder;
    static File cityFile;

    public static ProgressDialog dialog;

    AlertDialog.Builder ad;
    Context context;        //часть удаления переделать потом.
    int indexToRemove;

    //вызывается автоматически при создании Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        super.onCreate(savedInstanceState);

        //мщем компоненты для работы с GUI
        final LayoutInflater inflater = getLayoutInflater();
        setContentView(inflater.inflate(R.layout.activity_main, null));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        mFolder = new File(getFilesDir() + "/");
        cityFile = new File(mFolder.getAbsolutePath() + "/" + fileName);    //путь к файлу кеша

        ArrayList<String> na = new ArrayList<String>(); //этот костыль потом переделать!
        manageData("isFileExist", na);
        manageData("read", na); //получаем данные из файла и записываем в cities


        //создаем ListView
        ArrayList<String> citiesList = new ArrayList<>(Arrays.asList(cities));
        adapter=new RecyclerAdapter(this, citiesList);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        onItemClickAction(view, position);
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        indexToRemove = position;
                        ad.show();
                    }
                })
        );


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
        ad.setTitle("Подтвердите удаление");  // заголовок
        ad.setMessage("Вы уверены, что хотите удалить?"); // сообщение
        final String btn_yes = "Да";
        final String btn_no = "Нет";

        ad.setPositiveButton(btn_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                RecyclerAdapter.forRemove(indexToRemove);
                adapter.notifyItemRemoved(indexToRemove);
                manageData("update", adapter.getCity());

                Toast.makeText(getApplicationContext(), "Удалено",
                        Toast.LENGTH_SHORT).show();
            }
        });
        ad.setNegativeButton(btn_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });
        ad.setCancelable(true);


//        listView.setOnTouchListener(new ShowHideOnScroll(fab));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputDialog(view);
            }
        });


        //этот костыль убрать потом. Пофиксить обновление инфы при пересоздании activity
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        toolbar.setClickable(true);
        setSupportActionBar(toolbar);

//        updateInfo();
    }

    private void onItemClickAction(View view, int position){
        try {
            Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);

            String[] itemName = adapter.getCity().get(position).split(":");    //делим строку на массим и кидаем в Intent
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setExitTransition(new Explode());
                setResult(RESULT_OK, intent);
                startActivityForResult(intent, PICK_WEATHER_REQUEST, ActivityOptions
                        .makeSceneTransitionAnimation(this).toBundle());
            }else {
                setResult(RESULT_OK, intent);
                startActivityForResult(intent, PICK_WEATHER_REQUEST);
            }
        }catch (Exception ex){
            return;
        }
    }


//    //ф-я для редактирования listView
//    void fillData(ArrayList<String> cityInput) {
//        if(cityInput.get(0).toString().split(":").length >= 3) {
//            for (int i = 0; i < cityInput.size(); i++) {
//                String[] current_city = cityInput.get(i).split(":");
////                citiesArray.add(new City(current_city[0], current_city[2],
////                        Integer.parseInt(current_city[3]), current_city[1]));
//            }
//        }else{
//            cityFile.delete();
//            ArrayList<String> na = new ArrayList<String>();
//            manageData("isFileExist", na);
//            manageData("read", na); //получаем данные из файла и записываем в cities
//            citiesList = new ArrayList<>(Arrays.asList(cities));
//            fillData(citiesList);
//        }
//    }


    //показывает диалог для ввода города в базу данных
    protected void showInputDialog(final View view) {
        // get prompts.xml view

        LayoutInflater layoutInflater_dialog = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater_dialog.inflate(R.layout.info_add, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.editText);
        editText.requestFocus();
        showSoftKeyboard(view);

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        hideSoftKeyboard(view);
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
                                hideSoftKeyboard(view);
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    //отображение и скрытие клавиатуры
    public void showSoftKeyboard(View view) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
    public void hideSoftKeyboard(View view){
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }


    //показывает WaitDialog, чтобы у юзера не было возможности "понажимать" во время обновления
    public void showDialog(){
        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.loading));
        dialog.setCancelable(false);
        dialog.show();
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


    //добавляет новый город в БД
    private void addInfoToList(final String input){

        final ArrayList arrayList = new ArrayList<String>(Arrays.asList(input));

        final GetWeather asyncTask = (GetWeather) new GetWeather(this){
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

                        RecyclerAdapter.forAdd(output.get(0).toString(), adapter.getItemCount()+1);
                        adapter.notifyItemInserted(adapter.getItemCount());
                        adapter.notifyDataSetChanged();
                    } catch (Exception ex) {
                        Log.d("My log", "Ошибка " + ex);
                        return;
                    }

                }
                else Toast.makeText(getApplicationContext(), "No weather found for city: " + input, Toast.LENGTH_SHORT).show();
            }
        };
        asyncTask.execute(arrayList);
    }


    //ф-я обновления информации
    public void updateInfo(){
        if(isOnline()) {

            showDialog();

            ArrayList<String> arrayList = new ArrayList<String>();

            for (int i = 0; i < adapter.getCity().size(); i++) {
                String[] cityData = adapter.getCity().get(i).split(":");
                arrayList.add(cityData[0]);
            }

            GetWeather asyncTask = (GetWeather) new GetWeather(this){
                @Override
                public void processFinish(ArrayList<String> output) {
                    if(output != null) {
                        adapter.updateCity(output);
                        adapter.notifyDataSetChanged();

                        manageData("update", adapter.getCity());
                    }else{
                        Toast.makeText(getApplicationContext(), "Error: failed to update", Toast.LENGTH_SHORT).show();
                    }
                }
            };
                asyncTask.execute(arrayList);
        }
        else Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater Menuinflater = getMenuInflater();
        Menuinflater.inflate(R.menu.main_menu, menu);
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


    //изменение состояний activity
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isOnline() && requestCode == PICK_WEATHER_REQUEST && resultCode == 2560) {
            ArrayList<String> passedItem = new ArrayList<String>();
            passedItem = data.getStringArrayListExtra("passed_item");
            adapter.updateCity(passedItem);
            adapter.notifyDataSetChanged();
        }
    }


    //ф-я проверки "Подключены ли мы к интернету?"
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}


