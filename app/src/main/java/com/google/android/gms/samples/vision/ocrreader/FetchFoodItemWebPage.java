package com.google.android.gms.samples.vision.ocrreader;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.samples.vision.ocrreader.Adapter.RestaurantAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by mgo983 on 10/16/18.
 */

public class FetchFoodItemWebPage extends AsyncTask<String, Void, Document> {

    String LOG_TAG = FetchFoodItemWebPage.class.getSimpleName();

    Context context;

    String prefix_url;

    RestaurantAdapter adapter;


    public FetchFoodItemWebPage(OpenRestaurantMenuActivity activity){
        context = (Context) activity;
        prefix_url = "https://www.allmenus.com";

    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
    }

    @Override
    protected Document doInBackground(String...params){
        String encodedString = "";
        String UTF_8 = "UTF-8";

        String restaurant_url = "";


        Document  document = null;

        try{
            encodedString = (params[1].equals("encode") )? URLEncoder.encode(params[0], UTF_8): params[0];
            encodedString = prefix_url + encodedString;
            Log.d(LOG_TAG, encodedString);

            document = Jsoup.connect(encodedString).get();

        }catch (UnsupportedEncodingException e){
            Log.e(LOG_TAG, e + " ");
        }catch (IOException e){
            Log.e(LOG_TAG, e + " ");
        }
        return document;

    }

    @Override
    protected void onPostExecute(Document document){

        ((UseRecyclerActivity) context).processWebResults(document);

    }




}
