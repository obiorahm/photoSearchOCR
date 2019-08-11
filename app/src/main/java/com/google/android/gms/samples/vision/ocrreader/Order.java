package com.google.android.gms.samples.vision.ocrreader;

import java.util.ArrayList;

/**
 * Created by mgo983 on 1/5/19.
 */

public class Order {
    private static final String default_string_order = "nil";
    private static final int default_integer_order = -1;


    public static  final int OPTION_SIZE = 6;
    public static final int ORDER_ICE = 0;
    public static final int ORDER_COOKED = 1;
    public static final int ORDER_SLICED = 2;
    public static final int ORDER_NUTRITION = 3;
    public static final int ORDER_SAUCE = 4;
    public static final int ORDER_DRINK = 5;


    // used by ExpandOption adapter
    // used by shoppingCartAdapter
    public final static String SAUCES = "sauces";
    public final static String DRINKS = "drinks";
    public final static String NUTRITION = "nutrition";
    public final static String MEATS = "meats";


    public static String CONSOLIDATED_OPTION [][]= {
            //ice
            {"", "Ice please!", "No ice please." },
            //cooked
            {"", "Blue rare, please!", "Medium, please!", "Medium rare, please!", "Medium well, please!", "Rare, please!", "Well done, please!"},
            //sliced
            {"", "Please slice my meat!", "Don't slice my meat!"},
            //nutrition
            {"", "What's in this meal?", "" },
            //sauce
            {"","Can I have cheese sauce?", "Can I have some dressing?", "Can I have mayonnaise?", "Can I have mustard?" },
            //ice
            {"", "Ice please!", "No ice please."}};


    public Order(){}

    public static final String dummy_language = "a_dummy";


    public static final int DEFAULT_VAL = 0;

    String foodItemName;
    Integer orderValues [] = new Integer[OPTION_SIZE];


    public class Description{
        String name;
        String value;
    }

    ArrayList<Description> descriptions = new ArrayList<>();

    public Order(String foodItemName, Integer [] orderValues){
        this.foodItemName = foodItemName;
        this.orderValues = orderValues;

        for (int i = 0; i < OPTION_SIZE; i++){
            orderValues[i] = DEFAULT_VAL;
        }
    }


    public String getFoodItemName(){return foodItemName;}
    public Integer[] getOrderValues(){return orderValues;}


}
