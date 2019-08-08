package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.FetchNounDependency;
import com.google.android.gms.samples.vision.ocrreader.NounDependencyJsonHandler;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.SetAdapter;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mgo983 on 12/22/18.
 */

public class TextByTextAdapterIntercept extends RecyclerView.Adapter<TextByTextAdapterIntercept.ViewHolder> implements NounDependencyJsonHandler, SetAdapter {

    private ArrayList<String[]> mData = new ArrayList<>();
    private ArrayList<Integer> mImageData = new ArrayList<>();
    private HashMap<String,String> mListOfNouns = new HashMap<>();
    private HashMap<String, Uri> mUrls = new HashMap<>();

    private LayoutInflater inflater;
    private Context context;

    private String LOG_TAG = TextByTextAdapterIntercept.class.getSimpleName();

    private TextToSpeech myTTS;


    private int URI_POS = 1;
    private int WORD_POS = 0;

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
                Log.d(LOG_TAG, "processJSON " + chunk_root.get(i));
                String root_noun = ((String) chunk_root.get(i)).toLowerCase().trim();
                mListOfNouns.put(root_noun, root_noun);
                notifyDataSetChanged();
            }



        }catch (JSONException e){
            Log.e(LOG_TAG, e.toString());
        }
    }





    @Override
    public TextByTextAdapterIntercept.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.recognized_text_item, null);

        return new TextByTextAdapterIntercept.ViewHolder(convertView, parent);
    }


    @Override
    public void onBindViewHolder(final TextByTextAdapterIntercept.ViewHolder holder, int position){


        final String [] data = mData.get(position);
        final String word = data[WORD_POS];

        holder.mTextView.setText(word);

        holder.mImageView.setVisibility(mImageData.get(position));

        if (mListOfNouns.containsKey(word))
        {
            Glide.with(context).load(mUrls.get(word)).into(holder.mImageView);

        }

        Log.d(LOG_TAG, "bindviewholder " + word);


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


    public void displayDescriptiveImage(TextByTextAdapterIntercept.ViewHolder holder, String word, int position){
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

    private String getSelectedString(){
        String appendMData = "";
        for (String [] child : mData){
            appendMData += child[WORD_POS] + " ";
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
            //mData.add(child);

            String uri = "";
            String icon[] = {child.toLowerCase().trim(), uri};
            ((UseRecyclerActivity) context).loadImage(icon, this, false);

            mData.add(icon);
            mImageData.add(View.GONE);
            notifyDataSetChanged();
        }

        get_noun_dependency(fullText);

        //mData.add(text);
        //mImageData.add(View.GONE);
    }

    @Override
    public void addImageUrl(String word, Uri uri){
        mUrls.put(word, uri);
        String x = uri == null? "": uri.toString();
        Log.d(LOG_TAG, "found image "+ word + x + mListOfNouns.get(word));
        notifyDataSetChanged();
    }


    public void hideImage(){
        for (int i = 0; i < mImageData.size(); i++){
            mImageData.set(i, View.GONE);
        }
        notifyDataSetChanged();
    }



}