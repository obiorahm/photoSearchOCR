package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
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


import com.google.android.gms.samples.vision.ocrreader.AllOrders;
import com.google.android.gms.samples.vision.ocrreader.CurrentOrder;
import com.google.android.gms.samples.vision.ocrreader.FetchMealDetails;
import com.google.android.gms.samples.vision.ocrreader.ObjectsToHide;
import com.google.android.gms.samples.vision.ocrreader.OpenRestaurantMenuActivity;
import com.google.android.gms.samples.vision.ocrreader.Order;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mgo983 on 10/22/18.
 */

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.ViewHolder>  {

    LayoutInflater inflater;
    ArrayList<String> mData;
    private ArrayList<String> mDescription;
    private ArrayList<Integer> state = new ArrayList<>();
    private ArrayList<Boolean> checkBoxState = new ArrayList<>();
    private ArrayList<FoodDescriptionAdapter> foodDescriptionAdapters = new ArrayList<>();

    public static HashMap<String, Object[]> order = new HashMap<>();


    TextToSpeech myTTS;
    Context context;
    private String LOG_TAG = FoodItemAdapter.class.getSimpleName();

    private final static int STATE_NORMAL = 0;
    private final static int STATE_SELECT = 1;
    private final static int STATE_CURRENT_SELECT = 2;

    private final static Boolean UNCHECKED = false;
    private final static Boolean CHECKED = true;


    private RelativeLayout last_selected_relative_layout;

    private int normal = R.drawable.border;
    private int select = R.drawable.text_border;
    private int current_selection = R.drawable.select_border;

    private int[] STATES = { normal, select, current_selection};
    private String mealCategory;

    private AllOrders orders = new AllOrders();

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageButton mImageButton;
        private RecyclerView mTextByTextRecyclerView;
        private RelativeLayout mContainingRelativeLayout;

        private CheckBox mCheckBox;

        private ImageButton mImageButtonMore;
        private ImageButton mImageButtonLess;

        public RecyclerView mRecyclerViewWholeMealView;
        public RecyclerView mRecyclerFoodDescription;
        public ProgressBar mFoodDescriptionProgressBar;
        public ImageButton mImageButtonCancelEdamamSearch;
        public ProgressBar mProgressBarSearchingEdamam;
        public TextView mTextViewNoResult;

        public ViewHolder(View parent){
            super(parent);
            mTextByTextRecyclerView = parent.findViewById(R.id.single_food_item);

            mImageButton = parent.findViewById(R.id.speak_whole_text);

            mContainingRelativeLayout = parent.findViewById(R.id.containing_relative_layout);

            mCheckBox = parent.findViewById(R.id.select_food_item);

            mImageButtonMore = parent.findViewById(R.id.more_selection_details);
            mImageButtonLess = parent.findViewById(R.id.less_selection_details);

            mRecyclerFoodDescription = parent.findViewById(R.id.food_description_recycler);
            mFoodDescriptionProgressBar = parent.findViewById(R.id.food_description_recycler_progress_bar);
            mRecyclerViewWholeMealView = parent.findViewById(R.id.gridview_edit_meal);
            mImageButtonCancelEdamamSearch = parent.findViewById(R.id.cancel_gridview_edit_meal);
            mProgressBarSearchingEdamam = parent.findViewById(R.id.searching_edmame);
            mTextViewNoResult = parent.findViewById(R.id.no_result);

        }

    }

    public FoodItemAdapter(Context context, String mealCategory){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        myTTS = ((UseRecyclerActivity) context).myTTS;
        this.mealCategory = mealCategory;
        this.orders = orders;

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

        // setup horizontal text by text adapter
        TextByTextAdapterIntercept adapter = new TextByTextAdapterIntercept(context, R.layout.recognized_text_item, false);

        adapter.addItem(word);


        LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
        holder.mTextByTextRecyclerView.setLayoutManager(layoutManager);

        holder.mTextByTextRecyclerView.setAdapter(adapter);


        //select with relative layout instead

        holder.mContainingRelativeLayout.setSelected(false);

        holder.mCheckBox.setChecked(checkBoxState.get(position));


        holder.mImageButton.setOnClickListener((view) ->
            myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null, null));


        holder.mCheckBox.setOnClickListener((View view) ->
            changeState(word, holder, position, adapter));

        holder.mContainingRelativeLayout.setOnClickListener((View view)->{
            if(holder.mCheckBox.isChecked()) {
                holder.mCheckBox.setChecked(false);
                checkBoxState.set(position, UNCHECKED);
            }
            else {
                holder.mCheckBox.setChecked(true);
                checkBoxState.set(position,CHECKED);
            }
            changeState(word,holder, position, adapter);
        });


        holder.mImageButtonMore.setOnClickListener((View view) ->
            expandMore(holder, adapter, word, description, position)
        );

        holder.mImageButtonLess.setOnClickListener((View view) ->
            expandLess(holder, adapter));

    }

    private void setUpFoodDescriptionAdapter(FoodItemAdapter.ViewHolder holder, int position){
        LinearLayoutManager descriptionLayoutManager= new LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false);
        holder.mRecyclerFoodDescription.setLayoutManager(descriptionLayoutManager);
        FoodDescriptionAdapter  currentFoodDescriptionAdapter = foodDescriptionAdapters.get(position);
        ObjectsToHide objectsToHide = new ObjectsToHide(holder.mFoodDescriptionProgressBar, holder.mRecyclerFoodDescription);

        currentFoodDescriptionAdapter.setObjectsToHide(objectsToHide);
        holder.mRecyclerFoodDescription.setAdapter(currentFoodDescriptionAdapter);
    }


    private void expandMore(FoodItemAdapter.ViewHolder holder,
                            TextByTextAdapterIntercept adapterIntercept, String word, String description, int position){

        holder.mImageButtonLess.setVisibility(View.VISIBLE);

        holder.mImageButtonMore.setVisibility(View.GONE);

        setUpRecyclerView(word, holder);

        if (description != null && !(description.equals(""))){
            //Log.d(LOG_TAG, "object to hide " + objectsToHide.toString());

            holder.mFoodDescriptionProgressBar.setVisibility(View.VISIBLE);

            setUpFoodDescriptionAdapter(holder, position);

            holder.mFoodDescriptionProgressBar.setVisibility(View.GONE);
            holder.mRecyclerFoodDescription.setVisibility(View.VISIBLE);



        }

        //show adapters
        adapterIntercept.displayAllDescriptiveImages();

    }



    private void hideFoodDescription(FoodItemAdapter.ViewHolder holder){
        holder.mRecyclerFoodDescription.setVisibility(View.GONE);
    }


    private void expandLess(FoodItemAdapter.ViewHolder holder, TextByTextAdapterIntercept adapterIntercept){
        holder.mImageButtonLess.setVisibility(View.GONE);
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

    private void changeState(String word, FoodItemAdapter.ViewHolder holder, int position, TextByTextAdapterIntercept adapter){

        myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);

        if (holder.mCheckBox.isChecked()){

            if (last_selected_relative_layout != null){
                last_selected_relative_layout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_SELECT]));
            }
            last_selected_relative_layout = holder.mContainingRelativeLayout;


            holder.mContainingRelativeLayout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_CURRENT_SELECT]));
            state.set(position,STATE_CURRENT_SELECT);

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

        order.put(word, orderData);
    }

    public void resetLayout(String key){

        Object[]  current_order_item = order.get(key);
        ViewHolder holder = (ViewHolder) current_order_item[HOLDER];
        holder.mContainingRelativeLayout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_NORMAL]));
        holder.mCheckBox.setChecked(false);

        holder.mImageButtonCancelEdamamSearch.setVisibility(View.GONE);
        holder.mProgressBarSearchingEdamam.setVisibility(View.GONE);
        holder.mRecyclerViewWholeMealView.setVisibility(View.GONE);
        holder.mTextViewNoResult.setVisibility(View.GONE);

        Integer position = (Integer) current_order_item[POSITION];
        state.set(position,STATE_NORMAL);

    }


    private void goneViews(ViewHolder holder, TextByTextAdapterIntercept tbtAdapter){

        holder.mImageButtonCancelEdamamSearch.setVisibility(View.GONE);
        holder.mProgressBarSearchingEdamam.setVisibility(View.GONE);
        holder.mRecyclerViewWholeMealView.setVisibility(View.GONE);
        holder.mTextViewNoResult.setVisibility(View.GONE);

        tbtAdapter.hideImage();

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

    /*public void addItem(String text){
        mData.add(text);
        // initialize entry state to zero

        state.add(STATE_NORMAL);
        notifyDataSetChanged();
    }*/

    public void addItem(ArrayList<String> foodItems, ArrayList<String> foodItemDescription){
        mData = foodItems;
        mDescription = foodItemDescription;

        for (int i = 0; i < mData.size(); i++ ){
            state.add(STATE_NORMAL);
            checkBoxState.add(UNCHECKED);
            String description = mDescription.get(i);

            FoodDescriptionAdapter foodDescriptionAdapter = new FoodDescriptionAdapter(context);
            foodDescriptionAdapter.addItem(description);
            foodDescriptionAdapters.add(foodDescriptionAdapter);

        }
        notifyDataSetChanged();
    }

    public void getSelectedItem(AllOrders orders){
        for (int i = 0; i < mData.size(); i++){
            int currentState = state.get(i);
            if (currentState == STATE_SELECT || currentState == STATE_CURRENT_SELECT){
                String mealName = mData.get(i);
                HashMap description = foodDescriptionAdapters.get(i).getOrderDescription();
                CurrentOrder currentOrder = new CurrentOrder(mealName, description);
                orders.addOrder(currentOrder);

                state.set(i, STATE_NORMAL);
                checkBoxState.set(i, UNCHECKED);

                Log.d(LOG_TAG, "orders " + orders + " " +orders.orders.size() + " mealName " + mealName);
            }
        }
    }


    /*public void resetSelection(){
        for (int curr_state : state){
            curr_state = STATE_NORMAL;
            notifyDataSetChanged();
        }
        for (Boolean checkBox : checkBoxState){
            checkBox = UNCHECKED;
            notifyDataSetChanged();
        }



    }*/




}
