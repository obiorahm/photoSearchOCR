package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.Order;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;
import com.google.android.gms.samples.vision.ocrreader.WordNetHypernyms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mgo983 on 1/10/19.
 */

public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder> {

    private ArrayList<Object []> mData = new ArrayList<>();
    private ArrayList<Integer []> mSpecificOptions = new ArrayList<>();
    private ArrayList<String> mUrl = new ArrayList<>();

    LayoutInflater inflater;
    TextToSpeech myTTS;
    Context context;


    String LOG_TAG = ShoppingCartAdapter.class.getSimpleName();

    private static final String ORDER_PREFIX = "I'll have ";


    public class ViewHolder extends RecyclerView.ViewHolder{


        private ImageView mImageView;
        private TextView mTextView;
        private ImageButton mImageButton;
        private RecyclerView mRecyclerView;
        private RelativeLayout mRelativeLayout;
        private RecyclerView mOptionRecyclerView;
        private RecyclerView mExpandOptionRecyclerView;



        public ViewHolder(View parent){
            super(parent);
            mImageView = parent.findViewById(R.id.order_option);
            mTextView = parent.findViewById(R.id.order_option_text);
            mImageButton = parent.findViewById(R.id.remove_order);
            mRecyclerView = parent.findViewById(R.id.conversation_recycler);
            mRelativeLayout = parent.findViewById(R.id.containing_relative_layout);
            mOptionRecyclerView = parent.findViewById(R.id.food_item_options);
            mExpandOptionRecyclerView = parent.findViewById(R.id.order_option_items);
        }

    }


    public ShoppingCartAdapter(Context context){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        myTTS = ((UseRecyclerActivity) context).myTTS;
    }


    @Override
    public ShoppingCartAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.personal_order_items, null);
        return new ShoppingCartAdapter.ViewHolder(convertView);
    }



