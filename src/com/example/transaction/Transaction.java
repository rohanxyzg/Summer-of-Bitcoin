package com.example.transaction;

import java.util.ArrayList;

public class Transaction {
    String tx_id;
    float fee;
    float weight;

    public Transaction(String tax_id, float fee, float weight){
        this.tx_id = tax_id;
        this.fee = fee;
        this.weight = weight;
    }

    public float getWeight() {
        return weight;
    }

    public float getFee() {
        return fee;
    }
}
