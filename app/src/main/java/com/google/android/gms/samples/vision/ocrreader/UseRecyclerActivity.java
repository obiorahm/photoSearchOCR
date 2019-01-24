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

    public void setView(RecyclerWordAdapter adapter, RecyclerView recyclerView, ArrayList<String[]> edamanInfo){}

    public void setView(RecyclerWordAdapter adapter, ArrayList<String[]> edamanInfo){}

    public void setView( ArrayList<String []> edmanInfo){}

    public void processWebResults(Document document){}

    public void getRestaurantPhoto(String placeId, ImageView imageView){}

    /**
     *     Run in the activity and available to FetchRestaurantPlaceID. this function updates the adapter after the place ids have been
     *     have been retrieved from the Internet.
     */

    public void addPlaceIdToAdapter(HashMap<String, String[]> PlaceIdPack){

    }

    /**
     *
     * @param lngLatPack contains JSONdata for location in string
     */
    public void addLongLatToAdapter(HashMap<String, String[]> lngLatPack){

    }

    /***
     * @param streetViewPanoramaView
     */

    public void setUpPanorama(StreetViewPanoramaView streetViewPanoramaView){

    }

    /**
     *
     * @param streetViewPanoramaView view to place the panorama
     * @param longitude the longitude and latitude of the restaurant
     *
     */

    public void setUpPanorama(StreetViewPanoramaView streetViewPanoramaView, String longitude, String Latitude ){

    }
}
