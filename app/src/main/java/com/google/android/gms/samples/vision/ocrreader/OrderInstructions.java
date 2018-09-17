package com.google.android.gms.samples.vision.ocrreader;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mgo983 on 9/8/18.
 */

public class OrderInstructions {

    // sizes

    public int small_portion;
    public int normal_size;
    public int large_portion;
    public int blue_rare;
    public int share;
    public int vegetables;
    public int main_dish;
    public int egg_substitute;
    public int mayonnaise;
    public int dressing;
    public int cheese_sauce;
    public int mustard;
    public int sliced;
    public int whole;
    public int vegetarian;
    public int nutrition_facts;
    public int ice;
    public int water;
    public int warm;
    public int rare;
    public int medium_rare;
    public int medium;
    public int medium_well;
    public int well_done;

    public OrderInstructions(int val){
        this.small_portion = 0;
        this.normal_size = 0;
        this.large_portion = 0;
        this.blue_rare = 0;
        this.share = 0;
        this.vegetables = 0;
        this.main_dish = 0;
        this.egg_substitute = 0;
        this.mayonnaise = 0;
        this.dressing = 0;
        this.cheese_sauce = 0;
        this.mustard = 0;
        this.sliced = 0;
        this.whole = 0;
        this.vegetarian = 0;
        this.nutrition_facts = 0;
        this.ice = 0;
        this.water = 0;
        this.warm = 0;
        this.rare = 0;
        this.medium_rare = 0;
        this.medium = 0;
        this.medium_well = 0;
        this.well_done = 0;
    }

    public OrderInstructions(){}



    public void setSmall_portion(int big){this.small_portion = big;}
    public void setNormal_size(int bigger){this.normal_size = bigger;}
    public void setLarge_portion(int biggest){this.large_portion = biggest;}
    public void setShare(int share){this.share = share;}
    public void setVegetables(int vegetables){this.vegetables = vegetables;}
    public void setMain_dish(int main_dish){this.main_dish = main_dish;}
    public void setEgg_substitute(int egg_substitute){this.egg_substitute = egg_substitute;}
    public void setMayonnaise(int mayonnaise){this.mayonnaise = mayonnaise;}
    public void setDressing(int dressing){this.dressing = dressing;}
    public void setCheese_sauce(int cheese_sauce){this.cheese_sauce = cheese_sauce;}
    public void setMustard(int mustard){this.mustard = mustard;}
    public void setSliced(int sliced){this.sliced = sliced;}
    public void setWhole(int whole){this.whole = whole;}
    public void setVegetarian(int vegetarian){this.vegetarian = vegetarian;}
    public void setNutrition_facts(int nutrition_facts){this.nutrition_facts = nutrition_facts;}
    public void setIce(int ice){this.ice = ice;}
    public void setWater(int water){this.water = water;}
    public void setWarm(int warm){this.warm = warm;}
    public void setBlue_rare(int blue_rare){this.blue_rare = blue_rare;}
    public void setRare(int rare){this.rare = rare;}
    public void setMedium_rare(int medium_rare){this.medium_rare = medium_rare;}
    public void setMedium(int medium){this.medium = medium;}
    public void setMedium_well(int medium_well){this.medium_well = medium_well;}
    public void setWell_done(int well_done){this.well_done = well_done;}


    public void clone(OrderInstructions orderInstructions){
        this.small_portion = orderInstructions.small_portion;
        this.normal_size = orderInstructions.normal_size;
        this.large_portion = orderInstructions.large_portion;
        this.blue_rare = orderInstructions.blue_rare;
        this.share = orderInstructions.share;
        this.vegetables = orderInstructions.vegetables;
        this.main_dish = orderInstructions.main_dish;
        this.egg_substitute = orderInstructions.egg_substitute;
        this.mayonnaise = orderInstructions.mayonnaise;
        this.dressing = orderInstructions.dressing;
        this.cheese_sauce = orderInstructions.cheese_sauce;
        this.mustard = orderInstructions.mustard;
        this.sliced = orderInstructions.sliced;
        this.whole = orderInstructions.whole;
        this.vegetarian = orderInstructions.vegetarian;
        this.nutrition_facts = orderInstructions.nutrition_facts;
        this.ice = orderInstructions.ice;
        this.water = orderInstructions.water;
        this.warm = orderInstructions.warm;
        this.rare = orderInstructions.rare;
        this.medium_rare = orderInstructions.medium_rare;
        this.medium = orderInstructions.medium;
        this.medium_well = orderInstructions.medium_well;
        this.well_done = orderInstructions.well_done;
    }

