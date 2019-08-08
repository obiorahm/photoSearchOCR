package com.google.android.gms.samples.vision.ocrreader;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FetchOpenClipArtImage implements ImageEngine {

    String BASE_URL = "https://openclipart.org/search/json/";


    public Uri buildUrl (String queryParameter){
        final String QUERY = "query";
        final String SORT = "sort";

        Uri buildUri;

        buildUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(QUERY, queryParameter)
                .build();
        return buildUri;}
    public Uri handleJSON(String JSONData){
        try{
            String ImageUrl = null;
            if (JSONData != null){

                final JSONObject obj = new JSONObject(JSONData);
                final JSONArray payLoad = obj.getJSONArray("payload");

                int lengthOfJSONEntries = payLoad.length();
                if (lengthOfJSONEntries == 0) return null;


                JSONObject ithElement = payLoad.getJSONObject(0);
                ImageUrl = ithElement.getJSONObject("svg").getString("png_thumb");

                return Uri.parse(ImageUrl);

            }




        return null;
    }catch (JSONException e){
        Log.e("A JSON Exception: ", "the error: " + e);
    }

        return null;

    }
}
