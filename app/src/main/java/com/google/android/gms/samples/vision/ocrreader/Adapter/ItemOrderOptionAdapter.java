package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.SetAdapter;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;
import com.google.android.gms.samples.vision.ocrreader.WordNetHypernyms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ItemOrderOptionAdapter extends RecyclerView.Adapter<ItemOrderOptionAdapter.ViewHolder> implements SetAdapter {


        LayoutInflater inflater;
        ArrayList<String[]> mData = new ArrayList<>();
        ArrayList<Integer> state = new ArrayList<>();
        HashMap<String, Uri> mUrl = new HashMap<>();

        TextToSpeech myTTS;
        Context context;

        String mealName;
        String categoryName;





        private static int STATE_NORMAL = 0;

        private int normal = R.drawable.smaller_layer_drawable;
        private int select = R.drawable.option_button;
        private int reject = R.drawable.option_button_on;


        private int[] STATES = { normal, select, reject};


        private String LOG_TAG = ItemOrderOptionAdapter.class.getSimpleName();

        public class ViewHolder extends RecyclerView.ViewHolder{


            private ImageView mImageView;
            private TextView mTextView;

            public ViewHolder(View parent){
                super(parent);
                mImageView = parent.findViewById(R.id.order_option);
                mTextView = parent.findViewById(R.id.order_option_text);
            }

        }

        public ItemOrderOptionAdapter(Context context,  TextToSpeech myTTS, String mealName, String categoryName){
            this.context = context;
            inflater = LayoutInflater.from(context);

            this.myTTS = myTTS;
            this.mealName = mealName;
            this.categoryName = categoryName;
        }

        //private int[] STATES = { normal, select};


        @Override
        public ItemOrderOptionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View convertView = inflater.inflate(R.layout.enlarged_option_item, null);

            return new ItemOrderOptionAdapter.ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(final ItemOrderOptionAdapter.ViewHolder holder, int position){

            //final int pos = position;

            String[] text = mData.get(position);
            String label = text[1];
            String root_string = text[0];
            String type = text[TYPE_POS];

            if (label != null)
                holder.mTextView.setText(label);

            Uri url = mUrl.get(root_string);

            if (url != null){
                Glide.with(context).load(url).into(holder.mImageView);
                Log.d(LOG_TAG, url + "");
            }

            int current_state = state.get(position);

            switch(type){
                case "DYNAMIC":
                case "STATIC_ZERO_SUM":
                    holder.mImageView.setBackground(ContextCompat.getDrawable(context, STATES[current_state]));
                    break;
                case "STATIC":

                    break;
            }


            holder.mImageView.setOnClickListener((View view) -> {
                switch (type){
                    case "DYNAMIC":
                        case "STATIC_ZERO_SUM":
                            int nextstate = next_state(state.get(position));
                            holder.mImageView.setBackground(ContextCompat.getDrawable(context, STATES[nextstate]));
                            state.set(position, nextstate);
                            break;
                    case "STATIC":
                        int next_state = nextstate(text[URL_POS], current_state,position, holder);
                        state.set(position,next_state);
                        break;
                }
                myTTS.speak(label,TextToSpeech.QUEUE_FLUSH,null, null);


            });

        }

        @Override
        public int getItemCount(){
            return mData.size();
        }

        private int WORD_POS = 0;
        private int ROOT_WORD_POS = 1;
        private int TYPE_POS = 2;
        private int URL_POS = 3;

        public void addItem(String[] icon){
            mData.add(icon);

            switch (icon[TYPE_POS]){
                case "DYNAMIC":
                    ((UseRecyclerActivity) context).loadImage(icon, this, false);
                    state.add(STATE_NORMAL);
                    break;
                case "STATIC":
                case "STATIC_ZERO_SUM":
                    mUrl.put(icon[WORD_POS], Uri.parse(icon[URL_POS]));

            }


            notifyDataSetChanged();
        }


    @Override
    public void addImageUrl(String word, Uri uri){
        mUrl.put(word, uri);
        notifyDataSetChanged();
    }

    private int next_state(int state_id){
        int new_state = state_id + 1;
        if (new_state >= STATES.length)
            return STATE_NORMAL;
        else
            return new_state;

    }


    private int nextstate(String url, int current_state, int position, ViewHolder holder){

        int next_state = current_state + 1;

            try{
                String prefix = "file:///android_asset/";
                int firstPos = url.indexOf(prefix) + prefix.length();
                int lastPos = url.lastIndexOf("/");
                String new_string = url.substring(firstPos, lastPos);
                AssetManager assetManager = context.getAssets();
                String allAssets[] = assetManager.list(new_string);

                if (next_state >= allAssets.length){
                    next_state = 1;
                }


                String [] data = mData.get(position);
                Log.d(LOG_TAG,   new_string + "/" );
                String word = allAssets[next_state];
                int first_pos = 0;
                int last_pos = word.lastIndexOf(".");
                String clean_word = word.substring(first_pos, last_pos).replace("_"," ");
                data[URL_POS] = prefix + new_string + "/" + word;
                data[ROOT_WORD_POS] = clean_word;
                data[WORD_POS] = clean_word;
                Log.d(LOG_TAG, "next state " + next_state + prefix + new_string + "/" + word + " " + allAssets.length);
                Glide.with(context).load(data[URL_POS]).into(holder.mImageView);
                holder.mTextView.setText(word);
                mUrl.put(word, Uri.parse(data[URL_POS]));
                notifyDataSetChanged();

            }catch (IOException e){

            }
        return next_state;

    }


    public void addStaticOptions(){
        AssetManager assetManager = context.getAssets();
        WordNetHypernyms wordNetHypernyms = new WordNetHypernyms();

        //check if hypernym of parent and child makes the required hypernym.
        boolean parentHypernym ;
        boolean selfHypernym;

        // check if the words themselves are the required hypernyms.
        boolean isSelfHypernym;
        boolean isParentHypernym;

        try{
            final String allAssets[] = assetManager.list("top_level_icons");

            for (String option : allAssets){


                int firstPos = 0;
                int lastPos = option.lastIndexOf('.');
                String clean_option =  option.substring(firstPos, lastPos).replace("_", " ");
                Log.d(LOG_TAG, "clean option " + clean_option + " parent " + categoryName);

                String option_image_url = "file:///android_asset/top_level_icons/" + option;

                parentHypernym = wordNetHypernyms.getHypernym(WordNetHypernyms.DRINK_HYPERNYMS, categoryName);
                selfHypernym = wordNetHypernyms.getHypernym(WordNetHypernyms.DRINK_HYPERNYMS, mealName);
                isSelfHypernym = wordNetHypernyms.isHypernym(WordNetHypernyms.DRINK_HYPERNYMS, mealName);
                isParentHypernym = wordNetHypernyms.isHypernym(WordNetHypernyms.DRINK_HYPERNYMS, categoryName);

                switch (clean_option){
                    case FoodItemOrderOptionAdapter.DRINKS:
                        Log.d(LOG_TAG, "is_parent_hypernym " + parentHypernym + selfHypernym + isSelfHypernym + isParentHypernym);
                        if (parentHypernym || selfHypernym || isSelfHypernym || isParentHypernym)
                        {

                            /*String [] data = new String[4];
                            data[WORD_POS] = clean_option;
                            data[ROOT_WORD_POS] = clean_option;
                            data[TYPE_POS] = "STATIC";
                            data[URL_POS] = option_image_url;
                            state.add(1);
                            addItem(data);*/

                            String[] moreAssets = assetManager.list( clean_option );

                            if (moreAssets.length >= 1){
                                String more_options = moreAssets[0];
                                Log.d(LOG_TAG, "even more options size" + more_options);

                                lastPos = more_options.lastIndexOf('.');

                                String clean_more_option_ =  more_options.substring(firstPos, lastPos).replace("_"," ");

                                String [] data = new String[4];
                                data[WORD_POS] = clean_more_option_;
                                data[ROOT_WORD_POS] = clean_more_option_;
                                data[TYPE_POS] = "STATIC_ZERO_SUM";
                                data[URL_POS] = "file:///android_asset/" + clean_option + "/" + more_options ;
                                state.add(STATE_NORMAL);
                                addItem(data);

                                Log.d(LOG_TAG, "clean more option " + "file:///android_asset/" + clean_option + "/" + more_options);
                            }





                        }
                        break;
                    case FoodItemOrderOptionAdapter.MEATS:
                        // assume if drink then not meat

                        if (!(parentHypernym || selfHypernym || isParentHypernym || isSelfHypernym))
                        {
                            String[] moreAssets = assetManager.list( clean_option );
                            for (String more_options : moreAssets) {
                                Log.d(LOG_TAG, "more options size" + more_options);
                                String [] evenMoreAssets = assetManager.list(clean_option + "/" + more_options );

                                if (evenMoreAssets.length >= 2){
                                    String even_more_options = evenMoreAssets[1];
                                    Log.d(LOG_TAG, "even more options size" + even_more_options);

                                    lastPos = even_more_options.lastIndexOf('.');
                                    String clean_option_0 = clean_option;
                                    String clean_option_1 =  even_more_options.substring(firstPos, lastPos).replace("_"," ");

                                    String [] data = new String[4];
                                    data[WORD_POS] = clean_option_1;
                                    data[ROOT_WORD_POS] = clean_option_1;
                                    data[TYPE_POS] = "STATIC";
                                    data[URL_POS] = "file:///android_asset/" + clean_option_0 + "/" + more_options + "/" + even_more_options;
                                    state.add(1);
                                    addItem(data);

                                    Log.d(LOG_TAG, "clean more option " + "file:///android_asset/" + option + "/" + more_options + "/" + even_more_options);
                                }



                            }

                        }

                        break;
                    default:

                        String[] moreAssets = assetManager.list( clean_option );

                            if (moreAssets.length >= 1){
                                String more_options = moreAssets[0];
                                Log.d(LOG_TAG, "even more options size" + more_options);

                                lastPos = more_options.lastIndexOf('.');

                                String clean_more_option_ =  more_options.substring(firstPos, lastPos).replace("_"," ");

                                String [] data = new String[4];
                                data[WORD_POS] = clean_more_option_;
                                data[ROOT_WORD_POS] = clean_more_option_;
                                data[TYPE_POS] = "STATIC";
                                data[URL_POS] = "file:///android_asset/" + clean_option + "/" + more_options ;
                                state.add(1);
                                addItem(data);

                                Log.d(LOG_TAG, "clean more option " + "file:///android_asset/" + clean_option + "/" + more_options);
                            }



                        break;

                }


            }

        }catch (IOException e){
            Log.e(LOG_TAG, e + "");
        }


    }

}
