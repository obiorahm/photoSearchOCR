package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.samples.vision.ocrreader.CurrentPlaceMap;
import com.google.android.gms.samples.vision.ocrreader.GeographyActivity;
import com.google.android.gms.samples.vision.ocrreader.OpenRestaurantMenuActivity;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;
import java.util.ArrayList;


/**
 * Created by mgo983 on 10/17/18.
 */

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    LayoutInflater inflater;
    Context context;
    int resource;
    TextToSpeech myTTS;
    ArrayList<String[]> mData = new ArrayList<>();
    //private RecyclerView last_selected = null;
    private RelativeLayout last_selected_rl = null;

    private Object[] last_selected_property = null;
    private static String LOG_TAG = RestaurantAdapter.class.getSimpleName();

    private final int URL_POS = 0;
    private final int TITLE_POS = 1;
    private final int ADDRESS_POS = 2;
    private final int PLACE_ID_POS = 3;
    private final int LONGITUDE = 4;
    private final int LATITUDE = 5;
    private final int IMAGE_URL = 6;

    private final int IS_PANORAMA_VISIBLE = 0;
    private final int IS_LOGO_ENLARGED = 1;
    private final int IS_RELATIVE_LAYOUT_SELECTED = 2;

    private ArrayList<Object[]> mDataProperties = new ArrayList<>();

    public static String RESTAURANT_NAME = "com.google.android.gms.samples.vision.ocrreader.RESTAURANT_NAME";

    public static String RESTAURANT_URL = "com.google.android.gms.samples.vision.ocrreader.RESTAURANT_URL";




    public static class ViewHolder extends  RecyclerView.ViewHolder {

        private RecyclerView mRecyclerView;
        private View mBackground;
        private RelativeLayout mRelativeLayout;
        private ImageButton mImageButton;
        private ImageView mImageView;
        private ImageView mEnlargedImageView;
        private TextView mTextView;
        //private RadioButton mRadioButton;
        //private StreetViewPanoramaView mStreetViewPanoramaView;
        private ImageButton mImageButtonExpandMore;
        private ImageButton mImageButtonExpandLess;
        //private Fragment mFragemnt;


        public ViewHolder(View convertView) {
            super(convertView);
            mBackground = convertView;
            mRecyclerView = convertView.findViewById(R.id.text_by_text);
            mRelativeLayout = convertView.findViewById(R.id.internal_relative_layout);
            mImageButton = convertView.findViewById(R.id.speak_whole_text);
            mImageView = convertView.findViewById(R.id.descriptive_image);
            mEnlargedImageView = convertView.findViewById(R.id.enlarged_image);
            mTextView = convertView.findViewById(R.id.restaurant_name);
            //mStreetViewPanoramaView = convertView.findViewById(R.id.streetviewpanorama);

            //mRadioButton = convertView.findViewById(R.id.select_option);

            mImageButtonExpandMore = convertView.findViewById(R.id.expand_more);
            mImageButtonExpandLess = convertView.findViewById(R.id.expand_less);
            //RecyclerView parentRecyclerView = (RecyclerView) convertView.getParent();


        }
    }


        public void addItem(String[] item){


            mData.add(item);

            //remember properties of adapter
            Object[] properties = new Object[3];
            properties[IS_PANORAMA_VISIBLE] = View.GONE;
            properties[IS_LOGO_ENLARGED] = View.GONE;
            properties[IS_RELATIVE_LAYOUT_SELECTED] = false;

            mDataProperties.add(properties);


            //I'm using notifyItemChanged because notifyDataSetChanged redraws each all views and causes panorama flicker
            notifyItemChanged(mData.size() - 1);

            //notifyDataSetChanged();
        }


        public RestaurantAdapter(Context context, int resource, TextToSpeech myTTS){
            super();
            inflater = LayoutInflater.from(context);
            this.context = context;
            this.resource = resource;
            this.myTTS = myTTS;
        }

        @Override
        public RestaurantAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View convertView = LayoutInflater.from(parent.getContext()).inflate(resource, null);
            return new RestaurantAdapter.ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(final RestaurantAdapter.ViewHolder holder, final int position){
            final String[] restaurantData =  mData.get(position);
            final String word = restaurantData[TITLE_POS];
            final  String url = restaurantData[IMAGE_URL];
            final String placeId = restaurantData[PLACE_ID_POS];

            final Object[] properties = mDataProperties.get(position);

            holder.mImageView.setVisibility(View.VISIBLE);

            //set properties of each holder


            holder.mEnlargedImageView.setVisibility((int) properties[IS_LOGO_ENLARGED]);

            holder.mRelativeLayout.setSelected((boolean) properties[IS_RELATIVE_LAYOUT_SELECTED]);
            //holder.mRadioButton.setChecked((boolean) properties[IS_RELATIVE_LAYOUT_SELECTED]);


            // setup horizontal text by text adapter
            TextByTextAdapter adapter = new TextByTextAdapter(context, false, myTTS);

            String tokenizedString [] = word.split(" ");

            for (String child : tokenizedString){
                adapter.addItem(child);
            }

            LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
            holder.mRecyclerView.setLayoutManager(layoutManager);

            holder.mRecyclerView.setAdapter(adapter);

            holder.mRecyclerView.setOnClickListener(view -> myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null, null));


            // Use TextView instead of adapter
            holder.mTextView.setText(word);

            holder.mTextView.setOnClickListener(view -> myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null, null));


            holder.mBackground.setOnClickListener(view ->
                control_select(holder, position));

                String imageUrl = buildImageUrl(url);

                Glide.with(context).load(imageUrl).into(holder.mImageView);
                Glide.with(context).load(imageUrl).into(holder.mEnlargedImageView);


            holder.mImageView.setOnClickListener(view ->goToMenu(holder,position));

            Log.d(LOG_TAG, "internet " + url);


            //holder.mRadioButton.setOnClickListener((view)-> control_select(holder, position));


            StreetViewPanoramaView streetViewPanoramaView = ((Activity) context).findViewById(R.id.streetviewpanorama);

            holder.mImageButtonExpandMore.setOnClickListener((View view)->{expandPanorama(holder, streetViewPanoramaView, position);});

            holder.mImageButtonExpandLess.setOnClickListener((View view)->{hidePanorama(holder, streetViewPanoramaView, position);});


            //hide progressBar
            ProgressBar progressBar = ((Activity) context).findViewById(R.id.menu_progress);
            progressBar.setVisibility(View.GONE);


        }



        private void goToMenu(ViewHolder holder,int position){
            //if (holder.mRadioButton.isChecked()){
            control_select(holder,position);
                Intent openRestaurantIntent = new Intent(context, OpenRestaurantMenuActivity.class);
                openRestaurantIntent.putExtra(RESTAURANT_NAME, CurrentPlaceMap.selected_item);
                openRestaurantIntent.putExtra(RESTAURANT_URL, CurrentPlaceMap.selected_url);
                (context).startActivity(openRestaurantIntent);
            //}
        }


        private void expandPanorama(ViewHolder holder, StreetViewPanoramaView streetViewPanoramaView, int position){

            control_select(holder, position);

            holder.mImageButtonExpandMore.setVisibility(View.GONE);
            holder.mImageButtonExpandLess.setVisibility(View.VISIBLE);
            streetViewPanoramaView.setVisibility(View.VISIBLE);
        }


    private void hidePanorama(ViewHolder holder, StreetViewPanoramaView streetViewPanoramaView, int position){
        holder.mImageButtonExpandLess.setVisibility(View.GONE);
        holder.mImageButtonExpandMore.setVisibility(View.VISIBLE);
        streetViewPanoramaView.setVisibility(View.GONE);
    }



    private String buildImageUrl(String internetAddress){

            String logo = "";

        if (internetAddress != null && ! (internetAddress.equals(""))){
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



    private void control_select(RestaurantAdapter.ViewHolder holder, int position){
        String [] restaurantData = mData.get(position);
        String restaurantUrl = restaurantData[URL_POS];
        String restaurantName = restaurantData[TITLE_POS];
        Object[] currentProperties =  mDataProperties.get(position);
        String longitude = restaurantData[LONGITUDE];
        String latitude = restaurantData[LATITUDE];

        last_selected_rl = GeographyActivity.last_rl_parent;
        Log.d(LOG_TAG, "last_selected_rl " + last_selected_rl );
        if ( last_selected_property != null && last_selected_property != currentProperties){

            //update properties
            last_selected_property[IS_PANORAMA_VISIBLE] = View.GONE;
            last_selected_property[IS_LOGO_ENLARGED] = View.GONE;
            last_selected_property[IS_RELATIVE_LAYOUT_SELECTED] = false;
            //to enable redraw of view with new properties
        }


            holder.mRelativeLayout.setSelected(true);
            Log.d(LOG_TAG, "restaurantUrl " + restaurantUrl);
            CurrentPlaceMap.selected_item = restaurantName;
            CurrentPlaceMap.selected_url = restaurantUrl;

            //update properties
            currentProperties[IS_RELATIVE_LAYOUT_SELECTED] = true;

            //updated selected longitude and latitude
            CurrentPlaceMap.LONGITUDE = longitude.equals("")? LONGITUDE : Double.valueOf(longitude);
            CurrentPlaceMap.LATITUDE = latitude.equals("") ? LATITUDE : Double.valueOf(latitude);
            ((UseRecyclerActivity) context).setUpPanorama();

        last_selected_property = currentProperties;

        notifyDataSetChanged();


    }


    @Override
    public int getItemCount(){
        return mData.size();
    }


    private Animator currentAnimator;
    private int shortAnimationDuration = 2000;



    private void zoomImageFromTHumb(ImageView normalView, View expandedImageView, RestaurantAdapter.ViewHolder holder, int position){

        // if there's an animation in progress, cancel it
        // immediately and priceed with this one.


        if (currentAnimator != null){
            currentAnimator.cancel();
        }

        //notifyDataSetChanged();
        last_selected_property = mDataProperties.get(position);

        //Load the high-resolution "zoomed-in" image.

        // calculate the starting and ending bounds for the zoomed-in image.
        // this involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        //expandedImageView.setVisibility(View.VISIBLE);
        //mDataProperties.get(position)[IS_LOGO_ENLARGED] = View.VISIBLE;

        normalView.getGlobalVisibleRect(startBounds);
                holder.mRelativeLayout.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);


        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).

        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.

        normalView.setAlpha(0f);
        mDataProperties.get(position)[IS_LOGO_ENLARGED] = View.VISIBLE;
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;

// Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener((View view)-> {
            if (currentAnimator != null) {
                currentAnimator.cancel();
            }

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            AnimatorSet set1 = new AnimatorSet();
            set1.play(ObjectAnimator
                    .ofFloat(expandedImageView, View.X, startBounds.left))
                    .with(ObjectAnimator
                            .ofFloat(expandedImageView,
                                    View.Y,startBounds.top))
                    .with(ObjectAnimator
                            .ofFloat(expandedImageView,
                                    View.SCALE_X, startScaleFinal))
                    .with(ObjectAnimator
                            .ofFloat(expandedImageView,
                                    View.SCALE_Y, startScaleFinal));
            set1.setDuration(shortAnimationDuration);
            set1.setInterpolator(new DecelerateInterpolator());
            set1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    normalView.setAlpha(1f);
                    expandedImageView.setVisibility(View.GONE);
                    mDataProperties.get(position)[IS_LOGO_ENLARGED] = View.GONE;
                    currentAnimator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    normalView.setAlpha(1f);
                    mDataProperties.get(position)[IS_LOGO_ENLARGED] = View.GONE;
                    expandedImageView.setVisibility(View.GONE);
                    currentAnimator = null;
                }
            });
            set1.start();
            currentAnimator = set1;
        });


    }


}
