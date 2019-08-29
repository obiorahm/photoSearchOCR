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
        private static int STATIC_STATE_NORMAL = 1;

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


        @Override
        public ItemOrderOptionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View convertView = inflater.inflate(R.layout.enlarged_option_item, null);

            return new ItemOrderOptionAdapter.ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(final ItemOrderOptionAdapter.ViewHolder holder, int position){

            String[] text = mData.get(position);
            String label = text[1];
            String root_string = text[0];
            String type = text[TYPE_POS];

            if (label != null)
                holder.mTextView.setText(label);

            Uri url = mUrl.get(root_string);

            Log.d(LOG_TAG, "url: " + url);

            //if (url != null){
                Glide.with(context).load(url).into(holder.mImageView);
            //}

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
                String prefix = "";
                switch (type){
                    case "DYNAMIC":
                    case "STATIC_ZERO_SUM":
                        int nextstate = next_state(state.get(position));
                        holder.mImageView.setBackground(ContextCompat.getDrawable(context, STATES[nextstate]));
                        prefix = getPrefix(nextstate);
                        state.set(position, nextstate);
                        break;
                    case "STATIC":
                        int next_state = nextstate(text[URL_POS], current_state, position, holder);
                        state.set(position, next_state);
                        break;
                }
                myTTS.speak(prefix + label,TextToSpeech.QUEUE_FLUSH,null, null);


            });

        }


        private String getPrefix (int state){
            String [] current_state_prefix = {"", "with ", "no " };
            return current_state_prefix[state];
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
            ((UseRecyclerActivity) context).loadImage(icon, this, false);
            state.add(STATE_NORMAL);
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
                    next_state = STATIC_STATE_NORMAL;
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
                Log.d(LOG_TAG, "next state " + next_state + " " +prefix + new_string + "/" + word + " " + allAssets.length);
                Glide.with(context).load(data[URL_POS]).into(holder.mImageView);
                holder.mTextView.setText(word);
                mUrl.put(clean_word, Uri.parse(data[URL_POS]));
                notifyDataSetChanged();

            }catch (IOException e){

            }
        return next_state;

    }


    public void addStaticOptions(){
        AssetManager assetManager = context.getAssets();

        try{
            final String allAssets[] = assetManager.list("top_level_icons");

            for (String option : allAssets){


                int firstPos = 0;
                int lastPos = option.lastIndexOf('.');
                String clean_option =  option.substring(firstPos, lastPos).replace("_", " ");
                Log.d(LOG_TAG, "clean option " + clean_option + " parent " + categoryName);

                switch (clean_option){
                    case FoodItemOrderOptionAdapter.DRINKS:
                        if (isDrink())
                            setData(clean_option, 1, 0,  "STATIC_ZERO_SUM", STATE_NORMAL);
                        break;

                    case FoodItemOrderOptionAdapter.NUTRITION:
                        setData(clean_option, 1, 0,  "STATIC_ZERO_SUM", STATE_NORMAL);
                        break;

                    case FoodItemOrderOptionAdapter.MEATS:
                        // assume if drink then not meat
                        if (!isDrink())
                        {
                            String[] moreAssets = assetManager.list( clean_option );
                            for (String more_options : moreAssets) {
                                Log.d(LOG_TAG, "more options size" + more_options);
                                String addressPrefix = clean_option + "/" + more_options;
                                setData(addressPrefix, 2,1,  "STATIC", STATIC_STATE_NORMAL);
                            }

                        }
                        break;
                    default:
                        if (!isDrink())
                            setData(clean_option, 1, 0, "STATIC", STATIC_STATE_NORMAL);
                        break;
                }
            }

        }catch (IOException e){
            Log.e(LOG_TAG, e + "");
        }


    }


    private boolean isDrink(){

        //check if hypernym of parent and child makes the required hypernym.
        boolean parentHypernym ;
        boolean selfHypernym;

        // check if the words themselves are the required hypernyms.
        boolean isSelfHypernym;
        boolean isParentHypernym;

        WordNetHypernyms wordNetHypernyms = new WordNetHypernyms();

        parentHypernym = wordNetHypernyms.getHypernym(WordNetHypernyms.DRINK_HYPERNYMS, categoryName);
        selfHypernym = wordNetHypernyms.getHypernym(WordNetHypernyms.DRINK_HYPERNYMS, mealName);
        isSelfHypernym = wordNetHypernyms.isHypernym(WordNetHypernyms.DRINK_HYPERNYMS, mealName);
        isParentHypernym = wordNetHypernyms.isHypernym(WordNetHypernyms.DRINK_HYPERNYMS, categoryName);

        Log.d(LOG_TAG, "is_parent_hypernym " + parentHypernym + selfHypernym + isSelfHypernym + isParentHypernym);

        return (parentHypernym || selfHypernym || isSelfHypernym || isParentHypernym);

    }


    private void setData(String clean_option, int min_asset_length, int first_asset_pos, String type_pos, int init_state) {

        try {

            AssetManager assetManager = context.getAssets();
            String[] moreAssets = assetManager.list(clean_option);

            int firstPos = 0;

            if (moreAssets.length >= min_asset_length) {
                String more_options = moreAssets[first_asset_pos];
                Log.d(LOG_TAG, "even more options size" + more_options);

                int lastPos = more_options.lastIndexOf('.');

                String clean_more_option_ = more_options.substring(firstPos, lastPos).replace("_", " ");

                String[] data = new String[4];
                data[WORD_POS] = clean_more_option_;
                data[ROOT_WORD_POS] = clean_more_option_;
                data[TYPE_POS] = type_pos;
                data[URL_POS] = "file:///android_asset/" + clean_option + "/" + more_options;
                state.add(init_state);
                mUrl.put(clean_more_option_, Uri.parse(data[URL_POS]));
                mData.add(data);
                Log.d(LOG_TAG, "clean more option " + "file:///android_asset/" + clean_option + "/" + more_options);
            }

        }catch (IOException e){
            Log.e(LOG_TAG, e + " ");
        }
    }



}
