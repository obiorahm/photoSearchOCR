package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by mgo983 on 12/13/18.
 */

public class FetchRestaurantLongLat extends AsyncTask<ArrayList<String[]>, Void, HashMap<String, String[]>> {

    String LOG_TAG = FetchRestaurantLongLat.class.getSimpleName();

    HashMap<String, String[]> LongLatData = new HashMap<>();
    Context context;

    int RESTAURANT_ADDRESS = 2;
    int RESTAURANT_URL = 0;

    public FetchRestaurantLongLat(Context context){
        this.context = context;
    }



    @Override
    protected HashMap<String, String[]> doInBackground(ArrayList<String[]> ... params){

        for (int i = 0; i < params[0].size(); i++){
            String[] info = params[0].get(i);
            String searchString = info[RESTAURANT_ADDRESS];
            Uri uri = buildLongLatUri(searchString.trim());
            String json = getJSON(uri);
            String jsonAndLongLatHolder[] = {json, "", info[RESTAURANT_URL]};
            LongLatData.put(searchString, jsonAndLongLatHolder);
        }

        return LongLatData;

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

        } catch (FileNotFoundException e ){
            Log.e(LOG_TAG, " " + e );
            return null;
        }
        catch (IOException e){
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

        HashMap<String, String []> longLat = getLongLat(result);



        ((UseRecyclerActivity) context).beginFetchRestaurantLogos(result);

        ((UseRecyclerActivity) context).addLongLatToAdapter(longLat);

    }


    private Uri buildLongLatUri(String address){

        String BASEURL = "https://maps.googleapis.com/maps/api/geocode/json?";
        String ADDRESS = "address";
        String KEY = "key";

        Uri buildUri;

        buildUri = Uri.parse(BASEURL).buildUpon()
                .appendQueryParameter(ADDRESS, address)
                .appendQueryParameter(KEY, "AIzaSyC-W1DgpWK4sfOPngXLGDA6j62aGxWMMaU")
                .build();
        Log.d(LOG_TAG, "build uri " + buildUri.toString());
        return buildUri;
    }

    private HashMap<String, String[]> getLongLat(HashMap<String, String[]> result){
        //String stringResult [] = new String[result.size()];

        /**
         * key is address
         * value is {lng, lat}
         */
        HashMap<String, String[]> stringResult = new HashMap<>();

        Iterator resultIterator = result.entrySet().iterator();
        while (resultIterator.hasNext()){
            HashMap.Entry item = (HashMap.Entry) resultIterator.next();
            try{
                String[] value = (String[]) item.getValue();
                if (value != null || value.length != 0){

                    JSONObject placeData = new JSONObject(value[0]);
                    JSONArray candidate = placeData.getJSONArray("results");

                    if (candidate != null || candidate.length() > 0){
                        JSONObject firstObject = candidate.getJSONObject(0);
                        JSONObject geometry = firstObject.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        String lng = location.getString("lng");
                        String lat = location.getString("lat");

                        String [] lnglat = {lng, lat};
                        stringResult.put( (String) item.getKey(), lnglat);


                        Log.d(LOG_TAG, "location " + location.toString());
                    }


                }


            }catch (JSONException e){
                Log.e(LOG_TAG, e + "");
            }catch (NullPointerException e){
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


