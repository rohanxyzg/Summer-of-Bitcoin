package com.example.bitcoin;

import com.example.transaction.Transaction;
import com.example.transaction.TransactionCluster;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Bitcoin {

    static Map<String, Boolean> visited;
    static Map<String, List<String>> map;
    static Map<String, Transaction> txIdToTransactionMap;
    static float totalWeight;
    static float totalFee;
    static boolean isCycle;
    static List<String> stack;
    static Map<Integer, TransactionCluster> transactionClusterMap;
    static List<TransactionCluster> transactionClusterList;
    static Map<String, Boolean> recStack;
    static Map<String, Boolean> visitedCycle;
    static List<String> block;
    static void preCompute(String txID, String fee, String weight, String parentString) {
        String[] parents = new String[0];
        if(parentString != null) {
            parents = parentString.split(";");
        }
        List<String> parentTxIDList = new ArrayList<>();
        for(String par : parents){
            parentTxIDList.add(par);
            List<String> adj = map.get(par);
            if(adj!=null){
                adj.add(txID);
                map.put(par,adj);
            }
            else{
                adj = new ArrayList<>();
                adj.add(txID);
                map.put(par,adj);
            }
        }
        txIdToTransactionMap.put(txID, new Transaction(txID, Integer.parseInt(fee), (float) Math.ceil((float)Integer.parseInt(weight)/1000.0F)));
        visited.put(txID, false);
        visitedCycle.put(txID, false);
        recStack.put(txID, false);
    }

    private static void dfs(String currentTransactionTxID){
        visited.put(currentTransactionTxID, true);
        Transaction currentTransaction = txIdToTransactionMap.get(currentTransactionTxID);
        totalWeight += currentTransaction.getWeight();
        totalFee += currentTransaction.getFee();
        if(map.get(currentTransactionTxID) != null) {
            for (String child : map.get(currentTransactionTxID)) {
                if (!visited.get(child)) {
                    dfs(child);
                }
            }
        }
        stack.add(currentTransactionTxID);
    }

    static boolean isCyclicUtil(String currentTransactionTxID)
    {
        if (recStack.get(currentTransactionTxID))
            return true;

        if (visitedCycle.get(currentTransactionTxID))
            return false;

        visitedCycle.put(currentTransactionTxID,true);

        recStack.put(currentTransactionTxID,true);
        List<String> child = map.get(currentTransactionTxID);
        if(child != null) {
            for (String c : child) {
                if (isCyclicUtil(c))
                    return true;
            }
        }
        recStack.put(currentTransactionTxID,false);

        return false;
    }


    static void dp(){
        int len = transactionClusterList.size();
        int W = 4000000/1000;
        float[][] K = new float[len + 1][W + 1];

        // Build table K[][] in bottom up manner
        for (int i = 0; i <= len; i++) {
            for (int w = 0; w <= W; w++) {
                if (i == 0 || w == 0) {
                    K[i][w] = 0;
                } else if (transactionClusterList.get(i-1).getTotalWeight() <= w) {
                    K[i][w] = Math.max(transactionClusterList.get(i-1).getTotalFee() + K[i - 1][w - (int)transactionClusterList.get(i-1).getTotalWeight()], K[i - 1][w]);
                } else {
                    K[i][w] = K[i - 1][w];
                }
            }
        }

        // stores the result of Knapsack
        float res = K[len][W];

        float w = W;

        for (int i = len; i > 0 && res > 0 && w>=0; i--) {
            if (res == K[i - 1][(int)w]) {
                continue;
            } else {
                for (String taxID : transactionClusterList.get(i - 1).getTx_ids()) {
                    block.add(taxID);
                }
                res = res - transactionClusterList.get(i - 1).getTotalFee();
                w = w - transactionClusterList.get(i - 1).getTotalWeight();
                block.add("\n");

            }
        }

    }
    public static void main(String args[]){
        String path = "./storage/mempool.csv";
        String line = "";
        map = new HashMap<>();
        txIdToTransactionMap = new HashMap<>();
        visited = new HashMap<>();
        visitedCycle = new HashMap<>();
        recStack = new HashMap<>();
        block = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            while((line = br.readLine()) != null) {
                String[] values = line.split(",");
                preCompute(values[0], values[1], values[2], (values.length > 3)?values[3]:null);
            }
            int id = 0;
            isCycle = false;
            stack = new ArrayList<>();

            transactionClusterMap = new HashMap<>();
            transactionClusterList = new ArrayList<>();
            for(String txID : map.keySet()){
                if(!visited.get(txID)) {
                    totalWeight = 0.0F;
                    totalFee = 0.0F;
                    stack.clear();
                    dfs(txID);
                    boolean isCyclic = isCyclicUtil(txID);
                    if(!isCyclic) {
                        Collections.reverse(stack);
                        TransactionCluster transactionCluster = new TransactionCluster(stack, totalFee, totalWeight);
                        transactionClusterList.add(transactionCluster);
                        transactionClusterMap.put(id, transactionCluster);
                        id++;
                    }

                }
            }
            dp();

            FileWriter myWriter = new FileWriter("./storage/block.txt");
            for(String blockTaxID : block){
                myWriter.write(blockTaxID+"\n");
            }
            myWriter.close();



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
