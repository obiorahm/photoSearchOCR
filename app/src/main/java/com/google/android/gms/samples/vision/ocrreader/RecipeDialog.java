package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.samples.vision.ocrreader.Adapter.ImageAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RecipeListAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RecyclerWordAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.WordAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.eval.Mean;

/**
 * Created by mgo983 on 8/7/18.
 */

public class RecipeDialog extends DialogFragment {


    RecipeListAdapter recipeListAdapter;

    //ImageAdapter imageAdapter;

    private String LOG_TAG = RecipeDialog.class.getSimpleName();

    HashSet<String> measurementHypernyms = new HashSet<>();
    HashSet<String> foodCategories = new HashSet<>();


    public static FirebaseAuth firebaseAuth;

    //set up wordnet constants
    static final String WNDICT = "dict";
    static final String DOCUMENT = "document";
    static final File DocumentDir = new File(Environment.getExternalStoragePublicDirectory(DOCUMENT), WNDICT);
    static final String DB_REF_WORD = "word";
    final String WORD_IMAGE_REFERENCE  = "symbols";
    public static final String  ORDER_MEAL_TEXT = "com.google.android.gms.samples.vision.ocrreader.ORDER_MEAL_TEXT";





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recipe_list, container, false);

        populateFoodCategories();


        firebaseAuth = FirebaseAuth.getInstance();

        recipeListAdapter = new RecipeListAdapter(getActivity(), R.layout.recipe_item_list);

        try {

            JSONArray ingredientArray = new JSONArray(this.getArguments().getString(RecyclerWordAdapter.RECIPE_INGREDIENTS));

            /*test with testRecipeDialog
            JSONArray ingredientArray = testRecipeDialog();*/
            int n = 0;
            search(ingredientArray, n);

            ListView listView = (ListView) rootView.findViewById(R.id.recipe_list_view);
            listView.setAdapter(recipeListAdapter);


        }catch (JSONException e){
            Log.e(LOG_TAG, e + "");
        }catch (NullPointerException e){

            Log.e(LOG_TAG, e + " ");

        }

        final String meal_name = getArguments().getString(RecyclerWordAdapter.MEAL_NAME);

        ImageButton imageButton = rootView.findViewById(R.id.next_item);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //recipeListAdapter.

                String order = makeOrder(meal_name);
                DialogFragment newFragment = new OrderDialog();
                Bundle bundle = new Bundle();
                bundle.putString(ORDER_MEAL_TEXT, order );


                newFragment.setArguments(bundle);
                newFragment.show(getFragmentManager(),"ORDER_MEAL");
            }
        });

        //speak no and extra
        final TextView textViewExtra = (TextView) rootView.findViewById(R.id.no_item);
        textViewExtra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MealMenuActivity.myTTS.speak(textViewExtra.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        });

         final TextView textViewNone = (TextView) rootView.findViewById(R.id.more_item);
         textViewNone.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 MealMenuActivity.myTTS.speak(textViewNone.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
             }
         });

        return rootView;
    }

    private String makeOrder(String meal_name){
        HashSet<String> extras = recipeListAdapter.getExtraItems();
        String more = "";
        for (String item : extras){
            Log.d(LOG_TAG,  " I want more " + item);
            more += item + " ";
        }

        HashSet<String> nones = recipeListAdapter.getNone();
        String none = "";
        for (String item : nones){
            Log.d(LOG_TAG,  " I want no " + item);
            none += item + " ";
        }
        String order = "I'll have " + meal_name ;

        if (more.length() > 0)
            order += " extra " + more;

        if (none.length() > 0)
            order += " and no " + none;

        //((MealMenuActivity) getContext()).myTTS.speak(order, TextToSpeech.QUEUE_FLUSH, null);

        return order;

    }

    private JSONArray testRecipeDialog(){
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("rice");
        jsonArray.put("beans");
        jsonArray.put("sugar");
        jsonArray.put("salt");

        return jsonArray;

    }

    private void search(final JSONArray ingredientArray, int n){
        if (n < ingredientArray.length())
        {
            final int count = n + 1;
            Log.d(LOG_TAG, " count: " + count + " ingredient length: " + ingredientArray.length());
            final ArrayList<String[]> ingredient = new ArrayList<>();
            try{
                String food = findFoodInIngredient(ingredientArray.getString(n));
                //create array to accommodate TYPE_IMAGE content
                String[]  foodAndSpace = {food, ""};
                ingredient.add(foodAndSpace);

            }catch (IOException e){
                Log.e(LOG_TAG, e + "");
            }catch (IllegalArgumentException e){
                Log.e(LOG_TAG, e + "");
            }catch (JSONException e){
                Log.e(LOG_TAG, e +"");
            }


            final ArrayList<String[]> imageUrlsArray = new ArrayList<String[]>();


            //tokenize ingredient
            SimpleTokenizer simpleTokenizer = SimpleTokenizer.INSTANCE;
            final int FIRST_RECIPE_ITEM = 0;
            final int ITEM = 0;

            final String tokenizedIngredient [] = simpleTokenizer.tokenize(ingredient.get(FIRST_RECIPE_ITEM)[ITEM]);
            //String first_item = ingredient.get(FIRST_RECIPE_ITEM)[ITEM];
            //final String tokenizedIngredient [] = first_item.split(" ");


            //sign in anonymously to Firebase
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null){
                signInAnonymously();
            }

            Log.d(LOG_TAG, " tokenized_ingredient_now " + tokenizedIngredient.length);
            Log.d(LOG_TAG, "recipe item " + ingredient.get(FIRST_RECIPE_ITEM)[ITEM]);

            // ingredient is null return
            if (tokenizedIngredient.length == 0){
                search(ingredientArray, count);
                return;
            }

            // use image of food in sentence rather than first word
            ArrayList<String> category = new ArrayList<>();
            ArrayList<String > my_uri = new ArrayList<>();
            findFoodInSentence(tokenizedIngredient,
                    category,
                    0,
                    ingredient,
                    imageUrlsArray,
                    ingredientArray,
                    count,
                    my_uri);


        }

    }

    private void addItemToAdapters(String newItem, ArrayList<String[]> ingredient, ArrayList<String[]> imageUrlsArray, String [] tokenizedIngredient, JSONArray ingredientArray, int count ){
        // url and word in a string array
        String urlAndWord[] = {newItem, tokenizedIngredient[0]};
        ingredient.add(urlAndWord);
        imageUrlsArray.add(urlAndWord);
        recipeListAdapter.addItem(ingredient);
        recipeListAdapter.addImage(imageUrlsArray);
        getImageUrlsForOtherTokens(tokenizedIngredient, 1, imageUrlsArray);
        search(ingredientArray, count);

    }


    private void findFoodInSentence(final String[] tokenizedIngredient,
                                    final ArrayList<String> category,
                                    final int tokenCount,
                                    final ArrayList<String[]> ingredient,
                                    final ArrayList<String[]> imageUrlsArray,
                                    final JSONArray ingredientArray,
                                    final int count,
                                    final ArrayList<String> my_uri ){


            if (tokenizedIngredient[0].equals("beans")){
                Log.d(LOG_TAG + " beans ", "token_count " + tokenCount );
                Log.d(LOG_TAG + " beans ", "tokenized Ingredient count " + tokenizedIngredient.length );

            }

        String luri = my_uri.size() >= 1 ? my_uri.get(my_uri.size() - 1) : "";
        Log.d(LOG_TAG + " firebase url ", luri + " luri size" + my_uri.size() );


        if (tokenCount >= tokenizedIngredient.length){
            //check for null uri
            addItemToAdapters(luri,
                    ingredient,
                    imageUrlsArray,
                    tokenizedIngredient,
                    ingredientArray,
                    count);
            return;
        }

        final String finalCategory = category.size() >= 1 ? category.get(category.size() - 1) : null ;

        if ( foodCategories.contains(finalCategory) ){
            //check for null uri
            addItemToAdapters(luri,
                    ingredient,
                    imageUrlsArray,
                    tokenizedIngredient,
                    ingredientArray,
                    count);
            return;
        }
        Log.d(LOG_TAG + " token ",   tokenizedIngredient[tokenCount] );

        Query databaseReference = FirebaseDatabase.getInstance().getReference(DB_REF_WORD).child(tokenizedIngredient[tokenCount].toLowerCase());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                final ArrayList<Integer> count_children = new ArrayList<>();

                // if the data does not exist then load the next set of item

                if  (!dataSnapshot.exists()){
                    findFoodInSentence(tokenizedIngredient,
                            category,
                            tokenCount + 1,
                            ingredient,
                            imageUrlsArray,
                            ingredientArray,
                            count,
                            my_uri);
                    return;

                }
                // should have an else but else works by default
                //check for food category image
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    //now that we have the file name retrieve image from firebase storage
                    final String childValue [] = child.getValue().toString().split("/");


                        //now that we have the file name retrieve image from firebase storage
                        StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference();

                        firebaseStorage.child( WORD_IMAGE_REFERENCE + "/" + childValue[0] + "/" + childValue[2]).getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        count_children.add(1);
                                        Log.d(LOG_TAG + " firebase url ", my_uri.size() + " luri size " );

                                        Log.d(LOG_TAG + " firebase url ", count_children.size() + " " );

                                        int local_token_count = tokenCount + 1;


                                        if (foodCategories.contains(childValue[0]) &&
                                                category.size() == 0){

                                            //save the uri
                                            category.add(childValue[0]);
                                            my_uri.add(uri.toString());

                                        }

                                        if (tokenizedIngredient[tokenCount].equals("eggs")) {

                                            Log.d(LOG_TAG, " found eggs ");
                                            Log.d(LOG_TAG, " found eggs  category " + category.size());
                                            Log.d(LOG_TAG, " found eggs " +childValue[0] + foodCategories.contains(childValue[0]) + " ");
                                            Log.d(LOG_TAG, " found eggs " + count_children.size());
                                            Log.d(LOG_TAG + " found eggs ", "datasnapshotchildren " + dataSnapshot.getChildrenCount() );


                                        }
                                        if(count_children.size()  == dataSnapshot.getChildrenCount() &&
                                                tokenCount + 1 >= tokenizedIngredient.length &&
                                                category.size() == 0){
                                            category.add(childValue[0]);
                                            my_uri.add(uri.toString());

                                        }


                                        if(count_children.size()  == dataSnapshot.getChildrenCount()){
                                            if (tokenizedIngredient[tokenCount].equals("rice")){
                                                Log.d(LOG_TAG + " rice ", "category " + category.size() );
                                                Log.d(LOG_TAG + " rice ", "myuri " + my_uri.size() );
                                                Log.d(LOG_TAG + " rice ", "countchildren " + count_children.size() + 1 );
                                                Log.d(LOG_TAG + " rice ", "datasnapshotchildren " + dataSnapshot.getChildrenCount() );
                                            }
                                            findFoodInSentence(tokenizedIngredient,
                                                    category,
                                                    tokenCount + 1,
                                                    ingredient,
                                                    imageUrlsArray,
                                                    ingredientArray,
                                                    count,
                                                    my_uri);


                                        }



                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                count_children.add(1);

                                if(count_children.size() + 1 == dataSnapshot.getChildrenCount()){
                                    findFoodInSentence(tokenizedIngredient,
                                            category,
                                            tokenCount + 1,
                                            ingredient,
                                            imageUrlsArray,
                                            ingredientArray,
                                            count,
                                            my_uri);

                                }
                            }
                        });


                    Log.d(LOG_TAG + " child ", WORD_IMAGE_REFERENCE + "/" + childValue[0] +"/" + childValue[1] + "/" + childValue[2] + finalCategory);


                    Log.d(LOG_TAG, child.toString());
                }



               /* findFoodInSentence(tokenizedIngredient,
                        category,
                        tokenCount + 1,
                        ingredient,
                        imageUrlsArray,
                        ingredientArray,
                        count,
                        my_uri);*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private void getImageUrlsForOtherTokens(final String [] tokenizedIngredient, final int i, final ArrayList<String[]> imageUrlsArray){
        Log.d(LOG_TAG + "other tokens", imageUrlsArray.get(0)[0]);
        if (i < tokenizedIngredient.length){
            Query databaseReference = FirebaseDatabase.getInstance().getReference(DB_REF_WORD).child(tokenizedIngredient[i].toLowerCase()).limitToFirst(1);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //takes care of images that do not exist
                    if (!dataSnapshot.exists()){
                        //add searched word and Url
                        String[] urlAndWord = {"", tokenizedIngredient[i]};
                        imageUrlsArray.add(urlAndWord);
                        getImageUrlsForOtherTokens(tokenizedIngredient, i + 1, imageUrlsArray);

                    }

                    /* 0 is the category, 1 is the firebase generated id on push, 2 is the word itself
                    * */
                    for (DataSnapshot child: dataSnapshot.getChildren()){
                        String childValue [] = child.getValue().toString().split("/");
                        Log.d(LOG_TAG + " child ", WORD_IMAGE_REFERENCE + "/" + childValue[0] +"/" + childValue[1]);



                        //now that we have the file name retrieve image from firebase storage
                        StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference();

                        firebaseStorage.child( WORD_IMAGE_REFERENCE + "/" + childValue[0] + "/" + childValue[2]).getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        // add searched word and Url
                                        String [] urlAndWord = {uri.toString(), tokenizedIngredient[i]};
                                        imageUrlsArray.add(urlAndWord);
                                        Log.d(LOG_TAG + "other tokens", imageUrlsArray.size()+ "");

                                        getImageUrlsForOtherTokens(tokenizedIngredient, i + 1, imageUrlsArray);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //add searched word and Url
                                String[] urlAndWord = {"", tokenizedIngredient[i]};
                                imageUrlsArray.add(urlAndWord);
                                getImageUrlsForOtherTokens(tokenizedIngredient, i + 1, imageUrlsArray);
                            }
                        });

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            })  ;

        }
    }


