package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.DetectImageActivity;
import com.google.android.gms.samples.vision.ocrreader.R;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by mgo983 on 8/20/18.
 */

public class RecognizedTextAdapter extends RecyclerView.Adapter<RecognizedTextAdapter.ViewHolder> {

    private  ArrayList<String> mData = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;
    private TextToSpeech myTTS;

    private int currentClick = 0;

    private static  final String LOG_TAG = RecognizedTextAdapter.class.getSimpleName();

    public static class ViewHolder extends  RecyclerView.ViewHolder{

        private RecyclerView mRecyclerView;
        private RelativeLayout mBackground;

        public ViewHolder(View convertView){
            super(convertView);
            mBackground = convertView.findViewById(R.id.relative_layout);
            mRecyclerView = convertView.findViewById(R.id.text_by_text);
        }

    }

    public RecognizedTextAdapter(Context context, int resource){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        myTTS = ((DetectImageActivity) context).myTTS;
    }

    @Override
    public RecognizedTextAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_text, null);
        RecognizedTextAdapter.ViewHolder viewHolder = new RecognizedTextAdapter.ViewHolder(convertView);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final RecognizedTextAdapter.ViewHolder holder, final int position){
        final String word = mData.get(position);

        // setup horizontal text by text adapter
        TextByTextAdapter adapter = new TextByTextAdapter(context, R.layout.recognized_text_item);

        String tokenizedString [] = word.split(" ");

        for (String child : tokenizedString){
            adapter.addItem(child);
        }

        LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
        holder.mRecyclerView.setLayoutManager(layoutManager);

        holder.mRecyclerView.setAdapter(adapter);


        holder.mBackground.setSelected(false);
        //((DetectImageActivity) context).fetchSuggestionsFor("theer aer a coupel of mistaeks in this senence");


        holder.mRecyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentClick = position;
                holder.mRecyclerView.setSelected(true);
                //view.setSelected(true);


                Log.d(LOG_TAG, " position " + position );
            }
        });
        /*holder.mTextView.setText(word);

        //speak onClick
        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTTS.speak(word,TextToSpeech.QUEUE_FLUSH,null);
            }
        });*/


    }

    @Override
    public int getItemCount(){
        return mData.size();
    }

    public void addItem(String line){
        mData.add(line);
        notifyDataSetChanged();
    }

    public int getCurrentClick(){ return currentClick;}

}
