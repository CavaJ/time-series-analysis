package com.rb.tsa;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

//class to handle multivariate time series of patient files
public class MultivariateTimeSeries implements Serializable, Comparable<MultivariateTimeSeries>
{
    //dataset of this multivariate time series
    private Dataset dataset;

    //unique record id
    private int recordID;

    //time stamps hash to make this mtse comparable to other with same recordID and dataset (especially during embedding)
    private int tssHash;


    //TODO implement masking for multivariate time series


    //horizontality and verticality are determined by the positioning of values of each variable
    //main data structure to hold the time series; ts -> (var -> varValue)
    //    var1 var2 var3
    //ts1
    //ts2
    TreeMap<Integer, TreeMap<String, Float>> verticalData; // gives us a change to decrease, increase, change timestamps

    //var -> (ts -> varValue) // over ordered time stamps
    //     ts1 ts2 ts3 ts4
    //var1
    //var2
    TreeMap<String, TreeMap<Integer, Float>> horizontalData; // gives us a chance to increase, decrease, change variables

    //constructor
    //will take a record id, tree set of time stamps, tree set of var names, and list of value arrays of corresponding vars
    public MultivariateTimeSeries(Dataset dataset, int recordID, List<Integer> timeStamps, List<String> varNames, List<List<Float>> varValuesInTsOrder)
    {
        //prechecks
        if(timeStamps.isEmpty() || varNames.isEmpty() || varValuesInTsOrder.isEmpty() || atLeastOneEmpty(varValuesInTsOrder))
            throw new RuntimeException("Provided collections cannot be empty");

        if(varValuesInTsOrder.size() != varNames.size())
            throw new RuntimeException("var names, and their corresponding values should have the same size!");

        for(List<Float> thisVarValues : varValuesInTsOrder)
        {
            if(thisVarValues.size() != timeStamps.size())
                throw new RuntimeException("Number of time stamps and number of variable values should be the same");
        } // for


        //assign dataset
        this.dataset = dataset;
        //assign record id
        this.recordID = recordID;
        //assign tss hash //TODO this can be problematic when some timestamps are removed from the data structure
        this.tssHash = timeStamps.hashCode();


        //convert varNames to array
        String[] varNamesArray = varNames.toArray(new String[]{});
        //convert time stamps to array
        Integer[] tsArray = timeStamps.toArray(new Integer[]{});


        //initialize horizontal data
        horizontalData = new TreeMap<>();
        for(int varIndex = 0; varIndex < varNamesArray.length; varIndex ++)
        {
            TreeMap<Integer, Float> tsVarValueMap = new TreeMap<>();

            //populate horizontal data
            for(int tsIndex = 0; tsIndex < tsArray.length; tsIndex ++)
            {
                tsVarValueMap.put(tsArray[tsIndex], varValuesInTsOrder.get(varIndex).get(tsIndex));
            }

            horizontalData.put(varNamesArray[varIndex], tsVarValueMap);
        } // for


        //initialize vertical data
        verticalData = new TreeMap<>();
        for(int tsIndex = 0; tsIndex < tsArray.length; tsIndex ++)
        {
            TreeMap<String, Float> varValueMap = new TreeMap<>();

            for(int varIndex = 0; varIndex < varNamesArray.length; varIndex ++)
            {
                varValueMap.put(varNamesArray[varIndex], varValuesInTsOrder.get(varIndex).get(tsIndex));
            } // for

            verticalData.put(tsArray[tsIndex], varValueMap);
        } // for

    } // MultivariateTimeSeries


    //private method to update vertical data when horizontal data are updated
    private void updateVerticalData()
    {
        for (Integer ts : verticalData.keySet())
        {
            TreeMap<String, Float> varVarValueMap = new TreeMap<>();

            for (String varName : horizontalData.keySet())
            {
                varVarValueMap.put(varName, horizontalData.get(varName).get(ts));
            } // for

            verticalData.put(ts, varVarValueMap);
        } // for
    } // updateVerticalData


    //private method to update horizontal data when vertical data are updated
    private void updateHorizontalData()
    {
        for(String varName : horizontalData.keySet())
        {
            TreeMap<Integer, Float> tsVarValueMap = new TreeMap<>();

             for(Integer ts : verticalData.keySet())
             {
                 tsVarValueMap.put(ts, verticalData.get(ts).get(varName));
             } // for

            horizontalData.put(varName, tsVarValueMap);
        } // for

    } // updateHorizontalData


    private <T> boolean atLeastOneEmpty(Collection<? extends Collection<T>> complexCollection)
    {
        for(Collection<T> element : complexCollection)
        {
            if(element.isEmpty())
                return true;
        } // for

        return false;
    } // atLeastOneEmpty


