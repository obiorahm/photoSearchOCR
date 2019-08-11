package com.google.android.gms.samples.vision.ocrreader;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class AllOrders implements Serializable {




    public AllOrders(){}


    public HashMap<String, CurrentOrder> orders = new HashMap<>();

    public void addOrder(CurrentOrder anOrder){
        String uniqueOrderID = UUID.randomUUID().toString();
        orders.put(uniqueOrderID, anOrder);
    }


    public void removeOrder(String uniqueOrderID){
        orders.remove(uniqueOrderID);
    }

}
