package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.ImageButton;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RecognizedTextAdapter;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.firebase.FirebaseApp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mgo983 on 8/17/18.
 */

public class DetectImageActivity extends Activity implements TextToSpeech.OnInitListener, SpellCheckerSession.SpellCheckerSessionListener {

    String LOG_TAG = DetectImageActivity.class.getSimpleName();

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    //Text to speech variables
    private int MY_DATA_CHECK_CODE = 0;
    public TextToSpeech myTTS;

    public static final String MEAL_TO_GET = "com.google.android.gms.samples.vision.ocrreader.MEAL_TO_GET";

    private GestureDetector gestureDetector;

    private TextBlock selectedTextBlock = null;

    private RecognizedTextAdapter recognizedTextAdapter = null;

    TextServicesManager tsm;

    SpellCheckerSession session;

    public static String selected_meal = "";

    public static RecyclerView last_parent_di;

    //parent of last_parent

    public static RecyclerView parent;

@Override
    public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);

    RecyclerView last_parent_di = new RecyclerView(this);

    setContentView(R.layout.activity_image_detection);

    tsm = (TextServicesManager) getSystemService(TEXT_SERVICES_MANAGER_SERVICE);

    session = tsm.newSpellCheckerSession(null, Locale.ENGLISH, this, true);

    mGraphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.second_graphic_overlay);

    gestureDetector = new GestureDetector(this, new DetectImageActivity.CaptureGestureListener());

    //start text to speech
    Intent checkTTSIntent = new Intent();
    checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
    startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);


    Context context = getApplicationContext();

    //get image byte data
    Frame outputFrame = null;

    ImageView imageView = (ImageView) findViewById(R.id.image_to_detect);

    /*byte[] imageData = getIntent().getByteArrayExtra(OcrCaptureActivity.IMAGE_DATA_KEY);

    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

    Frame outputFrame = new Frame.Builder().setBitmap(bitmap).build();*/

    String fileName = getIntent().getStringExtra(OcrCaptureActivity.IMAGE_FILE_NAME);
    File file = new File(fileName);

    Uri uri = Uri.parse(fileName);



    //get image and convert to frame
    try{
        //InputStream inputStream = getAssets().open("farmhouse.png");

        InputStream inputStream = new FileInputStream(fileName);

        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

        Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

        Glide.with(this).load(file).into(imageView);

        //bitmap to frame
        //outputFrame = new Frame.Builder().setBitmap(bmp).build();

    }catch (IOException e){

        Log.e(LOG_TAG, e + "");

    }
    //detect image
    //TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
    //SparseArray<TextBlock> result = textRecognizer.detect(outputFrame);
    //textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlay));
    final SparseArray<TextBlock> result = OcrCaptureActivity.items;

    if (myTTS == null)
        myTTS = new TextToSpeech(this, this);

    //initialize adapter
    recognizedTextAdapter = new RecognizedTextAdapter(this, R.layout.horizontal_text);

    for (int i = 0; i < result.size(); i++){
        if (result.get(i) != null){
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, result.get(i));
            mGraphicOverlay.add(graphic);
            String textStr[] = result.get(i).getValue().split("\\r\\n|\\n|\\r");

            for(int j = 0; j < textStr.length; j++){

                String line_detected = textStr[j];
                //test for null string or empty string
                if (line_detected != null && line_detected != "" ){
                    recognizedTextAdapter.addItem(line_detected);
                    Log.d(LOG_TAG + "text lines: ",  " added item ");

                }
                Log.d(LOG_TAG + "text lines: ",  line_detected );
            }
            Log.d(LOG_TAG + " size : ", result.get(i).getValue() );
        }
    }


//test menu for development
    //test_di(recognizedTextAdapter);

    //back button

    ImageButton back_btn = (ImageButton) findViewById(R.id.back_btn_di);
    back_btn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    });

    ImageButton next_btn = (ImageButton) findViewById(R.id.next_btn_di);
    next_btn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent mealMenuActivity = new Intent(getApplicationContext(), MealMenuActivity.class);

            mealMenuActivity.putExtra(MEAL_TO_GET, selected_meal);
            startActivity(mealMenuActivity);
        }
    });

    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.detected_text_list_view);
    parent = recyclerView;


