package com.google.android.gms.samples.vision.ocrreader;

/**
 * Created by mgo983 on 1/5/19.
 */

public class Order {
    private static final String default_string_order = "nil";
    private static final int default_integer_order = -1;
    String food_item_name;
    int ice = default_integer_order;
    String cooked = default_string_order;
    int sliced = default_integer_order;
    int nutrition = default_integer_order;
    String sauce = default_string_order;

    public Order(String food_item_name,
                 int ice,
                 String cooked,
                 int sliced,
                 int nutrition,
                 String sauce){
        this.food_item_name = food_item_name;
        this.ice = ice;
        this.cooked = cooked;
        this.sliced = sliced;
        this.nutrition = nutrition;
        this.sauce = sauce;
    }

    public Order(String food_item_name){
        this.food_item_name = food_item_name;
    }

    public void setOrder(String food_item_name,
                         int ice,
                         String cooked,
                         int sliced,
                         int nutrition,
                         String sauce){
        this.food_item_name = food_item_name;
        this.ice = ice;
        this.cooked = cooked;
        this.sliced = sliced;
        this.nutrition = nutrition;
        this.sauce = sauce;
    }


}