/*    public static final int ORDER = 0;
    private static final int POSITION = 1;
    private static final int HOLDER = 2;
    public static final int FOOD_ITEM_ADAPTER = 3;*/
    private static final int MEAL_CATEGORY = 4;
    private static final int MEAL_NAME = 5;

    @Override
    public void onBindViewHolder(final ShoppingCartAdapter.ViewHolder holder, final int position){

        Object[] orderData = mData.get(position);

        final String meal_name = (String) orderData[MEAL_NAME];

        String finalText = ORDER_PREFIX + meal_name;

        holder.mTextView.setText(finalText);

        Glide.with(context).load(mUrl.get(position)).into(holder.mImageView);

        setOrderLanguage(holder, position);

        holder.mImageButton.setOnClickListener((View view) -> {

            FoodItemAdapter foodItemAdapter = (FoodItemAdapter) FoodItemAdapter.order.get(meal_name)[FoodItemAdapter.FOOD_ITEM_ADAPTER];
            foodItemAdapter.resetLayout(meal_name);
            FoodItemAdapter.order.remove(meal_name);

            holder.mTextView.setVisibility(View.GONE);
            holder.mImageView.setVisibility(View.GONE);
            holder.mImageButton.setVisibility(View.GONE);
            holder.mRecyclerView.setVisibility(View.GONE);
            holder.mRelativeLayout.setVisibility(View.GONE);
        });

        holder.mRelativeLayout.setOnClickListener((View view)->{
            //set up food item adapter
            String meal_category = (String) orderData[MEAL_CATEGORY];
            HashMap<String, Object[]> order = FoodItemAdapter.order;

            //FoodItemOrderOptionAdapter foodItemOrderOptionAdapter = new FoodItemOrderOptionAdapter(context,order, word, holder.mExpandOptionRecyclerView, myTTS);
            FoodItemOrderOptionAdapter foodItemOrderOptionAdapter = new FoodItemOrderOptionAdapter(context,order, meal_name, holder.mExpandOptionRecyclerView, myTTS);

            //addAllItems(foodItemOrderOptionAdapter, word);
            addAllItems(foodItemOrderOptionAdapter, meal_name, meal_category);
            LinearLayoutManager foodItemLayoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
            holder.mOptionRecyclerView.setLayoutManager(foodItemLayoutManager);

            holder.mOptionRecyclerView.setAdapter(foodItemOrderOptionAdapter);

            holder.mOptionRecyclerView.setVisibility(View.VISIBLE);
        });

        holder.mTextView.setOnClickListener((View view)->myTTS.speak(finalText,TextToSpeech.QUEUE_FLUSH,null, null));

    }




    public void addItem(String food_item_name, Integer [] specifics, String url, Object [] orderData ){
        //mData.add(food_item_name);
        mData.add(orderData);
        mSpecificOptions.add(specifics);
        mUrl.add(url);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount(){
        return mData.size();
    }

    private void setOrderLanguage(ViewHolder holder, int pos){
        LanguageAdapter languageAdapter= new LanguageAdapter(context);

        Integer[] specifics = mSpecificOptions.get(pos);
        for (int i = 0; i < specifics.length; i++){
            String current_sentence = Order.CONSOLIDATED_OPTION[i][specifics[i]];
            //String current_image_url = "";
            String current_image_url = getImageUrl(i, specifics[i]);
            String[] data = {current_sentence,current_image_url};
            if (!current_sentence.equals(""))
                languageAdapter.addItem(data);
        }

        holder.mRecyclerView.setAdapter(languageAdapter);
        LinearLayoutManager languageLayoutManager= new LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false);
        holder.mRecyclerView.setLayoutManager(languageLayoutManager);
    }


    /*private final static int ZERO_SUM = 0;
    private final static int MORE_CHOICES = 1;*/


    public String getImageUrl(int consolidatedOptionPosition, int pos){
        String url = "";
        String allAssets [];
        AssetManager assetManager = context.getAssets();

        switch (consolidatedOptionPosition){
            case 0:
                url += Order.DRINKS;
                break;

            case 1:
                url += Order.MEATS + "/cooked";
                break;

            case 2:
                url += Order.MEATS + "/sliced";
                break;

            case 3:
                url += Order.NUTRITION;
                break;

            case 4:
                url += Order.SAUCES;
                break;

            case 5:
                url += Order.DRINKS;
                break;
        }

        try{
            allAssets = assetManager.list(url );
            int position = 0;
            if (pos > 0 && pos < allAssets.length)
                position = pos;
            else if (pos > 0 && pos >= allAssets.length)
                position = pos - 1;

            if (position >= 0)
                url =  "file:///android_asset/" + url + "/" +allAssets[position];

            Log.d(LOG_TAG, "alternative " + url);

        }catch (IOException e){
            Log.e(LOG_TAG, e +"");
        }catch (NullPointerException e){
            Log.e(LOG_TAG, e+ "");
        }

        return url;
    }


    private void addAllItems(FoodItemOrderOptionAdapter adapter, String word, String mealCategory){
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
                Log.d(LOG_TAG, "clean option " + clean_option + " parent " + mealCategory);

                String option_image_url = "file:///android_asset/top_level_icons/" + option;

                parentHypernym = wordNetHypernyms.getHypernym(WordNetHypernyms.DRINK_HYPERNYMS, mealCategory);
                selfHypernym = wordNetHypernyms.getHypernym(WordNetHypernyms.DRINK_HYPERNYMS, word);
                isSelfHypernym = wordNetHypernyms.isHypernym(WordNetHypernyms.DRINK_HYPERNYMS, word);
                isParentHypernym = wordNetHypernyms.isHypernym(WordNetHypernyms.DRINK_HYPERNYMS, mealCategory);

                switch (clean_option){
                    case FoodItemOrderOptionAdapter.DRINKS:
                        Log.d(LOG_TAG, "is_parent_hypernym " + parentHypernym + selfHypernym + isSelfHypernym + isParentHypernym);
                        if (parentHypernym || selfHypernym || isSelfHypernym || isParentHypernym)
                            adapter.addItem(option_image_url);
                        break;
                    case FoodItemOrderOptionAdapter.MEATS:
                        // assume if drink then not meat

                        if (!(parentHypernym || selfHypernym || isParentHypernym || isSelfHypernym))
                            adapter.addItem(option_image_url);

                        break;
                    default:
                        adapter.addItem(option_image_url);
                        break;

                }


            }

        }catch (IOException e){
            Log.e(LOG_TAG, e + "");
        }


    }

}


