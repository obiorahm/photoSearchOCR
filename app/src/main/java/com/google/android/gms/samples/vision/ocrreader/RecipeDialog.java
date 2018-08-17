package com.google.android.gms.samples.vision.ocrreader;

import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import com.google.android.gms.samples.vision.ocrreader.Adapter.ImageAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RecipeListAdapter;
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


    public static FirebaseAuth firebaseAuth;

    //set up wordnet constants
    static final String WNDICT = "dict";
    static final String DOCUMENT = "document";
    static final File DocumentDir = new File(Environment.getExternalStoragePublicDirectory(DOCUMENT), WNDICT);
    static final String DB_REF_WORD = "word";
    final String WORD_IMAGE_REFERENCE  = "symbols";




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recipe_list, container, false);


        firebaseAuth = FirebaseAuth.getInstance();

        recipeListAdapter = new RecipeListAdapter(getActivity(), R.layout.recipe_item_list);

        try {
            JSONArray ingredientArray = new JSONArray(this.getArguments().getString(WordAdapter.RECIPE_INGREDIENTS));

            int n = 0;
            search(ingredientArray, n);

            ListView listView = (ListView) rootView.findViewById(R.id.recipe_list_view);
            listView.setAdapter(recipeListAdapter);


        }catch (JSONException e){
            Log.e(LOG_TAG, e + "");
        }

        ImageButton imageButton = rootView.findViewById(R.id.next_item);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //recipeListAdapter.
            }
        });

        return rootView;
    }


    private void search(final JSONArray ingredientArray, int n){
        if (n < ingredientArray.length())
        {
            final int count = n + 1;
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

            //sign in anonymously to Firebase
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null){
                signInAnonymously();
            }
            Query databaseReference = FirebaseDatabase.getInstance().getReference(DB_REF_WORD).child(tokenizedIngredient[0].toLowerCase()).limitToFirst(1);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // if the data does not exist then load the next set of item

                    if  (!dataSnapshot.exists()){
                        addItemToAdapters("",
                                ingredient,
                                imageUrlsArray,
                                tokenizedIngredient,
                                ingredientArray,
                                count);
                    }
                    // should have an else but else works by default
                    for (DataSnapshot child : dataSnapshot.getChildren()){

                        //now that we have the file name retrieve image from firebase storage
                        String childValue [] = child.getValue().toString().split("/");
                        Log.d(LOG_TAG + " child ", WORD_IMAGE_REFERENCE + "/" + childValue[0] +"/" + childValue[1]);



                        //now that we have the file name retrieve image from firebase storage
                        StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference();

                        firebaseStorage.child( WORD_IMAGE_REFERENCE + "/" + childValue[0] + "/" + childValue[2]).getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //imageUrlsArray.add(uri.toString());
                                        addItemToAdapters(uri.toString(),
                                                ingredient,
                                                imageUrlsArray,
                                                tokenizedIngredient,
                                                ingredientArray,
                                                count);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // add item so that column appears even though image is not found
                                addItemToAdapters("",
                                        ingredient,
                                        imageUrlsArray,
                                        tokenizedIngredient,
                                        ingredientArray,
                                        count);

                            }
                        });
                        Log.d(LOG_TAG, child.toString());
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

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

        //first do some natural language processing
        // remove all special characters and numbers
        String processedIngredient = anIngredient.replaceAll("[0-9/]", "");


        //break the recipe step into words to find ingredients
        String [] tokenizedIngredient = anIngredient.split(" ");
        //tokenizedIngredient[0] = processedIngredient;

        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String [] tokens = tokenizer.tokenize(processedIngredient);


        Log.d(LOG_TAG + " nlp", processedIngredient);

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
        int dialogWindowWidth = (int) (displayWidth * 0.9f);

        // Assign wrap content of dialog
        int dialogWindowHeight = WindowManager.LayoutParams.WRAP_CONTENT;

        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();

        params.width = dialogWindowWidth;
        params.height = dialogWindowHeight;


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
