package com.rb.tsa;

import org.apache.commons.io.FilenameUtils;

import javax.print.DocFlavor;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Launcher
{
    private static final String DATA_FOLDER = "C:\\Users\\rbabayev\\Downloads\\physionet-challenge\\original_data";

    //to check whether the variable naming is consistent throughout the dataset
    //private static HashSet<String> differentVariableNames = new HashSet<String>();


    public static void main(String[] args)
    {
	    String setADir = DATA_FOLDER + "\\set-a";
        String setBDir = DATA_FOLDER + "\\set-b";
        String setCDir = DATA_FOLDER + "\\set-c";

        HashSet<String> varsToDiscard = new HashSet<>();
        //Variables to discard: RecordID, Age, Gender, Height, ICUType, Weight
        varsToDiscard.add("RecordID");
        varsToDiscard.add("Age");
        varsToDiscard.add("Gender");
        varsToDiscard.add("Height");
        varsToDiscard.add("ICUType");
        //varsToDiscard.add("Weight"); // both general descriptor and time series variable, handled carefully


        String[] vars = new String[] {"Albumin", "ALP", "ALT", "AST", "Bilirubin", "BUN", "Cholesterol", "Creatinine",
                "DiasABP", "FiO2", "GCS", "Glucose", "HCO3", "HCT", "HR", "K", "Lactate", "Mg", "MAP", "MechVent", "Na",
                "NIDiasABP", "NIMAP", "NISysABP", "PaCO2", "PaO2", "pH", "Platelets", "RespRate", "SaO2",
                "SysABP", "Temp", "TroponinI", "TroponinT", "Urine", "WBC", "Weight"};
        TreeSet<String> consideredVars = new TreeSet<String>(Arrays.asList(vars));


        //populate global hashset
        //differentVariableNames.addAll(varsToDiscard);
        //differentVariableNames.addAll(consideredVars);

        //HashSet<String> initial = new HashSet<>(differentVariableNames);

        //output different variable names
        //System.out.println("Number of different variables in PhysioNet dataset: " + differentVariableNames.size());


        //list files in the local file path
        List<String> setAFilePaths = Utils.listFilesFromLocalPath(setADir, false);
        List<String> setBFilePaths = Utils.listFilesFromLocalPath(setBDir, false);
        List<String> setCFilePaths = Utils.listFilesFromLocalPath(setCDir, false);

        List<String> allFilePaths = new ArrayList<>(setAFilePaths);
        allFilePaths.addAll(setBFilePaths);
        allFilePaths.addAll(setCFilePaths);


        generateTimeSeriesData(varsToDiscard, consideredVars, allFilePaths);



        //System.out.println("Number of different variables in PhysioNet dataset (after pre_processing): " + differentVariableNames.size());
        //differentVariableNames.removeAll(initial);
        //System.out.println(differentVariableNames.size());


    } // main


    //helper method to construct time series data in the following structure:
    //ts1, var1Value, var2Value, var3Value, ....
    //ts2, var1Value, var2Value, var3Value, ....
    //...
    public static void generateTimeSeriesData(HashSet<String> varsToDiscard, TreeSet<String> consideredVars, List<String> allFilePaths)
    {
        //default padding length is the length of the variable with the longest name
        int defaultPaddingLength = 0;
        for(String var : consideredVars)
        {
            if(var.length() > defaultPaddingLength)
                defaultPaddingLength = var.length();
        } // for


        //list will keep the insertion order
        List<String> paddedConsideredVars = new ArrayList<>();
        for(String var : consideredVars)
            paddedConsideredVars.add(Utils.padLeftSpaces(var, defaultPaddingLength));

        String firstLineOfANewFile = Utils.padLeftSpaces("tsMinutes", defaultPaddingLength)
                + "," + String.join(",", paddedConsideredVars);
        List<String> firstLineComponents = new ArrayList<>(consideredVars); firstLineComponents.add(0, "tsMinutes");

        for(String filePath : allFilePaths)
        {
            //System.out.println("Processing file: " + filePath);

            String fileContents = Utils.fileContentsFromLocalFilePath(filePath);


            //StringBuilder sb = new StringBuilder("");
            TreeMap<Integer, String[]> tsLineValuesMap = new TreeMap<Integer, String[]>();

            //obtain a scanner for file contents string
            try (Scanner scanner = new Scanner(fileContents))
            {
                //discard first line
                //first line of the file is in the form => Time,Parameter,Value
                scanner.nextLine();


                int lineNumber = 1;

                //StringBuilder sb;
                String[] lineValues;

                //now read the fileContents line by line
                while (scanner.hasNextLine())
                {
                    //read the fileContents line by line
                    String line = scanner.nextLine();


                    lineNumber ++;

                    //the line structure is the following:
                    //time_stamp,variable_name,variable_value
                    String[] lineComponents = line.split(",");

                    String timeStampString = lineComponents[0];
                    int timeStampMinutes = Utils.toMinutes(timeStampString);

                    //for each time stamp create new entry in the tree map
                    lineValues = tsLineValuesMap.get(timeStampMinutes); // returns null if there is no such a key
                    if(lineValues == null) {
                        lineValues = new String[firstLineComponents.size()]; // with the size equal to number of vars
                        for(int i = 0; i < firstLineComponents.size(); i++)
                            lineValues[i] = Utils.padLeftSpaces("-", defaultPaddingLength); //firstLineComponents.get(i).length());

                        //add time stamp value first
                        //which will be in the index 0
                        lineValues[0] = Utils.padLeftSpaces(timeStampMinutes + "", defaultPaddingLength); //firstLineComponents.get(0).length());

                        //put it to the map
                        tsLineValuesMap.put(timeStampMinutes, lineValues);
                    }

                    String variableName = lineComponents[1];

                    String variableValue = lineComponents[2];

                    //handle general descriptor variables; handle weight carefully for timestamp 0
                    if(varsToDiscard.contains(variableName))
                    {
                        //TODO do something
                    }
                    else if(variableName.equals("Weight") && timeStampMinutes == 0)
                    {
                        //TODO do something
                    }
                    else {
                        //get the index of variable name
                        int index = firstLineComponents.indexOf(variableName);
                        //add the variable value to the corresponding index, it will update the map values implicitly
                        lineValues[index] = Utils.padLeftSpaces(variableValue, defaultPaddingLength); //firstLineComponents.get(index).length());
                    }

                } // while


                //now build a string from the values of variables and create a new file
                StringBuilder sb = new StringBuilder(firstLineOfANewFile).append("\n");
                //now for each key-value pair, add it to the sb
                for(Integer key : tsLineValuesMap.keySet())
                {
                    String[] lineVals = tsLineValuesMap.get(key);
                    sb.append(String.join(",", lineVals)).append("\n");
                } // for


                //write results to local file system
                Utils.writeToLocalFileSystem(filePath, "prepro", sb.toString(), "csv");


            } catch (Exception ex) {
                ex.printStackTrace();


            } // catch


        } // for

    } // generateTimeSeriesData



//    public static void generateTimeSeriesData2(HashSet<String> varsToDiscard, TreeSet<String> consideredVars, List<String> allFilePaths)
//    {
//        for(String filePath : allFilePaths)
//        {
//            //System.out.println("Processing file: " + filePath);
//
//            String fileContents = Utils.fileContentsFromLocalFilePath(filePath);
//
//            //a variable name -> timestamped value list
//            TreeMap<String, String[]> varTsValuesMap = new TreeMap<String, String[]>();
//
//
//            //obtain a scanner for file contents string
//            try (Scanner scanner = new Scanner(fileContents))
//            {
//                //discard first line
//                //first line of the file is in the form => Time,Parameter,Value
//                scanner.nextLine();
//
//
//                int lineNumber = 1;
//
//                //StringBuilder sb;
//                String[] lineValues;
//
//                //now read the fileContents line by line
//                while (scanner.hasNextLine())
//                {
//                    //read the fileContents line by line
//                    String line = scanner.nextLine();
//
//
//                    lineNumber ++;
//
//                    //the line structure is the following:
//                    //time_stamp,variable_name,variable_value
//                    String[] lineComponents = line.split(",");
//
//                    String timeStampString = lineComponents[0];
//                    int timeStampMinutes = Utils.toMinutes(timeStampString);
//
//                    //for each time stamp create new entry in the tree map
//                    lineValues = tsLineValuesMap.get(timeStampMinutes); // returns null if there is no such a key
//                    if(lineValues == null) {
//                        lineValues = new String[firstLineComponents.size()]; // with the size equal to number of vars
//                        for(int i = 0; i < firstLineComponents.size(); i++)
//                            lineValues[i] = Utils.padLeftSpaces("-", defaultPaddingLength); //firstLineComponents.get(i).length());
//
//                        //add time stamp value first
//                        //which will be in the index 0
//                        lineValues[0] = Utils.padLeftSpaces(timeStampMinutes + "", defaultPaddingLength); //firstLineComponents.get(0).length());
//
//                        //put it to the map
//                        tsLineValuesMap.put(timeStampMinutes, lineValues);
//                    }
//
//                    String variableName = lineComponents[1];
//
//                    String variableValue = lineComponents[2];
//
//                    //handle general descriptor variables; handle weight carefully for timestamp 0
//                    if(varsToDiscard.contains(variableName))
//                    {
//                        //TODO do something
//                    }
//                    else if(variableName.equals("Weight") && timeStampMinutes == 0)
//                    {
//                        //TODO do something
//                    }
//                    else {
//                        //get the index of variable name
//                        int index = firstLineComponents.indexOf(variableName);
//                        //add the variable value to the corresponding index, it will update the map values implicitly
//                        lineValues[index] = Utils.padLeftSpaces(variableValue, defaultPaddingLength); //firstLineComponents.get(index).length());
//                    }
//
//                } // while
//
//
//                //now build a string from the values of variables and create a new file
//                StringBuilder sb = new StringBuilder(firstLineOfANewFile).append("\n");
//                //now for each key-value pair, add it to the sb
//                for(Integer key : tsLineValuesMap.keySet())
//                {
//                    String[] lineVals = tsLineValuesMap.get(key);
//                    sb.append(String.join(",", lineVals)).append("\n");
//                } // for
//
//
//                //write results to local file system
//                Utils.writeToLocalFileSystem(filePath, "prepro", sb.toString(), "csv");
//
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//
//
//            } // catch
//
//
//        } // for
//    }


} // class Launcher