    //to vertical string method
    public String toVerticalString()
    {
        List<String> varNames = new ArrayList<>(horizontalData.keySet());
        varNames.add(0, "tsMinutes");

        //padding length
        int defaultPaddingLength = Utils.defaultPaddingLength(varNames);

        //now for each variable update string builder
        StringBuilder sb = new StringBuilder(Utils.padLeftSpaces(varNames.get(0), defaultPaddingLength));
        for(int index = 1; index < varNames.size(); index ++)
            sb.append(",").append(Utils.padLeftSpaces(varNames.get(index), defaultPaddingLength));
        sb.append("\n");


        //now for each time stamp append values
        for(Integer ts : verticalData.keySet())
        {
            sb.append(Utils.padLeftSpaces(ts + "", defaultPaddingLength));
            TreeMap<String, Float> varVarValuesMap = verticalData.get(ts);

            for(String varName : varVarValuesMap.keySet())
            {
                sb.append(",").append(Utils.padLeftSpaces(varVarValuesMap.get(varName).toString(), defaultPaddingLength));
            } // for
            sb.append("\n");
        } // for

        return sb.toString();
    } // toVerticalString


    //toHorizontalString method
    public String toHorizontalString()
    {
        //default padding for vars
        int defaultPaddingLengthForVars = Utils.defaultPaddingLength(horizontalData.keySet());

        List<Integer> timeStamps = new ArrayList<>(verticalData.keySet());
        String[] stringTimeStamps = new String[timeStamps.size()];
        for(int index = 0; index < stringTimeStamps.length; index ++) stringTimeStamps[index] = "tsm: " + timeStamps.get(index);
        int defaultPaddingLengthForTimestamps = Utils.defaultPaddingLength(stringTimeStamps);

        //choose the padding length which is bigger
        int finalPaddingLength = Math.max(defaultPaddingLengthForTimestamps, defaultPaddingLengthForVars);

        String[] paddedStringTimeStamps = new String[stringTimeStamps.length];
        for(int index = 0; index < paddedStringTimeStamps.length; index ++)
            paddedStringTimeStamps[index] = Utils.padLeftSpaces(stringTimeStamps[index], finalPaddingLength);

        //update string builder with initial empty padded string
        StringBuilder sb = new StringBuilder(Utils.padLeftSpaces("", defaultPaddingLengthForVars + 1)); //+1 for considering a space for comma
        //append time stamps for the header line
        sb.append(String.join(",", paddedStringTimeStamps));
        //append new line
        sb.append("\n");

        //now print values for each var
        for(String var : horizontalData.keySet())
        {
            sb.append(Utils.padLeftSpaces(var, defaultPaddingLengthForVars));
            TreeMap<Integer, Float> tsVarValuesMap = horizontalData.get(var);

            for(Integer ts : tsVarValuesMap.keySet())
            {
                sb.append(",").append(Utils.padLeftSpaces(tsVarValuesMap.get(ts) + "", finalPaddingLength));
            } // for

            //new line
            sb.append("\n");
        } // for

        return sb.toString();
    } // toHorizontalString


    //toString method executes toVerticalString method
    public String toString()
    {
        return toVerticalString();
    } // toString

    //TODO implement toCSVString method without any padding but similar to toVerticalString()


    //get method for vars
    public List<String> getVars()
    {
        return new ArrayList<>(horizontalData.keySet());
    } // getVars

    //get method for time stamps
    public List<Integer> getTimeStamps()
    {
        return new ArrayList<>(verticalData.keySet());
    } // getTimeStamps


    //get variable its ts ordered values as a map
    public Map<String, List<Float>> getVarValuesInTsOrder()
    {
        HashMap<String, List<Float>> varValuesMapInTsOrder = new HashMap<>();
        for(String var : horizontalData.keySet())
        {
            TreeMap<Integer, Float> tsVarValues = horizontalData.get(var);
            List<Float> thisVarValuesInTsOrder = new ArrayList<>();
            for(Integer ts : tsVarValues.keySet())
                thisVarValuesInTsOrder.add(tsVarValues.get(ts));

            varValuesMapInTsOrder.put(var, thisVarValuesInTsOrder);
        } // for

        return varValuesMapInTsOrder;
    } // getVarValuesInTsOrder


    //set method for setting var values in ts order
    public void setVarValuesInTsOrder(Map<String, List<Float>> newVarValues)
    {
        for(String var : newVarValues.keySet())
        {
            List<Float> newValuesOfThisVar = newVarValues.get(var);

            //map associated with this var in horizontal data
            TreeMap<Integer, Float> tsVarValues = horizontalData.get(var);
            Integer[] tss = tsVarValues.keySet().toArray(new Integer[]{});

            if(tss.length != newValuesOfThisVar.size())
                throw new RuntimeException("number of timestamps does not agree");

            for(int tsIndex = 0; tsIndex < tss.length; tsIndex ++)
            {
                //update the internal map of horizontal data, tsVarValues references it
                tsVarValues.put(tss[tsIndex], newValuesOfThisVar.get(tsIndex));
            } // for each ts

        } // for each var

        //horizontal data are updated, so update vertical data too
        updateVerticalData();
    } // setVarValuesInTsOrder


