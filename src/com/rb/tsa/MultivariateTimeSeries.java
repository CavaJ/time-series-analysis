package com.rb.tsa;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

//class to handle multivariate time series of patient files
public class MultivariateTimeSeries
{
    //unique record id
    private int recordID;

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
    public MultivariateTimeSeries(int recordID, List<Integer> timeStamps, List<String> varNames, List<List<Float>> varValuesInTsOrder)
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


        //assign record id
        this.recordID = recordID;


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
            for(String varName : verticalData.get(ts).keySet())
            {
                sb.append(",").append(Utils.padLeftSpaces(verticalData.get(ts).get(varName).toString(), defaultPaddingLength));
            } // for
            sb.append("\n");
        } // for

        return sb.toString();
    } // toVerticalString


    //TODO implement toHorizontalString method



    //helper method to create MultivariateTimeSeries from file
    //it will parse the file in the following format:
    //    var1 var2
    //ts1
    //ts2
    public static MultivariateTimeSeries fromFile(String localFilePath, String lineComponentDelimiter)
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
                    valuesInTsOrder.add(-1.0f);
                                   //.add(Float.POSITIVE_INFINITY);
            } // for each line component

        } // for each line

        return new MultivariateTimeSeries(recordID, timeStamps, varNames, new ArrayList<>(varVarValuesMap.values()));
    } // fromFile

} // class MultivariateTimeSeries
