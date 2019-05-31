package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Region;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.DetectImageActivity;
import com.google.android.gms.samples.vision.ocrreader.FetchMealDetails;
import com.google.android.gms.samples.vision.ocrreader.OpenRestaurantMenuActivity;
import com.google.android.gms.samples.vision.ocrreader.Order;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;
import com.google.android.gms.samples.vision.ocrreader.WordNetHypernyms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mgo983 on 10/22/18.
 */

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.ViewHolder> {

    LayoutInflater inflater;
    ArrayList<String> mData;
    private ArrayList<Integer> state = new ArrayList<>();
    //public static HashMap<String, Order> order = new HashMap<>();
    public static HashMap<String, Object[]> order = new HashMap<>();


    private

    TextToSpeech myTTS;
    Context context;
    private String LOG_TAG = FoodItemAdapter.class.getSimpleName();

    private final static int STATE_NORMAL = 0;
    private final static int STATE_SELECT = 1;
    private final static int STATE_CURRENT_SELECT = 2;

    private static RelativeLayout last_selected_relative_layout;
    private static RecyclerView last_selected_recycler;

    private int normal = R.drawable.border;
    private int select = R.drawable.text_border;
    private int current_selection = R.drawable.select_border;

    private int[] STATES = { normal, select, current_selection};
    private String mealCategory;

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageButton mImageButton;
        private ImageButton mImageButtonShowFoodItem;
        private RecyclerView mTextByTextRecyclerView;
        private RecyclerView mRecyclerView;
        private RecyclerView mExpandOptionRecyclerView;
        private RelativeLayout mContainingRelativeLayout;
        private RelativeLayout mParent;

        public ViewHolder(View parent){
            super(parent);
            //mTextView = parent.findViewById(R.id.single_food_item);

            mTextByTextRecyclerView = parent.findViewById(R.id.single_food_item);

            mImageButton = parent.findViewById(R.id.speak_whole_text);
            mImageButtonShowFoodItem = parent.findViewById(R.id.show_food_item_image);
            mRecyclerView = parent.findViewById(R.id.food_item_options);
            mExpandOptionRecyclerView = parent.findViewById(R.id.order_option_items);
            mContainingRelativeLayout = parent.findViewById(R.id.containing_relative_layout);
            mParent = (RelativeLayout) parent;
        }

    }

    public FoodItemAdapter(Context context, int resource, String mealCategory){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        //myTTS = ((DetectImageActivity) context).myTTS;
        myTTS = ((UseRecyclerActivity) context).myTTS;
        this.mealCategory = mealCategory;

    }

    @Override
    public FoodItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.single_food_item, null);
        //FoodItemAdapter.ViewHolder viewHolder = new FoodItemAdapter.ViewHolder(convertView);

        return new FoodItemAdapter.ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(final FoodItemAdapter.ViewHolder holder, int position){

        final String word = mData.get(position);
        //holder.mTextView.setText(word);

        // setup horizontal text by text adapter
        TextByTextAdapterIntercept adapter = new TextByTextAdapterIntercept(context, R.layout.recognized_text_item, false);

        String tokenizedString [] = word.split(" ");

        for (String child : tokenizedString){
            adapter.addItem(child);
        }

        LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
        holder.mTextByTextRecyclerView.setLayoutManager(layoutManager);

        holder.mTextByTextRecyclerView.setAdapter(adapter);

        /*// the text view is parent wide
        holder.mTextView.setSelected(false);


        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_view(word, holder);
            }
        });*/

        //select with relative layout instead

        holder.mContainingRelativeLayout.setSelected(false);

        /*holder.mContainingRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeState(word, holder, position);
                ((OpenRestaurantMenuActivity) context).hideOptionResults();
            }
        });*/

        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeState(word, holder, position);

            }
        });

        holder.mImageButtonShowFoodItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeState(word,holder, position, true);
            }
        });


        //set up food item adapter
        FoodItemOrderOptionAdapter foodItemOrderOptionAdapter = new FoodItemOrderOptionAdapter(context,order, word, holder.mExpandOptionRecyclerView);

        addAllItems(foodItemOrderOptionAdapter, word);
        LinearLayoutManager foodItemLayoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
        holder.mRecyclerView.setLayoutManager(foodItemLayoutManager);

        holder.mRecyclerView.setAdapter(foodItemOrderOptionAdapter);

