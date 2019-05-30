package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RecyclerWordAdapter;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mgo983 on 9/6/18.
 */

public class UseRecyclerActivity extends Activity  {

    public static TextToSpeech myTTS;

    //selected meal, restaurant
    public static String selected_item = "";

    public static RecyclerView last_parent_di;

    public static RelativeLayout last_rl_parent;

    public static Object [] last_selected_object;

    public void setView(RecyclerWordAdapter adapter, RecyclerView recyclerView, ArrayList<String[]> edamanInfo){}

    public void setView(RecyclerWordAdapter adapter, ArrayList<String[]> edamanInfo){}

    public void setView( ArrayList<String []> edmanInfo){}

    public void processWebResults(Document document){}

    public void beginFetchRestaurantLogos(HashMap<String, String[]> restaurantInfo){}


    /**
     *
     * @param lngLatPack contains JSONdata for location in string
     */
    public void addLongLatToAdapter(HashMap<String, String[]> lngLatPack){

    }

    /**
     *
     * @param imageUrlPack
     */

    public void addImageUrlToAdapter(HashMap<String, String[]> imageUrlPack){

    }


    /**
     *
     * @param streetViewPanoramaView view to place the panorama
     * @param longitude the longitude and latitude of the restaurant
     *
     */

    public void setUpPanorama(StreetViewPanoramaView streetViewPanoramaView, String longitude, String Latitude ){

    }

    /**
     *
     * @param imageView comes from the TextByTextAdapter
     */
    public void displayTextByTextImage(ImageView imageView){

    }

}
