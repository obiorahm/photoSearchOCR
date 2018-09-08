package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
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
import android.widget.ProgressBar;
import android.widget.TextView;


import com.google.android.gms.samples.vision.ocrreader.Adapter.RecognizedTextAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RecyclerWordAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.WordAdapter;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Created by mgo983 on 8/17/18.
 */

public class DetectImageActivity extends UseRecyclerActivity implements TextToSpeech.OnInitListener {

    public static String LOG_TAG = DetectImageActivity.class.getSimpleName();

    public static String newline = System.getProperty("line.separator");


    private GraphicOverlayFB<OcrGraphicFB> mGraphicOverlayFB;

    private ImageViewPreview imageViewPreview;
    //Text to speech variables
    private int MY_DATA_CHECK_CODE = 0;
    public TextToSpeech myTTS;

    public static final String MEAL_TO_GET = "com.google.android.gms.samples.vision.ocrreader.MEAL_TO_GET";

    private GestureDetector gestureDetector;

    private RecognizedTextAdapter recognizedTextAdapter = null;


    public static String selected_meal = "";

    public static RecyclerView last_parent_di;

    public static RecyclerView parent;


    private float NORMAL_STROKE_WIDTH = 4.0f;
    private float SELECTED_STROKE_WIDTH = 7.0f;



    /*
     * selected graphic so that only one textbox is selected at a time
     * new text blocks override old ones
     * */

    private OcrGraphicFB selectedGraphic;
    private FirebaseVisionText.TextBlock newTextBlock;
    private String currentLineSelection = null;

