package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.app.Activity;
import android.content.Context;

import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.StreetViewPanoramaView;

import com.google.android.gms.samples.vision.ocrreader.FetchWebPage;
import com.google.android.gms.samples.vision.ocrreader.GeographyActivity;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mgo983 on 10/17/18.
 */

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    LayoutInflater inflater;
    Context context;
    TextToSpeech myTTS;
    HashMap<String, String[]> mDataCheck = new HashMap<>();
    ArrayList<String[]> mData = new ArrayList<>();
    private RecyclerView last_selected = null;
    private RelativeLayout last_selected_rl = null;
    private static String LOG_TAG = RestaurantAdapter.class.getSimpleName();

    private final int URL_POS = 0;
    private final int TITLE_POS = 1;
    private final int ADDRESS_POS = 2;
    private final int PLACE_ID_POS = 3;
    private final int LONGITUDE = 4;
    private final int LATITUDE = 5;
    private final int IMAGE_URL = 6;

    private double DEFAULT_LONG = 151.20689;

    private double DEFAULT_LAT = -33.87365;



    public static class ViewHolder extends  RecyclerView.ViewHolder {

        private RecyclerView mRecyclerView;
        private View mBackground;
        private RelativeLayout mRelativeLayout;
        private ImageButton mImageButton;
        private ImageView mImageView;
        private StreetViewPanoramaView mStreetViewPanoramaView;
        //private Fragment mFragemnt;


        public ViewHolder(View convertView) {
            super(convertView);
            mBackground = convertView;
            mRecyclerView = convertView.findViewById(R.id.text_by_text);
            mRelativeLayout = convertView.findViewById(R.id.internal_relative_layout);
            mImageButton = convertView.findViewById(R.id.speak_whole_text);
            mImageView = convertView.findViewById(R.id.descriptive_image);
            mStreetViewPanoramaView = convertView.findViewById(R.id.streetviewpanorama);
            //RecyclerView parentRecyclerView = (RecyclerView) convertView.getParent();


        }
    }

        public RestaurantAdapter(){

        }

        public void addItem(String[] item){
            //final int POS_RESTAURANT_NAME = 1;
            //mData.put(item[POS_RESTAURANT_NAME], item);
            //check for duplicate restaurant name before adding items
            String restaurantName = item[TITLE_POS];
            String restaurantUrl = item[ADDRESS_POS];

            if (!mDataCheck.containsKey(restaurantName)){
                mDataCheck.put(restaurantName, item);
                mData.add(item);

                notifyDataSetChanged();
            }

        }

        public RestaurantAdapter(Context context, int resource){
            super();
            inflater = LayoutInflater.from(context);
            this.context = context;
            myTTS = ((GeographyActivity) context).myTTS;
        }

        @Override
        public RestaurantAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_text, null);
            RestaurantAdapter.ViewHolder viewHolder = new RestaurantAdapter.ViewHolder(convertView);


            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RestaurantAdapter.ViewHolder holder, final int position){
            final String[] restaurantData =  mData.get(position);
            final String word = restaurantData[TITLE_POS];
            final  String url = restaurantData[IMAGE_URL];

            holder.mImageView.setVisibility(View.VISIBLE);
            /*try{
                final String allAssets[] = ((Activity) context).getAssets().list("general");

                String restaurantIcon = allAssets[1];
                //InputStream ims = getAssets().open("location.png");

                Glide.with(context).load(Uri.parse("file:///android_asset/general/" + restaurantIcon)).into(holder.mImageView);

            }catch (IOException e){
                Log.e(LOG_TAG, e +" ");
            }*/

            // setup horizontal text by text adapter
            TextByTextAdapter adapter = new TextByTextAdapter(context, R.layout.recognized_text_item);

            String tokenizedString [] = word.split(" ");

            for (String child : tokenizedString){
                adapter.addItem(child);
            }

            LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
            holder.mRecyclerView.setLayoutManager(layoutManager);

            holder.mRecyclerView.setAdapter(adapter);


            //holder.mRecyclerView.setSelected(false);
            //((DetectImageActivity) context).fetchSuggestionsFor("theer aer a coupel of mistaeks in this senence");


            holder.mBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    control_select(holder, restaurantData);

                }
            });

            holder.mRecyclerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //control_select(holder, restaurantData);

                }
            });

            holder.mImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
                    //control_select(holder, restaurantData);
                }
            });


            Glide.with(context).load(buildImageUrl(url))
                    .into(holder.mImageView);
            Log.d(LOG_TAG, "internet " + url);



            //Log.d(LOG_TAG, "place id " +restaurantData[ADDRESS_POS]+  " Longitude " + restaurantData[LONGITUDE] + " Latitude" + restaurantData[LATITUDE]);
            if (!restaurantData[PLACE_ID_POS].equals("")){
                ((UseRecyclerActivity) context).getRestaurantPhoto(restaurantData[PLACE_ID_POS], holder.mImageView);
            }


            //hide progressBar
            ProgressBar progressBar = ((GeographyActivity) context).findViewById(R.id.menu_progress);
            progressBar.setVisibility(View.GONE);


        }


    private String buildImageUrl(String internetAddress){

            String logo = "";

        if (internetAddress != null || ! (internetAddress.equals(""))){
            int start = internetAddress.indexOf("//");
            int start1 = internetAddress.indexOf(".");
            String newAddress = (internetAddress.length() > start + 2) ? internetAddress.substring(start + 2): internetAddress;
            int end = newAddress.indexOf("/");
            Log.d(LOG_TAG, "internet address " + start + " " + end);

            String extractAddress = (end > start) ? newAddress.substring(0, end) : internetAddress.substring(start1 + 1);

            Log.d(LOG_TAG, "internet address " + extractAddress);

            logo = "https://logo.clearbit.com/" + extractAddress;
        }

        return logo;

    }

    private void control_select(RestaurantAdapter.ViewHolder holder, String[] restaurantData){
            String restaurantUrl = restaurantData[0];
            String restaurantName = restaurantData[1];
        last_selected_rl = GeographyActivity.last_rl_parent;
        Log.d(LOG_TAG, "last_selected_rl " + last_selected_rl );
        if (last_selected_rl != null && last_selected_rl != holder.mRelativeLayout){
            RestaurantAdapter.ViewHolder lastViewHolder = new RestaurantAdapter.ViewHolder(last_selected_rl);
            if (lastViewHolder.mRelativeLayout != null){
                lastViewHolder.mRelativeLayout.setSelected(false);
                lastViewHolder.mStreetViewPanoramaView.setVisibility(View.GONE);

            }

        }
        if(holder.mRelativeLayout.isSelected()){
            holder.mRelativeLayout.setSelected(false);
            holder.mStreetViewPanoramaView.setVisibility(View.GONE);

        }else{
            holder.mRelativeLayout.setSelected(true);
            GeographyActivity.selected_item = restaurantName;
            GeographyActivity.selected_url = restaurantUrl;
            holder.mStreetViewPanoramaView.setVisibility(View.VISIBLE);

            String longitude = restaurantData[LONGITUDE];
            String latitude = restaurantData[LATITUDE];


            Double currLongitude = longitude.equals("")? DEFAULT_LONG : Double.valueOf(longitude);
            Double currLatitude = latitude.equals("") ? DEFAULT_LAT: Double.valueOf(latitude);
            //holder.mStreetViewPanoramaView = new StreetViewPanoramaView(context, new StreetViewPanoramaOptions().position(new LatLng(currLatitude, currLongitude)));

            ((UseRecyclerActivity) context).setUpPanorama(holder.mStreetViewPanoramaView, restaurantData[LONGITUDE], restaurantData[LATITUDE]);

        }
        GeographyActivity.last_rl_parent = holder.mRelativeLayout;


        //set up panorama


    }

    @Override
    public int getItemCount(){
        return mData.size();
    }

    public void addPlaceIds(HashMap<String, String[]> placeId){
        final int PLACE_ID_PACK_POS = 1;
        for (String[] item : mData){

            //Log.d(LOG_TAG, "length" + item.length + " ");

            String address = item[ADDRESS_POS];
            String[] placeIdPack = placeId.get(address);
            if (placeId != null){
                item[PLACE_ID_POS] = placeIdPack[PLACE_ID_PACK_POS];
                notifyDataSetChanged();
            }
        }

    }


    public void addLngLat(HashMap<String, String[]> lngLat){
        final int LNG = 0;
        final int LAT = 1;
        for (String[] item: mData){
            String address = item[ADDRESS_POS];
            String[] lngLatPack = lngLat.get(address);
            if (lngLat != null && item != null && lngLatPack != null){
                item[LONGITUDE] = lngLatPack[LNG] ;
                item[LATITUDE] = lngLatPack[LAT];
                //notifyDataSetChanged();
            }else{

            }
        }
    }


    public void addImageUrl(HashMap<String, String[]> imageUrl){

        int LOGO_POS = 0;
        int NAME_POS = 1;

        for (String[] item: mData){
            String address = item[ADDRESS_POS];
            String[] imageUrlPack = imageUrl.get(address);
            String logo = imageUrlPack[LOGO_POS];
            String name = imageUrlPack[NAME_POS];

            // name equals test for restaurants that have the same address.
            if(imageUrl == null || imageUrlPack == null || !(name.equals(item[TITLE_POS]))){
                item[IMAGE_URL] = "";
            }else if(name.equals("Burger King")){
                item[IMAGE_URL] = "https://www.bk.com/";
            }
            else{
                item[IMAGE_URL] = logo ;

            }
            Log.d(LOG_TAG , " keys " + logo + " " + imageUrl.containsKey(address) +" " + address  + " "+ name);
        }
        notifyDataSetChanged();
    }


}
