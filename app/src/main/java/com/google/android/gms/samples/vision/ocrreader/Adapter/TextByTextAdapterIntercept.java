package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.app.Activity;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.DetectImageActivity;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import java.util.ArrayList;

/**
 * Created by mgo983 on 12/22/18.
 */

public class TextByTextAdapterIntercept extends RecyclerView.Adapter<TextByTextAdapterIntercept.ViewHolder> {

    private ArrayList<String> mData = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;

    private String LOG_TAG = TextByTextAdapterIntercept.class.getSimpleName();

    private TextToSpeech myTTS;

    private static RecyclerView lastParent = null;

    private boolean notFoodItem = true;


    public static class ViewHolder extends  RecyclerView.ViewHolder{
        public TextView mTextView;
        public RecyclerView mRecyclerView;
        public ImageView mImageView;
        public View mConvertView;


        public ViewHolder(View convertView, View parent){
            super(convertView);
            mTextView = convertView.findViewById(R.id.recognized_text);
            mImageView = convertView.findViewById(R.id.descriptive_image);
            mRecyclerView = (RecyclerView) parent;
            mConvertView = convertView;

        }

    }


    public TextByTextAdapterIntercept(Context context, int resource, boolean notFoodItem){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        //myTTS = ((DetectImageActivity) context).myTTS;
        myTTS = ((UseRecyclerActivity) context).myTTS;
        this.notFoodItem = notFoodItem;

    }


    @Override
    public TextByTextAdapterIntercept.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.recognized_text_item, null);
        TextByTextAdapterIntercept.ViewHolder viewHolder = new TextByTextAdapterIntercept.ViewHolder(convertView, parent);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final TextByTextAdapterIntercept.ViewHolder holder, int position){

        final String word = mData.get(position);
        holder.mTextView.setText(word);

        ((UseRecyclerActivity) context).loadImage(word, holder.mImageView, notFoodItem);
        holder.mTextView.setOnClickListener((View view) ->{

            if (holder.mImageView.getVisibility() == View.VISIBLE){
                holder.mImageView.setVisibility(View.GONE);
            }else{
                holder.mImageView.setVisibility(View.VISIBLE);
            }
                /*myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
                //lastParent = DetectImageActivity.last_parent_di;
                lastParent = UseRecyclerActivity.last_parent_di;
                if (lastParent != null && lastParent != holder.mRecyclerView){
                    lastParent.setSelected(false);
                }
                if(holder.mRecyclerView.isSelected()){
                    holder.mRecyclerView.setSelected(false);
                }else{
                    UseRecyclerActivity.selected_item = getSelectedString();
                    //DetectImageActivity.selected_meal = getSelectedString();
                    holder.mRecyclerView.setSelected(true);
                }
                DetectImageActivity.last_parent_di = holder.mRecyclerView;*/
        });
    }

    public String getSelectedString(){
        String appendMData = "";
        for (String child : mData){
            appendMData += child + " ";
        }
        return appendMData;
    }

    @Override
    public int getItemCount(){
        return mData.size();
    }

    public void addItem(String text){
        mData.add(text);
        notifyDataSetChanged();
    }

}