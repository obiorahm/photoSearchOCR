package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.R;

import java.util.ArrayList;

/**
 * Created by mgo983 on 8/14/18.
 */

public class ImageAdapter extends ArrayAdapter {

    private ArrayList<String> mData = new ArrayList<String>();
    private LayoutInflater inflater;
    private Context context;

    public ImageAdapter(Context context, int resource){
        super(context, resource);
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    public void addItem(String imageUrl){
        mData.add(imageUrl);
        notifyDataSetChanged();
    }


    @Override
    public int getCount(){
        return mData.size();
    }

    @Override
    public String getItem(int position){
        return (String) mData.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override

    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent){

        if (convertView == null){
            convertView =  inflater.inflate(R.layout.ingredient_image_item, null);
        }

        ImageView imageView = convertView.findViewById(R.id.image_item);
        Glide.with(context).load(Uri.parse(mData.get(position).toString())).into(imageView);

        return convertView;
    }

}
