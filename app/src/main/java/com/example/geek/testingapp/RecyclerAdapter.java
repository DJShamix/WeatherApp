package com.example.geek.testingapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

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
        String name = spName[0];
        String[] mName = name.split(",");
        String description = spName[2] + ": " + spName[1];
        int image = Integer.parseInt(spName[3]);

        holder.tv1.setText(mName[0]);
        holder.tv2.setText(description);
        holder.imageView.setImageResource(image);
//        holder.imageView.setOnClickListener(clickListener);
        holder.imageView.setTag(holder);
    }

//    View.OnClickListener clickListener=new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//
//            try {
//                RecyclerViewHolder vholder = (RecyclerViewHolder) v.getTag();
//                int position = vholder.getAdapterPosition();
//
//                removeAt(position);
//
//                Toast.makeText(context,"This is position "+position,Toast.LENGTH_SHORT ).show();
//            }catch (Exception ex){
//                return;
//            }
//        }
//    };

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

    public void updateCity(ArrayList cities){
        city.clear();
        city.addAll(cities);
    }

    public ArrayList<String> getCity(){
        return city;
    }
}
