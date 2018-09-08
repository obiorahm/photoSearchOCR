package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by mgo983 on 9/7/18.
 */

public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.ViewHolder> {

    ArrayList<String> mData;
    Context context;

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View convertView){
            super(convertView);
        }

    }

    public void addItem(String [] m){

    }

    @Override

    public void onBindViewHolder(final ImageRecyclerAdapter.ViewHolder holder, final int position) {

    }

    @Override
    public int getItemCount(){
        return mData.size();
    }

    @Override
    public ImageRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        return new ViewHolder(new View(context));

    }
}
