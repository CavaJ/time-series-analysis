package com.rb.tsa;

import java.util.Set;
import java.util.TreeMap;

//multiton class to hold variable ranges for each dataset
public class VarRanges
{
    private Dataset dataset;

    //main data structure which will map var name to ranges
    TreeMap<String, Ranges> varAndRangesMap;

    //constructor
    public VarRanges(Dataset dataset)
    {
        this.dataset = dataset;
        varAndRangesMap = new TreeMap<>();
    } // VarRanges

    //add method which adds variable and its ranges to the data structure
    public void add(String varName, Ranges ranges)
    {
        //report if replace occurs
        if(varAndRangesMap.containsKey(varName)) System.err.println("Ranges for variable: " + varName + "are replaced");

        varAndRangesMap.put(varName, ranges);
    } // add


    //method to get all vars
    public Set<String> varNames()
    {
        return varAndRangesMap.keySet();
    } // varNames

    //TODO make this class multiton
} // class VarRanges
