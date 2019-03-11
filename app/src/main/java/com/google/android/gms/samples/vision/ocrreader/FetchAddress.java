package com.google.android.gms.samples.vision.ocrreader;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mgo983 on 2/26/19.
 */

public class FetchAddress extends AsyncTask<String[], Void, String> {

    String LOG_TAG = FetchAddress.class.getSimpleName();


    public FetchAddress(){

    };

    @Override
    protected String doInBackground(String[] ... params){

        String x = "";

        x = getJSON(Uri.parse("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + params[0][0] +"," +params[0][1]+"&key=" + "AIzaSyC-W1DgpWK4sfOPngXLGDA6j62aGxWMMaU"));

        return x;


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
    protected void onPostExecute(String result){

        Log.d(LOG_TAG, "address " + result);

    }
}
