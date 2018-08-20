package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.DetectImageActivity;
import com.google.android.gms.samples.vision.ocrreader.R;

import java.util.ArrayList;

/**
 * Created by mgo983 on 8/20/18.
 */

public class TextByTextAdapter extends RecyclerView.Adapter<TextByTextAdapter.ViewHolder> {

    private ArrayList<String> mData = new ArrayList<String>();
    private LayoutInflater inflater;
    private Context context;

    private String LOG_TAG = TextByTextAdapter.class.getSimpleName();

    private TextToSpeech myTTS;

    public TextByTextAdapter(){

    }

    public static class ViewHolder extends  RecyclerView.ViewHolder{
        public TextView mTextView;

        public ViewHolder(View convertView){
            super(convertView);
            mTextView = convertView.findViewById(R.id.recognized_text);
        }

    }


    public TextByTextAdapter(Context context, int resource){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        myTTS = ((DetectImageActivity) context).myTTS;
    }


    @Override
    public TextByTextAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recognized_text_item, null);
        TextByTextAdapter.ViewHolder viewHolder = new TextByTextAdapter.ViewHolder(convertView);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(TextByTextAdapter.ViewHolder holder, int position){

        final String word = mData.get(position);
        holder.mTextView.setText(word);

        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
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
