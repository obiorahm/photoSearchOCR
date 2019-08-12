package com.google.android.gms.samples.vision.ocrreader;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FetchEdamameImage extends  FetchImageEngine  {

    private final static String BASEURL = "https://api.edamam.com/search";
    private final static String API_KEY = "37345295e38efe1ea020cbc391ee11a8";
    private final static String API_ID = "0940281c";
    private String NUM_TO = "1";
    private final static String NUM_FROM = "0";

    String LOG_TAG = FetchEdamameImage.class.getSimpleName();


    FetchEdamameImage(SetAdapter adapter){
        this.adapter = adapter;
    }

@Override
    public Uri buildUrl (String queryParameter){
        final String API_KEY_NAME = "app_key";
        final String API_ID_NAME = "app_id";
        final String NUM_TO_NAME = "to";
        final String NUM_FROM_NAME = "from";
        final String QUERY = "q";

        Uri buildUri;

        buildUri = Uri.parse(BASEURL).buildUpon()
                .appendQueryParameter(API_ID_NAME, API_ID)
                .appendQueryParameter(API_KEY_NAME,API_KEY)
                .appendQueryParameter(NUM_FROM_NAME, NUM_FROM)
                .appendQueryParameter(NUM_TO_NAME,NUM_TO)
                .appendQueryParameter(QUERY, queryParameter)

                .build();
        return buildUri;
    }

    @Override
    public Uri handleJSON(String JSONData){

        try{


                JSONObject JSONEntry = new JSONObject(JSONData);

                JSONArray hits = JSONEntry.getJSONArray("hits");

                if (hits.length() != 0)
                {

                        JSONObject recipe = hits.getJSONObject(0);
                        // get url
                        String recipeInfoUrl = recipe.getJSONObject("recipe").getString("image");

                        return Uri.parse(recipeInfoUrl);


                }


        }catch (JSONException e ){
            Log.e(LOG_TAG, e + "");

        }catch (NullPointerException e){
            Log.e(LOG_TAG, e + "");
        }

        return null;
    }

    @Override
    public void nextEngine(String searchString) {
        FetchPixabayImage fetchPixabayImage = new FetchPixabayImage(adapter);
        fetchPixabayImage.execute(searchString);
    }
}
