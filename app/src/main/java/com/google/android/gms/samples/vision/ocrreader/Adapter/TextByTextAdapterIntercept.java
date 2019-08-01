package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.app.Activity;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.DetectImageActivity;
import com.google.android.gms.samples.vision.ocrreader.FetchNounDependency;
import com.google.android.gms.samples.vision.ocrreader.NounDependencyJsonHandler;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mgo983 on 12/22/18.
 */

public class TextByTextAdapterIntercept extends RecyclerView.Adapter<TextByTextAdapterIntercept.ViewHolder> implements NounDependencyJsonHandler {

    private ArrayList<String> mData = new ArrayList<>();
    private ArrayList<Integer> mImageData = new ArrayList<>();
    private HashMap<String,String> mListOfNouns = new HashMap<>();

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
        myTTS = ((UseRecyclerActivity) context).myTTS;
        this.notFoodItem = notFoodItem;

    }

    private void get_noun_dependency(String wholeText){
        FetchNounDependency nounDependency = new FetchNounDependency(this);
        nounDependency.execute(wholeText);


    }

@Override
    public void processJson(String text){
        try{
            JSONObject data = new JSONObject(text);
            JSONArray chunk = data.getJSONArray("chunk");
            JSONArray chunk_root =  data.getJSONArray("chunk_root");
            for (int i = 0; i < chunk.length(); i++){
                mListOfNouns.put((String) chunk_root.get(i), (String) chunk_root.get(i));
            }



        }catch (JSONException e){
            Log.e(LOG_TAG, e.toString());
        }
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

        holder.mImageView.setVisibility(mImageData.get(position));

        if (mListOfNouns.containsKey(word))
            ((UseRecyclerActivity) context).loadImage(word, holder.mImageView, notFoodItem);

        holder.mTextView.setOnClickListener((View view) ->{

            myTTS.speak(getSelectedString(),TextToSpeech.QUEUE_FLUSH, null, null);

            if (holder.mImageView.getVisibility() == View.VISIBLE){
                mImageData.set(position, View.GONE);
                holder.mImageView.setVisibility(View.GONE);
            }else{
                mImageData.set(position, View.VISIBLE);
                holder.mImageView.setVisibility(View.VISIBLE);
            }

        });
    }


    private void displayDescriptiveImage(TextByTextAdapterIntercept.ViewHolder holder, String word, int position){
        if (holder.mImageView.getVisibility() == View.VISIBLE){
            mImageData.set(position, View.GONE);
            holder.mImageView.setVisibility(View.GONE);
        }else{
            mImageData.set(position, View.VISIBLE);
            holder.mImageView.setVisibility(View.VISIBLE);
        }
    }


    public void displayAllDescriptiveImages(){
        for (int i = 0; i < mImageData.size(); i++){
            mImageData.set(i,View.VISIBLE);
        }
        notifyDataSetChanged();
    }

    public void hideAllDescriptiveImages(){
        for (int i = 0; i < mImageData.size(); i++){
            mImageData.set(i,View.GONE);
        }
        notifyDataSetChanged();
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

    public void addItem(String fullText){

        String tokenizedString [] = fullText.split(" ");

        for (String child : tokenizedString){
            mData.add(child);
            mImageData.add(View.GONE);
        }

        get_noun_dependency(fullText);

        //mData.add(text);
        //mImageData.add(View.GONE);
        notifyDataSetChanged();
    }


    public void hideImage(){
        for (int i = 0; i < mImageData.size(); i++){
            mImageData.set(i, View.GONE);
        }
        notifyDataSetChanged();
    }




}