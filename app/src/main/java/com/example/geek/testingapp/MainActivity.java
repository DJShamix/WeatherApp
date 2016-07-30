package com.example.geek.testingapp;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.EditText;
import android.widget.Toast;

import com.example.geek.testingapp.recycler.RecyclerAdapter;
import com.example.geek.testingapp.recycler.RecyclerItemClickListener;
import com.example.geek.testingapp.service.GetWeather;
import com.example.geek.testingapp.service.ManageData;
import com.example.geek.testingapp.test.DBHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity{

    static final int PICK_WEATHER_REQUEST = 1;  // The request code

    RecyclerView recyclerView;
    public RecyclerAdapter adapter;

    private EditText txtInput;
    private SwipeRefreshLayout mRoot;
    SwipeRefreshLayout swiperefresh;
    public static ProgressDialog dialog;

    public final String fileName = "cacheInfo.txt"; //название файла кеша.
    public static String[] cities;
    FileOutputStream fop = null;
    ManageData manageData;
    static File mFolder;

    AlertDialog.Builder ad;
    Context context;
    int indexToRemove;

    //TOdo переместить инициализацию элементов активити в asincTask
    //вызывается автоматически при создании Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }

        super.onCreate(savedInstanceState);

        //этот костыль убрать потом. Пофиксить обновление инфы при пересоздании activity
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        //мщем компоненты для работы с GUI
        final LayoutInflater inflater = getLayoutInflater();
        setContentView(inflater.inflate(R.layout.activity_main, null));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        mRoot = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        manageData = new ManageData(getFilesDir(), fileName);       //путь к файлу кеша
        manageData.isFileExsists();


        //создаем ListView
        ArrayList<String> citiesList = new ArrayList<>();
        citiesList = manageData.read();
        adapter=new RecyclerAdapter(this, citiesList);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(mlinearLayoutManager);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(),
                        recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {

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
                removeItem();
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
        toolbar.setClickable(true);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        setSupportActionBar(toolbar);

        if(isOnline())
            updateInfo();
    }


    //при нажатии на элемент списка, идем сюда
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


    //ф-я удаление города
    private void removeItem(){

        final String backup = adapter.forGetCity().get(indexToRemove);

        adapter.forRemove(indexToRemove);
        adapter.notifyItemRemoved(indexToRemove);
        manageData.update(adapter.getCity());

        final Snackbar snackbar = Snackbar.make(mRoot, "Удалено", Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(getResources().getColor(R.color.accent_color));
        snackbar.setAction("Отмена", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.addAt(backup, indexToRemove);
                adapter.notifyDataSetChanged();
                manageData.update(adapter.getCity());
                Snackbar snackbar1 = Snackbar.make(mRoot, "City is restored!", Snackbar.LENGTH_SHORT);
                snackbar1.show();
            }
        });
        snackbar.show();
    }


    //показывает диалог для ввода города в базу данных
    protected void showInputDialog(final View view) {

        if (isOnline()) {

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
                                    String newCity = editText.getText().toString();
                                     if (!newCity.matches("") && !newCity.matches(" ")) {
                                         Log.d("My log", "Внесено:'" + newCity + "'");
                                         showDialog();
                                         addInfoToList(newCity);
                                     }
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
        }else{
            Snackbar snackbar = Snackbar.make(mRoot, R.string.no_internet, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
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

    //добавляет новый город в БД
    private void addInfoToList(final String input){

        final ArrayList arrayList = new ArrayList<String>(Arrays.asList(input));

        final GetWeather asyncTask = (GetWeather) new GetWeather(this){
            @Override
            public void processFinish(ArrayList<String> output) {
                if(output != null) {

                    if (!adapter.isCityExists(output)){

                        manageData.write(output);

                        Log.d("My log", "Результат поиска:\n'" + output.get(0).toString() + "'");
                        adapter.forAdd(output.get(0).toString(), adapter.getItemCount()+1);
                        adapter.notifyItemInserted(adapter.getItemCount());
                        adapter.notifyDataSetChanged();
                        recyclerView.getLayoutManager()
                                .smoothScrollToPosition(recyclerView, null, adapter.getItemCount() - 1);
                    }else{
                        Snackbar snackbar = Snackbar
                                .make(mRoot, R.string.item_exists, Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }

                } else Toast.makeText(getApplicationContext(),
                        getString(R.string.no_weather_found) + input, Toast.LENGTH_SHORT).show();
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

                        manageData.update(adapter.getCity());
                    }else{
                        Toast.makeText(getApplicationContext(), R.string.failedToUpdate, Toast.LENGTH_SHORT).show();
                    }
                }
            };
                asyncTask.execute(arrayList);
        }else {
            Snackbar snackbar = Snackbar.make(mRoot, R.string.no_internet, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }


    //для работы меню
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

            case R.id.update:
                updateInfo();
                return true;

            default:
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
            if(passedItem.size() > 0) {
                adapter.updateCity(passedItem);
                adapter.notifyDataSetChanged();
            }
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


