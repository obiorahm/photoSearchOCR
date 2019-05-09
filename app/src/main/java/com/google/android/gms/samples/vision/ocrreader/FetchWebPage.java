package com.google.android.gms.samples.vision.ocrreader;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.samples.vision.ocrreader.Adapter.RestaurantAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by mgo983 on 10/16/18.
 */

public class FetchWebPage extends AsyncTask<String, Void, Document[]> {

    String LOG_TAG = FetchWebPage.class.getSimpleName();

    Context context;

    String prefix_url;

    RestaurantAdapter adapter;

    public FetchWebPage(Activity activity, RestaurantAdapter adapter){
        this.adapter = adapter;
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
    protected Document[] doInBackground(String...params){
        String encodedString = "";
        String UTF_8 = "UTF-8";

        String restaurant_url = "";


        Document [] document = new Document[2];
         document[0] = null;
        try{
            encodedString = (params[1].equals("encode") )? URLEncoder.encode(params[0], UTF_8): params[0];
            encodedString = prefix_url + encodedString;
            Log.d(LOG_TAG, encodedString);

            document[0] = Jsoup.connect(encodedString).get();

            processWebResults(document[0]);



        }catch (UnsupportedEncodingException e){
            Log.e(LOG_TAG, e + " ");
        }catch (IOException e){
            Log.e(LOG_TAG, e + " ");
        }
        return document;

    }

    @Override
    protected void onPostExecute(Document[] document){

        ((UseRecyclerActivity) context).processWebResults(document[0]);

    }

    public void processWebResults(Document document){

        ArrayList<String> addresses = new ArrayList<>();
        ArrayList<String[]> urls = new ArrayList<>();

        try {
            Elements testElement = document.select(".restaurant-list-item" );
            for (Element element : testElement){
                //get name
                Elements name_element = element.select(".name a");
                String restaurant_name = name_element.text();
                String restaurant_url = name_element.attr("href");
                String restaurant_address = "";
                Elements addressElements = element.select(".address");
                for (Element address : addressElements){
                    restaurant_address += address.text() + " ";
                }

                String placeId = "";
                String longitude = "";
                String latitude = "";
                String logoID = "";

                //set up logo
                Document logoDocument = getLogoDocument(restaurant_url);
                logoID = getLogoAddress(logoDocument);

                String item[] = {restaurant_url, restaurant_name, restaurant_address, placeId, longitude, latitude, logoID};
                addresses.add(restaurant_address);
                urls.add(item);

                adapter.addItem(item);
                Log.d(LOG_TAG, "restaurant name " +restaurant_name + " restaurant_url " + restaurant_url + " restaurant_address " + restaurant_address);
            }





        }catch (NullPointerException e){
            Log.e(LOG_TAG, e + " null pointer");

        }
        /*getLocationFromAddress(urls);
        recyclerView.setAdapter(adapter);*/


    }


    private Document getLogoDocument(String item){
        String prefix_url = "https://www.allmenus.com";
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

    private String getLogoAddress(Document document){
        String logo = "";

        if (document != null){
            logo = document.select(".menu-link").attr("href");

            Log.d(LOG_TAG, "internet address " + logo);

        }
        return logo;
    }


}
