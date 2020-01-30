package com.rb.tsa;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.*;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

//class containing utility methods
public class Utils
{
    public static int defaultPaddingLength(Collection<String> stringCollection)
    {
        int defaultPaddingLength = 0;
        for(String var : stringCollection)
        {
            if(var.length() > defaultPaddingLength)
                defaultPaddingLength = var.length();
        } // for

        return defaultPaddingLength + 1; // + 1 to make a space for the longest named variable
    } // defaultPaddingLength


    public static int defaultPaddingLength(String... array)
    {
        int defaultPaddingLength = 0;
        for(String var : array)
        {
            if(var.length() > defaultPaddingLength)
                defaultPaddingLength = var.length();
        } // for

        return defaultPaddingLength + 1; // + 1 to make a space for the longest named variable
    } // defaultPaddingLength


    public static String padLeftSpaces(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append(' ');
        }
        sb.append(inputString);

        return sb.toString();
    }


    //helper method to get the file name (base name + extension) from the given path
    public static String fileNameFromPath(String path) {
        //Apache IO fileNameUtils works on every path
        //Tested on:
        //-----------------------------------------------
        //"hdfs://123.23.12.4344:9000/user/filename.txt"
        //"resources/cabs.xml"
        //"bluebird:/usr/doctools/dp/file.txt"
        //"\\\\server\\share\\file.xml"
        //"file:///C:/Users/Default/Downloads/file.pdf"
        //-----------------------------------------------
        //returns empty string for not found file names e.g. for => "\\\\server\\share\\"

        //return name of the directory if the path is a dir path
        return FilenameUtils.getName(path);
    } // fileNameFromPath


    //method to convert hours:minutes to minutes
    public static int toMinutes(String timeStampString)
    {
        String[] hoursAndMinutes = timeStampString.split(":");
        int hoursByMinutes = Integer.parseInt(hoursAndMinutes[0]) * 60;
        int minutes = Integer.parseInt(hoursAndMinutes[1]);
        return hoursByMinutes + minutes;
    }



    //helper method to list files from the local path in the local file system
    public static List<String> listFilesFromLocalPath(String localPathString, boolean recursive)
    {
        //resulting list of files
        List<String> localFilePaths = new ArrayList<String>();

        //get the Java file instance from local path string
        File localPath = new File(localPathString);


        //this case is possible if the given localPathString does not exit => which means neither file nor a directory
        if (!localPath.exists()) {
            System.err.println("\n" + localPathString + " is neither a file nor a directory; please provide correct local path");

            //return with empty list
            return new ArrayList<String>();
        } // if


        //at this point localPath does exist in the file system => either as a directory or a file


        //if recursive approach is requested
        if (recursive) {
            //recursive approach => using a queue
            Queue<File> fileQueue = new LinkedList<File>();

            //add the file in obtained path to the queue
            fileQueue.add(localPath);

            //while the fileQueue is not empty
            while (!fileQueue.isEmpty()) {
                //get the file from queue
                File file = fileQueue.remove();

                //file instance refers to a file
                if (file.isFile()) {
                    //update the list with file absolute path
                    localFilePaths.add(file.getAbsolutePath());
                } // if
                else   //else file instance refers to a directory
                {
                    //list files in the directory and add to the queue
                    File[] listedFiles = file.listFiles();
                    for (File listedFile : listedFiles) {
                        fileQueue.add(listedFile);
                    } // for
                } // else

            } // while
        } // if
        else        //non-recursive approach
        {
            //if the given localPathString is actually a directory
            if (localPath.isDirectory()) {
                File[] listedFiles = localPath.listFiles();

                //loop all listed files
                for (File listedFile : listedFiles) {
                    //if the given listedFile is actually a file, then update the resulting list
                    if (listedFile.isFile())
                        localFilePaths.add(listedFile.getAbsolutePath());
                } // for
            } // if
            else        //it is a file then
            {
                //return the one and only file absolute path to the resulting list
                localFilePaths.add(localPath.getAbsolutePath());
            } // else
        } // else


        //return the resulting list; list can be empty if given path is an empty directory without files and sub-directories
        return localFilePaths;
    } // listFilesFromLocalPath


    public static String fileContentsFromLocalFilePath(String localFilePath) {
        InputStream localFileInputStream = inputStreamFromLocalFilePath(localFilePath);
        return stringContentFromInputStream(localFileInputStream);
    } // fileContentsFromLocalFilePath


    //helper method convert the file's or whatever input stream to string content;
    //some code snippets can cause IOException here, therefore we throw exception;
    //it will be able to get string content both from a local file or network file
    private static String stringContentFromInputStream(InputStream inputStream)
    {
        //string builder to contain all lines of the local file or network file
        StringBuilder stringContentBuilder = new StringBuilder("");

        //a buffered reader to read input stream
        BufferedReader br = null;

        try {
            //instead of reading whole file input stream at once, we read the file input stream character by character
            //create a buffered reader from input stream via input stream reader
            br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            //variable to hold the int representation of character read
            int charRep = -1;

            //READING LINE BY LINE CAUSES EXCEPTIONS ESPECIALLY FROM INPUT STREAMS; THEREFORE WE READ CHAR BY CHAR
            //read the input stream character by character
            //The character read, as an integer in the range 0 to 65535 (0x00-0xffff), or -1 if the end of the stream has been reached
            while ((charRep = br.read()) != -1) {
                //char 2 bytes;	range => 0 to 65,536 (unsigned)
                stringContentBuilder.append((char) charRep);
            } // while

        } // try
        catch (Exception ex) // will catch all exceptions including "IOException"
        {
            ex.printStackTrace();

            //if any problem occurs during read operation, return an empty string
            return "";
        } // catch
        finally {
            //close buffered reader
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } // catch
            } // if
        } // finally


        //return the resulting string from string builder
        return stringContentBuilder.toString();
    } // stringContentFromInputStream


    //an overloaded version of the method to get the input stream from the local file path
    public static InputStream inputStreamFromLocalFilePath(String localFilePath)
    {
        //initialize input stream with empty string bytes;
        //when converted it will be empty string and in turn empty visits array list
        InputStream targetStream = new ByteArrayInputStream("".getBytes());

        try {
            //we assume that file exists
            File localFile = new File(localFilePath);

            //if local file does not exist (as a directory or a file); then return input stream with empty string bytes;
            //else if local file does exist, but does not refer to file; then again return input stream with empty string bytes;
            if (!localFile.exists() || (localFile.exists() && !localFile.isFile()))
                return targetStream;


            //at this point localFile exists, and it is actually a file
            targetStream = new FileInputStream(localFile);
            //return the resulting input stream
            return targetStream;
        } // try
        catch (Exception ex) {
            ex.printStackTrace();

            //if any problem occurs during input stream generation, return input stream with empty string bytes;
            //which in conversion will be empty string and in turn empty visits array list
            return new ByteArrayInputStream("".getBytes());
        } // catch
    } // inputStreamFromLocalFilePath


    //returns the path of the written file
    public static String writeToLocalFileSystem(String localFilePathString, String newDirName,
                                              String writableContent, String newFileExtension)
    {
        //new file path to return
        String newFilePath = null;

        //to be closed in finally
        PrintWriter printWriter = null;

        try {
            File localFile = new File(localFilePathString);

            //if the localFile is a directory, print message to standard error and return
            if (localFile.isDirectory()) {
                System.err.println("\n" + localFilePathString + " is a directory; not able to generate writable file path from it");
            } // if
            //there is a possibility given path does not exist which means does not refer to either file or directory
            //so perform checks for each of them; which is equivalent to FileSystem.resolvePath() for HDFS
            else if (localFile.isFile())
            {
                //get the parent directory of localFile
                File localFileParentDir = localFile.getParentFile();

                //make a new directory called <resultFileNameAppendix> in the parent directory; if that directory exists do not create it
                //getAbsolutePath() returns path string without trailing directory separator
                File resultingSubDir = new File(localFileParentDir.getAbsolutePath() + File.separator + newDirName);

                //mkdir a directory if the resulting sub dir path does not exist
                if (!resultingSubDir.exists())
                    resultingSubDir.mkdir();


                //change the file extension
                String localFileName = localFile.getName();
                String currentFileExtension = FilenameUtils.getExtension(localFileName);
                String newLocalFileName = localFileName.replace(currentFileExtension, newFileExtension);


                //now create a writable path from resultingSubDir and the given localFile
                //getAbsolutePath() returns path string without trailing directory separator
                String writableFilePathString = resultingSubDir.getAbsolutePath() + File.separator + newLocalFileName;

                //create a file from writable path
                File writableFile = new File(writableFilePathString);
                writableFile.createNewFile();

                //write with print writer
                printWriter = new PrintWriter(writableFile);
                printWriter.print(writableContent);

                System.out.println("Wrote file => " + writableFile.getAbsolutePath());

                //update newFilePath
                newFilePath = writableFile.getAbsolutePath();
            } // else if
            else
                System.err.println("\n" + localFilePathString + " is neither a file nor a directory; please provide correct file path");

        } // try
        catch (Exception ex) {
            ex.printStackTrace();
        } // catch
        finally {
            if (printWriter != null) printWriter.close();
        } // finally


        //return the file path
        return newFilePath;
    } // writeToLocalFileSystem




    //helper method to return var and its ranges
    public static VarRanges varRangesFromLocalFilePath(String lineComponentDelimiter, Dataset dataset, String varRangesLocalFilePath)
    {
        //read the variable ranges file and extract the ranges
        String varRangesFileContents = Utils.fileContentsFromLocalFilePath(varRangesLocalFilePath);
        String[] varRangesLines = StringUtils.split(varRangesFileContents, "\r\n|\r|\n");
        //first line is the header line, extract the range variables

        //obtain var ranges for this dataset
        VarRanges varRanges = VarRanges.getInstance(dataset);


        //now for each other line, extract the ranges of each variable, each line contains one variable, except first header line
        for(int index = 1; index < varRangesLines.length; index ++)
        {
            String thisLine = varRangesLines[index];

            //thisLineComponents will have the same length as of #ranges + 1
            String[] thisLineComponents = thisLine.split(lineComponentDelimiter);
            for(int idx = 0; idx < thisLineComponents.length; idx ++) thisLineComponents[idx] = thisLineComponents[idx].trim(); // trim every element

            //var name is at index 0 of thisLineComponents
            String thisVar = thisLineComponents[0];
            //OUTLIER_LOW, VALID_LOW, IMPUTE, VALID_HIGH, OUTLIER_HIGH
            Ranges rangesForThisVar
                    = new Ranges(dataset, thisVar,
                    Float.parseFloat(thisLineComponents[1]),
                    Float.parseFloat(thisLineComponents[2]),
                    Float.parseFloat(thisLineComponents[3]),
                    Float.parseFloat(thisLineComponents[4]),
                    Float.parseFloat(thisLineComponents[5]));

            //update the var ranges
            varRanges.add(thisVar, rangesForThisVar);
        } // for

        System.out.println("Read VarRanges files");

        return varRanges;
    } // varRanges


    //helper method to extract general descriptors from all files in each set of files
    //All valid values for general descriptors are non-negative (≥ 0).
    //A value of -1 indicates missing or unknown data, e.g. height or weight are not recorded
    //the structure to be returned is as follows:
    //RecordID,Age,Gender,Height,ICUType,Weight
    //132539,54,0,-1,4,-1
    //......
    public static void writeGeneralDescriptorsRecords(LinkedHashSet<String> descriptorVars, String lineComponentDelimiter,
                                                      String destinationLocalDirPath, String newFileExtension, String ...fileSetLocalDirPaths)
    {
        //for each file set directory, write one general descriptors records
        for(String thisFileSetDirPath : fileSetLocalDirPaths)
        {
            String thisFileSetDirName = fileNameFromPath(thisFileSetDirPath);

            StringBuilder genDescRecordsBuilder = new StringBuilder(String.join(lineComponentDelimiter, descriptorVars)).append("\n");

            //obtain file paths in this dir path
            List<String> filePathsInThisDir = listFilesFromLocalPath(thisFileSetDirPath, false);

            //now for each file, obtain general descriptors
            for(String filePath : filePathsInThisDir)
            {
                //obtain file contents
                String fileContents = fileContentsFromLocalFilePath(filePath);
                //split the lines
                String[] lines = StringUtils.split(fileContents, "\r\n|\r|\n");


                //map which maps varName to its value
                LinkedHashMap<String, String> varNameAndValueMap = new LinkedHashMap<>();
                //populate
                for(String varName : descriptorVars)
                    varNameAndValueMap.put(varName, null);


                //discard the first line, which is Time,Parameter,Value
                //for each line obtain descriptor vars and their values
                for(int index = 1; index < lines.length; index ++)
                {
                    String thisLine = lines[index];

                    //split the line with delimiter and obtain values
                    String[] thisLineComponents = thisLine.split(lineComponentDelimiter);

                    //first component is timestamp in hh:mm, then var name and then its value
                    int tsMinutes = Utils.toMinutes(thisLineComponents[0]);
                    String varName = thisLineComponents[1];
                    String varValue = thisLineComponents[2];

                    if(descriptorVars.contains(varName) && tsMinutes == 0)
                    {
                        //in some files descriptors vars are ordered differently, so use map to insert value correctly
                        varNameAndValueMap.put(varName, varValue);
                    } // if

                    //do not process other lines having tsMinutes bigger than 0
                    if(tsMinutes > 0) break;
                } // for each line


                //populate general descriptors for this file
                for(String varName : varNameAndValueMap.keySet())
                    genDescRecordsBuilder.append(varNameAndValueMap.get(varName)).append(lineComponentDelimiter);
                //append new line
                genDescRecordsBuilder.append("\n");


                //report if some variable have null value in the map after processing
                if(varNameAndValueMap.containsValue(null))
                    System.out.println("map, contains null value for some varName => " + filePath);
            } // for each file


            String newFileName
                    = "GeneralDescriptorsRecords"
                    + thisFileSetDirName.replace("set", "") + "." + newFileExtension;
            try {
                File newFile = new File(destinationLocalDirPath + File.separator + newFileName);
                newFile.createNewFile();
                PrintWriter pw = new PrintWriter(newFile);
                pw.print(genDescRecordsBuilder.toString());
                pw.close();

                System.out.println("Wrote file => " + newFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            } // catch

        } // for each file set dir

    } // writeGeneralDescriptorsRecords



    //helper method to write mtses to multi-instance format:
    //@relation physionet
    // @attribute patient_record_id {132539,132540,132541,132543,132545,...,163037}
    // @attribute bag relational
    //   @attribute (NI)DiasABP numeric
    //   @attribute (NI)MAP numeric
    //   @attribute (NI)SysABP numeric
    //   @attribute ALP numeric
    //   @attribute ALT numeric
    //   ...
    //   @attribute pH numeric
    // @end bag
    // @attribute class {0,1}
    //
    // @data
    // 132539,"0,59.26,79.05,119.4,116.75,394.61,506.54,2.92,27.42,2.91,156.52,1.51,0.55,11.4,141.5,23.12,30.68,87.52,4.14,2.92,2.03,139.07,40.47,150.42,190.81,19.72,96.64,37.04,7.15,1.2,119.57,12.67,83.6,7.49\n...",0
    // ...
    public static void writeMTSEsToMultiInstanceArffFile(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, String bagName,
                                                  String[] classLabels, String destinationDirPath, String newDirNameToPutFile)
    {
        if(mtses.isEmpty()) throw new RuntimeException("At lease one mtse should be provided");
        List<String> vars = mtses.get(0).getVars();

        //feedback
        System.out.println("Generating content for multi-instance arff file...");

        StringBuilder sb = new StringBuilder("@relation " + dataset.toSimpleString()).append("\n\n");
        sb.append("@attribute ").append(bagName).append(" ")
                .append("{").append(String.join(",", toStringCollection(outcomes.getRecordIDs()))).append("}").append("\n");
        sb.append("@attribute bag relational").append("\n");
        vars.forEach(s -> {
            sb.append(" @attribute ").append(s.trim()).append(" numeric").append("\n");
        });
        sb.append("@end bag").append("\n");
        sb.append("@attribute class {").append(String.join(",", classLabels)).append("}").append("\n\n");
        sb.append("@data").append("\n");
        for(MTSE mtse : mtses)
        {
            sb.append(mtse.getRecordID()).append(",").append("\"");
            Map<Integer, List<Float>> valuesInVarNameOrder = mtse.getValuesInVarNameOrder();
            List<String> joinedValuesInVarNameOrder = new ArrayList<>();
            for(Integer ts : valuesInVarNameOrder.keySet())
            {
                //it joins all elements in the row by comma
                joinedValuesInVarNameOrder.add(String.join(",", toStringCollection(valuesInVarNameOrder.get(ts))));
            } // for
            //then joins rows by \n and appends a comma, class label and new line character
            sb.append(String.join("\\n", joinedValuesInVarNameOrder)).append("\"").append(",")
                    .append(outcomes.get(mtse.getRecordID()).getInHospitalDeath0Or1()).append("\n");
        } // for


        String newFileName
                = dataset.toSimpleString() + ".arff";
        try
        {
            File dir = new File(destinationDirPath + File.separator + newDirNameToPutFile);
            if(!dir.exists()) dir.mkdir();

            File newFile = new File( dir.getAbsolutePath() + File.separator + newFileName);
            newFile.createNewFile();
            PrintWriter pw = new PrintWriter(newFile);
            pw.print(sb.toString());
            pw.close();

            System.out.println("Wrote file => " + newFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } // catch
    } // writeMTSEsToMultiInstanceArffFile


    //helper method to convert Number collection to String collection
    public static Collection<String> toStringCollection(Collection<? extends Number> collection)
    {
        List<String> stringReps = new ArrayList<>();
        for(Number n : collection)
        {
            stringReps.add(n.toString());
        } // for

        return stringReps;
    } // toStringCollection


    //numeric attribute range in the form of "first-last"
    public static void csv2Arff(String localFilePathString, String numericAttributeRange)
    {

        //System.out.println("\nUsage: CSV2Arff <input.csv> <output.arff>\n");

        //file consistency check
        try
        {
            File localFile = new File(localFilePathString);

            //if the localFile is a directory, print message to standard error and return
            if (localFile.isDirectory()) {
                System.err.println("\n" + localFilePathString + " is a directory; not able to generate writable file path from it");
            } // if
            //there is a possibility given path does not exist which means does not refer to either file or directory
            //so perform checks for each of them; which is equivalent to FileSystem.resolvePath() for HDFS
            else if (localFile.isFile())
            {
                //get the parent directory of localFile
                File localFileParentDir = localFile.getParentFile();

                //make a new directory called <resultFileNameAppendix> in the parent directory; if that directory exists do not create it
                //getAbsolutePath() returns path string without trailing directory separator
                File resultingSubDir = new File(localFileParentDir.getAbsolutePath() + File.separator + "arff"); //newDirName);

                //mkdir a directory if the resulting sub dir path does not exist
                if (!resultingSubDir.exists())
                    resultingSubDir.mkdir();


                //change the file extension
                String localFileName = localFile.getName();
                String currentFileExtension = FilenameUtils.getExtension(localFileName);
                String newLocalFileName = localFileName.replace(currentFileExtension, "arff"); //newFileExtension);


                //now create a writable path from resultingSubDir and the given localFile
                //getAbsolutePath() returns path string without trailing directory separator
                String writableFilePathString = resultingSubDir.getAbsolutePath() + File.separator + newLocalFileName;

                //create a file from writable path
                File writableFile = new File(writableFilePathString);
                writableFile.createNewFile();


                //now generate arff file using WEKA API
                // load CSV
                CSVLoader loader = new CSVLoader();
                loader.setSource(localFile);
                loader.setNumericAttributes(numericAttributeRange); // forces all attributed to be set numeric, argument should be "first-last"
                Instances data = loader.getDataSet();

                // save ARFF
                ArffSaver saver = new ArffSaver();
                saver.setInstances(data);
                saver.setFile(writableFile);
                //saver.setDestination(writableFile); no need as of weka 3.5.3
                saver.writeBatch();


                System.out.println("Generated arff file at => " + writableFile.getAbsolutePath());
            } // else if
            else
                System.err.println("\n" + localFilePathString + " is neither a file, nor a directory; please provide correct file path");

        } // try
        catch (Exception ex) {
            ex.printStackTrace();
        } // catch

    } // csv2Arff


    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }


    public static boolean isFloat(String str) {
        try {
            Float.parseFloat(str);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }


    //method to calculate mean arterial (blood) pressure from systolic (sbp or sysabp) and diastolic (dbp or disabp) blood pressures
    public static float map(int sbp, int dbp)
    {
        if(sbp < 0 || dbp < 0)
            return -1;

        //formula is (sbp + 2 * dbp) / 3
        float sum = sbp + 2.0f * dbp;

        return format("#.##", RoundingMode.HALF_UP, sum / 3);
    } // map


    //helper method to calculate sysabp (sbp) from map and diasabp (dbp)
    public static int sbp(float map, int dbp)
    {
        if(Float.compare(map, 0.0f) < 0 || dbp < 0)
            return -1;

        float sbp = 3.0f * map - 2.0f * dbp;
        if(Float.compare(sbp, 0.0f) < 0)
            return -1;

        return (int) sbp;
    } // sbp


    //helper method to calculate diasabp (dbp) from mao and sysabp (sbp)
    public static int dbp(float map, int sbp)
    {
        if(Float.compare(map, 0.0f) < 0 || sbp < 0)
            return -1;

        float dbp = (3.0f * map - 1.0f * sbp) / 2.0f;
        if(Float.compare(dbp, 0.0f) < 0)
            return -1;

        return (int) dbp;
    } // dbp


    public static float format(String decimalFormatPattern, RoundingMode roundingMode, float value)
    {
        DecimalFormat df = new DecimalFormat(decimalFormatPattern);
        df.setRoundingMode(roundingMode);

        return Float.parseFloat(df.format(value));
    } // format


    //helper method to calculate the mean of float point values
    public static float mean(Collection<Float> values)
    {
        float sum = 0.0f;
        for(float value : values)
        {
            sum += value;
        } // for

        return format("#.##", RoundingMode.HALF_UP, sum / values.size());
    } // mean


    public static float median(Float[] input)
    {
        if (input.length == 0) {
            throw new IllegalArgumentException("to calculate median we need at least 1 element");
        }

        Arrays.sort(input);

        if (input.length % 2 == 0)
        {
            return (input[input.length / 2 - 1] + input[input.length / 2]) / 2;
        }
        else
            return input[input.length / 2];
    } // median


    //TODO use the mean obtained from training dataset for test data set during imputation of test dataset
    //helper method to compute the mean of variables in all given time series using masking vector
    public static Map<String, Float> meansOfVariablesUsingMaskingVector(List<MTSE> mtses)
    {
        if(mtses.isEmpty()) throw new RuntimeException("At least one mtse should be provided as an argument");

        HashMap<String, Float> varMeanMap = new HashMap<>();
        //all mtses have the same vars, so get vars
        List<String> vars = mtses.get(0).getVars();

        //for each variable compute the mean
        for(String var : vars)
        {
            float sumOfMaskingMultipliedByVarValueForThisVar = 0;
            float sumOfMaskingsForThisVar = 0;

            for(MTSE mtse : mtses)
            {
                List<Float> varValuesInTsOrderForThisVar = mtse.getVarValuesInTsOrder().get(var);
                List<Integer> maskingsInTsOrderForThisVar = mtse.getMaskingsInTsOrder().get(var);

                //if masking size and number of var values are different throw exception
                if(varValuesInTsOrderForThisVar.size() != maskingsInTsOrderForThisVar.size())
                    throw new RuntimeException("Masking size and number of var values should be the same");

                for(int tsIndex = 0; tsIndex < varValuesInTsOrderForThisVar.size(); tsIndex ++)
                {
                    sumOfMaskingsForThisVar += maskingsInTsOrderForThisVar.get(tsIndex);
                    sumOfMaskingMultipliedByVarValueForThisVar
                            += maskingsInTsOrderForThisVar.get(tsIndex) * varValuesInTsOrderForThisVar.get(tsIndex);
                } // for each tsIndex
            } // for each mtse

            float meanForThisVar
                    = Utils.format("#.##", RoundingMode.HALF_UP,
                    sumOfMaskingMultipliedByVarValueForThisVar / sumOfMaskingsForThisVar);
            varMeanMap.put(var, meanForThisVar);
        } // for each var

        return varMeanMap;
    } // meansOfVariablesUsingMaskingVector


    //var args to array method
    public static <T> T[] varArgsToArray(T... args)
    {
        return args;
    }


    //helper method to generate outcomes from "outcomes" files
    public static Outcomes outcomesFromLocalFilePaths(String lineComponentDelimiter, Dataset dataset, String... outcomesLocalFilePaths)
    {
        if(outcomesLocalFilePaths.length == 0)
            throw new RuntimeException("Please provide at least one file path for outcomes");

        //create outcomes for the given dataset
        Outcomes outcomes = Outcomes.getInstance(dataset);

        //now process each file
        for(String outcomesFilePath : outcomesLocalFilePaths)
        {
            String fileContents = fileContentsFromLocalFilePath(outcomesFilePath);
            //obtain lines
            String[] lines = StringUtils.split(fileContents, "\r\n|\r|\n");

            //now for each line, split the line, obtain the outcomes
            //file structure is as follows:
            //RecordID,SAPS-I,SOFA,Length_of_stay,Survival,In-hospital_death
            //132539,6,1,5,-1,0
            //132540,16,8,8,-1,0
            //132541,21,11,19,-1,0
            //.....

            //all values are integer representable
            //discard the first line which is the header line
            for(int index = 1; index < lines.length; index ++)
            {
                String thisLine = lines[index];

                //line will have 6 components
                String[] thisLineComponents = thisLine.split(lineComponentDelimiter);

                //retrieve values
                int recordID = Integer.parseInt(thisLineComponents[0]);
                int sapsIScore = Integer.parseInt(thisLineComponents[1]);
                int sofaScore = Integer.parseInt(thisLineComponents[2]);
                int lengthOfStayInDays = Integer.parseInt(thisLineComponents[3]);
                int survivalInDays = Integer.parseInt(thisLineComponents[4]);
                int inHospitalDeath0Or1 = Integer.parseInt(thisLineComponents[5]);

                //create an outcome
                Outcome outcome = new Outcome(recordID, sapsIScore, sofaScore, lengthOfStayInDays, survivalInDays, inHospitalDeath0Or1);
                //update outcomes for this dataset
                outcomes.add(recordID, outcome);

            } // for each line
        } // for each file

        System.out.println("Read Outcomes files");

        return outcomes;
    } // outcomesFromLocalFilePaths


    //helper method to obtain general descriptors from file paths
    public static GeneralDescriptorsRecords genDescRecordsFromLocalFilePaths(String lineComponentDelimiter, Dataset dataset,
                                                                             String... generalDescriptorsRecordsLocalFilePaths)
    {
        if(generalDescriptorsRecordsLocalFilePaths.length == 0)
            throw new RuntimeException("Please provide at least one file path for general descriptors records");

        //create general descriptors records for the given dataset
        GeneralDescriptorsRecords records = GeneralDescriptorsRecords.getInstance(dataset);

        //now process each file
        for(String genDescRecordsLocalFilePath : generalDescriptorsRecordsLocalFilePaths)
        {
            String fileContents = fileContentsFromLocalFilePath(genDescRecordsLocalFilePath);
            //obtain lines
            String[] lines = StringUtils.split(fileContents, "\r\n|\r|\n");

            //now for each line, split the line, obtain the general descriptors records
            //file structure is as follows:
            ///RecordID,Age,Gender,Height,ICUType,Weight
            //132539,54,0,-1,4,-1
            //......


            //discard the first line which is the header line
            for(int index = 1; index < lines.length; index ++)
            {
                String thisLine = lines[index];

                //line will have 6 components
                String[] thisLineComponents = thisLine.split(lineComponentDelimiter);

                try {
                    //retrieve values
                    int recordID = Integer.parseInt(thisLineComponents[0]);
                    int ageInYears = Integer.parseInt(thisLineComponents[1]);
                    int gender0or1 = Integer.parseInt(thisLineComponents[2]);
                    float heightInCentimeters = Float.parseFloat(thisLineComponents[3]);
                    int icuTypeCode = Integer.parseInt(thisLineComponents[4]);
                    float weighInKg = Float.parseFloat(thisLineComponents[5]);

                    //create a GeneralDescriptorsRecord
                    GeneralDescriptorsRecord record = new GeneralDescriptorsRecord(recordID, ageInYears,
                            gender0or1, heightInCentimeters, icuTypeCode, weighInKg);
                    //update GeneralDescriptorsRecords for this dataset
                    records.add(recordID, record);

                }
                catch(NumberFormatException ex)
                {
                    ex.printStackTrace();
                    System.out.println(genDescRecordsLocalFilePath + "  => " + thisLine + " => " + (index + 1));
                } // catch

            } // for each line
        } // for each file


        System.out.println("Read GeneralDescriptorsRecords files");

        return records;
    } // genDescRecordsFromLocalFilePaths

} // class Utils
