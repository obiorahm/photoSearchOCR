package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.samples.vision.ocrreader.Adapter.RecyclerWordAdapter;

import org.jsoup.nodes.Document;

/**
 * Created by mgo983 on 9/6/18.
 */

public class UseRecyclerActivity extends Activity  {

    public static TextToSpeech myTTS;

    //selected meal, restaurant
    public static String selected_item = "";

    public static RecyclerView last_parent_di;

    public void setView(RecyclerWordAdapter adapter, RecyclerView recyclerView){

    }

    public void processWebResults(Document document){

    }
}
