package com.google.android.gms.samples.vision.ocrreader;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by mgo983 on 10/24/18.
 */

public class FetchRestaurantPlaceID extends AsyncTask<ArrayList<String>, Void, HashMap<String, String[]>> {

    String LOG_TAG = FetchRestaurantPlaceID.class.getSimpleName();

    HashMap<String, String[]> placeData = new HashMap<>();
    Context context;

    public FetchRestaurantPlaceID(Context context){
        this.context = context;
    }



    @Override
    protected HashMap<String, String[]> doInBackground(ArrayList<String> ... params){

        for (int i = 0; i < params[0].size(); i++){
            String searchString = params[0].get(i);
            Uri uri = buildPlaceUri(searchString.trim());
            String json = getJSON(uri);
            String jsonAndPlaceIdHolder[] = {json, ""};
            placeData.put(searchString, jsonAndPlaceIdHolder);
        }

        return placeData;

    }


    public String getJSON(Uri uri){

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try{

            URL url = new URL(uri.toString());
            Log.v(LOG_TAG,"The built Uri " + url);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null){
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null){
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0){
                return null;
            }

            //Log.d(LOG_TAG, buffer.toString());

            String result = buffer.toString();
            return result;

        }catch (IOException e){
            Log.e(LOG_TAG, "Error", e);
            return null;
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(reader != null){
                try{
                    reader.close();
                }catch (final IOException e){
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    @Override
    protected void onPostExecute(HashMap<String, String[]> result){

        String[] placesIds = getPlacesIds(result);
        ((UseRecyclerActivity) context).addPlaceIdToAdapter(result);


    }


    private Uri buildPlaceUri(String address){
        String BASEURL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json";
        String ADDRESS = "input";
        String KEY = "key";
        String INPUTTYPE = "inputtype";

        Uri buildUri;

        buildUri = Uri.parse(BASEURL).buildUpon()
                .appendQueryParameter(ADDRESS, address)
                .appendQueryParameter(INPUTTYPE,"textquery")
                .appendQueryParameter(KEY, "AIzaSyC-W1DgpWK4sfOPngXLGDA6j62aGxWMMaU")
                .build();
        Log.d(LOG_TAG, "build uri " + buildUri.toString());
        return buildUri;
    }

    private String[] getPlacesIds(HashMap<String, String[]> result){
        String stringResult [] = new String[result.size()];

        Iterator resultIterator = result.entrySet().iterator();
        while (resultIterator.hasNext()){
            HashMap.Entry item = (HashMap.Entry) resultIterator.next();
            try{
                String[] value = (String[]) item.getValue();
                JSONObject placeData = new JSONObject(value[0]);
                JSONArray candidate = placeData.getJSONArray("candidates");
                JSONObject placeIdObject = candidate.getJSONObject(0);
                String placeId = placeIdObject.getString("place_id");

                value[1] = placeId;
                Log.d(LOG_TAG, " placeId " + placeId);
                Log.d(LOG_TAG, placeData.toString());

            }catch (JSONException e){
                Log.e(LOG_TAG, e + "");
            }
        }

        /*for (int i = 0; i < result.size(); i++){
            try{
                JSONObject placeData = new JSONObject(result.get(i));
                JSONArray candidate = placeData.getJSONArray("candidates");
                JSONObject placeIdObject = candidate.getJSONObject(0);
                String placeId = placeIdObject.getString("place_id");
                stringResult[i] = placeId;
                Log.d(LOG_TAG, " placeId" + placeId);


            }catch (JSONException e){
                Log.e(LOG_TAG, e + "");
            }

        }*/

        return stringResult;

    }
}
