package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.android.gms.samples.vision.ocrreader.FetchMealDetails;
import com.google.android.gms.samples.vision.ocrreader.OpenRestaurantMenuActivity;
import com.google.android.gms.samples.vision.ocrreader.Order;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;
import com.google.android.gms.samples.vision.ocrreader.WordNetHypernyms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mgo983 on 10/22/18.
 */

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.ViewHolder>  {

    LayoutInflater inflater;
    ArrayList<String> mData;
    ArrayList<String> mDescription;
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


    private RelativeLayout last_selected_relative_layout;
    private RecyclerView last_selected_recycler;
    private RecyclerView last_selected_expand_option;
    private TextByTextAdapterIntercept last_tbt_adapter;


    private RecyclerView last_selected_mRecyclerViewWholeMealView;
    private ImageButton last_selected_mImageButtonCancelEdamamSearch;
    private ProgressBar last_selected_mProgressBarSearchingEdamam;
    private TextView last_selected_mTextViewNoResult;

    private int normal = R.drawable.border;
    private int select = R.drawable.text_border;
    private int current_selection = R.drawable.select_border;

    private int[] STATES = { normal, select, current_selection};
    private String mealCategory;

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageButton mImageButton;
        //private ImageButton mImageButtonShowFoodItem;
        private RecyclerView mTextByTextRecyclerView;
        //private RecyclerView mRecyclerView;
        //private RecyclerView mExpandOptionRecyclerView;
        private RelativeLayout mContainingRelativeLayout;
        private RelativeLayout mParent;
/*        private LinearLayout mEndSeparator;
        private LinearLayout mStartSeparator;*/
        private CheckBox mCheckBox;

        private ImageButton mImageButtonMore;
        private ImageButton mImageButtonLess;

        public RecyclerView mRecyclerViewWholeMealView;
        public RecyclerView mRecyclerFoodDescription;
        public ImageButton mImageButtonCancelEdamamSearch;
        public ProgressBar mProgressBarSearchingEdamam;
        public TextView mTextViewNoResult;

        public ViewHolder(View parent){
            super(parent);
            //mTextView = parent.findViewById(R.id.single_food_item);

            mTextByTextRecyclerView = parent.findViewById(R.id.single_food_item);

            mImageButton = parent.findViewById(R.id.speak_whole_text);
            //mImageButtonShowFoodItem = parent.findViewById(R.id.show_food_item_image);
            //mRecyclerView = parent.findViewById(R.id.food_item_options);
            //mExpandOptionRecyclerView = parent.findViewById(R.id.order_option_items);
            mContainingRelativeLayout = parent.findViewById(R.id.containing_relative_layout);

            /*mStartSeparator = parent.findViewById(R.id.start_separator);
            mEndSeparator = parent.findViewById(R.id.end_separator);*/
            mCheckBox = parent.findViewById(R.id.select_food_item);

            mImageButtonMore = parent.findViewById(R.id.more_selection_details);
            mImageButtonLess = parent.findViewById(R.id.less_selection_details);

            mRecyclerFoodDescription = parent.findViewById(R.id.food_description_recycler);
            mRecyclerViewWholeMealView = parent.findViewById(R.id.gridview_edit_meal);
            mImageButtonCancelEdamamSearch = parent.findViewById(R.id.cancel_gridview_edit_meal);
            mProgressBarSearchingEdamam = parent.findViewById(R.id.searching_edmame);
            mTextViewNoResult = parent.findViewById(R.id.no_result);

            mParent = (RelativeLayout) parent;
        }

    }

    public FoodItemAdapter(Context context, String mealCategory){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        myTTS = ((UseRecyclerActivity) context).myTTS;
        this.mealCategory = mealCategory;

    }

    @Override
    public FoodItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.single_food_item, null);


        return new FoodItemAdapter.ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(final FoodItemAdapter.ViewHolder holder, int position){

        final String word = mData.get(position);
        final String description = mDescription.get(position);
        //holder.mTextView.setText(word);

        // setup horizontal text by text adapter
        TextByTextAdapterIntercept adapter = new TextByTextAdapterIntercept(context, R.layout.recognized_text_item, false);

        adapter.addItem(word);


        LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
        holder.mTextByTextRecyclerView.setLayoutManager(layoutManager);

        holder.mTextByTextRecyclerView.setAdapter(adapter);


        //if desription is not empty setup description of meal
        if (description != null && !(description.equals("")))
            setUpFoodDescriptionAdapter(holder, position);




        //select with relative layout instead

        holder.mContainingRelativeLayout.setSelected(false);


        holder.mImageButton.setOnClickListener((view) ->
            myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null, null));

        /*holder.mImageButtonShowFoodItem.setOnClickListener((view) ->
            setUpRecyclerView(word, holder));*/

        holder.mCheckBox.setOnClickListener((View view) ->
            changeState(word, holder, position, adapter));

        holder.mContainingRelativeLayout.setOnClickListener((View view)->{
            if(holder.mCheckBox.isChecked())
                holder.mCheckBox.setChecked(false);
            else
                holder.mCheckBox.setChecked(true);
            changeState(word,holder, position, adapter);
        });


        holder.mImageButtonMore.setOnClickListener((View view) ->
            expandMore(holder, adapter, word, description)
        );

        holder.mImageButtonLess.setOnClickListener((View view) ->
            expandLess(holder, adapter));

    }

    private void setUpFoodDescriptionAdapter(FoodItemAdapter.ViewHolder holder, int position){
        FoodDescriptionAdapter foodDescriptionAdapter = new FoodDescriptionAdapter(context);
        LinearLayoutManager descriptionLayoutManager= new LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false);
        holder.mRecyclerFoodDescription.setLayoutManager(descriptionLayoutManager);
        foodDescriptionAdapter.addItem(mDescription.get(position));
        holder.mRecyclerFoodDescription.setAdapter(foodDescriptionAdapter);
    }

    private void expandMore(FoodItemAdapter.ViewHolder holder,
                            TextByTextAdapterIntercept adapterIntercept, String word, String description){
        holder.mImageButtonLess.setVisibility(View.VISIBLE);
        //holder.mRecyclerView.setVisibility(View.VISIBLE);
        /*holder.mEndSeparator.setVisibility(View.VISIBLE);
        holder.mStartSeparator.setVisibility(View.VISIBLE);*/

        showFoodDescription(holder, description);

        holder.mImageButtonMore.setVisibility(View.GONE);

        setUpRecyclerView(word, holder);

        //shoe adapters
        adapterIntercept.displayAllDescriptiveImages();

    }

    private void showFoodDescription(FoodItemAdapter.ViewHolder holder, String description){
        if (description != null && !(description.equals("")))
            holder.mRecyclerFoodDescription.setVisibility(View.VISIBLE);
    }

    private void hideFoodDescription(FoodItemAdapter.ViewHolder holder){
        holder.mRecyclerFoodDescription.setVisibility(View.GONE);
    }


    private void expandLess(FoodItemAdapter.ViewHolder holder, TextByTextAdapterIntercept adapterIntercept){
        holder.mImageButtonLess.setVisibility(View.GONE);
        //holder.mRecyclerView.setVisibility(View.GONE);
        //holder.mExpandOptionRecyclerView.setVisibility(View.GONE);
        /*holder.mEndSeparator.setVisibility(View.GONE);
        holder.mStartSeparator.setVisibility(View.GONE);*/

        hideFoodDescription(holder);


        //hide tenRecycler
        holder.mImageButtonCancelEdamamSearch.setVisibility(View.GONE);
        holder.mProgressBarSearchingEdamam.setVisibility(View.GONE);
        holder.mRecyclerViewWholeMealView.setVisibility(View.GONE);
        holder.mTextViewNoResult.setVisibility(View.GONE);


        holder.mImageButtonMore.setVisibility(View.VISIBLE);

        //
        adapterIntercept.hideAllDescriptiveImages();



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



    private void changeState(String word, FoodItemAdapter.ViewHolder holder, int position, TextByTextAdapterIntercept adapter){

        myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);

        if (holder.mCheckBox.isChecked()){

            if (last_selected_relative_layout != null){
                last_selected_relative_layout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_SELECT]));
            }
            last_selected_relative_layout = holder.mContainingRelativeLayout;
            if (last_selected_recycler != null){
                /*last_selected_recycler.setVisibility(View.GONE);
                last_selected_expand_option.setVisibility(View.GONE);
                last_end_separator.setVisibility(View.GONE);
                last_start_separator.setVisibility(View.GONE);
                last_tbt_adapter.hideImage();

                last_selected_mImageButtonCancelEdamamSearch.setVisibility(View.GONE);
                last_selected_mProgressBarSearchingEdamam.setVisibility(View.GONE);
                last_selected_mRecyclerViewWholeMealView.setVisibility(View.GONE);
                last_selected_mTextViewNoResult.setVisibility(View.GONE);*/

            }
            //last_selected_recycler = holder.mRecyclerView;
            //last_selected_expand_option = holder.mExpandOptionRecyclerView;
            last_tbt_adapter = adapter;
            /*last_start_separator = holder.mStartSeparator;
            last_end_separator = holder.mEndSeparator;*/

            last_selected_mImageButtonCancelEdamamSearch = holder.mImageButtonCancelEdamamSearch;
            last_selected_mProgressBarSearchingEdamam = holder.mProgressBarSearchingEdamam;
            last_selected_mRecyclerViewWholeMealView = holder.mRecyclerViewWholeMealView;
            last_selected_mTextViewNoResult = holder.mTextViewNoResult;


            holder.mContainingRelativeLayout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_CURRENT_SELECT]));
            visibleViews(holder);
            state.set(position,STATE_CURRENT_SELECT);

            getNounDependency(mDescription.get(position));

            addOrder(word, position, holder);


        }else{


            holder.mContainingRelativeLayout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_NORMAL]));

            goneViews(holder, adapter);

            if (last_selected_relative_layout == holder.mContainingRelativeLayout)
                last_selected_relative_layout = null;

            ((OpenRestaurantMenuActivity) context).hide_food_image_recycler();

            state.set(position,STATE_NORMAL);

            removeOrder(word);

        }


    }


    private void getNounDependency(String description){
        /*FetchNounDependency fetchNounDependency = new FetchNounDependency(this);
        if (description != null && !(description.equals(""))){
            fetchNounDependency.execute(description);
        }*/
    }

    public static final int ORDER = 0;
    private static final int POSITION = 1;
    private static final int HOLDER = 2;
    public static final int FOOD_ITEM_ADAPTER = 3;
    public static final int MEAL_CATEGORY = 4;
    public static final int MEAL_NAME = 5;


    private void removeOrder(String word){
        order.remove(word);
    }


    private void addOrder(String word, int position, ViewHolder holder){
        Integer order_items [] = new Integer[Order.OPTION_SIZE];
        Order current_order = new Order(word, order_items);
        Object [] orderData = new Object[6];
        orderData[ORDER] = current_order;
        orderData[POSITION] = position;
        orderData[HOLDER] = holder;
        orderData[FOOD_ITEM_ADAPTER] = this;
        orderData[MEAL_CATEGORY] = mealCategory;
        orderData[MEAL_NAME] = word;


        //order.put(word, current_order);
        order.put(word, orderData);
    }

    public void resetLayout(String key){

        Object[]  current_order_item = order.get(key);
        ViewHolder holder = (ViewHolder) current_order_item[HOLDER];
        holder.mContainingRelativeLayout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_NORMAL]));
        //holder.mExpandOptionRecyclerView.setVisibility(View.GONE);
        holder.mCheckBox.setChecked(false);
        //holder.mRecyclerView.setVisibility(View.GONE);
        /*holder.mStartSeparator.setVisibility(View.GONE);
        holder.mEndSeparator.setVisibility(View.GONE);*/

        holder.mImageButtonCancelEdamamSearch.setVisibility(View.GONE);
        holder.mProgressBarSearchingEdamam.setVisibility(View.GONE);
        holder.mRecyclerViewWholeMealView.setVisibility(View.GONE);
        holder.mTextViewNoResult.setVisibility(View.GONE);

        Integer position = (Integer) current_order_item[POSITION];
        state.set(position,STATE_NORMAL);

    }


    private void goneViews(ViewHolder holder, TextByTextAdapterIntercept tbtAdapter){

        //holder.mExpandOptionRecyclerView.setVisibility(View.GONE);
        //holder.mRecyclerView.setVisibility(View.GONE);
        /*holder.mEndSeparator.setVisibility(View.GONE);
        holder.mStartSeparator.setVisibility(View.GONE);*/

        holder.mImageButtonCancelEdamamSearch.setVisibility(View.GONE);
        holder.mProgressBarSearchingEdamam.setVisibility(View.GONE);
        holder.mRecyclerViewWholeMealView.setVisibility(View.GONE);
        holder.mTextViewNoResult.setVisibility(View.GONE);

        tbtAdapter.hideImage();

    }

    private void visibleViews(ViewHolder holder){

        /*holder.mRecyclerView.setVisibility(View.VISIBLE);
        holder.mStartSeparator.setVisibility(View.VISIBLE);
        holder.mEndSeparator.setVisibility(View.VISIBLE);*/

    }



    private void setUpRecyclerView(String wholeMealText, FoodItemAdapter.ViewHolder holder){

        boolean blockOrText = false;

        RecyclerWordAdapter recyclerWordAdapter = new RecyclerWordAdapter(context, R.layout.gridview_item, myTTS, wholeMealText, blockOrText);


        holder.mProgressBarSearchingEdamam.setVisibility(View.GONE);

        FetchMealDetails fetchMealDetails = new FetchMealDetails(recyclerWordAdapter, context, FetchMealDetails.FOR_TENTATIVE_ORDER, holder);
        fetchMealDetails.execute(wholeMealText);

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

    public void addItem(ArrayList<String> foodItems, ArrayList<String> foodItemDescription){
        mData = foodItems;
        mDescription = foodItemDescription;

        for (String item : foodItems){
            state.add(STATE_NORMAL);
        }
    }



}
