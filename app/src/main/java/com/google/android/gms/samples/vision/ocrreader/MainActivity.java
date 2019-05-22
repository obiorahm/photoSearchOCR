package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;

/**
 * Created by mgo983 on 10/19/18.
 */

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newmain);


        Button location_menu_btn = findViewById(R.id.location_based_menu);
        location_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(getApplicationContext(), GeographyActivity.class);
                Intent intent = new Intent(getApplicationContext(), PlacesActivity.class);
                startActivity(intent);
            }
        });

        Button paper_menu_btn = findViewById(R.id.text_based_menu);
        paper_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TextMenuActivity.class);
                startActivity(intent);
            }
        });

    }


}
