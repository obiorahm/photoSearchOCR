package com.google.android.gms.samples.vision.ocrreader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.google.android.gms.samples.vision.ocrreader.Adapter.RecognizedTextAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RecyclerWordAdapter;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlayFB;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.ImageViewPreview;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by mgo983 on 8/17/18.
 */

public class DetectImageActivity extends UseRecyclerActivity implements TextToSpeech.OnInitListener {

    public static String LOG_TAG = DetectImageActivity.class.getSimpleName();

    private GraphicOverlayFB<OcrGraphicFB> mGraphicOverlayFB;


    private ImageViewPreview imageViewPreview;
    //Text to speech variables
    private int MY_DATA_CHECK_CODE = 0;
    //public TextToSpeech myTTS;

    public static final String MEAL_TO_GET = "com.google.android.gms.samples.vision.ocrreader.MEAL_TO_GET";

    private GestureDetector gestureDetector;

    private RecognizedTextAdapter recognizedTextAdapter = null;

    /*
    public static String selected_meal = "";

    public static RecyclerView last_parent_di;
    */

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

    private ArrayList<OcrGraphicFB> graphicDraws = new ArrayList<>();


@Override
    public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);

    //RecyclerView last_parent_di = new RecyclerView(this);

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

    /*if (myTTS == null)
        myTTS = new TextToSpeech(this, this);*/

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
    togglelistImageOn.setOnCheckedChangeListener((CompoundButton compoundButton, boolean b) ->{
        if (b){
            imageMode(imageView, recyclerView);
        }else{
            listMode(imageView, recyclerView);
        }
    });

    ImageView imageViewList = findViewById(R.id.list_mode);
    imageViewList.setOnClickListener((View view) ->{
        listMode(imageView, recyclerView);
        togglelistImageOn.setChecked(false);
    });

    ImageView imageViewImage = findViewById(R.id.image_mode);
    imageViewImage.setOnClickListener((View view) ->{
        imageMode(imageView, recyclerView);
        togglelistImageOn.setChecked(true);
    });

    ImageView imageViewClear = findViewById(R.id.clear_overlay);
    imageViewClear.setOnClickListener((View view) ->{
        Log.d(LOG_TAG,"length of graphic " + graphics.size());
        // remove red items and blue items
        // remove blocks and lines
        remove_all_lines();
        remove_all_blocks();
        removePath();

        // reset public variables
        newTextBlock = null;
        selectedGraphic = null;
        currentLineSelection = null;
        takeDownRecyclerView();

    });

    //back button

    ImageButton back_btn =  findViewById(R.id.back_btn_di);
    back_btn.setOnClickListener((View view) ->{
        if (togglelistImageOn.isChecked()){
            finish();
        }else{
            imageMode(imageView, recyclerView);
            togglelistImageOn.setChecked(true);
        }
    });

    ImageButton next_btn =  findViewById(R.id.next_btn_di);
    next_btn.setOnClickListener((View view) ->{

        if(!(togglelistImageOn.isChecked())){
            Intent mealMenuActivity = new Intent(getApplicationContext(), MealMenuActivity.class);
            mealMenuActivity.putExtra(MEAL_TO_GET, currentLineSelection);
            startActivity(mealMenuActivity);
        }

    });

    ImageButton imageButtonClearOnSiteRecycler = findViewById(R.id.cancel_gridview_edit_meal);
    imageButtonClearOnSiteRecycler.setOnClickListener((View view) ->{
        takeDownRecyclerView();
        speak("clear");
    });


    ImageButton imageButtonGroupLines = findViewById(R.id.group_lines);
    imageButtonGroupLines.setOnClickListener((View view) ->{
        group_selected_lines();
        takeDownRecyclerView();
        speak("group");
    });


    ImageButton imageButtonSeeSelection = findViewById(R.id.see_selection);
    imageButtonSeeSelection.setOnClickListener((View view) ->{
        if (currentLineSelection != null){
            //setUpRecyclerView(currentLineSelection, getBlockOrText());
            setUpRecyclerView(ocrGraphicGetText(), getBlockOrText());
            speak("see photo");

        }
    });


    ImageButton imageButtonMakeOrder = findViewById(R.id.make_order);
    imageButtonMakeOrder.setOnClickListener((View view) ->{
        ProgressBar progressBar = findViewById(R.id.dialog_progress);
        progressBar.setVisibility(View.VISIBLE);
        makeOrder();
        speak("order");

    });

    ImageButton imageButtonMakeSquare = findViewById(R.id.make_square);
    imageButtonMakeSquare.setOnClickListener((View view) ->{
        graphicDraw.updatePath(null, mX,mY);
        graphicDraw.postInvalidate();
    });


    recyclerView.setAdapter(recognizedTextAdapter);


    }

    private void speak(String toSpeak){
        myTTS.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }


    private void makeOrder(){
        if (currentLineSelection != null){

            String mealText = currentLineSelection.replaceAll("[0-9]","");

            FetchMealDetails fetchMealDetails = new FetchMealDetails(this, ocrGraphicGetText());
            fetchMealDetails.execute(mealText);
        }
    }

    private String ocrGraphicGetText(){
        String finalText = "";
        if (selectedGraphic == null)
            return finalText;
        FirebaseVisionText.TextBlock block = selectedGraphic.getTextBlock();
        FirebaseVisionText.Line line = selectedGraphic.getLine();
        if (block != null)
            finalText = block.getText();
        else if (line != null)
            finalText = line.getText();
        return finalText;
    }


    private boolean getBlockOrText(){
        if (selectedGraphic != null)
            return (selectedGraphic.getTextBlock() != null);
        return false;
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
        try{
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
                //mGraphicOverlayFB.addXPosition(selectedGraphic.)
                //imageViewPreview.setmGraphicOverlay(mGraphicOverlayFB);


            }

        }catch (NullPointerException e){
            Log.d(LOG_TAG, e + "");
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


    private void removePath(){

        for (OcrGraphicFB graphicFB : graphicDraws){
            mGraphicOverlayFB.remove(graphicFB);
        }
        mPath = null;
        //graphicDraw.updatePath(mPath);
    }


    /***
     *Since we are collecting lines ontap, they may not be ordered according to occurence on the text
     * We combine the text in each line according to the values of their bounding boxes
     * @param rect_top_values get the topvalues of all rects
     * @param string_values and the string values of all the lines
     * @return returning first text for now
     */

    private String combineText(ArrayList<Integer> rect_top_values, HashMap<Integer, String> string_values){
        Collections.sort(rect_top_values);

        StringBuilder finalString_sb = new StringBuilder();
        finalString_sb.append(string_values.get(rect_top_values.get(0)));
        finalString_sb.append(", ");

        for (int i = 1; i < rect_top_values.size(); i++){
            finalString_sb.append(string_values.get(rect_top_values.get(i)));
            finalString_sb.append(" ");
        }

        return finalString_sb.toString();

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

        ImageButton imageButtonMakeOrder = findViewById(R.id.make_order);
        imageButtonMakeOrder.setVisibility(View.VISIBLE);

        ImageButton imageButtonClearOverlay = findViewById(R.id.clear_overlay);
        imageButtonClearOverlay.setVisibility(View.VISIBLE);

        TextView textViewGroupLines = findViewById(R.id.group_lines_text);
        textViewGroupLines.setVisibility(View.VISIBLE);

        TextView textViewPhoto = findViewById(R.id.see_selection_text);
        textViewPhoto.setVisibility(View.VISIBLE);

        TextView textViewOrder = findViewById(R.id.make_order_text);
        textViewOrder.setVisibility(View.VISIBLE);

        TextView textViewClearSelection = findViewById(R.id.clear_overlay_text);
        textViewClearSelection.setVisibility(View.VISIBLE);


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

        ImageButton imageButtonMakeOrder = findViewById(R.id.make_order);
        imageButtonMakeOrder.setVisibility(View.GONE);

        ImageButton imageButtonClearOverlay = findViewById(R.id.clear_overlay);
        imageButtonClearOverlay.setVisibility(View.GONE);

        TextView textViewGroupLines = findViewById(R.id.group_lines_text);
        textViewGroupLines.setVisibility(View.GONE);

        TextView textViewPhoto = findViewById(R.id.see_selection_text);
        textViewPhoto.setVisibility(View.GONE);

        TextView textViewOrder = findViewById(R.id.make_order_text);
        textViewOrder.setVisibility(View.GONE);

        TextView textViewClearSelection = findViewById(R.id.clear_overlay_text);
        textViewClearSelection.setVisibility(View.GONE);

    }


    /**
     * Firebase Detection
     * @param bmp the bitmap to detect text in
     */
    private void firebaseTextDetection(/*Uri uri,*/ Bitmap bmp){

        try {

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bmp);


            FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();

            textRecognizer.processImage(image)
                    .addOnSuccessListener((FirebaseVisionText result) -> {
                // Task completed successfully
                // ...
                //String resultText = result.getText();
                for (FirebaseVisionText.TextBlock block: result.getTextBlocks()) {

                    //test block
                    //OcrGraphicFB graphicFB = new OcrGraphicFB(mGraphicOverlayFB, block);
                    //mGraphicOverlayFB.add(graphicFB);

                    for (FirebaseVisionText.Line line: block.getLines()) {

                        OcrGraphicFB graphic = new OcrGraphicFB(mGraphicOverlayFB, line);
                        mGraphicOverlayFB.add(graphic);

                    }
                }
            })
                    .addOnFailureListener((@NonNull Exception e) ->{
                // Task failed with an exception
                // ...
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

        /*switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                c = touchStart(e.getRawX(),e.getRawY());
                break;

            case MotionEvent.ACTION_UP:
                graphicDraw.postInvalidate();
                c = touchUp();
                break;

            case MotionEvent.ACTION_MOVE:
                graphicDraw.postInvalidate();
                c = touchMove(e.getRawX(), e.getRawY());
                break;

        }*/

        return c || super.onTouchEvent(e);
    }


    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }


    }



    Path mPath = null;
    float mX;
    float mY;
    double TOUCH_TOLERANCE = 4.0;
    OcrGraphicFB graphicDraw = null;

    private boolean touchStart(float rawX, float rawY){
        graphicDraw = new OcrGraphicFB(mGraphicOverlayFB, mPath);
        graphicDraw.setsRectPaint(Color.GREEN);
        graphicDraw.setRecPaintStrokeWidth(10.0f);
        mGraphicOverlayFB.add(graphicDraw);


        mPath = new Path();
        graphicDraws.add(graphicDraw);

        mPath.reset();
        mPath.moveTo(rawX,rawY);
        mX = rawX;
        mY = rawY;

        graphicDraw.updatePath(mPath, mX,mY);
        return true;

    }

    private boolean touchMove(float rawX, float rawY){
        float dx = Math.abs(rawX - mX);
        float dy = Math.abs(rawY - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE){
            mPath.quadTo(mX, mY, (rawX + mX)/2, (rawY + mY) /2);
            mX = rawX;
            mY = rawY;
        }

        graphicDraw.updatePath(mPath, mX, mY);
        return true;
    }

    private boolean touchUp(){
        mPath.lineTo(mX, mY);
        graphicDraw.updatePath(mPath, mX, mY);
        return true;
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
        FirebaseVisionText.TextBlock block;
        if (graphic != null) {
            line = graphic.getLine();
            block = graphic.getTextBlock();
            if (line != null && line.getText()!= null) {
                String text_line_content;
                text_line_content = line.getText();

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
public void setView(RecyclerWordAdapter adapter, ArrayList<String []> edmanInfo){

    // make progress bar invisible
    ProgressBar searchingEdmame = findViewById(R.id.searching_edmame);
    searchingEdmame.setVisibility(View.GONE);

    // no results returned
    if (edmanInfo.size() == 0){
        TextView textViewNoResult = findViewById(R.id.no_result);
        textViewNoResult.setVisibility(View.VISIBLE);
    }else{
        for (String[] recipeInformation : edmanInfo){
            adapter.addItem(recipeInformation);
        }
    }

    RecyclerView recyclerView = findViewById(R.id.gridview_edit_meal);
    LinearLayoutManager layoutManager= new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);

    //RecyclerView recyclerView = (RecyclerView) findViewById(R.id.gridview_edit_meal);
    recyclerView.setVisibility(View.VISIBLE);

}


    public void setUpRecyclerView(String wholeMealText, Boolean blockOrText){

        String mealText = "";
        if (currentLineSelection != null)
            mealText = currentLineSelection.replaceAll("[0-9]","");



        RecyclerWordAdapter recyclerWordAdapter = new RecyclerWordAdapter(this, R.layout.gridview_item, myTTS, wholeMealText, blockOrText);

        ProgressBar progressBar = findViewById(R.id.searching_edmame);
        progressBar.setVisibility(View.VISIBLE);

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
