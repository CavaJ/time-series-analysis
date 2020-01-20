package com.rb.tsa;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;

//class to hold all outcomes from all different files
public class Outcomes
{
    //main data structure to hold the outcomes
    //it will map from integer to outcome
    private LinkedHashMap<Integer, Outcome> outcomes;

    //dataset - this "outcomes" is attached to
    private Dataset dataset;


    //multiton pattern, for each dataset there can be only one "Outcomes"
    private static HashMap<Dataset, Outcomes> multiton = new HashMap<>();


    //static method to access multiton
    public static Outcomes getInstance(Dataset dataset)
    {
        //if multiton does not contain outcomes for this dataset
        if(!multiton.containsKey(dataset)) multiton.put(dataset, new Outcomes(dataset));

        return multiton.get(dataset);
    } // getInstance


    //constructor
    private Outcomes(Dataset dataset)
    {
        this.dataset = dataset;
        outcomes = new LinkedHashMap<>();
    } // Outcomes

    //emptiness checker method
    public boolean isEmpty()
    {
        return outcomes.isEmpty();
    }

    //method to add an outcome and its corresponding record id
    public void add(int recordID, Outcome outcome)
    {
        //report if replace occurs
        if(outcomes.containsKey(recordID)) System.err.println("Outcomes for recordID: " + recordID + "is replaced");

        outcomes.put(recordID, outcome);
    } // add

    //method for getting outcome by recordID
    public Outcome get(int recordID)
    {
        return outcomes.get(recordID);
    } // get


    //method to obtain record ids
    public Set<Integer> getRecordIDs()
    {
        return outcomes.keySet();
    }


    //get the dataset this "outcomes" belongs to
    public Dataset getDataset() {
        return dataset;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Outcomes)) return false; // it is ok even if o is null
        return this.hashCode() == ((Outcomes) o).hashCode();
    }

    //hash code is determined by the dataset, since the same dataset cannot have two "outcomes" instances
    @Override
    public int hashCode() {
        return Objects.hash(dataset);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        for(Outcome outcome : outcomes.values())
        {
            sb.append(outcome).append("\n");
        } // for

        return sb.toString();
    } // toString


    //TODO Outcome class can be defined as a private class inside this class
    //private Outcome instance can be instantiated from getInstance() method from its parameters
    //which will have the same parameters as the constructor of Outcome class

} // class Outcomes
