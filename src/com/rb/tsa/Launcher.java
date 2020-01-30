package com.rb.tsa;



import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Launcher
{
    private static final String DATA_FOLDER = "C:\\Users\\rbabayev\\Downloads\\physionet-challenge\\original_data";


    //to check whether the variable naming is consistent throughout the dataset
    //private static HashSet<String> differentVariableNames = new HashSet<String>();


    public static void main(String[] args)
    {
        String setADir = DATA_FOLDER + File.separator + "set-a";
        String setBDir = DATA_FOLDER + File.separator + "set-b";
        String setCDir = DATA_FOLDER + File.separator + "set-c";

        //snippet to generate general descriptors for each file of each file set in a separate file
        //LinkedHashSet<String> descriptorVars = new LinkedHashSet<String>(Arrays.asList("RecordID", "Age", "Gender", "Height", "ICUType", "Weight"));
        //Utils.writeGeneralDescriptorsRecords(descriptorVars, ",", DATA_FOLDER, "txt", setADir, setBDir, setCDir);
        //System.exit(0);


        //after running the above snippet we have the following files
        String setAGenDescRecordsFile = DATA_FOLDER + File.separator + "GeneralDescriptorsRecords-a.txt";
        String setBGenDescRecordsFile = DATA_FOLDER + File.separator + "GeneralDescriptorsRecords-b.txt";
        String setCGenDescRecordsFile = DATA_FOLDER + File.separator + "GeneralDescriptorsRecords-c.txt";

        String setAOutcomesFile = DATA_FOLDER + File.separator + "Outcomes-a.txt";
        String setBOutcomesFile = DATA_FOLDER + File.separator + "Outcomes-b.txt";
        String setCOutcomesFile = DATA_FOLDER + File.separator + "Outcomes-c.txt";


        String varRangesFilePath = //DATA_FOLDER + "\\variable_ranges_physionet.csv";
                "resources/variable_ranges_physionet.csv";

        //method to handle outcomes
        Outcomes outcomes = Utils.outcomesFromLocalFilePaths(",", Dataset.PhysioNet, setAOutcomesFile, setBOutcomesFile, setCOutcomesFile);
        //method to handle var ranges
        VarRanges varRanges = Utils.varRangesFromLocalFilePath(",", Dataset.PhysioNet, varRangesFilePath);
        //method to handle general descriptors records
        GeneralDescriptorsRecords genDescRecords
                = Utils.genDescRecordsFromLocalFilePaths(",", Dataset.PhysioNet,
                                                setAGenDescRecordsFile, setBGenDescRecordsFile, setCGenDescRecordsFile);



        HashSet<String> varsToDiscard = new HashSet<>();
        //Variables to discard: RecordID, Age, Gender, Height, ICUType, Weight
        varsToDiscard.add("RecordID");
        varsToDiscard.add("Age");
        varsToDiscard.add("Gender");
        varsToDiscard.add("Height");
        varsToDiscard.add("ICUType");
        //varsToDiscard.add("Weight"); // both general descriptor and time series variable, handled carefully
        varsToDiscard.add("MechVent"); // this is a time series variable with values 0 and 1, but no significance in time series prediction, therefore discarded


        String[] vars = new String[]{"Albumin", "ALP", "ALT", "AST", "Bilirubin", "BUN", "Cholesterol", "Creatinine",
                "DiasABP", "FiO2", "GCS", "Glucose", "HCO3", "HCT", "HR", "K", "Lactate", "Mg", "MAP",
                /*"MechVent",*/  // discarded MechVent variable which has the value of only 0 or 1, added this variable to the set of discarded variables above
                "Na", "NIDiasABP", "NIMAP", "NISysABP", "PaCO2", "PaO2", "pH", "Platelets", "RespRate", "SaO2",
                "SysABP", "Temp", "TroponinI", "TroponinT", "Urine", "WBC", "Weight"};
        TreeSet<String> consideredVars = new TreeSet<String>(Arrays.asList(vars));


        //populate global hashset
        //differentVariableNames.addAll(varsToDiscard);
        //differentVariableNames.addAll(consideredVars);

        //HashSet<String> initial = new HashSet<>(differentVariableNames);

        //output different variable names
        //System.out.println("Number of different variables in PhysioNet dataset: " + differentVariableNames.size());


        //list files in the local file path
//        List<String> setAFilePaths = Utils.listFilesFromLocalPath(setADir, false);
//        List<String> setBFilePaths = Utils.listFilesFromLocalPath(setBDir, false);
//        List<String> setCFilePaths = Utils.listFilesFromLocalPath(setCDir, false);
//
//        List<String> allFilePaths = new ArrayList<>(setAFilePaths);
//        allFilePaths.addAll(setBFilePaths);
//        allFilePaths.addAll(setCFilePaths);



        //generateTimeSeriesData(varsToDiscard, consideredVars, outcomes, allFilePaths, "", "prepro");
        //generateTimeSeriesData2(varsToDiscard, consideredVars, allFilePaths);
//
//        for(String localFilePath : allPreProFilePaths)
//            Utils.csv2Arff(localFilePath, "1-" + (consideredVars.size() + 1)); // +1 for tsMinutes


//        List<String> setAPreProFilePaths = Utils.listFilesFromLocalPath(setADir + File.separator + "prepro", false);
//        List<String> setBPreProFilePaths = Utils.listFilesFromLocalPath(setBDir + File.separator + "prepro", false);
//        List<String> setCPreProFilePaths = Utils.listFilesFromLocalPath(setCDir + File.separator + "prepro", false);
//
//        List<String> allPreProFilePaths = new ArrayList<>();
//        allPreProFilePaths.addAll(setAPreProFilePaths);
//        allPreProFilePaths.addAll(setBPreProFilePaths);
//        allPreProFilePaths.addAll(setCPreProFilePaths);


        //remove outliers depending on variable ranges
        //List<String> allOutlierRemovalFilePaths =
                //removeOutliers(varRanges, consideredVars, allPreProFilePaths, "", "outlier_removal");


//        List<String> setAOutlierRemovalFilePaths
//                = Utils.listFilesFromLocalPath(setADir + File.separator + "prepro" + File.separator + "outlier_removal", false);
//        List<String> setBOutlierRemovalFilePaths
//                = Utils.listFilesFromLocalPath(setBDir + File.separator + "prepro" + File.separator + "outlier_removal", false);
//        List<String> setCOutlierRemovalFilePaths
//                = Utils.listFilesFromLocalPath(setCDir + File.separator + "prepro" + File.separator + "outlier_removal", false);
//        List<String> allOutlierRemovalFilePaths = new ArrayList<>();
//        allOutlierRemovalFilePaths.addAll(setAOutlierRemovalFilePaths);
//        allOutlierRemovalFilePaths.addAll(setBOutlierRemovalFilePaths);
//        allOutlierRemovalFilePaths.addAll(setCOutlierRemovalFilePaths);


        //combineVars(consideredVars, allPreProFilePaths, "", BloodPressurePreference.PreferInvasive);
        //for(String varName : newConsideredVars)
        //    System.out.print("\"" + varName + "\", ");


//        List<String> setAVarJoinFilePaths = Utils.listFilesFromLocalPath(setADir + File.separator + "prepro" + File.separator + "varJoin", false);
//        List<String> setBVarJoinFilePaths = Utils.listFilesFromLocalPath(setBDir + File.separator + "prepro" + File.separator + "varJoin", false);
//        List<String> setCVarJoinFilePaths = Utils.listFilesFromLocalPath(setCDir + File.separator + "prepro" + File.separator + "varJoin", false);
//        List<String> allVarJoinFilePaths = new ArrayList<>();
//        allVarJoinFilePaths.addAll(setAVarJoinFilePaths);
//        allVarJoinFilePaths.addAll(setBVarJoinFilePaths);
//        allVarJoinFilePaths.addAll(setCVarJoinFilePaths);


//        long start = System.currentTimeMillis();
//        float fMissingValuePlaceHolder = -2.0f;
//        List<MTSE> mtses = new ArrayList<>();
//
//        //obtain multivariate time series from each file
//        for(String filePath : allVarJoinFilePaths)
//        {
//            MTSE mtse
//                    = MTSE.fromFile(Dataset.PhysioNet, filePath, ",", fMissingValuePlaceHolder);
//            //println(mtse.toVerticalString());
//
//            mtses.add(mtse);
//            //break;
//        } // for
//
//        Imputations imputations = Imputations.getInstance();
//        List<MTSE> imtses
//                = imputations.impute(mtses, Imputations.ImputeMethod.MEAN_VALUE_WITH_MASKING_VECTOR_IMPUTATION, varRanges, fMissingValuePlaceHolder);
//        for(MTSE imtse : imtses)
//        {
//            //println(imtse.toVerticalString());
//            //println(imtse.toVerticalMaskingVectorString());
//            //println(imtse.toCSVString(true));
//            //break;
//
//            //imtse.writeToFile(DATA_FOLDER, "mean_imputed_set-", "csv", true);
//        } // for
//
//        long end = System.currentTimeMillis();
//        println("It took " + TimeUnit.MILLISECONDS.toSeconds(end-start) + " seconds for imputation");



        //imputed file paths
        List<String> setAMeanImputedFilePaths = Utils.listFilesFromLocalPath(DATA_FOLDER + File.separator + "mean_imputed_set-a", false);
        List<String> setBMeanImputedFilePaths = Utils.listFilesFromLocalPath(DATA_FOLDER + File.separator + "mean_imputed_set-b", false);
        List<String> setCMeanImputedFilePaths = Utils.listFilesFromLocalPath(DATA_FOLDER + File.separator + "mean_imputed_set-c", false);
        List<String> allMeanImputedFilePaths = new ArrayList<>();
        allMeanImputedFilePaths.addAll(setAMeanImputedFilePaths);
        allMeanImputedFilePaths.addAll(setBMeanImputedFilePaths);
        allMeanImputedFilePaths.addAll(setCMeanImputedFilePaths);


        long start = System.currentTimeMillis();
        float fMissingValuePlaceHolder = -2.0f;
        List<MTSE> mtses = new ArrayList<>();
        for(String filePath : allMeanImputedFilePaths)
        {
            MTSE mtse = MTSE.fromFile(Dataset.PhysioNet, filePath, ",", fMissingValuePlaceHolder);
            mtses.add(mtse);

            //println(mtse);
            //break;
        } // for
        long end = System.currentTimeMillis();
        println("It took " + TimeUnit.MILLISECONDS.toSeconds(end-start) + " to create mtses from files");
        //println(outcomes);
        //println(outcomes.countInHospitalDeathPositive(0, 4000)
        //        + " <=> " + outcomes.countInHospitalDeathNegative(0, 4000)
        //        + " <=> " + outcomes.countInHospitalDeathPositive(0, 4000) / (outcomes.size() * 1.0f));
        //println(outcomes.getRecordIDs().toString());
        Utils.writeMTSEsToMultiInstanceArffFile(Dataset.PhysioNet, mtses, outcomes,
                "patient_record_id", new String[]{"0", "1"}, DATA_FOLDER, "multi_instance_arff");


        //for(String localFilePath : allVarJoinFilePaths)
        //    Utils.csv2Arff(localFilePath, "1-35");


        String[] newVars = new String[] {"(NI)DiasABP", "(NI)MAP", "(NI)SysABP", "ALP", "ALT", "AST", "Albumin", "BUN",
                "Bilirubin", "Cholesterol", "Creatinine", "FiO2", "GCS", "Glucose", "HCO3", "HCT", "HR", "K", "Lactate",
                /*"MechVent",*/ // discarded MechVent variable which has only 0 or 1
                "Mg", "Na", "PaCO2", "PaO2", "Platelets", "RespRate", "SaO2", "Temp", "TroponinI", "TroponinT",
                "Urine", "WBC", "Weight", "pH"};
        TreeSet<String> newConsideredVars = new TreeSet<String>(Arrays.asList(newVars));
        //checkAndFix(newConsideredVars, allVarJoinFilePaths);
        //check variables before merging
        //checkAndFix(consideredVars, allPreProFilePaths);
        //checkAndFix(consideredVars, allOutlierRemovalFilePaths);


        //TODO complete the generation of own hand-engineered file for outlier removal
        //TODO remove outliers by methodological approach
        //TODO handle empty variables, empty files and missing data (some other imputation methods can also be implemented)
        //TODO load multi-instance arff to weka, split first 4000 bags for training and the next 4000 for testing
        //TODO you have not performed the mean imputation correctly: calculate mean from set-a and apply for both set-a, set-b and set-c as stated on GRU-D paper


        //System.out.println("Number of different variables in PhysioNet dataset (after pre_processing): " + differentVariableNames.size());
        //differentVariableNames.removeAll(initial);
        //System.out.println(differentVariableNames.size());


    } // main

    private static void checkAndFix(TreeSet<String> newConsideredVars, List<String> allVarJoinFilePaths)
    {
        //no patients have age smaller than 15, which is also used by GRU-D and deep learning benchmark papers for MIMIC-III dataset, too.

        //int fileCounter = 0;

        int tsCountSum = 0;

        int maxTimeStep = 0;


        //create a list from newConsideredVars which keeps the insertion order
        ArrayList<String> newConsideredVarsList = new ArrayList<>(newConsideredVars);
        //also add tsMinutes into considered var list at the index of 0
        newConsideredVarsList.add(0, "tsMinutes");


        //fileName, varName, varEmptiness
        //HashMap<String, LinkedHashMap<String, StringBuilder>> fileNameVarEmptinessMap = new HashMap<>();

        //map to collect all variable values in all files in an ordered fashion, such that max and min can be extracted
        LinkedHashMap<String, TreeSet<Float>> varAndOrderedValuesMap = new LinkedHashMap<>();
        //initialize the map with all variables
        for(String var : newConsideredVars)
            varAndOrderedValuesMap.put(var, new TreeSet<Float>());


        for(String localFilePath : allVarJoinFilePaths)
        {
            String fileContents = Utils.fileContentsFromLocalFilePath(localFilePath);
            String[] lines = StringUtils.split(fileContents,"\r\n|\r|\n");

            int emptyLinesCount = 0;

            LinkedHashMap<String, StringBuilder> varAndEmptinessMap = new LinkedHashMap<>(); // to keep the insertion order
            for(String var : newConsideredVars)
            {
                //populate all vars with empty string builders
                varAndEmptinessMap.put(var, new StringBuilder(""));
            } // for


            //discard empty files
            //if(lines.length - 1 == 0)
            //    println("File: " + localFilePath + " => is empty" + "\n");


            //line at index 0 is a header line, do not touch it
            for(int index = 1; index < lines.length; index ++)
            {
                String thisLine = lines[index];

                //divide this line to components
                String[] thisLineComponents = thisLine.split(",");

                //newConsideredVarsList and thisLineComponents must have the same size
                //it is ensured by adding tsMinutes to newConsideredVarsList above the outer for loop

                for(String var : newConsideredVars)
                {
                    int indexOfVar = newConsideredVarsList.indexOf(var);
                    String varValue = thisLineComponents[indexOfVar].trim(); // trim to get rid of spaces

                    //update map
                    varAndEmptinessMap.get(var).append(varValue);

                    TreeSet<Float> varValuesSet = varAndOrderedValuesMap.get(var);
                    if(Utils.isFloat(varValue))
                        varValuesSet.add(Float.valueOf(varValue));
                } // for


                //there is no other line with all variables empty except the line with time stamp 0
                //line with time stamp 0 is discarded for all files in generateTimeSeriesData() method
                //StringBuilder empty = new StringBuilder("");
                //for(int idx = 1; idx < thisLineComponents.length; idx ++) // discard the first component which is ts minutes and is always nonempty
                //    empty.append(thisLineComponents[idx].trim());
                //
                //if(empty.toString().isEmpty())
                //{
                //    emptyLinesCount++;
                //    println("File: " + localFilePath + " => empty line => " + index);
                //}

            } // for

            //after checking all lines we have populated, put var and its emptiness as a map for this file
            //fileNameVarEmptinessMap.put(Utils.fileNameFromPath(localFilePath), varAndEmptinessMap);



            //check which variable has all values missing in each file
            //no variable is empty in all files, but separate files have empty variables
            //StringBuilder emptyVarsDescription = new StringBuilder("");
            //for(String var : varAndEmptinessMap.keySet())
            //{
            //    if(varAndEmptinessMap.get(var).toString().isEmpty())
            //        emptyVarsDescription.append("\"").append(var).append("\"").append(",");
            //} // for
            //println("File : " + localFilePath + " => "  + emptyVarsDescription.toString() + " are empty");
            //System.out.println();


            //count the number of files with number of lines >= 20, -2 for header line and line with time stamp 0
            //if(lines.length - 2 >= 20) fileCounter ++;







            //TODO they have merged some lines together, e.g. by a time interval, check this

            tsCountSum += lines.length - 1 - emptyLinesCount; // -1 for header line
            if(lines.length - 1 - emptyLinesCount > maxTimeStep)
                maxTimeStep = lines.length - 1 - emptyLinesCount;

        } // for all files


        //for each variable prints its value set
        for(String var : varAndOrderedValuesMap.keySet())
            println("\"" + var + "\" => " + varAndOrderedValuesMap.get(var) + ", Mean => "
                    + Utils.mean(varAndOrderedValuesMap.get(var)) + ", Size: " + varAndOrderedValuesMap.get(var).size() + "\n");




//        LinkedHashMap<String, Boolean> globalVarEmptinessMap = new LinkedHashMap<>();
//        for(String var : newConsideredVars)
//        {
//            //initially all vars are empty
//            globalVarEmptinessMap.put(var, true);
//        } // for
//
//
//        //now for all files and for all variables populate the above map
//        for(String fileName : fileNameVarEmptinessMap.keySet())
//        {
//            for(String var : fileNameVarEmptinessMap.get(fileName).keySet())
//            {
//                Boolean currentEmptiness = globalVarEmptinessMap.get(var);
//                Boolean newEmptiness
//                        = currentEmptiness && fileNameVarEmptinessMap.get(fileName).get(var).toString().isEmpty();
//                globalVarEmptinessMap.put(var, newEmptiness);
//            } // for
//        } // for
//
//
//        for(String var : globalVarEmptinessMap.keySet())
//        {
//            if(globalVarEmptinessMap.get(var))
//                println("\"" + var + "\" is empty in all files");
//        } // for


        //Number of files (set-a, set-b, set-c, lines.length - 1 >= 20) with number of lines >= 20: 11815 vs. all files count: 12000
        //println("Number of files with number of lines >= 20: " + fileCounter + " vs. all files count: " + allVarJoinFilePaths.size());

        //Average number of time stamps in 12000 files (set-a, set-b, set-c, tsCountSum += lines.length - 1): 74.86266666666667
        //Average number of time stamps in 4000 files (set-a, tsCountSum += lines.length - 1): 74.816
        //Average number of time stamps in 4000 files (set-b, tsCountSum += lines.length - 1): 74.767
        //Average number of time stamps in 4000 files (set-c, tsCountSum += lines.length - 1): 75.005
        println("Average number of time stamps in " + allVarJoinFilePaths.size() + " files: " + (tsCountSum / (allVarJoinFilePaths.size() * 1.0)));

        //Maximum time steps in 4000 files (set-a, lines.length - 1 > maxTimeStep): 203
        println("Maximum time steps in " + + allVarJoinFilePaths.size() + " files: " + maxTimeStep);
    } // checkAndFix







    //TODO varRanges will be passed as an argument to this method
    //helper method to remove outliers based on specified variable ranges
    public static List<String> removeOutliers(VarRanges varRanges, TreeSet<String> consideredVars, List<String> allPreProFilePaths,
                                               String missingValuePlaceHolder, String newDirNameToPutFiles)
    {
        //the new file paths to return
        List<String> newFilePaths = new ArrayList<>();


        //create a list from consideredVars which keeps the insertion order
        ArrayList<String> consideredVarsList = new ArrayList<>(consideredVars);
        //also add tsMinutes into considered var list at the index of 0
        consideredVarsList.add(0, "tsMinutes");


        //calculate the padding length
        int defaultPaddingLength = Utils.defaultPaddingLength(consideredVars);


        //now read all files one by one and remove outliers
        for(String localFilePath : allPreProFilePaths)
        {
            String fileContents = Utils.fileContentsFromLocalFilePath(localFilePath);
            String[] lines = StringUtils.split(fileContents, "\r\n|\r|\n");


            StringBuilder linesBuilder
                    = new StringBuilder(Utils.padLeftSpaces("tsMinutes", defaultPaddingLength));
            //now populate the first line with new considered vars
            for(String consideredVarName : consideredVars)
                linesBuilder.append(",").append(Utils.padLeftSpaces(consideredVarName, defaultPaddingLength));


            //METADATA is obtained by GeneralDescriptorsRecords
            //append metadata, metadata is available as the last component in the header row
            //String[] splits = lines[0].split(",");
            //linesBuilder.append(",").append(splits[splits.length - 1]);


            //new line
            linesBuilder.append("\n");


            //for each line, split and obtain variable value, skip the first line which is the header line
            for(int index = 1; index < lines.length; index ++)
            {
                String thisLine = lines[index];

                //obtain the components of this line
                String[] thisLineComponents = thisLine.split(",");


                //append tsMinutes first, it is at index 0
                linesBuilder.append(Utils.padLeftSpaces(thisLineComponents[0], defaultPaddingLength));


                //thisLineComponents and consideredVarsList have the same length and variable names, values are indexed at the same location
                for(int idx = 1; idx < thisLineComponents.length; idx ++) // skip tsMinutes by starting at idx 1
                {
                    String varName = consideredVarsList.get(idx);
                    String varValue = thisLineComponents[idx];

                    //obtain ranges for the current variable
                    Ranges ranges = varRanges.getRanges(varName);

                    //new var value after outlier removal
                    String newVarValue;

                    //all variable values are numeric and can be converted to float
                    //the check will also eliminate parsing of missing or incorrect data
                    if(Utils.isFloat(varValue))
                    {
                        //V.ix[V < ranges.OUTLIER_LOW[variable]] = np.nan
                        //V.ix[V > ranges.OUTLIER_HIGH[variable]] = np.nan
                        //V.ix[V < ranges.VALID_LOW[variable]] = ranges.VALID_LOW[variable]
                        //V.ix[V > ranges.VALID_HIGH[variable]] = ranges.VALID_HIGH[variable]

                        float fValue = Float.parseFloat(varValue);

                        if(Float.compare(fValue, ranges.getOutlierLow()) < 0)
                            newVarValue = missingValuePlaceHolder;
                        else if(Float.compare(fValue, ranges.getOutlierHigh()) > 0)
                            newVarValue = missingValuePlaceHolder;
                        //at this point -> outlierLow <= fValue <= outlierHigh
                        else if(Float.compare(fValue, ranges.getValidLow()) < 0) //outlierLow <= fValue < validLow
                            newVarValue = ranges.getValidHigh() + "";
                        else if(Float.compare(fValue, ranges.getValidHigh()) > 0) //validHigh < fValue <= outlierHigh
                            newVarValue = ranges.getValidHigh() + "";
                        else                                    // validLow <= fValue <= validHigh
                            newVarValue = varValue;
                    } // if
                    else newVarValue = missingValuePlaceHolder;


                    //append each line component
                    linesBuilder.append(",").append(Utils.padLeftSpaces(newVarValue, defaultPaddingLength));
                } // for each line component

                //new line
                linesBuilder.append("\n");

            } // for each line


            //write results to local file system, method might return null if IO exception occurs
            String newFilePath = Utils.writeToLocalFileSystem(localFilePath, newDirNameToPutFiles, linesBuilder.toString(), "csv");
            if(newFilePath != null)
                newFilePaths.add(newFilePath);
            else
                System.err.println("newFilePath is null at removeOutliersMethod() method");
        } // for each file


        return newFilePaths;
    } // removeOutliers






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
                //String headerLine =
                scanner.nextLine();


                //METADATA is obtained by GeneralDescriptorsRecords
                //append metadata, metadata is available as the last component in the header row
                //String[] splits = headerLine.split(",");
                //linesBuilder.append(",").append(splits[splits.length - 1]);
                //linesBuilder.append("\n");


                //int lineNumber = 1;

                //now read each line one by one
                while(scanner.hasNextLine())
                {
                    String thisLine = scanner.nextLine();

                    //lineNumber++;

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
    public static List<String> generateTimeSeriesData(HashSet<String> varsToDiscard, TreeSet<String> consideredVars,
                                                      Outcomes outcomes, List<String> allFilePaths,
                                                      String missingValuePlaceHolder, String newDirNameToPutFiles)
    {
        //metadata is added as the last component in the header row
        /*
        General descriptors:
        --------------------
        RecordID (a unique integer for each ICU stay)
        Age (years)
        Gender (0: female, or 1: male)
        Height (cm)
        ICUType (1: Coronary Care Unit, 2: Cardiac Surgery Recovery Unit,
        3: Medical ICU, or 4: Surgical ICU)
        Weight (kg)*.
        --------------------
        A value of -1 indicates missing or unknown data (for example, if a patient's height was not recorded).
         */

        //file paths generated at the end
        List<String> newFilePaths = new ArrayList<>();


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


            //all files are actually record ids
            String fileName = Utils.fileNameFromPath(filePath);
            int recordID = Integer.parseInt(FilenameUtils.removeExtension(fileName));


            //general descriptors or meta parameters will be appended as the last component to the header row
            //therefore, there is only one meta string builder for each file
            //StringBuilder metaBuilder = new StringBuilder().append(",").append("{"); //meta start withs ,{ => comma is for separating it from other variables


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


                    ///handle MechVent carefully which is the variable we are not interested
                    if(variableName.equals("MechVent"))
                        continue; // continue to the next variable, each line represents one variable value



                    //handle general descriptor variables (which are only available at timestamp 0);
                    //handle weight carefully for timestamp 0
                    //varsToDiscard does not contain Weight
                    if(varsToDiscard.contains(variableName) && timeStampMinutes == 0)
                    {
                        //meta will have the following structure:
                        //{RecordID: value;Age: value;Gender: value;Height: value;ICUType: value;Weight: value}
                        //metaBuilder.append(variableName).append("=").append(variableValue).append(";");
                    }
                    //WE CONSIDER WEIGHT VARIABLE AS A TIME SERIES VARIABLE AT TIMESTAMP 0
                    //else if(variableName.equals("Weight") && timeStampMinutes == 0)
                    //{
                    //    //add weight at timestamp 0 as meta value
                    //    //weight will be last meta value to be appended
                    //    //metaBuilder.append(variableName).append("=").append(variableValue).append(";");
                    //}
                    else {
                        //get the index of variable name
                        int index = firstLineComponents.indexOf(variableName);
                        //add the variable value to the corresponding index, it will update the map values implicitly
                        lineValues[index]
                                = Utils.padLeftSpaces(ensureNonNegativityAndGet(variableValue, missingValuePlaceHolder),
                                            defaultPaddingLength); //firstLineComponents.get(index).length());
                    }

                } // while


                //close meta and add it the first line of a new file
                //metaBuilder.append("InHospitalDeath").append("=").append(outcomes.get(recordID).getInHospitalDeath0Or1()).append("}");
                //firstLineOfANewFile += metaBuilder.toString();


                //explicitly remove the key == 0 and associated values, since, timestamp==0 has all variables empty
                //WRONG 133919.txt in set-a has pH value at time stamp 0, do not remove the key
                //tsLineValuesMap.remove(0);



                //now build a string from the values of variables and create a new file
                StringBuilder sb = new StringBuilder(firstLineOfANewFile).append("\n");
                //now for each key-value pair, add it to the sb
                for(Integer key : tsLineValuesMap.keySet())
                {
                    String[] lineVals = tsLineValuesMap.get(key);
                    sb.append(String.join(",", lineVals)).append("\n");
                } // for


                //write results to local file system, method might return null if IO exception occurs
                String newFilePath = Utils.writeToLocalFileSystem(filePath, newDirNameToPutFiles, sb.toString(), "csv");
                if(newFilePath != null)
                    newFilePaths.add(newFilePath);
                else
                    System.err.println("newFilePath is null at generateTimeSeriesData() method");

            } catch (Exception ex) {
                ex.printStackTrace();
            } // catch


        } // for each file


        return newFilePaths;
    } // generateTimeSeriesData



    //all variable values in physionet dataset are float representable, remove negative values of variables by the following method
    //valid values of variables are non-negative (>= 0)
    private static String ensureNonNegativityAndGet(String variableValue, String missingValuePlaceHolder)
    {
        if(Utils.isFloat(variableValue))
        {
            float fValue = Float.parseFloat(variableValue);
            if(Float.compare(fValue, 0.0f) < 0)
                return missingValuePlaceHolder;
            else
                return variableValue;
        } // if
        else
            return missingValuePlaceHolder;
    } // ensureNonNegativityAndGet



    //helper method to construct time series data in the following structure:
    //       ts1:   ts2:    ts3:
    //var1 :  1     2       3
    //var2:   1     2       3
    public static void generateTimeSeriesData2(HashSet<String> varsToDiscard, TreeSet<String> consideredVars, List<String> allFilePaths,
                                               String missingValuePlaceHolder)
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
                        Arrays.fill(lineValues, missingValuePlaceHolder);

                        //add time stamp value first
                        //which will be in the index 0
                        lineValues[0] = timeStampMinutes + "";

                        //put it to the map
                        tsLineValuesMap.put(timeStampMinutes, lineValues);
                    }


                    //handle general descriptor variables; handle weight carefully for timestamp 0
                    if(varsToDiscard.contains(variableName))
                    {
                        // do nothing
                    }
                    //else if(variableName.equals("Weight") && timeStampMinutes == 0)
                    //{
                    //
                    //}
                    else {
                        //get the index of variable name
                        int index = consideredVarList.indexOf(variableName) + 1; // +1 for time stamp
                        //add the variable value to the corresponding index, it will update the map values implicitly
                        lineValues[index] = ensureNonNegativityAndGet(variableValue, missingValuePlaceHolder);
                    } // else

                } // while


                //explicitly remove the key == 0 and associated values, since, timestamp 0 has all variables empty
                //WRONG 133919.txt in set-a has pH value at time stamp 0, do not remove the key
                //tsLineValuesMap.remove(0);


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
                for(Integer timeStampMinutes : tsLineValuesMap.keySet())
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

    private static void println(Object o)
    {
        System.out.println(o);
    } // println

} // class Launcher
