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
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;


import com.google.android.gms.samples.vision.ocrreader.Adapter.RecognizedTextAdapter;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlayFB;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.ImageViewPreview;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Created by mgo983 on 8/17/18.
 */

public class DetectImageActivity extends Activity implements TextToSpeech.OnInitListener, SpellCheckerSession.SpellCheckerSessionListener {

    String LOG_TAG = DetectImageActivity.class.getSimpleName();

    //private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private GraphicOverlayFB<OcrGraphicFB> mGraphicOverlayFB;

    //private CameraSourcePreview cameraSourcePreview;
    private ImageViewPreview imageViewPreview;
    //Text to speech variables
    private int MY_DATA_CHECK_CODE = 0;
    public TextToSpeech myTTS;

    public static final String MEAL_TO_GET = "com.google.android.gms.samples.vision.ocrreader.MEAL_TO_GET";

    private GestureDetector gestureDetector;

    //private FirebaseVisionText.Line selectedTextBlock = null;

    private RecognizedTextAdapter recognizedTextAdapter = null;

    TextServicesManager tsm;

    SpellCheckerSession session;

    public static String selected_meal = "";

    public static RecyclerView last_parent_di;


    //parent of last_parent

    public static RecyclerView parent;

    //public static String LOG_TAG  = DetectImageActivity.class.getSimpleName();

    //track graphic

    private ArrayList<OcrGraphicFB> graphics = new ArrayList<>();

    private float NORMAL_STROKE_WIDTH = 4.0f;
    private float SELECTED_STROKE_WIDTH = 7.0f;

@Override
    public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);

    RecyclerView last_parent_di = new RecyclerView(this);

    setContentView(R.layout.activity_image_detection);

    tsm = (TextServicesManager) getSystemService(TEXT_SERVICES_MANAGER_SERVICE);

    session = tsm.newSpellCheckerSession(null, Locale.ENGLISH, this, true);

    imageViewPreview =  findViewById(R.id.image_view_preview);

    //mGraphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.second_graphic_overlay);
    mGraphicOverlayFB =  findViewById(R.id.second_graphic_overlay);

    gestureDetector = new GestureDetector(this, new DetectImageActivity.CaptureGestureListener());

    //start text to speech
    Intent checkTTSIntent = new Intent();
    checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
    startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);


    Context context = getApplicationContext();

    //get image byte data
    //Frame outputFrame = null;

    final ImageView imageView = findViewById(R.id.image_to_detect);

    /*byte[] imageData = getIntent().getByteArrayExtra(OcrCaptureActivity.IMAGE_DATA_KEY);

    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

    Frame outputFrame = new Frame.Builder().setBitmap(bitmap).build();*/

    String fileName = getIntent().getStringExtra(OcrCaptureActivity.IMAGE_FILE_NAME);
    //File file = new File(fileName);

    Uri uri = Uri.parse(fileName);

    if (myTTS == null)
        myTTS = new TextToSpeech(this, this);

    //initialize adapter
    recognizedTextAdapter = new RecognizedTextAdapter(this, R.layout.horizontal_text);




    //get image and convert to frame
    try{
        //InputStream inputStream = getAssets().open("farmhouse.png");

        InputStream inputStream = new FileInputStream(fileName);

        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

        Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

        //Glide.with(this).load(file).into(imageView);

        imageViewPreview.setmBitmap(bmp);
        imageView.setImageBitmap(bmp);

        //bitmap to frame
        //outputFrame = new Frame.Builder().setBitmap(bmp).build();


        firebaseTextDetection(uri, bmp, recognizedTextAdapter);
        imageViewPreview.setmGraphicOverlay(mGraphicOverlayFB);


    }catch (IOException e){

        Log.e(LOG_TAG, e + "");

    }
    //detect image
    //TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
    //SparseArray<TextBlock> result = textRecognizer.detect(outputFrame);
    //textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlay));
    //final SparseArray<TextBlock> result = OcrCaptureActivity.items;


    /*for (int i = 0; i < result.size(); i++){
        if (result.get(i) != null){
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, result.get(i));
            mGraphicOverlay.add(graphic);
            //regular expressions to catch spaces, tabs, commas and full stops
            String textStr[] = result.get(i).getValue().split("\\r\\n|\\n|\\r|\\.|\\,");

            for(int j = 0; j < textStr.length; j++){

                String line_detected = textStr[j];
                //test for null string or empty string
                if (line_detected != null && line_detected != "" ){
                    // replace & with "and"
                    line_detected = line_detected.replace("&", "and");
                    //recognizedTextAdapter.addItem(line_detected);
                    Log.d(LOG_TAG + "text lines: ",  " added item ");

                }
                Log.d(LOG_TAG + "text lines: ",  line_detected );
            }
            Log.d(LOG_TAG + " size : ", result.get(i).getValue() );
        }
    }*/


//test menu for development
    //test_di(recognizedTextAdapter);



    final RecyclerView recyclerView = findViewById(R.id.detected_text_list_view);
    parent = recyclerView;