    //getter method for record id
    public int getRecordID() {
        return recordID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultivariateTimeSeries that = (MultivariateTimeSeries) o;
        return  dataset == that.dataset
                && recordID == that.recordID
                && tssHash == that.tssHash;
    }

    @Override
    public int hashCode()
    {
        //we might have time series of the same record id with possibly and different time stamps
        return Objects.hash(dataset, recordID, tssHash); //getVars().size()); mtses having the same vars can be compared
    }


    //compareTo method for natural ordering
    public int compareTo(MultivariateTimeSeries other)
    {
        int result = dataset.compareTo(other.dataset);
        if(result == 0)
        {
            result = Integer.compare(recordID, other.recordID);
            if(result == 0)
                result = Integer.compare(tssHash, other.tssHash);
        } // if

        return result;
    } // compareTo


    //helper method to create MultivariateTimeSeries from file
    //it will parse the file in the following format:
    //    var1 var2
    //ts1
    //ts2
    public static MultivariateTimeSeries fromFile(Dataset dataset, String localFilePath, String lineComponentDelimiter, float floatMissingValuePlaceHolder)
    {
        //all files are actually record ids
        String fileName = Utils.fileNameFromPath(localFilePath);
        int recordID = Integer.parseInt(FilenameUtils.removeExtension(fileName));


        //obtain file contents
        String fileContents = Utils.fileContentsFromLocalFilePath(localFilePath);
        //split the contents to lines
        String[] lines = StringUtils.split(fileContents, "\r\n|\r|\n");

        //tree set of time stamps
        List<Integer> timeStamps = new ArrayList<>();

        //obtain var names by splitting the first line
        String[] varNamesArray = lines[0].split(lineComponentDelimiter);
        //trim every element
        for(int index = 0; index < varNamesArray.length; index ++) varNamesArray[index] = varNamesArray[index].trim();
        // skip the first index which is "tsMinutes"
        List<String> varNames = Arrays.asList(varNamesArray).subList(1, varNamesArray.length);


        //list to hold the list of values of each var in time stamp order
        //List<List<Float>> varValuesInTsOrder = new ArrayList<>(varNames.size()); // have the same size as the number of vars

        //var and its values in ts order
        LinkedHashMap<String, List<Float>> varVarValuesMap = new LinkedHashMap<>();


        //for each line parse the line by collecting relevant information, skip first line which is the header line
        for(int lineIndex = 1; lineIndex < lines.length; lineIndex ++)
        {
            String thisLine = lines[lineIndex];

            //split the line with delimiter
            String[] thisLineComponents = thisLine.split(lineComponentDelimiter);
            for(int componentIndex = 0; componentIndex < thisLineComponents.length; componentIndex ++)
                thisLineComponents[componentIndex] = thisLineComponents[componentIndex].trim(); // trim every element


            //the first component is tsMinutes, which is integer
            int tsMinutes = Integer.parseInt(thisLineComponents[0]);
            timeStamps.add(tsMinutes);


            //start from the component 1 which is contains the value of the first variable
            for (int varIndex = 1; varIndex < thisLineComponents.length; varIndex++)
            {
                //if list does not exist for this var, create it
                if (!varVarValuesMap.containsKey(varNamesArray[varIndex])) varVarValuesMap.put(varNamesArray[varIndex], new ArrayList<>());

                //obtain values
                List<Float> valuesInTsOrder = varVarValuesMap.get(varNamesArray[varIndex]);

                //if it is a float parsable, then assign float value, otherwise assign Float.POSITIVE_INFINITY (which means, missing value)
                if(Utils.isFloat(thisLineComponents[varIndex]))
                    valuesInTsOrder.add(Float.valueOf(thisLineComponents[varIndex]));
                else
                    valuesInTsOrder.add(floatMissingValuePlaceHolder);
                                   // .add(-1.0f);
                                   //.add(Float.POSITIVE_INFINITY);
            } // for each line component

        } // for each line

        return new MultivariateTimeSeries(dataset, recordID, timeStamps, varNames, new ArrayList<>(varVarValuesMap.values()));
    } // fromFile


    //get var values in ts order
    private List<List<Float>> listOfVarValuesInTsOrder()
    {
        List<List<Float>> varValuesInTsOrder = new ArrayList<>();
        for(String var : horizontalData.keySet())
        {
            TreeMap<Integer, Float> tsVarValues = horizontalData.get(var);
            List<Float> thisVarValuesInTsOrder = new ArrayList<>();
            for(Integer ts : tsVarValues.keySet())
                thisVarValuesInTsOrder.add(tsVarValues.get(ts));

            varValuesInTsOrder.add(thisVarValuesInTsOrder);
        } // for

        return varValuesInTsOrder;
    } // listOfVarValuesInTsOrder


    //helper method to copy this multivariate time series
    public MultivariateTimeSeries deepCopy()
    {
        return new MultivariateTimeSeries(dataset, getRecordID(), getTimeStamps(), getVars(), listOfVarValuesInTsOrder());
    } // deepCopy

} // class MultivariateTimeSeries
