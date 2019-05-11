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


/**
 * Created by mgo983 on 10/16/18.
 */

public class FetchWebPage extends AsyncTask<String, Void, Document[]> {

    String LOG_TAG = FetchWebPage.class.getSimpleName();

    private Context context;

    private String prefix_url;

    private RestaurantAdapter adapter;

    public FetchWebPage(Activity activity, RestaurantAdapter adapter){
        this.adapter = adapter;
        context =  activity;
        prefix_url = "https://www.allmenus.com/custom-results/-/";
    }


    @Override
    protected void onPreExecute(){
        super.onPreExecute();
    }

    @Override
    protected Document[] doInBackground(String...params){
        String encodedString;
        String UTF_8 = "UTF-8";



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

        //((UseRecyclerActivity) context).setAdapter();

    }

    private void processWebResults(Document document){

        try {
            Elements testElement = document.select(".restaurant-list-item" );
            for (Element element : testElement){
                //get name
                Elements name_element = element.select(".name a");
                String restaurant_name = name_element.text();
                String restaurant_url = name_element.attr("href");
                Elements addressElements = element.select(".address");
                StringBuilder restaurant_name_sb = new StringBuilder();
                for (Element address : addressElements){
                    restaurant_name_sb.append(address.text());
                    restaurant_name_sb.append(" ");
                }
                String restaurant_address = restaurant_name_sb.toString();


                String placeId = "";

                //set up logo
                Document logoDocument = getLogoDocument(restaurant_url);
                String logoID = getLogoAddress(logoDocument);


                //get placeId, longitude and latitude
                String[] lngLat = getPanoramaData(restaurant_address);
                String longitude = lngLat[0];
                String latitude = lngLat[1];

                String item[] = {restaurant_url, restaurant_name, restaurant_address, placeId, longitude, latitude, logoID};

                // return to the UI thread to update the view when the result becomes available

                ((Activity) context).runOnUiThread(()-> {
                    // Stuff that updates the UI
                    adapter.addItem(item);

                });


                Log.d(LOG_TAG, "restaurant name " +restaurant_name + " restaurant_url " + restaurant_url + " restaurant_address " + restaurant_address);
            }





        }catch (NullPointerException e){
            Log.e(LOG_TAG, e + " null pointer");

        }


    }


    private String[] getPanoramaData(String restaurantAddress){

        Uri uri = buildLongLatUri(restaurantAddress.trim());
        String json = getJSON(uri);

        return getLongLat(json);
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
    private String getJSON(Uri uri){

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try{

            URL url = new URL(uri.toString());
            Log.v(LOG_TAG,"The built Uri " + url);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if(inputStream == null){
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null){
                buffer.append(line);
                buffer.append("\n");
            }

            if (buffer.length() == 0){
                return null;
            }

            //Log.d(LOG_TAG, buffer.toString());


            return buffer.toString();

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
        /*
         * key is address
         * value is {lng, lat}
         */


            String[] lngLat = new String[2];

            try{
                if (json != null ){

                    JSONObject placeData = new JSONObject(json);
                    JSONArray candidate = placeData.getJSONArray("results");

                    if (candidate != null && candidate.length() > 0){
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
                    lngLat[0] ="";
                    lngLat[1] = "";
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
        String encodedString;
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
