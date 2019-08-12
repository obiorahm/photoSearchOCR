package com.google.android.gms.samples.vision.ocrreader;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class FetchPixabayImage extends  FetchImageEngine  {

    String LOG_TAG = FetchPixabayImage.class.getSimpleName();

    static String BASE_URL =  "https://pixabay.com/api/";
    static String API_KEY = "5321405-e3d51a927066916f670cf60c0";


    FetchPixabayImage(SetAdapter adapter){
        this.adapter = adapter;
    }

    @Override
    public Uri buildUrl (String queryParameter){
        final String API_KEY_NAME = "key";
        final String QUERY = "q";

        Uri buildUri;

        buildUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(API_KEY_NAME,API_KEY)
                .appendQueryParameter(QUERY, queryParameter)

                .build();
        return buildUri;
    }

    @Override
    public Uri handleJSON(String JSONData){

        try{
            final String imageUrl;
            final JSONObject obj = new JSONObject(JSONData);
            final JSONArray hits = obj.getJSONArray("hits");

            int lengthOfJSONEntries = hits.length();
            if (lengthOfJSONEntries == 0) return null;
            int noOfObjects = lengthOfJSONEntries >= 1 ? 1: lengthOfJSONEntries;

            if (noOfObjects >= 1){
                JSONObject ithElement = hits.getJSONObject(0);
                imageUrl = ithElement.getString("previewURL");
                Log.v("JSON Url", imageUrl);

                return Uri.parse(imageUrl);
            }


        }catch(JSONException e){
            Log.e("JSON Error: ", e.toString());
        }
        return null;
    }

    @Override
    public void nextEngine(String searchString) {

    }


}