    public void putOrder(String order, Integer choice){
        switch (order){
            case "small_portion":
                setSmall_portion(choice);
                break;
            case "normal_size":
                setNormal_size(choice);
                break;
            case "large_portion":
                setLarge_portion(choice);
                break;
            case "share":
                setShare(choice);
                break;
            case "vegetables":
                setVegetables(choice);
                break;
            case "main_dish":
                setMain_dish(choice);
                break;
            case "egg_substitute":
                setEgg_substitute(choice);
                break;
            case "mayonnaise":
                setMayonnaise(choice);
                break;
            case "dressing":
                setDressing(choice);
                break;
            case "cheese_sauce":
                setCheese_sauce(choice);
                break;
            case "mustard":
                setMustard(choice);
                break;
            case "sliced":
                setSliced(choice);
                break;
            case "whole":
                setWhole(choice);
                break;
            case "vegetarian":
                setVegetarian(choice);
                break;
            case "nutrition_facts":
                setNutrition_facts(choice);
                break;
            case "ice":
                setIce(choice);
                break;
            case "water":
                setWater(choice);
                break;
            case "warm":
                setWarm(choice);
                break;
            case "blue_rare":
                setBlue_rare(choice);
                break;
            case "rare":
                setRare(choice);
                break;
            case "medium_rare":
                setMedium_rare(choice);
                break;
            case "medium":
                setMedium(choice);
                break;
            case "medium_well":
                setMedium_well(choice);
                break;
            case "well_done":
                setWell_done(choice);
                break;
        }
    }

    public int getOrder(String order){
        switch (order){
            case "small_portion":
                return small_portion;
            case "normal_size":
                return normal_size;
            case "large_portion":
                return large_portion;
            case "share":
                return share;
            case "vegetables":
                return vegetables;
            case "main_dish":
                return main_dish;
            case "egg_substitute":
                return egg_substitute;
            case "mayonnaise":
                return mayonnaise;
            case "dressing":
                return dressing;
            case "cheese_sauce":
                return cheese_sauce;
            case "mustard":
                return mustard;
            case "sliced":
                return sliced;
            case "whole":
                return whole;
            case "vegetarian":
                return vegetarian;
            case "nutrition_facts":
                return nutrition_facts;
            case "ice":
                return ice;
            case "water":
                return water;
            case "warm":
                return warm;
            case "blue_rare":
                return blue_rare;
            case "rare":
                return rare;
            case "medium_rare":
                return medium_rare;
            case "medium":
                return medium;
            case "medium_well":
                return medium_well;
            case "well_done":
                return well_done;
            default:
                return 0;
        }
    }

    public Map toMap(){
        HashMap<String, Integer> completeOrder = new HashMap<>();
        completeOrder.put("small_portion", small_portion);
        completeOrder.put("normal_size", normal_size);
        completeOrder.put("large_portion", large_portion);
        completeOrder.put("blue_rare", blue_rare);
        completeOrder.put("share", share);
        completeOrder.put("vegetables", vegetables);
        completeOrder.put("main_dish", main_dish);
        completeOrder.put("egg_substitute", egg_substitute);
        completeOrder.put("mayonnaise", mayonnaise);
        completeOrder.put("dressing", dressing);
        completeOrder.put("cheese_sauce", cheese_sauce);
        completeOrder.put("mustard", mustard);
        completeOrder.put("sliced", sliced);
        completeOrder.put("whole", whole);
        completeOrder.put("vegetarian", vegetarian);
        completeOrder.put("nutrition_facts", nutrition_facts);
        completeOrder.put("ice", ice);
        completeOrder.put("water", water);
        completeOrder.put("warm", warm);
        completeOrder.put("rare", rare);
        completeOrder.put("medium_rare", medium_rare);
        completeOrder.put("medium", medium);
        completeOrder.put("medium_well", medium_well);
        completeOrder.put("well_done", well_done);
        return completeOrder;
    }

}

