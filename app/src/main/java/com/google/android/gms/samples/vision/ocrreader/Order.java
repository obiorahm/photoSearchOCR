package com.google.android.gms.samples.vision.ocrreader;

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

    public static final int DEFAULT_VAL = 0;

    String foodItemName;
    Integer orderValues [] = new Integer[OPTION_SIZE];


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