private void populateFoodCategories(){
// list of food categories we are interested in
        foodCategories.add("alcoholic_beverage");
        foodCategories.add("canned_food");
        foodCategories.add("condiments");
        foodCategories.add("dairy");
        foodCategories.add("drinks");
        foodCategories.add("fast_food");
        foodCategories.add("fish");
        foodCategories.add("fishes");
        foodCategories.add("food");
        foodCategories.add("foods");
        foodCategories.add("fruits");
        foodCategories.add("grains");
        foodCategories.add("herbs_and_spices");
        foodCategories.add("juices");
        foodCategories.add("junk_food");
        foodCategories.add("meat");
        foodCategories.add("sandwiches");
        foodCategories.add("seafood");
        foodCategories.add("snacks");
        foodCategories.add("soups");
        foodCategories.add("sweets");
        foodCategories.add("vegetables");


}


    private String findFoodInIngredient(String anIngredient) throws IOException {
        //add measurement hypernyms
        measurementHypernyms.add("dishware");
        measurementHypernyms.add("container");
        measurementHypernyms.add("avoirdupois_unit");
        measurementHypernyms.add("containerful");
        measurementHypernyms.add("crockery");
        measurementHypernyms.add("cooking_utensil");
        measurementHypernyms.add("cookware");

        //other hypernyms
        measurementHypernyms.add("outlet");
        measurementHypernyms.add("sales_outlet");
        measurementHypernyms.add("retail_store");
        measurementHypernyms.add("mercantile_establishment");
        measurementHypernyms.add("metric_weight_unit");
        measurementHypernyms.add("weight_unit");
        measurementHypernyms.add("apothecaries'_unit");
        measurementHypernyms.add("apothecaries'_weight");
        measurementHypernyms.add("troy_unit");
        measurementHypernyms.add("small_indefinite_quantity");
        measurementHypernyms.add("small_indefinite_amount");
        measurementHypernyms.add("linear_unit");
        measurementHypernyms.add("linear_measure");
        measurementHypernyms.add("degree");
        measurementHypernyms.add("metric_linear_unit");
        measurementHypernyms.add("metric_capacity_unit");
        measurementHypernyms.add("common_fraction");
        measurementHypernyms.add("simple_fraction");
        measurementHypernyms.add("digit");
        measurementHypernyms.add("figure");
        measurementHypernyms.add("British_capacity_unit");
        measurementHypernyms.add("Imperial_capacity_unit");



        //first do some natural language processing
        // remove all special characters and numbers
        String processedIngredient = anIngredient.replaceAll("[0-9/]", "");


        //break the recipe step into words to find ingredients
        String [] tokenizedIngredient = anIngredient.split(" ");
        //tokenizedIngredient[0] = processedIngredient;

        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String [] tokens = tokenizer.tokenize(processedIngredient);


        Log.d(LOG_TAG + " nlp", processedIngredient);
        String x = tokens.length > 0 ? tokens[0] : "no string ";
        Log.d(LOG_TAG , "measurement tokens " + x);

        String path = DocumentDir.getAbsolutePath();

        URL url = new URL("file", null, path);

        /*net.sf.extjwnl.dictionary.Dictionary  dictionary = net.sf.extjwnl.dictionary.Dictionary.getDefaultResourceInstance();
        IndexWord ACCOMPLISH = dictionary.getIndexWord(net.sf.extjwnl.data.POS.NOUN, "dog");
        Log.d(LOG_TAG + "dog" , ACCOMPLISH.getLemma());*/

        // construct the dictionary object and open it
        IDictionary dict = new Dictionary( url);


        boolean dictIsOpen = dict.open();
        Log.d(LOG_TAG, " " + dictIsOpen);

        String finalWords  = "";
        for (String item : tokens){
            //remove numbers
            if (!(item == null)){
                // look up first sense of the word item
                IIndexWord idxWord = dict . getIndexWord (item, POS.NOUN );
                if (idxWord != null){
                    IWordID wordID = idxWord . getWordIDs ().get (0) ;
                    IWord word = dict . getWord ( wordID );

                    ISynset synset = word.getSynset ();

                    // get the hypernyms
                    List< ISynsetID > hypernyms = synset.getRelatedSynsets (Pointer.HYPERNYM);

                    boolean removeMeasurements = false;
                    List<IWord > words ;
                    for( ISynsetID sid : hypernyms ){
                        words = dict.getSynset(sid).getWords ();
//            Log.d("",sid + " {");
                        for(Iterator<IWord > i = words.iterator(); i.hasNext () ;){
                            String aHypernym = i.next().getLemma();
                            if (measurementHypernyms.contains(aHypernym))
                                removeMeasurements = true;

                            Log.d("JWI " + item , aHypernym);
                        }
                        //Log.d ("","}");
                    }
                    if (!removeMeasurements && !(hypernyms == null)){
                        finalWords += item + " ";
                    }

                }


            }



        }

        return finalWords;

    }

    @Override
    public void onResume() {

        // Get screen width and height in pixels
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // The absolute width of the available display size in pixels.
        int displayWidth = displayMetrics.widthPixels;

        // Set alert dialog width equal to screen width 90%
        int dialogWindowWidth = (int) (displayWidth * 0.85f);

        // Assign wrap content of dialog
        //int dialogWindowHeight = WindowManager.LayoutParams.WRAP_CONTENT;

        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();

        params.width = dialogWindowWidth;
        //params.height = dialogWindowHeight;


        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }

    public void signInAnonymously() {

        firebaseAuth.signInAnonymously().addOnSuccessListener(getActivity(), new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

            }
        }).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

}
