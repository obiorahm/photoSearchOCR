package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.DetectImageActivity;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import java.util.ArrayList;

/**
 * Created by mgo983 on 10/22/18.
 */

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.ViewHolder> {

    LayoutInflater inflater;
    ArrayList<String> mData;
    TextToSpeech myTTS;
    Context context;

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTextView;
        private ImageButton mImageButton;

        public ViewHolder(View parent){
            super(parent);
            mTextView = parent.findViewById(R.id.single_food_item);
            mImageButton = parent.findViewById(R.id.speak_whole_text);
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
                select_view(word, view);
            }
        });

        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_view(word, holder.mTextView);
            }
        });
    }

    private void select_view(String word,View view){
        myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);

        if (view.isSelected()){
            view.setSelected(false);
        }else{
            view.setSelected(true);
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
