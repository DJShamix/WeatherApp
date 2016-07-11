package com.example.geek.testingapp.custom_list;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.geek.testingapp.R;

import java.util.ArrayList;

public class BoxAdapter extends BaseAdapter{
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<City> objects;
    private Resources resources;
    private String packageName;

    public BoxAdapter(Context context, ArrayList<City> cities) {
        ctx = context;
        objects = cities;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item, parent, false);
        }

        City p = getCity(position);

        // заполняем View в пункте списка данными: наименование, описание погоды
        // и картинка
        ((TextView) view.findViewById(R.id.city_name)).setText(p.name);
        ((TextView) view.findViewById(R.id.description)).setText(p.description + "");
        ((TextView) view.findViewById(R.id.degrees)).setText(p.degrees);
        ((ImageView) view.findViewById(R.id.ivImage)).setImageResource(p.image);

//        int resourceId = getResources().getIdentifier("drawable/icon_" + p.image, "drawable", MainActivity.PACKAGE_NAME);
//
//        @SuppressWarnings("deprecation")
//        Drawable weatherIconDrawable = getResources().getDrawable(resourceId);
//        ((ImageView) view.findViewById(R.id.ivImage)).setImageDrawable(weatherIconDrawable);

        return view;
    }

    // город по позиции
    City getCity(int position) {
        return ((City) getItem(position));
    }

    // содержимое корзины
    ArrayList<City> getBox() {
        ArrayList<City> box = new ArrayList<City>();
        for (City p : objects) {
            // если в корзине
                box.add(p);
        }
        return box;
    }

    public Resources getResources() {
        return resources;
    }

    public String getPackageName() {
        return packageName;
    }
}
