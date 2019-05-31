package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.samples.vision.ocrreader.PlacesActivity;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import java.util.ArrayList;


public class PossiblePlacesAdapter extends RecyclerView.Adapter<PossiblePlacesAdapter.ViewHolder> {

    ArrayList<Object[]> mData = new ArrayList<>();
    Context context;
    LayoutInflater inflater;
    TextToSpeech myTTS;
    public static int RESTAURANT_ADDRESS = 0;

    public static int IS_SELECTED = 1;
    public static int IS_PANORAMA_VISIBLE = 2;
    public static int IS_LOGO_ENLARGED =3;

    public static int LATITUDE = 4;
    public static int LONGITUDE = 5;
    public static int BITMAP = 6;

    public PossiblePlacesAdapter(Context context){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        myTTS = ((PlacesActivity) context).myTTS;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private RecyclerView mRecyclerView;
        private View mBackground;
        private RelativeLayout mRelativeLayout;
        private ImageButton mImageButton;
        private ImageView mImageView;
        private ImageView mEnlargedImageView;
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
            mEnlargedImageView = convertView.findViewById(R.id.enlarged_image);
            //RecyclerView parentRecyclerView = (RecyclerView) convertView.getParent();


        }

    }




    @Override
    public PossiblePlacesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_text, null);


        return new PossiblePlacesAdapter.ViewHolder(convertView);
    }


    @Override
    public void onBindViewHolder(final PossiblePlacesAdapter.ViewHolder holder, final int position){
        Object[] data = mData.get(position);

        final String word = (String) data[RESTAURANT_ADDRESS];
        // setup horizontal text by text adapter
        TextByTextAdapter adapter = new TextByTextAdapter(context, true);

        String tokenizedString [] = word.split(" ");

        for (String child : tokenizedString){
            adapter.addItem(child);
        }


        holder.mRelativeLayout.setSelected((boolean) data[IS_SELECTED]);

        //set up panorama if properties[IS_PANORORAMA_VISIBLE] is visible
        int isPanoramaVisible =  (int) data[IS_PANORAMA_VISIBLE];
        if (isPanoramaVisible == View.VISIBLE){
            ((UseRecyclerActivity) context).setUpPanorama(holder.mStreetViewPanoramaView, Double.toString((Double) data[LONGITUDE]) , Double.toString((Double)data[LATITUDE]));
        }

        holder.mStreetViewPanoramaView.setVisibility(isPanoramaVisible);


        holder.mImageButton.setOnClickListener(view -> myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null));

        holder.mBackground.setOnClickListener(view -> control_select(holder, position));


        LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
        holder.mRecyclerView.setLayoutManager(layoutManager);

        holder.mRecyclerView.setAdapter(adapter);

        Bitmap bitmap = (Bitmap) data[BITMAP];

        Drawable d = new BitmapDrawable(context.getResources(),bitmap);

        if (bitmap != null){

            Glide.with(context).load(d).into(holder.mImageView);
            Glide.with(context).load(d).into(holder.mEnlargedImageView);


        }

        holder.mImageView.setOnClickListener(view ->
                zoomImageFromTHumb((ImageView)  view, holder.mEnlargedImageView, holder, position, bitmap));

        //hide progressBar
        ProgressBar progressBar = ((PlacesActivity) context).findViewById(R.id.menu_progress);
        progressBar.setVisibility(View.GONE);


    }



    @Override
    public int getItemCount(){
        return mData.size();
    }


    public void addItem (Object [] data ){
        //Object[] data = new Object[5];

        //data[RESTAURANT_ADDRESS] = (String) place;
        data[IS_PANORAMA_VISIBLE] = View.GONE;
        data[IS_SELECTED] = false;
        data[IS_LOGO_ENLARGED] = View.GONE;
        //data[LONGITUDE] = Double.toString(longitude);
        //data[LATITUDE] = Double.toString(latitude);

        mData.add(data);
    notifyDataSetChanged();
    }

    Object[] last_selected_property;

    private void control_select(PossiblePlacesAdapter.ViewHolder holder, int position){

        Object[] currentProperties =  mData.get(position);


        if ( last_selected_property != null && last_selected_property != currentProperties){

            //update properties
            last_selected_property[IS_SELECTED] = false;
            last_selected_property[IS_PANORAMA_VISIBLE] = View.GONE;
            last_selected_property[IS_LOGO_ENLARGED] = View.GONE;
            //to enable redraw of view with new properties
        }
        if( (boolean) currentProperties[IS_SELECTED]){
            holder.mRelativeLayout.setSelected(false);

            //update properties
            currentProperties[IS_SELECTED] = false;
            currentProperties[IS_PANORAMA_VISIBLE] = View.GONE;
            currentProperties[IS_LOGO_ENLARGED] = View.GONE;

        }else{

            PlacesActivity.selectedAddress = (String) currentProperties[RESTAURANT_ADDRESS];
            holder.mRelativeLayout.setSelected(true);

            //update properties
            currentProperties[IS_SELECTED] = true;
            currentProperties[IS_PANORAMA_VISIBLE] = View.VISIBLE;


        }
        notifyDataSetChanged();

        last_selected_property = currentProperties;


        //set up panorama


    }

    Animator currentAnimator;
    int shortAnimationDuration = 2000;

    private void zoomImageFromTHumb(ImageView normalView, View expandedImageView, PossiblePlacesAdapter.ViewHolder holder, int position, Bitmap bitmap){

        // if there's an animation in progress, cancel it
        // immediately and priceed with this one.


        if (currentAnimator != null){
            currentAnimator.cancel();
        }

        //notifyDataSetChanged();
        last_selected_property = mData.get(position);

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
        mData.get(position)[IS_LOGO_ENLARGED] = View.VISIBLE;

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
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentAnimator != null) {
                    currentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
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
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        normalView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mData.get(position)[IS_LOGO_ENLARGED] = View.GONE;
                        currentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        normalView.setAlpha(1f);
                        mData.get(position)[IS_LOGO_ENLARGED] = View.GONE;
                        expandedImageView.setVisibility(View.GONE);
                        currentAnimator = null;
                    }
                });
                set.start();
                currentAnimator = set;
            }
        });


    }


}
