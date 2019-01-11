package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mgo983 on 1/10/19.
 */

public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder> {

    private ArrayList<String> mData = new ArrayList<>();
    private ArrayList<Integer []> mSpecificOptions = new ArrayList<>();
    private ArrayList<String> mUrl = new ArrayList<>();

    LayoutInflater inflater;
    TextToSpeech myTTS;
    Context context;


    String LOG_TAG = ShoppingCartAdapter.class.getSimpleName();


    public class ViewHolder extends RecyclerView.ViewHolder{


        private ImageView mImageView;
        private TextView mTextView;

        public ViewHolder(View parent){
            super(parent);
            mImageView = parent.findViewById(R.id.order_option);
            mTextView = parent.findViewById(R.id.order_option_text);
        }

    }


    public ShoppingCartAdapter(Context context){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        myTTS = ((UseRecyclerActivity) context).myTTS;
    }


    @Override
    public ShoppingCartAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.enlarged_option_item, null);
        return new ShoppingCartAdapter.ViewHolder(convertView);
    }


    @Override
    public void onBindViewHolder(final ShoppingCartAdapter.ViewHolder holder, final int position){

        holder.mTextView.setText(mData.get(position));

    }

    public void addItem(String food_item_name, Integer [] specifics, String url ){
        mData.add(food_item_name);
        mSpecificOptions.add(specifics);
        mUrl.add(url);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount(){
        return mData.size();
    }
}
