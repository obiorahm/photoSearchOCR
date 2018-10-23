package com.google.android.gms.samples.vision.ocrreader;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by mgo983 on 10/16/18.
 */

public class FetchWebPage extends AsyncTask<String, Void, Document> {

    String LOG_TAG = FetchWebPage.class.getSimpleName();

    Context context;

    String prefix_url;

    public FetchWebPage(Activity activity){
        context = (Context) activity;
        prefix_url = "https://www.allmenus.com/custom-results/-/";
    }


    public FetchWebPage(OpenRestaurantMenuActivity activity){
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
        Document document = null;
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
