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

        ((UseRecyclerActivity) context).setAdapter();

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


                //get placeId, longitude and latitude
                String[] lngLat = getPanoramaData(restaurant_address);
                longitude = lngLat[0];
                latitude = lngLat[1];

                String item[] = {restaurant_url, restaurant_name, restaurant_address, placeId, longitude, latitude, logoID};
                addresses.add(restaurant_address);
                urls.add(item);

                // return to the UI thread to update the view when the result becomes available

                ((Activity) context).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // Stuff that updates the UI
                        adapter.addItem(item);

                    }
                });


                Log.d(LOG_TAG, "restaurant name " +restaurant_name + " restaurant_url " + restaurant_url + " restaurant_address " + restaurant_address);
            }





        }catch (NullPointerException e){
            Log.e(LOG_TAG, e + " null pointer");

        }
        /*getLocationFromAddress(urls);
        recyclerView.setAdapter(adapter);*/


    }




    private String[] getPanoramaData(String restaurantAddress){
        String[] panoramaData = new String[2];
        int LONGITUDE = 0;
        int LATITUDE = 1;

        String searchString = restaurantAddress;
        Uri uri = buildLongLatUri(searchString.trim());
        String json = getJSON(uri);

        panoramaData = getLongLat(json);
        return panoramaData;
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


    //get long lat JSON
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


    private String[] getLongLat(String json){
        //String stringResult [] = new String[result.size()];

        /**
         * key is address
         * value is {lng, lat}
         */


            String[] lngLat = new String[2];

            try{
                if (json != null ){

                    JSONObject placeData = new JSONObject(json);
                    JSONArray candidate = placeData.getJSONArray("results");

                    if (candidate != null || candidate.length() > 0){
                        JSONObject firstObject = candidate.getJSONObject(0);
                        JSONObject geometry = firstObject.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        String lng = location.getString("lng");
                        String lat = location.getString("lat");

                        lngLat[0] = lng;
                        lngLat[1] = lat;

                        Log.d(LOG_TAG, "location " + location.toString());
                    }


                }else{
                    String [] lnglat = {"", ""};
                }


            }catch (JSONException e){
                Log.e(LOG_TAG, e + "");
            }catch (NullPointerException e){
                Log.e(LOG_TAG, e + "");

            }


        return lngLat;

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
