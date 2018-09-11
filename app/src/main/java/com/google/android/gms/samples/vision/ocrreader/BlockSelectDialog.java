package com.google.android.gms.samples.vision.ocrreader;

import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.Adapter.BlockRecyclerAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RecyclerWordAdapter;

/**
 * Created by mgo983 on 9/7/18.
 */

public class BlockSelectDialog extends DialogFragment {

    BlockRecyclerAdapter questionsAdapter;
    TextToSpeech myTTS;
    String LOG_TAG = BlockSelectDialog.class.getSimpleName();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.dialog_block_select, container, false);

        ImageView imageView = rootView.findViewById(R.id.image);

        Uri uri = Uri.parse(getArguments().getString(RecyclerWordAdapter.IMAGE_URI));

        final OrderInstructions orderInstructions = new OrderInstructions();

        Glide.with(getActivity()).load(uri).into(imageView);

        myTTS = DetectImageActivity.myTTS;

        String wholeOrder = getArguments().getString(FetchMealDetails.WHOLE_ORDER);

        questionsAdapter = new BlockRecyclerAdapter(getActivity(), R.layout.list_view_items, myTTS, rootView, orderInstructions, wholeOrder);

        questionsAdapter.addItem();

        //addAdapterItems(questionsAdapter);

        TextView textViewMealName = rootView.findViewById(R.id.meal_name);
        textViewMealName.setText(getMealName(wholeOrder));

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.questions);



        LinearLayoutManager layoutManager= new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(ContextCompat.getDrawable(getActivity(),R.drawable.border));
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(questionsAdapter);

        final TextView textViewQuestion = rootView.findViewById(R.id.selected_option);
        textViewQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTTS.speak(textViewQuestion.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        });


        return rootView;

    }

    private String getMealName(String wholeOrder){
       String[] mealName = wholeOrder.split(",");
       return mealName[0];
    }

    @Override
    public void onResume() {

        // Get screen width and height in pixels
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // The absolute width of the available display size in pixels.
        int displayWidth = displayMetrics.widthPixels;

        // Set alert dialog width equal to screen width 90%
        int dialogWindowWidth = (int) (displayWidth * 0.90f);

        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();

        params.width = dialogWindowWidth;
        //params.height = dialogWindowHeight;


        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }

    /*private void addAdapterItems(BlockRecyclerAdapter questionAdapter){
        questionAdapter.addItem("Can I order an appetizer size or half-size entrée?");
        questionAdapter.addItem("Can I split a dish with someone at my table?");
        questionAdapter.addItem("Could you give me a larger portion of vegetables and a smaller portion of the main dish? ");
        questionAdapter.addItem("What can I substitute?");
        questionAdapter.addItem("Could you leave off the (sour cream, cheese sauce, dressing, mayonnaise, etc.)?");
        questionAdapter.addItem("Can you make this dish with sliced chicken breast?");
        questionAdapter.addItem("Which dishes do you recommend for vegetarians?");
        questionAdapter.addItem("Do you have nutrition information on any of your dishes?");
        questionAdapter.addItem("No ice please?");
        questionAdapter.addItem("Can I have my meat well done?");


    }*/

}
