package com.rb.tsa;



import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.math.RoundingMode;
import java.util.*;

public class Launcher {
    private static final String DATA_FOLDER = "C:\\Users\\rbabayev\\Downloads\\physionet-challenge\\original_data";

    //to check whether the variable naming is consistent throughout the dataset
    //private static HashSet<String> differentVariableNames = new HashSet<String>();


    public static void main(String[] args) {
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


        String[] vars = new String[]{"Albumin", "ALP", "ALT", "AST", "Bilirubin", "BUN", "Cholesterol", "Creatinine",
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


        //generateTimeSeriesData(varsToDiscard, consideredVars, allFilePaths, "");
        //generateTimeSeriesData2(varsToDiscard, consideredVars, allFilePaths);

        List<String> setAPreProFilePaths = Utils.listFilesFromLocalPath(setADir + File.separator + "prepro", false);
        List<String> setBPreProFilePaths = Utils.listFilesFromLocalPath(setBDir + File.separator + "prepro", false);
        List<String> setCPreProFilePaths = Utils.listFilesFromLocalPath(setCDir + File.separator + "prepro", false);

        List<String> allPreProFilePaths = new ArrayList<>();
        allPreProFilePaths.addAll(setAPreProFilePaths);
        allPreProFilePaths.addAll(setBPreProFilePaths);
        allPreProFilePaths.addAll(setCPreProFilePaths);
//
//        for(String localFilePath : allPreProFilePaths)
//            Utils.csv2Arff(localFilePath, "1-38");


        //combineVars(consideredVars, allPreProFilePaths, "", BloodPressurePreference.PreferInvasive);
        //for(String varName : newConsideredVars)
        //    System.out.print("\"" + varName + "\", ");


        List<String> setAVarJoinFilePaths = Utils.listFilesFromLocalPath(setADir + File.separator + "prepro" + File.separator + "varJoin", false);
        List<String> setBVarJoinFilePaths = Utils.listFilesFromLocalPath(setBDir + File.separator + "prepro" + File.separator + "varJoin", false);
        List<String> setCVarJoinFilePaths = Utils.listFilesFromLocalPath(setCDir + File.separator + "prepro" + File.separator + "varJoin", false);
        List<String> allVarJoinFilePaths = new ArrayList<>();
        allVarJoinFilePaths.addAll(setAVarJoinFilePaths);
        allVarJoinFilePaths.addAll(setBVarJoinFilePaths);
        allVarJoinFilePaths.addAll(setCVarJoinFilePaths);

        //for(String localFilePath : allVarJoinFilePaths)
        //    Utils.csv2Arff(localFilePath, "1-35");

        //TODO check which variable has all values missing in each file and which lines has all values missing in each file
        String[] newVars = new String[] {"(NI)DiasABP", "(NI)MAP", "(NI)SysABP", "ALP", "ALT", "AST", "Albumin", "BUN",
                "Bilirubin", "Cholesterol", "Creatinine", "FiO2", "GCS", "Glucose", "HCO3", "HCT", "HR", "K", "Lactate",
                "MechVent", "Mg", "Na", "PaCO2", "PaO2", "Platelets", "RespRate", "SaO2", "Temp", "TroponinI", "TroponinT",
                "Urine", "WBC", "Weight", "pH"};
        TreeSet<String> newConsideredVars = new TreeSet<String>(Arrays.asList(newVars));
        checkAndFix(consideredVars, allVarJoinFilePaths); // TODO first check which lines are empty in each file, check MechVent


        //TODO join 6 variables together to obtain 33 variables
        //TODO remove outliers?
        //TODO incorporate outcomes and general descriptors in data pre-processing routine
        //TODO handle empty variables, empty files and missing data


        //System.out.println("Number of different variables in PhysioNet dataset (after pre_processing): " + differentVariableNames.size());
        //differentVariableNames.removeAll(initial);
        //System.out.println(differentVariableNames.size());


    } // main

    private static void checkAndFix(TreeSet<String> consideredVars, List<String> allVarJoinFilePaths)
    {
        
    } // checkAndFix


    //helper method to combine:
    //NIDiasABP with DiasABP
    //NIMAP with MAP
    //NISysABP with SysABP
    public static TreeSet<String> combineVars(TreeSet<String> consideredVars, List<String> allPreProFilePaths, String missingValuePlaceHolder, BloodPressurePreference bbp)
    {
        String varDiasABP = "DiasABP";
        String varNIDiasABP = "NIDiasABP";
        String varMAP = "MAP"; // mean arterial pressure or mean arterial blood pressure (mabp)
        String varNIMAP = "NIMAP";
        String varSysABP = "SysABP";
        String varNISysABP = "NISysABP";


        //new vars
        String newVarDiasABP = "(NI)DiasABP";
        String newVarMAP = "(NI)MAP";
        String newVarSysABP = "(NI)SysABP";


        TreeSet<String> newConsideredVars = new TreeSet<>(consideredVars);
        newConsideredVars.remove(varDiasABP);
        newConsideredVars.remove(varNIDiasABP);
        newConsideredVars.remove(varMAP);
        newConsideredVars.remove(varNIMAP);
        newConsideredVars.remove(varSysABP);
        newConsideredVars.remove(varNISysABP);

        //add new vars to the tree set
        newConsideredVars.add(newVarDiasABP);
        newConsideredVars.add(newVarMAP);
        newConsideredVars.add(newVarSysABP);


        //calculate the padding length
        int defaultPaddingLength = Utils.defaultPaddingLength(newConsideredVars);


        //convert the consideredVars to the list
        ArrayList<String> consideredVarsList = new ArrayList<>(consideredVars);
        int indexOfDiasABP = consideredVarsList.indexOf(varDiasABP) + 1; // + 1 is because line contains one more variable called tsMinutes
        int indexOfNIDiasABP = consideredVarsList.indexOf(varNIDiasABP) + 1;
        int indexOfMAP = consideredVarsList.indexOf(varMAP) + 1;
        int indexOfNIMAP = consideredVarsList.indexOf(varNIMAP) + 1;
        int indexOfSysABP = consideredVarsList.indexOf(varSysABP) + 1;
        int indexOfNISysABP = consideredVarsList.indexOf(varNISysABP) + 1;

        for(String localFilePath : allPreProFilePaths)
        {
            String fileContents = Utils.fileContentsFromLocalFilePath(localFilePath);

            StringBuilder linesBuilder
                    = new StringBuilder(Utils.padLeftSpaces("tsMinutes", defaultPaddingLength));
            //now populate the first line with new considered vars
            for(String newConsideredVarName : newConsideredVars)
                linesBuilder.append(",").append(Utils.padLeftSpaces(newConsideredVarName, defaultPaddingLength));
            linesBuilder.append("\n");


            try (Scanner scanner = new Scanner(fileContents))
            {
                //discard the first line
                scanner.nextLine();

                int lineNumber = 1;

                //now read each line one by one
                while(scanner.hasNextLine())
                {
                    String thisLine = scanner.nextLine();

                    lineNumber++;

                    //now split the line into contents
                    //each line contains tsMinutes and values of variables in the alphabetical order of variable names
                    //alphabetical order of variable names are present in consideredVars argument
                    String[] thisLineComponents = thisLine.split(",");

                    //variable values starting from the index 1, because index 0 is tsMinutes
                    //mABP and niMABP will be converted to float; niSysABP, sysABP, niDiasABP, diasABP will be converted to integer
                    //if number format exception occurs then it is a missing value

                    //IMPORTANT: use trim()
                    //Integer.parseInt(" 5"); // space before; yields NumberFormatException
                    //Integer.parseInt("5 "); // space after; yields NumberFormatException

                    String diasABP = thisLineComponents[indexOfDiasABP].trim(); // invasive
                    String niDiasABP = thisLineComponents[indexOfNIDiasABP].trim(); // non-invasive

                    String sysABP = thisLineComponents[indexOfSysABP].trim(); // invasive
                    String niSysABP = thisLineComponents[indexOfNISysABP].trim(); // non-invasive

                    String mABP = thisLineComponents[indexOfMAP].trim(); // invasive
                    String niMABP = thisLineComponents[indexOfNIMAP].trim(); // non-invasive


                    //All valid values for general descriptors, time series variables, outcome-related descriptors are non-negative (≥ 0).
                    //A value of -1 indicates missing or unknown data (for example, if a patient's height was not recorded).


                    //VAR 1
                    String newDiasABP = combineIntRepresentableVars(diasABP, niDiasABP, missingValuePlaceHolder, bbp);


                    //VAR 2
                    String newSysABP = combineIntRepresentableVars(sysABP, niSysABP, missingValuePlaceHolder, bbp);


                    //VAR 3
                    String newMABP = combineFloatRepresentableVars(mABP, niMABP, missingValuePlaceHolder, bbp);


                    //TRY TO POPULATE missing sbp from (map and dbp), missing dbp from (sbp, map), or missing map from (sbp, dbp)
                    int sbp = Utils.isInteger(newSysABP) ? Integer.parseInt(newSysABP) : -1;
                    int dbp = Utils.isInteger(newDiasABP) ? Integer.parseInt(newDiasABP) : -1;
                    float map = Utils.isFloat(newMABP) ? Float.parseFloat(newMABP) : -1.0f;

                    //if newMABP value is not present, then calculate it from sbp and dbp, otherwise keep original newMABP value
                    if (sbp >= 0 && dbp >= 0 && Float.compare(map, 0.0f) < 0) // if map is missing
                    {
                        map = Utils.map(sbp, dbp); // may return -1, in case of incorrect data
                        newMABP = Float.compare(map, 0.0f) >= 0 ? map + "" : missingValuePlaceHolder;
                    }
                    else if((sbp >= 0 && dbp < 0) &&  Float.compare(map, 0.0f) >= 0) // only dbp missing, calculate it from variables map and sbp
                    {
                        dbp = Utils.dbp(map, sbp); // may return -1, in case of incorrect data
                        newDiasABP = dbp >= 0 ? dbp + "" : missingValuePlaceHolder;
                    }
                    else if((sbp < 0 && dbp >= 0) && Float.compare(map, 0.0f) >= 0)  // only sbp missing, calculate it from variables map and dbp
                    {
                        sbp = Utils.sbp(map, dbp); // may return -1, in case of incorrect data
                        newSysABP = sbp >= 0 ? sbp + "" : missingValuePlaceHolder;
                    }
                    //else map is missing, and you cannot calculate either sbp or dbp, or one of (sbp, dbp) is missing, so map cannot be calculated


                    //System.err.println("File: " + localFilePath + " => "
                    //        + lineNumber + " => mABP: " + mABP + ", niMABP: " + niMABP + ", newMABP: " + newMABP +  ", sbp: " + sbp + ", dbp: " + dbp + ", cmap: "
                    //        + "*" /*Utils.map(sbp, dbp)*/ );


                    //now populate the lines
                    //according to alphabetical order (NI)DiasABP, (NI)MAP, (NI)SysABP are ordered as written and comes before any other variable name in the list
                    //therefore, after tsMinutes, values of these variables will be present and then the values of other variables will be added
                    linesBuilder.append(Utils.padLeftSpaces(thisLineComponents[0] + "", defaultPaddingLength)); //first put ts value


                    for(String newConsideredVar : newConsideredVars)
                    {
                        //handle new vars separately, they will added in the order of if else according to alphabetical order
                        if(newConsideredVar.equals(newVarDiasABP))
                            linesBuilder.append(",").append(Utils.padLeftSpaces(newDiasABP, defaultPaddingLength));
                        else if(newConsideredVar.equals(newVarMAP))
                            linesBuilder.append(",").append(Utils.padLeftSpaces(newMABP, defaultPaddingLength));
                        else if(newConsideredVar.equals(newVarSysABP))
                            linesBuilder.append(",").append(Utils.padLeftSpaces(newSysABP, defaultPaddingLength));
                        else
                        {
                            int indexOfOtherVar = consideredVarsList.indexOf(newConsideredVar) + 1;
                            String otherVarValue = thisLineComponents[indexOfOtherVar];

                            linesBuilder.append(",").append(Utils.padLeftSpaces(otherVarValue, defaultPaddingLength));
                        } // else
                    }
                    //add the end of line
                    linesBuilder.append("\n");

                } // while

            } catch (Exception ex) {
                ex.printStackTrace();
            }


            Utils.writeToLocalFileSystem(localFilePath, "varJoin", linesBuilder.toString(), "csv");

        } // for

        //return the set of new vars
        return newConsideredVars;
    } // combineVars


    private enum BloodPressurePreference {PreferInvasive, PreferNonInvasive, PreferAverage};

    //method for combining int representable variable values
    private static String combineIntRepresentableVars(String invasiveVarValue, String nonInvasiveVarValue, String missingValuePlaceHolder, BloodPressurePreference bbp)
    {
        if(!Utils.isInteger(invasiveVarValue) && !Utils.isInteger(nonInvasiveVarValue))
            return missingValuePlaceHolder;
        else if(Utils.isInteger(invasiveVarValue) && !Utils.isInteger(nonInvasiveVarValue))
        {
            return Integer.parseInt(invasiveVarValue) >= 0 ? invasiveVarValue : missingValuePlaceHolder; // 0 is also a legal measurement
        }
        else if(!Utils.isInteger(invasiveVarValue) && Utils.isInteger(nonInvasiveVarValue))
        {
            return Integer.parseInt(nonInvasiveVarValue) >= 0 ? nonInvasiveVarValue : missingValuePlaceHolder; // 0 is also a legal measurement
        }
        else // both are integers
        {
            int val1 = Integer.parseInt(invasiveVarValue);
            int val2 = Integer.parseInt(nonInvasiveVarValue);

            if(val1 > 0 && val2 > 0)
            {
                //this paper suggests choosing invasive one over non-invasive: https://onlinelibrary.wiley.com/doi/full/10.1111/j.1365-2044.2012.07302.x
                switch(bbp)
                {
                    case PreferInvasive:
                        return val1 + "";
                    case PreferNonInvasive:
                        return val2 + "";
                    default:
                        //take their average
                        return ((val1 + val2) / 2) + ""; // integer division is OK here, since the original values are integers
                } // switch
            }
            else if(val1 > 0) // since val2 <= 0 here
            {
                return val1 + "";
            }
            else if(val2 > 0) // since val1 <= 0 here
            {
                return val2 + "";
            }
            else if(val1 == 0 && val2 == 0)
                return 0 + "";
            else // val1 < 0 && val2 < 0
            {
                return missingValuePlaceHolder;
            } // else
        } // else

    } // combineIntRepresentableVars


    private static String combineFloatRepresentableVars(String invasiveVarValue, String nonInvasiveVarValue, String missingValuePlaceHolder, BloodPressurePreference bbp)
    {
        if(!Utils.isFloat(invasiveVarValue) && !Utils.isFloat(nonInvasiveVarValue))
            return missingValuePlaceHolder;
        else if(Utils.isFloat(invasiveVarValue) && !Utils.isFloat(nonInvasiveVarValue))
        {
            // 0 is also a legal measurement
            float var1FloatValue = Float.parseFloat(invasiveVarValue);
            return Float.compare(var1FloatValue, 0.0f) >= 0 ? invasiveVarValue : missingValuePlaceHolder;
        }
        else if(!Utils.isFloat(invasiveVarValue) && Utils.isFloat(nonInvasiveVarValue))
        {
            // 0 is also a legal measurement
            float var2FloatValue = Float.parseFloat(nonInvasiveVarValue);
            return Float.compare(var2FloatValue, 0.0f) >= 0 ? nonInvasiveVarValue : missingValuePlaceHolder;
        }
        else // both are floats
        {
            float val1 = Float.parseFloat(invasiveVarValue);
            float val2 = Float.parseFloat(nonInvasiveVarValue);

            //val1 > 0 and val2 > 0
            if(Float.compare(val1, 0.0f) > 0 && Float.compare(val2, 0.0f) > 0)
            {
                //this paper suggests choosing invasive one over non-invasive: https://onlinelibrary.wiley.com/doi/full/10.1111/j.1365-2044.2012.07302.x
                switch(bbp)
                {
                    case PreferInvasive:
                        return val1 + "";
                    case PreferNonInvasive:
                        return val2 + "";
                    default:
                        //take their average
                        float average = Utils.format("#.##", RoundingMode.HALF_UP, (val1 + val2) / 2.0f);
                        return average + "";
                } // switch
            }
            else if(Float.compare(val1, 0.0f) > 0) // since val2 <= 0 here
            {
                return val1 + "";
            }
            else if(Float.compare(val2, 0.0f) > 0) // since val1 <= 0 here
            {
                return val2 + "";
            }
            else if(Float.compare(val1, 0.0f) == 0 && Float.compare(val2, 0.0f) == 0) // both are 0
                return 0 + ""; // 0 is also a legal measurement
            else // val1 < 0 && val2 < 0
            {
                return missingValuePlaceHolder;
            } // else
        } // else
    } // combineFloatRepresentableVars


    //helper method to construct time series data in the following structure:
    //ts1, var1Value, var2Value, var3Value, ....
    //ts2, var1Value, var2Value, var3Value, ....
    //...
    public static void generateTimeSeriesData(HashSet<String> varsToDiscard, TreeSet<String> consideredVars, List<String> allFilePaths,
                                              String missingValuePlaceHolder)
    {
        //default padding length is the length of the variable with the longest name
        int defaultPaddingLength = Utils.defaultPaddingLength(consideredVars);


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


                //StringBuilder sb;
                String[] lineValues;

                //now read the fileContents line by line
                while (scanner.hasNextLine())
                {
                    //read the fileContents line by line
                    String line = scanner.nextLine();


                    //the line structure is the following:
                    //time_stamp,variable_name,variable_value
                    String[] lineComponents = line.split(",");

                    String timeStampString = lineComponents[0];
                    int timeStampMinutes = Utils.toMinutes(timeStampString);

                    //for each time stamp create new entry in the tree map
                    lineValues = tsLineValuesMap.get(timeStampMinutes); // returns null if there is no such a key
                    if(lineValues == null) {
                        lineValues = new String[firstLineComponents.size()]; // with the size equal to number of vars + 1
                        for(int i = 0; i < firstLineComponents.size(); i++)
                            lineValues[i] = Utils.padLeftSpaces(missingValuePlaceHolder, defaultPaddingLength); //firstLineComponents.get(i).length());

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



    //helper method to construct time series data in the following structure:
    //       ts1:   ts2:    ts3:
    //var1 :  1     2       3
    //var2:   1     2       3
    public static void generateTimeSeriesData2(HashSet<String> varsToDiscard, TreeSet<String> consideredVars, List<String> allFilePaths)
    {
        //default padding length is the length of the variable with the longest name
        int defaultPaddingLength = Utils.defaultPaddingLength(consideredVars);

        for(String filePath : allFilePaths)
        {
            //System.out.println("Processing file: " + filePath);

            String fileContents = Utils.fileContentsFromLocalFilePath(filePath);

            //generate padding for the first component of the first line
            StringBuilder linesBuilder = new StringBuilder(Utils.padLeftSpaces("", defaultPaddingLength));


            //tree map to hold time series -> variable values
            TreeMap<Integer, String[]> tsLineValuesMap = new TreeMap<Integer, String[]>();


            //create an array list from considered vars, so that the order in the set is kept
            ArrayList<String> consideredVarList = new ArrayList<>(consideredVars);


            //obtain a scanner for file contents string
            try (Scanner scanner = new Scanner(fileContents))
            {
                //discard first line
                //first line of the file is in the form => Time,Parameter,Value
                scanner.nextLine();

                //StringBuilder sb;
                //TreeMap<Integer, String> tsVarValueMap;

                int maxTimeStampLength = 0;


                String[] lineValues;


                //now read the fileContents line by line
                while (scanner.hasNextLine())
                {
                    //read the fileContents line by line
                    String line = scanner.nextLine();


                    //the line structure is the following:
                    //time_stamp,variable_name,variable_value
                    String[] lineComponents = line.split(",");

                    String timeStampString = lineComponents[0];

                    //maxTimeStampLength will be used for padding of values
                    if(timeStampString.length() > maxTimeStampLength)
                        maxTimeStampLength = timeStampString.length();


                    int timeStampMinutes = Utils.toMinutes(timeStampString);


                    String variableName = lineComponents[1];

                    String variableValue = lineComponents[2];


                    //for each time stamp create new entry in the tree map
                    lineValues = tsLineValuesMap.get(timeStampMinutes); // returns null if there is no such a key
                    if(lineValues == null) {
                        lineValues = new String[consideredVars.size() + 1]; // with the size equal to number of vars + 1 (+1 for time stamp)
                        Arrays.fill(lineValues, "-");

                        //add time stamp value first
                        //which will be in the index 0
                        lineValues[0] = timeStampMinutes + "";

                        //put it to the map
                        tsLineValuesMap.put(timeStampMinutes, lineValues);
                    }


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
                        int index = consideredVarList.indexOf(variableName) + 1; // +1 for time stamp
                        //add the variable value to the corresponding index, it will update the map values implicitly
                        lineValues[index] = variableValue;
                    } // else

                } // while

                //now populate the map in the form:
                //       ts1:   ts2:    ts3:
                //var1 :  1     2       3
                //var2:   1     2       3


                //transformation snippet
                TreeMap<String, ArrayList<String>> varTsVarValuesMap = new TreeMap<>();
                for(String var : consideredVars)
                {
                    //var values are listed according to the time stamp order or chronological order
                    ArrayList<String> valuesOfThisVarInEachTimeStamp = new ArrayList<>();
                    for(Integer thisTimeStamp : tsLineValuesMap.keySet())
                    {
                        String[] lineVals = tsLineValuesMap.get(thisTimeStamp);
                        int varIndex = consideredVarList.indexOf(var) + 1;
                        String varValueAtThisTimeStamp = lineVals[varIndex];

                        //populate arraylist
                        valuesOfThisVarInEachTimeStamp.add(varValueAtThisTimeStamp);
                    } // for

                    //now populate the tree map
                    varTsVarValuesMap.put(var, valuesOfThisVarInEachTimeStamp);
                } // for



                //now populate the first line
                for(int timeStampMinutes : tsLineValuesMap.keySet())
                {
                    linesBuilder
                            .append(",")
                            .append(Utils.padLeftSpaces("tsm: " + timeStampMinutes, maxTimeStampLength + 5)); // +5 for tsm:
                } // for
                linesBuilder.append("\n");


                //now populate the lines with values
                for(String var : varTsVarValuesMap.keySet())
                {
                    linesBuilder.append(Utils.padLeftSpaces(var, defaultPaddingLength));

                    for(String valueOfThisVarInThisTimeStamp : varTsVarValuesMap.get(var))
                    {
                        linesBuilder
                                .append(",")
                                .append(Utils.padLeftSpaces(valueOfThisVarInThisTimeStamp, maxTimeStampLength + 5)); // +5 for tsm:
                    } // for

                    linesBuilder.append("\n");
                } // for



                //write results to local file system
                Utils.writeToLocalFileSystem(filePath, "prepro2", linesBuilder.toString(), "csv");


            } catch (Exception ex) {
                ex.printStackTrace();


            } // catch

        } // for

    } // generateTimeSeriesData2


} // class Launcher
