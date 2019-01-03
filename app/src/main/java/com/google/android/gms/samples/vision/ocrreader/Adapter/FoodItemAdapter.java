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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.DetectImageActivity;
import com.google.android.gms.samples.vision.ocrreader.FetchMealDetails;
import com.google.android.gms.samples.vision.ocrreader.OpenRestaurantMenuActivity;
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
        private ImageButton mImageButtonShowFoodItem;
        private RecyclerView mRecyclerView;
        private RelativeLayout mContainingRelativeLayout;

        public ViewHolder(View parent){
            super(parent);
            mTextView = parent.findViewById(R.id.single_food_item);
            mImageButton = parent.findViewById(R.id.speak_whole_text);
            mImageButtonShowFoodItem = parent.findViewById(R.id.show_food_item_image);
            mRecyclerView = parent.findViewById(R.id.food_item_options);
            mContainingRelativeLayout = parent.findViewById(R.id.containing_relative_layout);
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

        /*// the text view is parent wide
        holder.mTextView.setSelected(false);


        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_view(word, holder);
            }
        });*/

        //select with relative layout instead

        holder.mContainingRelativeLayout.setSelected(false);

        holder.mContainingRelativeLayout.setOnClickListener(new View.OnClickListener() {
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

        holder.mImageButtonShowFoodItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imageViewShowFoodItem = ((OpenRestaurantMenuActivity) context).findViewById(R.id.food_image);
                setUpRecyclerView(word, false);
                //imageViewShowFoodItem.setVisibility(View.VISIBLE);
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
/*    private void select_view(String word,FoodItemAdapter.ViewHolder holder){
        myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);

        if (holder.mTextView.isSelected()){
            holder.mTextView.setSelected(false);
            holder.mRecyclerView.setVisibility(View.GONE);

        }else{
            holder.mTextView.setSelected(true);
            holder.mRecyclerView.setVisibility(View.VISIBLE);
        }
    }*/

    private void select_view(String word,FoodItemAdapter.ViewHolder holder){
        myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);

        if (holder.mContainingRelativeLayout.isSelected()){
            holder.mContainingRelativeLayout.setSelected(false);
            holder.mRecyclerView.setVisibility(View.GONE);

        }else{
            holder.mContainingRelativeLayout.setSelected(true);
            holder.mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void setUpRecyclerView(String wholeMealText, Boolean blockOrText){


        OpenRestaurantMenuActivity openRestaurantMenuActivity = (OpenRestaurantMenuActivity) context;

        RecyclerView recyclerView = openRestaurantMenuActivity.findViewById(R.id.gridview_edit_meal);

        RecyclerWordAdapter recyclerWordAdapter = new RecyclerWordAdapter(context, R.layout.gridview_item, myTTS, wholeMealText, blockOrText);


        ProgressBar progressBar = openRestaurantMenuActivity.findViewById(R.id.searching_edmame);
        progressBar.setVisibility(View.VISIBLE);

        FetchMealDetails fetchMealDetails = new FetchMealDetails(recyclerWordAdapter, context);
        fetchMealDetails.execute(wholeMealText);

        recyclerView.setAdapter(recyclerWordAdapter);

        ImageButton imageButtonclearRecycler = openRestaurantMenuActivity.findViewById(R.id.cancel_gridview_edit_meal);
        imageButtonclearRecycler.setVisibility(View.VISIBLE);

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
