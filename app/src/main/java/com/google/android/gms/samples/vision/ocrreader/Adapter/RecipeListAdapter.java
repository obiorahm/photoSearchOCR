package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.MealMenuActivity;
import com.google.android.gms.samples.vision.ocrreader.R;



import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.data.IHasLifecycle;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import opennlp.tools.tokenize.SimpleTokenizer;


/**
 * Created by mgo983 on 8/7/18.
 */

public class RecipeListAdapter extends ArrayAdapter {

    ArrayList<String[]> ingredients = new ArrayList<>();

    LayoutInflater inflater;
    Context context;
    String LOG_TAG = RecipeListAdapter.class.getSimpleName();
    TextToSpeech myTTS;

    //set up wordnet constants
    static final String WNDICT = "dict";
    static final String DOCUMENT = "document";
    static final File DocumentDir = new File(Environment.getExternalStoragePublicDirectory(DOCUMENT), WNDICT);

    HashSet<String> measurementHypernyms = new HashSet<>();

    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_FULL_CONTENT = 1;

    private TreeSet imageSet = new TreeSet();
    private TreeSet fullContentSet = new TreeSet();


    public RecipeListAdapter(Context context, int resource){
        super(context, resource);
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void addItem(String[] an_ingredient){
        ingredients.add(an_ingredient);

    }

    public void addImage(String imageUrl){

    }


    @Override
    public int getCount(){
        return ingredients.size();
    }

    @Override
    public String[] getItem(int position){
        return (String []) ingredients.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null){
            convertView = inflater.inflate(R.layout.recipe_item_list, null);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.ingredient_name);

        Log.d(LOG_TAG, "the ingredients" + ingredients.get(position)[0]);

        //position 0 contains the recipe itself
        String anIngridient = ingredients.get(position)[0];

        String food = "";

        try{
            food = findFoodInIngredient(anIngridient);

        }catch (IOException e){
            Log.e(LOG_TAG, e + "");
        }catch (IllegalArgumentException e){
            Log.e(LOG_TAG, e + "");
        }

        //textView.setText(anIngridient);
        textView.setText(food);

        final String spoken_food = food;

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTTS = ((MealMenuActivity) context).myTTS;

                myTTS.speak(spoken_food, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        return convertView;
    }

    private String findFoodInIngredient(String anIngredient) throws IOException{
        //add measurement hypernyms
        measurementHypernyms.add("dishware");
        measurementHypernyms.add("container");
        measurementHypernyms.add("avoirdupois_unit");
        measurementHypernyms.add("containerful");

        //first do some natural language processing
        // remove all special characters and numbers
        String processedIngredient = anIngredient.replaceAll("[0-9/]", "");


        //break the recipe step into words to find ingredients
        String [] tokenizedIngredient = anIngredient.split(" ");
        //tokenizedIngredient[0] = processedIngredient;

        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String [] tokens = tokenizer.tokenize(processedIngredient);


        Log.d(LOG_TAG + " nlp", processedIngredient);

        String food = "";

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
}
