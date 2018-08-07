package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.app.Activity;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.R;

import java.util.ArrayList;

/**
 * Created by mgo983 on 8/6/18.
 */

public class WordAdapter extends ArrayAdapter {

    private LayoutInflater inflater;
    private Context context;
    private TextToSpeech myTTs;
    private ArrayList<String> mData = new ArrayList<String>();

    public WordAdapter(Context context, int resource, TextToSpeech myTTS){
        super(context,resource);
        this.myTTs = myTTS;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void addItem(String wordInMeal){
        mData.add(wordInMeal);
    }

    @Override
    public int getCount(){
        return mData.size();
    }

    @Override
    public String getItem(int position){
        return (String) mData.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = inflater.inflate(R.layout.gridview_item, null);
        }

        final TextView textView = (TextView) convertView.findViewById(R.id.word_in_text);
        textView.setText(mData.get(position));


        myTTs.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                textView.setHighlightColor(((Activity) context).getResources().getColor(R.color.common_google_signin_btn_text_dark_focused));
            }

            @Override
            public void onDone(String s) {
                textView.setHighlightColor(((Activity) context).getResources().getColor(R.color.common_google_signin_btn_text_light_disabled));

            }

            @Override
            public void onError(String s) {

            }
        });

        //say all the words
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String wholeString = mData.get(0);
                for (int i = 1; i < mData.size(); i++){
                    wholeString += " " + mData.get(i);
                }
                myTTs.speak(wholeString, TextToSpeech.QUEUE_FLUSH, null);
            }
        });


        return convertView;
    }
}
