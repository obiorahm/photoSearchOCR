package com.google.android.gms.samples.vision.ocrreader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by mgo983 on 2/25/19.
 */

public class FetchRestaurantLogo extends AsyncTask<HashMap<String, String[]> , Void, HashMap<String, String[]>> {

    Context context;

    String LOG_TAG = FetchRestaurantLogo.class.getSimpleName();

    String prefix_url = "https://www.allmenus.com";

    int RESTAURANT_URL = 2;
    int RESTAURANT_NAME = 1;

    public FetchRestaurantLogo(Context context){
        this.context = context;
    }

    @Override
    protected  HashMap<String, String[]> doInBackground(HashMap<String, String[]> ... params){
        HashMap<String, String[]> restaurantLogoUrls = new HashMap<>();

        Iterator resultIterator = params[0].entrySet().iterator();
        while (resultIterator.hasNext()) {

            HashMap.Entry entry = (HashMap.Entry) resultIterator.next();
            String[] entryInfo = ((String []) entry.getValue());
            String item = entryInfo[RESTAURANT_URL];
            String newItem = item.substring(1); // right after the first slash
            Document document = getDocument(item);
            String logo = getLogoAddress(document);
            String [] allInfo = {logo, entryInfo[RESTAURANT_NAME]};

            restaurantLogoUrls.put((String) entry.getKey(), allInfo);

        }

        return restaurantLogoUrls;
    }


    private String getLogoAddress(Document document){
        String logo = "";

        if (document != null){
            logo = document.select(".menu-link").attr("href");

            Log.d(LOG_TAG, "internet address " + logo);

        }
        return logo;
    }


    private Document getDocument(String item){
        String encodedString = "";
        String UTF_8 = "UTF-8";
        Document document = null;
        try{
            encodedString = (item.equals("encode") )? URLEncoder.encode(item, UTF_8): item;
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
    protected void onPostExecute(HashMap<String, String[]> result){

        ((UseRecyclerActivity) context).addImageUrlToAdapter(result);
    }

}