//        holder.mRecyclerView.setOnClickListener(new View.OnClickListener() {
        holder.mParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeState(word,holder,position);
            }
        });
    }


    private void addAllItems(FoodItemOrderOptionAdapter adapter, String word){
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
                Log.d(LOG_TAG, "clean option " + clean_option + "parent " + mealCategory);

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




    private void changeState(String word, FoodItemAdapter.ViewHolder holder, int position){
        myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
        int current_state = state.get(position);
        //int next_state = current_state + 1;

        switch (current_state){
            case STATE_CURRENT_SELECT:
            case STATE_SELECT:
                holder.mContainingRelativeLayout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_NORMAL]));
                holder.mRecyclerView.setVisibility(View.GONE);
                state.set(position,STATE_NORMAL);

                removeOrder(word);

                break;
            case STATE_NORMAL:
                if (last_selected_relative_layout != null){
                    last_selected_relative_layout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_SELECT]));
                }
                last_selected_relative_layout = holder.mContainingRelativeLayout;
                if (last_selected_recycler != null){
                    last_selected_recycler.setVisibility(View.GONE);
                }
                last_selected_recycler = holder.mRecyclerView;


                holder.mContainingRelativeLayout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_CURRENT_SELECT]));
                holder.mRecyclerView.setVisibility(View.VISIBLE);
                state.set(position,STATE_CURRENT_SELECT);

                addOrder(word, position, holder);

                break;
        }


    }

    private void changeState(String word, FoodItemAdapter.ViewHolder holder, int position, boolean image){
        myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
        //((OpenRestaurantMenuActivity) context).hideSelectedOptionRecycler();
        holder.mExpandOptionRecyclerView.setVisibility(View.GONE);

        int current_state = state.get(position);
        //int next_state = current_state + 1;

        switch (current_state){
            case STATE_CURRENT_SELECT:
            case STATE_SELECT:
                holder.mContainingRelativeLayout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_NORMAL]));
                holder.mRecyclerView.setVisibility(View.GONE);
                state.set(position,STATE_NORMAL);
                ((OpenRestaurantMenuActivity) context).hide_food_image_recycler();

                removeOrder(word);

                break;
            case STATE_NORMAL:
                if (last_selected_relative_layout != null){
                    last_selected_relative_layout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_SELECT]));
                }
                last_selected_relative_layout = holder.mContainingRelativeLayout;
                if (last_selected_recycler != null){
                    last_selected_recycler.setVisibility(View.GONE);
                }
                last_selected_recycler = holder.mRecyclerView;

                setUpRecyclerView(word, false);
                //((OpenRestaurantMenuActivity) context).hideSelectedOptionRecycler();
                holder.mExpandOptionRecyclerView.setVisibility(View.GONE);

                holder.mContainingRelativeLayout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_CURRENT_SELECT]));
                holder.mRecyclerView.setVisibility(View.VISIBLE);
                state.set(position,STATE_CURRENT_SELECT);

                addOrder(word, position, holder);

                break;
        }


    }

    public static final int ORDER = 0;
    public static final int POSITION = 1;
    public static final int HOLDER = 2;
    public static final int FOOD_ITEM_ADAPTER = 3;



    private void removeOrder(String word){
        order.remove(word);
    }


    private void addOrder(String word, int position, ViewHolder holder){
        Integer order_items [] = new Integer[Order.OPTION_SIZE];
        Order current_order = new Order(word, order_items);
        Object [] orderData = new Object[4];
        orderData[ORDER] = current_order;
        orderData[POSITION] = position;
        orderData[HOLDER] = holder;
        orderData[FOOD_ITEM_ADAPTER] = this;
        //order.put(word, current_order);
        order.put(word, orderData);
    }

    public void resetLayout(String key){

        Object[]  current_order_item = order.get(key);
        ViewHolder holder = (ViewHolder) current_order_item[HOLDER];
        holder.mContainingRelativeLayout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_NORMAL]));
        holder.mRecyclerView.setVisibility(View.GONE);

        Integer position = (Integer) current_order_item[POSITION];
        state.set(position,STATE_NORMAL);
        //((OpenRestaurantMenuActivity) context).hide_food_image_recycler();
    }






    public void setUpRecyclerView(String wholeMealText, Boolean blockOrText){


        OpenRestaurantMenuActivity openRestaurantMenuActivity = (OpenRestaurantMenuActivity) context;

        RecyclerView recyclerView = openRestaurantMenuActivity.findViewById(R.id.gridview_edit_meal);

        RecyclerWordAdapter recyclerWordAdapter = new RecyclerWordAdapter(context, R.layout.gridview_item, myTTS, wholeMealText, blockOrText);


        ProgressBar progressBar = openRestaurantMenuActivity.findViewById(R.id.searching_edmame);
        progressBar.setVisibility(View.VISIBLE);

        FetchMealDetails fetchMealDetails = new FetchMealDetails(recyclerWordAdapter, context);
        fetchMealDetails.execute(wholeMealText);

        recyclerView.setAdapter(recyclerWordAdapter);

        ImageButton imageButtonclearRecycler = openRestaurantMenuActivity.findViewById(R.id.cancel_gridview_edit_meal);
        imageButtonclearRecycler.setVisibility(View.VISIBLE);

    }




    @Override
    public int getItemCount(){
        return mData.size();
    }

    public void addItem(String text){
        mData.add(text);
        // initialize entry state to zero

        state.add(STATE_NORMAL);
        notifyDataSetChanged();
    }

    public void addItem(ArrayList<String> foodItems){
        mData = foodItems;
        for (String item : foodItems){
            state.add(STATE_NORMAL);
        }
    }



}
