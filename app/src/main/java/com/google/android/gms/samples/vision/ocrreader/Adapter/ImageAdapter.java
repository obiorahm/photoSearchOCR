package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.MealMenuActivity;
import com.google.android.gms.samples.vision.ocrreader.R;

import java.util.ArrayList;

/**
 * Created by mgo983 on 8/14/18.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private ArrayList<String[]> mData = new ArrayList<String[]>();
    private LayoutInflater inflater;
    private Context context;

    private String LOG_TAG = ImageAdapter.class.getSimpleName();

    private TextToSpeech myTTS;

    public ImageAdapter(){

    }

    public static class ViewHolder extends  RecyclerView.ViewHolder{
        public TextView mTextView;
        public ImageView mImageView;

        public ViewHolder(View convertView){
            super(convertView);
            mTextView = convertView.findViewById(R.id.test_text);
            mImageView = convertView.findViewById(R.id.image_item);
        }

    }

    public ImageAdapter(Context context, int resource){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        myTTS = ((MealMenuActivity) context).myTTS;
    }

    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredient_image_item, null);
        ViewHolder viewHolder = new ViewHolder(convertView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        final int WORD_INDEX = 1;
        final int URI_INDEX = 0;
        final String word = mData.get(position)[WORD_INDEX];
        holder.mTextView.setText(word);
        Glide.with(context).load(mData.get(position)[URI_INDEX]).into(holder.mImageView);

        //speak onClick
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTTS.speak(word,TextToSpeech.QUEUE_FLUSH,null);
            }
        });


    }

    @Override
    public int getItemCount(){
        return mData.size();
    }

    public void addItem(String[] imageUrl){
        mData.add(imageUrl);
        notifyDataSetChanged();
    }




}
