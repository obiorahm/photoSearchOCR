package com.google.android.gms.samples.vision.ocrreader.ui.camera;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.samples.vision.ocrreader.Adapter.RestaurantAdapter;
import com.google.android.gms.samples.vision.ocrreader.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by mgo983 on 10/16/18.
 */

public class FetchWebPage extends AsyncTask<String, Void, Document> {

    String LOG_TAG = FetchWebPage.class.getSimpleName();

    RecyclerView recyclerView;
    Context context;
    RestaurantAdapter adapter;

    public FetchWebPage(Context context){
        recyclerView = ((Activity) context).findViewById(R.id.detected_location_list_view);
        adapter = new RestaurantAdapter(context, R.layout.horizontal_text);

        // apparently the recycler view does not work without setting up a layout manager
        LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
    }

    @Override
    protected Document doInBackground(String...params){
        String encodedString = "";
        String UTF_8 = "UTF-8";
        String prefix_url = "https://www.allmenus.com/custom-results/-/";
        Document document = null;
        try{
            encodedString = URLEncoder.encode(params[0], UTF_8);
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

        try {
            String testString = document.body().html();
            Elements testElement = document.select(".restaurant-list .name a" );
            for (Element element : testElement){
                String[] item = {element.attr("href"), element.text()};
                adapter.addItem(item);
                Log.d(LOG_TAG, element.attr("href") +" " + element.text());
            }

        }catch (NullPointerException e){
            Log.e(LOG_TAG, e + " null pointer");

        }

        recyclerView.setAdapter(adapter);

    }
}
