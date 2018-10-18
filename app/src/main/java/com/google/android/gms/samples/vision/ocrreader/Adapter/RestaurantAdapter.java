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

import com.google.android.gms.samples.vision.ocrreader.DetectImageActivity;
import com.google.android.gms.samples.vision.ocrreader.GeographyActivity;
import com.google.android.gms.samples.vision.ocrreader.R;

import java.util.ArrayList;

/**
 * Created by mgo983 on 10/17/18.
 */

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    LayoutInflater inflater;
    Context context;
    TextToSpeech myTTS;
    ArrayList<String[]> mData = new ArrayList<>();
    private RecyclerView last_selected = null;



    public static class ViewHolder extends  RecyclerView.ViewHolder {

        private RecyclerView mRecyclerView;
        private View mBackground;
        private RelativeLayout mRelativeLayout;
        private ImageButton mImageButton;


        public ViewHolder(View convertView) {
            super(convertView);
            mBackground = convertView;
            mRecyclerView = convertView.findViewById(R.id.text_by_text);
            mRelativeLayout = convertView.findViewById(R.id.relative_layout);
            mImageButton = convertView.findViewById(R.id.speak_whole_text);

        }
    }

        public RestaurantAdapter(){

        }

        public void addItem(String[] item){
            mData.add(item);
            notifyDataSetChanged();
        }

        public RestaurantAdapter(Context context, int resource){
            super();
            inflater = LayoutInflater.from(context);
            this.context = context;
            myTTS = ((GeographyActivity) context).myTTS;
        }

        @Override
        public RestaurantAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_text, null);
            RestaurantAdapter.ViewHolder viewHolder = new RestaurantAdapter.ViewHolder(convertView);


            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RestaurantAdapter.ViewHolder holder, final int position){
            final String[] restaurantData =  mData.get(position);
            final int URL_POS = 0;
            final int TITLE_POS = 1;
            final String word = restaurantData[TITLE_POS];

            // setup horizontal text by text adapter
            TextByTextAdapter adapter = new TextByTextAdapter(context, R.layout.recognized_text_item);

            String tokenizedString [] = word.split(" ");

            for (String child : tokenizedString){
                adapter.addItem(child);
            }

            LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
            holder.mRecyclerView.setLayoutManager(layoutManager);

            holder.mRecyclerView.setAdapter(adapter);


            holder.mRecyclerView.setSelected(false);
            //((DetectImageActivity) context).fetchSuggestionsFor("theer aer a coupel of mistaeks in this senence");


            holder.mBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    control_select(holder, word);

                }
            });

            holder.mRecyclerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    control_select(holder, word);

                }
            });

            holder.mImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
                    control_select(holder, word);
                }
            });



        }

    private void control_select(RestaurantAdapter.ViewHolder holder, String word){
        last_selected = DetectImageActivity.last_parent_di;
        if (last_selected != null && last_selected != holder.mRecyclerView){
            RestaurantAdapter.ViewHolder lastViewHolder = new RestaurantAdapter.ViewHolder(last_selected);
            lastViewHolder.mRecyclerView.setSelected(false);
        }
        if(holder.mRecyclerView.isSelected()){
            holder.mRecyclerView.setSelected(false);
        }else{
            holder.mRecyclerView.setSelected(true);
            GeographyActivity.selected_restaurant = word;
        }
        GeographyActivity.last_parent_di = holder.mRecyclerView;

    }

    @Override
    public int getItemCount(){
        return mData.size();
    }


}
