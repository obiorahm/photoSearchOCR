package com.google.android.gms.samples.vision.ocrreader;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by mgo983 on 8/23/18.
 */

public class OrderDialog extends DialogFragment {

    public void OrderDialog(){}
    TextToSpeech myTTS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.order_meal, container, false);

        myTTS = MealMenuActivity.myTTS;


        final  String order = getArguments().getString(RecipeDialog.ORDER_MEAL_TEXT);

        TextView textView = (TextView) rootView.findViewById(R.id.order_meal_text);
        textView.setText(order);

        ImageButton imageButton = (ImageButton) rootView.findViewById(R.id.speak_order);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTTS.speak(order, TextToSpeech.QUEUE_FLUSH, null);

            }
        });

        return rootView;
    }
}