    private ArrayList<OcrGraphicFB> graphics = new ArrayList<>();


@Override
    public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);

    RecyclerView last_parent_di = new RecyclerView(this);

    setContentView(R.layout.activity_image_detection);

    imageViewPreview =  findViewById(R.id.image_view_preview);

    mGraphicOverlayFB =  findViewById(R.id.second_graphic_overlay);

    gestureDetector = new GestureDetector(this, new DetectImageActivity.CaptureGestureListener());

    //start text to speech
    Intent checkTTSIntent = new Intent();
    checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
    startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);


    Context context = getApplicationContext();



    final ImageView imageView = findViewById(R.id.image_to_detect);


    String fileName = getIntent().getStringExtra(OcrCaptureActivity.IMAGE_FILE_NAME);

    /*Uri uri = Uri.parse(fileName);*/

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


        imageViewPreview.setmBitmap(bmp);
        imageView.setImageBitmap(bmp);

        firebaseTextDetection(/*uri,*/ bmp);
        imageViewPreview.setmGraphicOverlay(mGraphicOverlayFB);


    }catch (IOException e){

        Log.e(LOG_TAG, e + "");

    }



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
            // remove red items and blue items
            // remove blocks and lines
            remove_all_lines();
            remove_all_blocks();

            // reset public variables
            newTextBlock = null;
            selectedGraphic = null;
            currentLineSelection = null;

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

    ImageButton imageButtonClearOnSiteRecycler = findViewById(R.id.cancel_gridview_edit_meal);
    imageButtonClearOnSiteRecycler.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            takeDownRecyclerView();
        }
    });


    ImageButton imageButtonGroupLines = findViewById(R.id.group_lines);
    imageButtonGroupLines.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            group_selected_lines();
        }
    });


    ImageButton imageButtonSeeSelection = findViewById(R.id.see_selection);
    imageButtonSeeSelection.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SeeSelection();
        }
    });



    recyclerView.setAdapter(recognizedTextAdapter);


    }

    private void SeeSelection(){
        if (currentLineSelection != null){
            /*feedback for selected graphic
            selectedGraphic.setRecPaintStrokeWidth(SELECTED_STROKE_WIDTH);
            selectedGraphic.setsRectPaint(R.color.teal);*/

            setUpRecyclerView(currentLineSelection);
        }
    }

    private void group_selected_lines(){
        //replace lines in current block
        replace_lines_in_curr_block();


        //draw on the overlay a block selector
        draw_block();
        //hide line selectors

        hide_lines_in_block();

        //set currentLineSelection to title meal
        set_selected_title();
        //create meal adapter

        imageViewPreview.setmGraphicOverlay(mGraphicOverlayFB);


    }


    private void set_selected_title(){
        if (newTextBlock != null){
            currentLineSelection = newTextBlock.getLines().get(0).getText();
            Log.d("current selection 2", currentLineSelection);
        }
    }

    private void draw_block(){

        if (graphics.size() > 0){
            FirebaseVisionText.Line firstLine = graphics.get(0).getLine();
            if (firstLine == null)
                return;
            Rect rect = firstLine.getBoundingBox();
            if (rect == null)
                return;
            int min_rect_top = rect.top;
            int min_rect_left = rect.left;
            int max_rect_bottom = rect.bottom;
            int max_rect_right = rect.right;

            Rect new_rect = new Rect();
            ArrayList<FirebaseVisionText.Line> line_array = new ArrayList<FirebaseVisionText.Line>();

            // variables for combining string
            ArrayList<Integer> rect_top_values = new ArrayList<>();
            HashMap<Integer, String> line_texts = new HashMap<>();


            for (OcrGraphicFB child : graphics){
                FirebaseVisionText.Line curr_line = child.getLine();
                Rect curr_rect = curr_line.getBoundingBox();
                if (curr_rect.top < min_rect_top)
                    min_rect_top = curr_rect.top;

                if (curr_rect.left < min_rect_left)
                    min_rect_left = curr_rect.left;

                if (curr_rect.bottom > max_rect_bottom)
                    max_rect_bottom = curr_rect.bottom;

                if (curr_rect.right > max_rect_right)
                    max_rect_right = curr_rect.right;

                line_array.add(child.getLine());

                rect_top_values.add(curr_rect.top);
                line_texts.put(curr_rect.top, curr_line.getText());

            }

            new_rect.right = max_rect_right;
            new_rect.bottom = max_rect_bottom;
            new_rect.left = min_rect_left;
            new_rect.top = min_rect_top;

            newTextBlock = new FirebaseVisionText.TextBlock(
                    combineText(rect_top_values, line_texts),
                    new_rect,
                    firstLine.getRecognizedLanguages(),
                    line_array,
                    firstLine.getConfidence()
                    );
            //clear current graphic
            if (selectedGraphic != null)
                mGraphicOverlayFB.remove(selectedGraphic);

            selectedGraphic = new OcrGraphicFB(mGraphicOverlayFB, newTextBlock);
            selectedGraphic.setsRectPaint(Color.GREEN);
            selectedGraphic.setRecPaintStrokeWidth(SELECTED_STROKE_WIDTH);
            mGraphicOverlayFB.add(selectedGraphic);
            //imageViewPreview.setmGraphicOverlay(mGraphicOverlayFB);


        }

    }



    /**
     * now hide the lines in the block
     */

    private void hide_lines_in_block(){
        if (graphics.size() > 0){
            for (OcrGraphicFB graphicFB : graphics){
                mGraphicOverlayFB.remove(graphicFB);
            }
            //graphics.clear();

        }
    }


    /**
     * remove all selected lines from the overlay
     */
    private void remove_all_lines(){
        try{
            for(OcrGraphicFB graphic : graphics){
                graphic.setsRectPaint(Color.WHITE);
                graphic.setRecPaintStrokeWidth(NORMAL_STROKE_WIDTH);
                recognizedTextAdapter.removeItem(graphic.getLine().getText());
            }
            graphics.clear();
        }catch (NullPointerException e){
            Log.e(LOG_TAG, e + " ");
        }

    }


    /**
     * remove all selected blocks from the overlay and restore unselected lines
     */
    private void  remove_all_blocks(){
        //if (selectedGraphic != null) {

            //first replace the lines in the currently selected graphic
            replace_lines_in_curr_block();
        //}

    }


    /***
     * replace lines in the current selected block before selecting another block
     */

    private void replace_lines_in_curr_block(){
        if (selectedGraphic == null)
            return;
        FirebaseVisionText.TextBlock block = selectedGraphic.getTextBlock();
        if (block == null)
            return;

        if (newTextBlock != null){
            List<FirebaseVisionText.Line> lines = newTextBlock.getLines();
            for (FirebaseVisionText.Line line : lines){
                OcrGraphicFB new_line = new OcrGraphicFB(mGraphicOverlayFB, line);
                //OcrGraphicFB new_line = getGraphic_to_remove(line);
                mGraphicOverlayFB.add(new_line);

                //they are not currently selected so remove from current selection
                //since objects are being unselected remove them from the adapter
                recognizedTextAdapter.removeItem(line.getText());
                graphics.remove(getGraphic_to_remove(line));
            }
        }

        if (selectedGraphic != null){
            mGraphicOverlayFB.remove(selectedGraphic);
            selectedGraphic = null;
        }
    }


    /**
     * get the OcrGraphic to remove from graphic based on the line parameter
     * @param line is the current line
     * @return returns an OrcGraphic that has the same line as line
     */
    private OcrGraphicFB getGraphic_to_remove(FirebaseVisionText.Line line){
        for (OcrGraphicFB graphicFB : graphics){
            FirebaseVisionText.Line curr_line = graphicFB.getLine();
            if (curr_line != null){
                if (curr_line == line)
                    return graphicFB;
            }

        }
        return null;
    }




    /***
     *Since we are collecting lines ontap, they may not be ordered according to occurence on the text
     * We combine the text in each line according to the values of their bounding boxes
     * @param rect_top_values get the topvalues of all rects
     * @param string_values and the string values of all the lines
     * @return returning first text for now
     */

    private String combineText(ArrayList<Integer> rect_top_values, HashMap<Integer, String> string_values){
    //String finalString = "";
        Collections.sort(rect_top_values);
        /*for (Integer child : rect_top_values){
            finalString += string_values.get(child) + newline;
        }*/

        return string_values.get(rect_top_values.get(0));

    }

    private void imageMode(ImageView imageView, RecyclerView recyclerView){
        recyclerView.setVisibility(View.GONE);
        imageViewPreview.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        mGraphicOverlayFB.setVisibility(View.VISIBLE);

        ImageButton imageButton = findViewById(R.id.see_selection);
        imageButton.setVisibility(View.VISIBLE);

        ImageButton imageButtonGroupLines = findViewById(R.id.group_lines);
        imageButtonGroupLines.setVisibility(View.VISIBLE);


    }

    private void listMode(ImageView imageView, RecyclerView recyclerView){
        recyclerView.setVisibility(View.VISIBLE);
        imageViewPreview.setVisibility(View.GONE);
        imageView.setVisibility(View.INVISIBLE);
        mGraphicOverlayFB.setVisibility(View.GONE);

        ImageButton imageButton = findViewById(R.id.see_selection);
        imageButton.setVisibility(View.GONE);

        ImageButton imageButtonGroupLines = findViewById(R.id.group_lines);
        imageButtonGroupLines.setVisibility(View.GONE);

    }


    /**
     * Firebase Detection
     * @param bmp the bitmap to detect text in
     */
    private void firebaseTextDetection(/*Uri uri,*/ Bitmap bmp){

        try {

            //InputStream inputStream = getResources().getAssets().open("farmhouse.png");
            //BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            //Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bmp);


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

                                for (FirebaseVisionText.Line line: block.getLines()) {

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
    int counter = 0;
    private boolean onTap(float rawX, float rawY) {

        final OcrGraphicFB graphic = mGraphicOverlayFB.getGraphicAtLocation(rawX, rawY);
        FirebaseVisionText.Line line = null;
        FirebaseVisionText.TextBlock block = null;
        if (graphic != null) {
            line = graphic.getLine();
            block = graphic.getTextBlock();
            Log.d("graphic", "just got touched " + ++counter + (line == null));
            //final FirebaseVisionText.Line final_text = line;
            if (line != null && line.getText()!= null) {
                String text_line_content;
                text_line_content = line.getText();
                //selectedTextBlock = line;

                if (graphic.getsRectPaint() == Color.WHITE){
                    graphic.setsRectPaint(Color.GREEN);
                    graphic.setRecPaintStrokeWidth(SELECTED_STROKE_WIDTH);
                    recognizedTextAdapter.addItem(text_line_content);
                    // track tapped graphics
                    graphics.add(graphic);
                    currentLineSelection = text_line_content;

                    //reset previous selectedGraphic
                    reset_last_selected();
                    //keep track of last selected graphic
                    selectedGraphic = graphic;
                }else{
                    graphic.setsRectPaint(Color.WHITE);
                    graphic.setRecPaintStrokeWidth(NORMAL_STROKE_WIDTH);

                    graphics.remove(graphic);
                    recognizedTextAdapter.removeItem(text_line_content);
                    //takeDownRecyclerView();

                    // reset global variables
                    if (graphic == selectedGraphic){
                        selectedGraphic = null;
                        currentLineSelection = null;
                    }

                }

                myTTS.speak(text_line_content, TextToSpeech.QUEUE_FLUSH, null);

            }
            else if (block != null && block.getText() != null) {
                processTextBlock(graphic);
            }else{
                Log.d(LOG_TAG, "text data is null");

            }
        }
        else {
            Log.d(LOG_TAG,"no text detected");
        }
        takeDownRecyclerView();
        return line != null;
    }


    private void reset_last_selected(){
        if (selectedGraphic != null){
            FirebaseVisionText.Line line = selectedGraphic.getLine();
            FirebaseVisionText.TextBlock block = selectedGraphic.getTextBlock();

            if (line == null && block != null){
                replace_lines_in_curr_block();
            }else{
                selectedGraphic.setRecPaintStrokeWidth(SELECTED_STROKE_WIDTH);
                selectedGraphic.setsRectPaint(Color.RED);
            }
        }
    }

    /**
     * when a block is tapped do the following
     * currentline selection holds what is assumed to be the food title of the whole selection
     * current line selection to be retrieved in the recycler;
     * @param graphic is the tapped text block
     */
    private void processTextBlock(OcrGraphicFB graphic){

        FirebaseVisionText.TextBlock block = graphic.getTextBlock();

        currentLineSelection =  block.getLines().get(0).getText();

        String text_block_content = block.getText();
        myTTS.speak(text_block_content, TextToSpeech.QUEUE_FLUSH, null);

        Log.d("current selection ", currentLineSelection);


    }

@Override
public void onResume(){
        super.onResume();

}

@Override
public void setView(RecyclerWordAdapter adapter, RecyclerView recyclerView){
    LinearLayoutManager layoutManager= new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);

    //RecyclerView recyclerView = (RecyclerView) findViewById(R.id.gridview_edit_meal);
    recyclerView.setVisibility(View.VISIBLE);

}





    public void setUpRecyclerView(String mealText){

        mealText = mealText.replaceAll("[0-9]","");

        RecyclerWordAdapter recyclerWordAdapter = new RecyclerWordAdapter(this, R.layout.gridview_item, myTTS, mealText);

        FetchMealDetails fetchMealDetails = new FetchMealDetails(recyclerWordAdapter, this);
        fetchMealDetails.execute(mealText);

        ImageButton imageButtonclearRecycler = findViewById(R.id.cancel_gridview_edit_meal);
        imageButtonclearRecycler.setVisibility(View.VISIBLE);

    }


    public void takeDownRecyclerView(){

        //clear the recycler from the page
        RecyclerView recyclerView = findViewById(R.id.gridview_edit_meal);
        recyclerView.setVisibility(View.GONE);

        //clear textview if no results
        TextView textViewNoResult = findViewById(R.id.no_result);
        textViewNoResult.setVisibility(View.GONE);

        ImageButton imageButtonclearRecycler = findViewById(R.id.cancel_gridview_edit_meal);
        imageButtonclearRecycler.setVisibility(View.GONE);

    }


}
