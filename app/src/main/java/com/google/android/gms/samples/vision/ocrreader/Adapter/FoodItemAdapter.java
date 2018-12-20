package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.DetectImageActivity;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by mgo983 on 10/22/18.
 */

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.ViewHolder> {

    LayoutInflater inflater;
    ArrayList<String> mData;
    TextToSpeech myTTS;
    Context context;
    private String LOG_TAG = FoodItemAdapter.class.getSimpleName();

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTextView;
        private ImageButton mImageButton;
        private RecyclerView mRecyclerView;

        public ViewHolder(View parent){
            super(parent);
            mTextView = parent.findViewById(R.id.single_food_item);
            mImageButton = parent.findViewById(R.id.speak_whole_text);
            mRecyclerView = parent.findViewById(R.id.food_item_options);
        }

    }

    public FoodItemAdapter(Context context, int resource){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        //myTTS = ((DetectImageActivity) context).myTTS;
        myTTS = ((UseRecyclerActivity) context).myTTS;

    }

    @Override
    public FoodItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.single_food_item, null);
        FoodItemAdapter.ViewHolder viewHolder = new FoodItemAdapter.ViewHolder(convertView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final FoodItemAdapter.ViewHolder holder, int position){

        final String word = mData.get(position);
        holder.mTextView.setText(word);

        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_view(word, holder);
            }
        });

        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_view(word, holder);
            }
        });

        //set up food item adapter
        FoodItemOrderOptionAdapter foodItemOrderOptionAdapter = new FoodItemOrderOptionAdapter(context);

        addAllItems(foodItemOrderOptionAdapter);
        LinearLayoutManager foodItemLayoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
        holder.mRecyclerView.setLayoutManager(foodItemLayoutManager);

        holder.mRecyclerView.setAdapter(foodItemOrderOptionAdapter);
    }


    private void addAllItems(FoodItemOrderOptionAdapter adapter){
        AssetManager assetManager = context.getAssets();
        try{
            final String allAssets[] = assetManager.list("top_level_icons");

            for (String option : allAssets){
                String option_image_url = "file:///android_asset/top_level_icons/" + option;
                adapter.addItem(option_image_url);
            }

        }catch (IOException e){
            Log.e(LOG_TAG, e + "");
        }


    }
    private void select_view(String word,FoodItemAdapter.ViewHolder holder){
        myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);

        if (holder.mTextView.isSelected()){
            holder.mTextView.setSelected(false);
            holder.mRecyclerView.setVisibility(View.GONE);

        }else{
            holder.mTextView.setSelected(true);
            holder.mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount(){
        return mData.size();
    }

    public void addItem(String text){
        mData.add(text);
        notifyDataSetChanged();
    }

    public void addItem(ArrayList<String> foodItems){
        mData = foodItems;
    }

}