// apparently the recycler view does not work without setting up a layout manager
    LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false);
    recyclerView.setLayoutManager(layoutManager);



    recyclerView.setAdapter(recognizedTextAdapter);

    fetchSuggestionsFor("Peter livs in Brlin");

    }

    private void test_di(RecognizedTextAdapter recognizedTextAdapter){
        recognizedTextAdapter.addItem("Beer-Battered Wisconsin Cheese Curds");
        recognizedTextAdapter.addItem("Cinnamon Pecan Monkey Bread");
        recognizedTextAdapter.addItem("Bavarian Pretzel");
        recognizedTextAdapter.addItem("Norwegian Smoked Salmon");
        recognizedTextAdapter.addItem("Amish Chicken Soup");
        recognizedTextAdapter.addItem("Farm Green Salad");
        recognizedTextAdapter.addItem("Chopped Salad");
        recognizedTextAdapter.addItem("Farmer Breakfast");
        recognizedTextAdapter.addItem("Farm Eggs Benedict");
        recognizedTextAdapter.addItem("Spinach and Mighty Vine Tomato Frittata");
        recognizedTextAdapter.addItem("Brown Sugar Brioche French Toast");
        recognizedTextAdapter.addItem("Strauss Farms Grass Fed Steak and Farm Eggs");
        recognizedTextAdapter.addItem("White Breakfast Flatbread");
        recognizedTextAdapter.addItem("Veggie Burger");
        recognizedTextAdapter.addItem("Open Faced Breakfast Sandwich");
        recognizedTextAdapter.addItem("Heirloom Grains Breakfast Bowl");
        recognizedTextAdapter.addItem("Mimosa");
        recognizedTextAdapter.addItem("Farmhouse Bloody Mary");
        recognizedTextAdapter.addItem("Sparrow Coffee and Tea");
        recognizedTextAdapter.addItem("Mac and Cheese");


    }

    //checks whether the user has the TTS data installed. If it is not, the user will be prompted to install it.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                myTTS = new TextToSpeech(this, this);
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }
    @Override
    public void onInit(int initStatus){
        //initialize firebase
        FirebaseApp.initializeApp(this);
        if (initStatus == TextToSpeech.SUCCESS) {
            myTTS.setLanguage(Locale.US);
            myTTS.setSpeechRate(0.6f);
        }

    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (myTTS != null) {
            myTTS.stop();
            myTTS.shutdown();
        }
        super.onDestroy();
    }

    /*classes and functions for gesture activities for
    *
    **/

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        boolean c = gestureDetector.onTouchEvent(e);

        return c || super.onTouchEvent(e);
    }


    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    /**
     * onTap is called to capture the first TextBlock under the tap location and return it to
     * the Initializing Activity.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the activity is ending.
     */
    private boolean onTap(float rawX, float rawY) {
        final OcrGraphic graphic = mGraphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        if (graphic != null) {
            text = graphic.getTextBlock();
            final TextBlock final_text = text;
            if (text != null && text.getValue() != null) {
                String text_block_content = "";
                text_block_content = text.getValue();
                selectedTextBlock = text;
                //Rect rect = text.getBoundingBox();
                graphic.setsRectPaint(Color.RED);

                myTTS.speak(text_block_content, TextToSpeech.QUEUE_FLUSH, null);

            }
            else {
                Log.d(LOG_TAG, "text data is null");
            }
        }
        else {
            Log.d(LOG_TAG,"no text detected");
        }
        return text != null;
    }

    @Override
    public void onGetSuggestions(SuggestionsInfo[] results) {
        final StringBuffer sb = new StringBuffer("");
        for(SuggestionsInfo result:results){
            int n = result.getSuggestionsCount();
            for(int i=0; i < n; i++){
                int m = result.getSuggestionsCount();
                sb.append(result.getSuggestionAt(i));
            }
        }
        Log.d(LOG_TAG, " spell-checker "+ sb.length() + " sb length ");

        for (int i = 0; i < sb.length(); i++){
            Log.d(LOG_TAG, " spell-checker "+ sb.substring(i) + i);

        }
    }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {
        final StringBuffer sb = new StringBuffer("");
        for(SentenceSuggestionsInfo result:results){
            int n = result.getSuggestionsCount();
            for(int i=0; i < n; i++){
                int m = result.getSuggestionsInfoAt(i).getSuggestionsCount();

                for(int k=0; k < m; k++) {
                    sb.append(result.getSuggestionsInfoAt(i).getSuggestionAt(k));
                            //.append("\n");
                }
                //sb.append("\n");
            }
        }
        Log.d(LOG_TAG, " spell-checker "+ sb.length() + " sb length ");

        for (int i = 0; i < sb.length(); i++){
            Log.d(LOG_TAG, " spell-checker "+ sb.substring(i) + i);

        }

    }
@Override
public void onResume(){
        super.onResume();
// start spell checker service
    tsm = (TextServicesManager) getSystemService(TEXT_SERVICES_MANAGER_SERVICE);

    session = tsm.newSpellCheckerSession(null, Locale.ENGLISH, this, true);

}

public void fetchSuggestionsFor(String sentence){

        TextInfo textInfo = new TextInfo(sentence);

        TextInfo [] textInfos = {textInfo};
        //Log.d(LOG_TAG, " spell-checker "+ Locale.getDefault().getDisplayLanguage());
        session.getSentenceSuggestions(textInfos, 5);
        //session.getSuggestions(textInfo, 5);

    }

}
