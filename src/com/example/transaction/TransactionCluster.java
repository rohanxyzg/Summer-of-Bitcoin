package com.example.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TransactionCluster {
    List<String> tx_ids;
    float totalFee;
    float totalWeight;

    public TransactionCluster(List<String> tx, float fee, float weight){
        tx_ids = new ArrayList<>();
        for(String w:tx){
            tx_ids.add(w);
        }
        this.totalFee = fee;
        this.totalWeight = weight;
    }

    public float getTotalFee() {
        return totalFee;
    }

    public List<String> getTx_ids() {
        return tx_ids;
    }

    public float getTotalWeight() {
        return totalWeight;
    }
}