// apparently the recycler view does not work without setting up a layout manager
    LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false);
    recyclerView.setLayoutManager(layoutManager);




    final CompoundButton togglelistImageOn =  findViewById(R.id.switch_view);
    togglelistImageOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b){
                    imageMode(imageView, recyclerView);
            }else{
                    listMode(imageView, recyclerView);
            }
        }
    });

    ImageView imageViewList = findViewById(R.id.list_mode);
    imageViewList.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            listMode(imageView, recyclerView);
            togglelistImageOn.setChecked(false);
        }
    });

    ImageView imageViewImage = findViewById(R.id.image_mode);
    imageViewImage.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            imageMode(imageView, recyclerView);
            togglelistImageOn.setChecked(true);
        }
    });

    ImageView imageViewClear = findViewById(R.id.clear_overlay);
    imageViewClear.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(LOG_TAG,"length of graphic " + graphics.size());
            for(OcrGraphicFB graphic : graphics){
                graphic.setsRectPaint(Color.WHITE);
                graphic.setRecPaintStrokeWidth(NORMAL_STROKE_WIDTH);
                recognizedTextAdapter.removeItem(graphic.getLine().getText());;
            }
        }
    });

    //back button

    ImageButton back_btn =  findViewById(R.id.back_btn_di);
    back_btn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (togglelistImageOn.isChecked()){
                finish();
            }else{
                imageMode(imageView, recyclerView);
                togglelistImageOn.setChecked(true);
            }
        }
    });

    ImageButton next_btn =  findViewById(R.id.next_btn_di);
    next_btn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (togglelistImageOn.isChecked()){
                listMode(imageView, recyclerView);
                togglelistImageOn.setChecked(false);
            }else{
                Intent mealMenuActivity = new Intent(getApplicationContext(), MealMenuActivity.class);
                mealMenuActivity.putExtra(MEAL_TO_GET, selected_meal);
                startActivity(mealMenuActivity);
            }
        }
    });



    recyclerView.setAdapter(recognizedTextAdapter);

    fetchSuggestionsFor("Peter livs in Brlin");

    }


    private void imageMode(ImageView imageView, RecyclerView recyclerView){
        recyclerView.setVisibility(View.GONE);
        imageViewPreview.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        mGraphicOverlayFB.setVisibility(View.VISIBLE);

    }

    private void listMode(ImageView imageView, RecyclerView recyclerView){
        recyclerView.setVisibility(View.VISIBLE);
        imageViewPreview.setVisibility(View.GONE);
        imageView.setVisibility(View.INVISIBLE);
        mGraphicOverlayFB.setVisibility(View.GONE);
    }

/*orc detection()
    private void ocrDetect(Frame frame, final RecognizedTextAdapter recognizedTextAdapter){
        TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();
        SparseArray<TextBlock> result = textRecognizer.detect(frame);
        for (int i = 0; i < result.size(); i++){
            TextBlock block = result.get(i);
            block.getComponents();
        }

    }*/
// Firebase  detection
    private void firebaseTextDetection(Uri uri, Bitmap bmp, final RecognizedTextAdapter recognizedTextAdapter){

        try {

            //InputStream inputStream = getResources().getAssets().open("farmhouse.png");
            //BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            //Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);

            //FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(doBrightness(bmp, 10));

            //FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(this, uri);

            FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();

            textRecognizer.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText result) {
                            // Task completed successfully
                            // ...
                            //String resultText = result.getText();
                            for (FirebaseVisionText.TextBlock block: result.getTextBlocks()) {
                                /*String blockText = block.getText();


                                Float blockConfidence = block.getConfidence();
                                List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                                Point[] blockCornerPoints = block.getCornerPoints();
                                Rect blockFrame = block.getBoundingBox();*/
                                for (FirebaseVisionText.Line line: block.getLines()) {
                                    /*String lineText = line.getText();

                                    Float lineConfidence = line.getConfidence();
                                    List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                                    Point[] lineCornerPoints = line.getCornerPoints();
                                    Rect lineFrame = line.getBoundingBox();*/

                                    OcrGraphicFB graphic = new OcrGraphicFB(mGraphicOverlayFB, line);
                                    mGraphicOverlayFB.add(graphic);

                                }
                            }
                        }
                    })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                    // ...
                                }
                            });
        } catch (NullPointerException e){
            /*catch (IOException e) {
            e.printStackTrace();
        }*/
        }/*catch (IOException e){
            e.printStackTrace();
        }*/




    }




/*    private void test_di(RecognizedTextAdapter recognizedTextAdapter){
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


    }*/

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
        final OcrGraphicFB graphic = mGraphicOverlayFB.getGraphicAtLocation(rawX, rawY);
        FirebaseVisionText.Line line = null;
        if (graphic != null) {
            line = graphic.getLine();
            //final FirebaseVisionText.Line final_text = line;
            if (line != null && line.getText()!= null) {
                String text_line_content;
                text_line_content = line.getText();
                //selectedTextBlock = line;

                if (graphic.getsRectPaint() == Color.WHITE){
                    graphic.setsRectPaint(Color.RED);
                    graphic.setRecPaintStrokeWidth(SELECTED_STROKE_WIDTH);
                    recognizedTextAdapter.addItem(text_line_content);
                    // track tapped graphics
                    graphics.add(graphic);

                }else{
                    recognizedTextAdapter.removeItem(text_line_content);
                    graphic.setsRectPaint(Color.WHITE);
                    graphic.setRecPaintStrokeWidth(NORMAL_STROKE_WIDTH);
                    graphics.remove(graphic);

                }

                myTTS.speak(text_line_content, TextToSpeech.QUEUE_FLUSH, null);

            }
            else {
                Log.d(LOG_TAG, "text data is null");
            }
        }
        else {
            Log.d(LOG_TAG,"no text detected");
        }
        return line != null;
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

    //increase image brightness

    public static Bitmap doBrightness(Bitmap src, int value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();

        Log.d("static_Log_tag", " image width " + width + " image height " + height);

        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // increase/decrease each channel
                R += value;
                if(R > 255) { R = 255; }
                else if(R < 0) { R = 0; }

                G += value;
                if(G > 255) { G = 255; }
                else if(G < 0) { G = 0; }

                B += value;
                if(B > 255) { B = 255; }
                else if(B < 0) { B = 0; }

                // apply new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }
}
