package com.example.geek.testingapp.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.geek.testingapp.R;

import java.util.ArrayList;
import java.util.Arrays;

public class RecyclerAdapter extends  RecyclerView.Adapter<RecyclerViewHolder>{

    static Context context;
    LayoutInflater inflater;
    public static ArrayList<String> city = new ArrayList<>();

    public RecyclerAdapter(Context context, ArrayList<String> citiesArray) {
        city = citiesArray;

        this.context=context;
        inflater=LayoutInflater.from(context);
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_list, parent, false);

        RecyclerViewHolder viewHolder=new RecyclerViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {

        String[] spName = city.get(position).split(":");

        if(spName.length > 1) {
            String name = spName[0];
            String[] mName = name.split(",");
            String description = spName[2] + ": " + spName[1];
            int image = Integer.parseInt(spName[3]);

            holder.tv1.setText(mName[0]);
            holder.tv2.setText(description);
            holder.imageView.setImageResource(image);
            holder.imageView.setTag(holder);
        }else {
            holder.tv1.setText("База данных пуста");
            holder.tv2.setText("Добавьте ваш первый город");
            holder.imageView.setImageResource(R.drawable.na);
            holder.imageView.setTag(holder);
        }
    }

    @Override
    public int getItemCount() {
        return city.size();
    }

    public static void forRemove(int position){
        RecyclerAdapter ra = new RecyclerAdapter(context, city);
        ra.removeAt(position);
    }

    public void removeAt(int position) {
        city.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, RecyclerAdapter.city.size());
    }

    public static void forAdd(String name, int position){
        RecyclerAdapter ra = new RecyclerAdapter(context, city);
        ra.addAt(name, position);
    }

    public void addAt(String name, int position){
        city.add(name);
        notifyDataSetChanged();
        notifyItemInserted(position);
        notifyItemRangeChanged(position, RecyclerAdapter.city.size());
    }

    public void updateCity(ArrayList<String> cities){
        city.clear();
        city.addAll(cities);
    }

    public static ArrayList<String> forGetCity(){
        RecyclerAdapter ra = new RecyclerAdapter(context, city);
        return ra.getCity();
    }
    public ArrayList<String> getCity(){
        return city;
    }

    public boolean isCityExists(ArrayList<String> input){

        String checkName = input.get(0).toString();
        String[] checkInfo = checkName.split(":");
        checkName = checkInfo[0].toString();

        ArrayList<String> cityNames = new ArrayList<String>();
        for(int i = 0; i< city.size(); i++){
            String item = city.get(i).toString();
            String[] info = item.split(":");
            cityNames.add(i, info[0]);
        }

        if(cityNames.contains(checkName))
            return true;
        else return false;
    }
}
