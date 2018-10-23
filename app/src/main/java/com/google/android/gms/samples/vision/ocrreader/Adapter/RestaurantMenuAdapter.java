package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.GeographyActivity;
import com.google.android.gms.samples.vision.ocrreader.OpenRestaurantMenuActivity;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import java.util.ArrayList;

/**
 * Created by mgo983 on 10/18/18.
 */

public class RestaurantMenuAdapter extends RecyclerView.Adapter<RestaurantMenuAdapter.ViewHolder> {


    LayoutInflater inflater;
    Context context;
    TextToSpeech myTTS;
    ArrayList<String[]> mData = new ArrayList<>();
    ArrayList<ArrayList<String>> mfoodItems = new ArrayList<>();
    private RecyclerView last_selected = null;

    public RelativeLayout last_selected_rl = null;



    public static class ViewHolder extends  RecyclerView.ViewHolder {

        private RecyclerView mRecyclerView;
        private RecyclerView mFoodItemRecyclerView;
        private View mBackground;
        private RelativeLayout mRelativeLayout;
        private ImageButton mImageButton;
        private TextView mTextView;
        private ImageButton mInfoImageButton;
        private TextView mInfoTextView;


        public ViewHolder(View convertView) {
            super(convertView);
            mBackground = convertView;
            mRecyclerView = convertView.findViewById(R.id.text_by_text);
            mFoodItemRecyclerView = convertView.findViewById(R.id.food_item);
            //mRelativeLayout = convertView.findViewById(R.id.relative_layout);
            mRelativeLayout = convertView.findViewById(R.id.enclosing_layout);
            mImageButton = convertView.findViewById(R.id.speak_whole_text);
            mInfoTextView = convertView.findViewById(R.id.info_text);
            mInfoImageButton = convertView.findViewById(R.id.more_info);

        }
    }

    public RestaurantMenuAdapter(){

    }

    public void addItem(String[] category, ArrayList<String> category_items){
        mData.add(category);
        mfoodItems.add(category_items);

        notifyDataSetChanged();
    }

    public RestaurantMenuAdapter(Context context, int resource){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        myTTS = ((OpenRestaurantMenuActivity) context).myTTS;
    }

    @Override
    public RestaurantMenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item, null);
        RestaurantMenuAdapter.ViewHolder viewHolder = new RestaurantMenuAdapter.ViewHolder(convertView);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RestaurantMenuAdapter.ViewHolder holder, final int position){
        final String[] restaurantData =  mData.get(position);
        final int CATEGORY_TITLE = 0;
        final int CATEGORY_DESCRIPTION = 1;
        final String word = restaurantData[CATEGORY_TITLE];
        final String description = restaurantData[CATEGORY_DESCRIPTION];

        // setup horizontal text by text adapter
        TextByTextAdapter adapter = new TextByTextAdapter(context, R.layout.recognized_text_item);

        String tokenizedString [] = word.split(" ");

        for (String child : tokenizedString){
            adapter.addItem(child);
        }

        LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
        holder.mRecyclerView.setLayoutManager(layoutManager);

        holder.mRecyclerView.setAdapter(adapter);


        holder.mRelativeLayout.setSelected(false);
        //((DetectImageActivity) context).fetchSuggestionsFor("theer aer a coupel of mistaeks in this senence");


        holder.mBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                control_select(holder, restaurantData);

            }
        });

        holder.mRecyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                control_select(holder, restaurantData);

            }
        });

        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
                //control_select(holder, restaurantData);
            }
        });

        holder.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                control_select(holder, restaurantData);
            }
        });

        holder.mInfoTextView.setText(description);

        holder.mInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTTS.speak(description, TextToSpeech.QUEUE_FLUSH,null);
            }
        });


        holder.mInfoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.mInfoTextView.getVisibility() == View.VISIBLE){
                    holder.mInfoTextView.setVisibility(View.GONE);
                }else{
                    holder.mInfoTextView.setVisibility(View.VISIBLE);
                }
            }
        });


        //set up food item adapter
        FoodItemAdapter foodItemAdapter = new FoodItemAdapter(context, R.layout.single_food_item);

        foodItemAdapter.addItem(mfoodItems.get(position));

        LinearLayoutManager FoodItemLayoutManager= new LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false);
        holder.mFoodItemRecyclerView.setLayoutManager(FoodItemLayoutManager);

        holder.mFoodItemRecyclerView.setAdapter(foodItemAdapter);




    }

/*    private void control_select(RestaurantMenuAdapter.ViewHolder holder, String[] MenuData){
        String mealCategory = MenuData[0];
        last_selected = OpenRestaurantMenuActivity.last_parent_di;
        if (last_selected != null && last_selected != holder.mRecyclerView){
            RestaurantMenuAdapter.ViewHolder lastViewHolder = new RestaurantMenuAdapter.ViewHolder(last_selected);
            lastViewHolder.mRecyclerView.setSelected(false);
        }
        if(holder.mRecyclerView.isSelected()){
            holder.mRecyclerView.setSelected(false);
        }else{
            holder.mRecyclerView.setSelected(true);
            OpenRestaurantMenuActivity.selected_item = mealCategory;
        }
        OpenRestaurantMenuActivity.last_parent_di = holder.mRecyclerView;

    }*/


    private void control_select(RestaurantMenuAdapter.ViewHolder holder, String[] MenuData){
        String mealCategory = MenuData[0];
        last_selected_rl = OpenRestaurantMenuActivity.last_rl_parent;
        if (last_selected_rl != null && last_selected_rl != holder.mRelativeLayout){
            RestaurantMenuAdapter.ViewHolder lastViewHolder = new RestaurantMenuAdapter.ViewHolder(last_selected_rl);
            lastViewHolder.mRelativeLayout.setSelected(false);
            lastViewHolder.mFoodItemRecyclerView.setVisibility(View.GONE);
        }
        if(holder.mRelativeLayout.isSelected()){
            holder.mRelativeLayout.setSelected(false);
            holder.mFoodItemRecyclerView.setVisibility(View.GONE);

        }else{
            holder.mRelativeLayout.setSelected(true);
            OpenRestaurantMenuActivity.selected_item = mealCategory;
            holder.mFoodItemRecyclerView.setVisibility(View.VISIBLE);

        }
        OpenRestaurantMenuActivity.last_rl_parent = holder.mRelativeLayout;

    }
    @Override
    public int getItemCount(){
        return mData.size();
    }


}
