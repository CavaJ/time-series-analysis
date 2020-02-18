package com.rb.tsa;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.*;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.MultiInstanceToPropositional;
import weka.filters.unsupervised.attribute.PropositionalToMultiInstance;
import weka.gui.visualize.Plot2D;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Queue;
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



    //to obtain input stream from a string
    public static InputStream stringToInputStream(String str)
    {
        return new ByteArrayInputStream(str.getBytes());
    } // stringToInputStream


    //helper method to convert mtses to weka instances
    public static Instances mtsesToMIData(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, String bagName,
                                          String[] classLabels, boolean includeTsMinutes, String subsetOfTheDataset) throws Exception
    {
        if(mtses.isEmpty()) throw new RuntimeException("At least one mtse should be provided");
        List<String> vars = mtses.get(0).getVars();

        //obtain record ids from mtses in the given list order, where real bags will be constructed for these mtses
        List<String> recordIdsInMtses = new ArrayList<>();
        mtses.forEach(mtse -> recordIdsInMtses.add(mtse.getRecordID() + ""));

        //feedback
        System.out.println("Generating content for multi-instance format...");

        StringBuilder sb = new StringBuilder("@relation " + dataset.toSimpleString() + "_" + subsetOfTheDataset).append("\n\n");
        sb.append("@attribute ").append(bagName).append(" ")
                .append("{").append(String.join(",", recordIdsInMtses)).append("}").append("\n");
        sb.append("@attribute bag relational").append("\n");

        if(includeTsMinutes) sb.append(" @attribute ").append("tsMinutes").append(" numeric").append("\n");

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
                if(includeTsMinutes)
                    //it joins all elements in the row by comma
                    joinedValuesInVarNameOrder.add(ts + "," + String.join(",", toStringCollection(valuesInVarNameOrder.get(ts))));
                else
                    joinedValuesInVarNameOrder.add(String.join(",", toStringCollection(valuesInVarNameOrder.get(ts))));
            } // for
            //then joins rows by \n and appends a comma, class label and new line character
            sb.append(String.join("\\n", joinedValuesInVarNameOrder)).append("\"").append(",")
                    .append(outcomes.get(mtse.getRecordID()).getInHospitalDeath0Or1()).append("\n");
        } // for

        System.out.println("Relation " + dataset.toSimpleString() + "_" + subsetOfTheDataset + " is loaded as Weka Instances...");
        return new ConverterUtils.DataSource(stringToInputStream(sb.toString())).getDataSet();

    } // mtsesToMIData



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


    //transform


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
                                                  String[] classLabels, boolean includeTsMinutes,
                                                         String destinationDirPath, String newDirNameToPutFile, String fileNameAppendix)
    {
        if(mtses.isEmpty()) throw new RuntimeException("At lease one mtse should be provided");
        List<String> vars = mtses.get(0).getVars();

        //obtain record ids from mtses in the given list order, where real bags will be constructed for these mtses
        List<String> recordIdsInMtses = new ArrayList<>();
        mtses.forEach(mtse -> recordIdsInMtses.add(mtse.getRecordID() + ""));

        //feedback
        System.out.println("Generating content for multi-instance arff file...");

        StringBuilder sb = new StringBuilder("@relation " + dataset.toSimpleString() + "_" + fileNameAppendix).append("\n\n");
        sb.append("@attribute ").append(bagName).append(" ")
                .append("{").append(String.join(",", recordIdsInMtses)).append("}").append("\n");
        sb.append("@attribute bag relational").append("\n");

        if(includeTsMinutes) sb.append(" @attribute ").append("tsMinutes").append(" numeric").append("\n");

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
                if(includeTsMinutes)
                    //it joins all elements in the row by comma
                    joinedValuesInVarNameOrder.add(ts + "," + String.join(",", toStringCollection(valuesInVarNameOrder.get(ts))));
                else
                    joinedValuesInVarNameOrder.add(String.join(",", toStringCollection(valuesInVarNameOrder.get(ts))));
            } // for
            //then joins rows by \n and appends a comma, class label and new line character
            sb.append(String.join("\\n", joinedValuesInVarNameOrder)).append("\"").append(",")
                    .append(outcomes.get(mtse.getRecordID()).getInHospitalDeath0Or1()).append("\n");
        } // for


        String newFileName
                = dataset.toSimpleString() + "_" + fileNameAppendix + ".arff";
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


    //helper method to print weka instances
    public static void printWekaInstances(Instances data, int limit)
    {
        if(limit < 1 || limit > data.numInstances())
            throw new RuntimeException("limit should be in range");

        for(int index = 0; index < limit; index ++)
        {
            System.out.println(data.instance(index).toString());
        } // for
    } // printWekaInstances


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
                //below line not working with weka version 3.7.0
                //loader.setNumericAttributes(numericAttributeRange); // forces all attributed to be set numeric, argument should be "first-last"
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


//    //TODO implement the overloaded version to compute the median form the list
//    public static float median(List<Float> list)
//    {
//        if (list.size() == 0) {
//            throw new IllegalArgumentException("to calculate median we need at least 1 element");
//        }
//
//        Collections.sort(list);
//
//
//
//    } // median


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


    //helper method to calculate the medians of variables in mtses
    public static Map<String, Float> mediansOfVariables(List<MTSE> mtses)
    {
        if(mtses.isEmpty()) throw new RuntimeException("At least one mtse should be provided as an argument");

        HashMap<String, Float> varMedianMap = new HashMap<>();
        //all mtses have the same vars, so get vars
        List<String> vars = mtses.get(0).getVars();

        HashMap<String, List<Float>> varAndAllValuesMap = new HashMap<>();
        //initialize a list for each var
        for(String var : vars)
            varAndAllValuesMap.put(var, new ArrayList<Float>());

        //for each variable, collect all its non-negative values
        for(String var : vars)
        {
            List<Float> allValuesOfThisVar = varAndAllValuesMap.get(var);

            for(MTSE mtse : mtses)
            {
                List<Float> varValuesInTsOrderForThisVar = mtse.getVarValuesInTsOrder().get(var);
                List<Integer> maskingsInTsOrderForThisVar = mtse.getMaskingsInTsOrder().get(var);

                //if masking size and number of var values are different throw exception
                if(varValuesInTsOrderForThisVar.size() != maskingsInTsOrderForThisVar.size())
                    throw new RuntimeException("Masking size and number of var values should be the same");

                for (float valueAtThisTs : varValuesInTsOrderForThisVar)
                {
                    if (valueAtThisTs >= 0) //only add valid values to the list, missing values are < 0
                        allValuesOfThisVar.add(valueAtThisTs);
                } // for each tsIndex


            } // for each mtse

        } // for each var

        //now for each var sort its list and compute median
        for(String var : varAndAllValuesMap.keySet())
        {
            List<Float> list = varAndAllValuesMap.get(var);
            //list.sort(Float::compareTo);

            //compute the median
            float median = median(list.toArray(new Float[]{})); // method internally sorts the array
            varMedianMap.put(var, median);
        } // for

        return varMedianMap;
    } // mediansOfVariables



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

                //if(!(inHospitalDeath0Or1 == 0 || inHospitalDeath0Or1 == 1)) System.err.println("missing outcomes ");

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



    //helper method to cross validate data
    public static void crossValidate(Classifier cls, Instances data, Instances holdOutTestData, int seed, int folds,
                                     boolean displayRocCurve) throws Exception
    {
        System.out.println("Original data: " + Utils.classImbalanceOnWekaInstances(data));

        // randomize data
        Random rand = new Random(seed);
        Instances randData = new Instances(data);
        randData.randomize(rand);

        System.out.println("Randomized data: " + Utils.classImbalanceOnWekaInstances(data));

        //  For example in a binary classification problem where we want to predict if a passenger on Titanic survived or not.
        //  we have two classes here Passenger either survived or did not survive.
        //  We ensure that each fold has a percentage of passengers that survived, and a percentage of passengers that did not survive.
        if (randData.classAttribute().isNominal()) {//TODO check class size in each fold; num instances of negative and positive class versus non-stratified case
            System.out.println("Class value is nominal, stratifying folds");
            randData.stratify(folds);
        }

        System.out.println("Randomized data after stratification: " + Utils.classImbalanceOnWekaInstances(data));

        // perform cross-validation
        System.out.println();
        System.out.println("=== Setup ===");
        System.out.println("Classifier: " + cls.toString()); //weka.core.Utils.toCommandLine(cls));
        System.out.println("Dataset: " + data.relationName());
        System.out.println("Folds: " + folds);
        System.out.println("Seed: " + seed);
        System.out.println();
        Evaluation evalAll = new Evaluation(randData);
        for (int n = 0; n < folds; n++)
        {
            Evaluation eval = new Evaluation(randData);
            Instances train = randData.trainCV(folds, n, rand);
            Instances test = randData.testCV(folds, n);
            // the above code is used by the StratifiedRemoveFolds filter, the
            // code below by the Explorer/Experimenter:
            // Instances train = randData.trainCV(folds, n, rand);

            System.out.println("Fold " + (n+1) + ", training data => " + Utils.classImbalanceOnWekaInstances(train));
            if(holdOutTestData == null)
                System.out.println("Fold " + (n+1) + ", test data => " + Utils.classImbalanceOnWekaInstances(test));
            else
                System.out.println("Fold " + (n+1) + ", test data => " + Utils.classImbalanceOnWekaInstances(holdOutTestData));


            // build and evaluate classifier
            Classifier clsCopy = AbstractClassifier.makeCopy(cls);
                                //Classifier.makeCopy(cls); //weka 3.7.0
            clsCopy.buildClassifier(train);
            if(holdOutTestData == null) {
                eval.evaluateModel(clsCopy, test);
                evalAll.evaluateModel(clsCopy, test);
            }
            else {
                eval.evaluateModel(clsCopy, holdOutTestData);
                evalAll.evaluateModel(clsCopy, holdOutTestData);
            }


            System.out.println("\n=== Fold " + (n+1) + ", Classifier: " + clsCopy.getClass() + ",      AUROC: " + eval.areaUnderROC(0)
                    + ",      Weighted AUROC: " + eval.weightedAreaUnderROC() + ",      Accuracy: " + accuracy(eval) + " ==== ");

            System.out.println("=== Fold " + (n+1) + ", Classifier: " + clsCopy.getClass() + ", Avg. AUROC: " + evalAll.areaUnderROC(0)
                    + ", Avg. Weighted AUROC: " + evalAll.weightedAreaUnderROC() + ", Avg. Accuracy: " + accuracy(evalAll) +  " ==== \n");

            // output evaluation
            System.out.println();
            System.out.println(eval.toMatrixString("=== Confusion matrix for fold " + (n+1) + "/" + folds + " ===\n"));
        } // for


        //TODO save model and test on hold-out-set;
        // check with weka explorer: https://www.youtube.com/watch?v=UzT4W1tOKD4
        // this page https://waikato.github.io/weka-wiki/generating_classifier_evaluation_output_manually/
        // says models are saved as built from full training set, even after cross validation,
        // in this case, compare explorer output to programmatic train-test output


        //finally, evaluate model on hold-out test data; not meaningful
        //System.out.println("Final evaluation on hold-out test set...");
        //evalAll.evaluateModel(Classifier.makeCopy(cls), holdOutTestData);


        //evalAll.crossValidateModel(cls, data, folds, new Random(seed)); // Equivalent to the for loop above


        // output evaluation
        System.out.println();
        System.out.println(evalAll.toSummaryString("=== " + folds + "-fold Cross-validation ===", false));

        System.out.println("AUROC => " + evalAll.areaUnderROC(0));
        System.out.println("Weighted AUROC => " + evalAll.weightedAreaUnderROC());
        System.out.println("Accuracy => " + accuracy(evalAll));
        System.out.println("====================================================");


        // generate curve
        if(displayRocCurve)
            toROCCurve(evalAll);
    } // crossValidate


    //helper method to train and test on in given number of runs
    public static void simpleTrainTestOverMultipleRuns(Classifier cls, Instances trainingData, int runs,
                                                       Instances testData, boolean displayROCCurve
                                                        //, Instances separateHoldOutSet, boolean testOnSeparateHoldOutSet
                                                        ) throws Exception
    {
        System.out.println("WEKA version: " + Version.VERSION);
        System.out.println("Original data: " + Utils.classImbalanceOnWekaInstances(trainingData));

        // perform cross-validation
        System.out.println();
        System.out.println("=== Setup ===");
        System.out.println("Classifier: " + cls.toString()); //weka.core.Utils.toCommandLine(cls));
        System.out.println("Dataset: " + trainingData.relationName());
        System.out.println("Runs: " + runs);
        System.out.println();
        Evaluation evalAll = new Evaluation(trainingData);
        for (int i = 0; i < runs; i++)
        {
            // randomize data
            int seed = i + 1;
            Random rand = new Random(seed);
            Instances randTrainingData = new Instances(trainingData);
            randTrainingData.randomize(rand);


            // build classifier
            Classifier clsCopy = AbstractClassifier.makeCopy(cls);
                                //Classifier.makeCopy(cls); // weka 3.7.0
            clsCopy.buildClassifier(randTrainingData);
            // evaluate classifier
            Evaluation eval = new Evaluation(randTrainingData);
            eval.evaluateModel(clsCopy, testData);
            evalAll.evaluateModel(clsCopy, testData);


            System.out.println("Run " + (i+1) + ", training data => " + Utils.classImbalanceOnWekaInstances(randTrainingData));
            System.out.println("Run " + (i+1) + ", test data => " + Utils.classImbalanceOnWekaInstances(testData));


            System.out.println("\n=== Run " + (i+1) + ", Classifier: " + clsCopy.getClass() + ",      AUROC: " + eval.areaUnderROC(0)
                    + ",      Weighted AUROC: " + eval.weightedAreaUnderROC() + ",      Accuracy: " + accuracy(eval) + " ==== ");

            System.out.println("=== Run " + (i+1) + ", Classifier: " + clsCopy.getClass() + ", Avg. AUROC: " + evalAll.areaUnderROC(0)
                                + ", Avg. Weighted AUROC: " + evalAll.weightedAreaUnderROC() + ", Avg. Accuracy: " + accuracy(evalAll) +  " ==== \n");


            // output evaluation
            System.out.println();
            System.out.println(eval.toMatrixString("=== Confusion matrix for run " + (i+1) + "/" + runs + " ===\n"));
        } // for


        // output evaluation
        System.out.println();
        System.out.println(evalAll.toSummaryString("=== " + runs + "-runs Train-test ===", false));

        System.out.println("AUROC => " + evalAll.areaUnderROC(0));
        System.out.println("Weighted AUROC => " + evalAll.weightedAreaUnderROC());
        System.out.println("Accuracy => " + accuracy(evalAll));
        System.out.println("====================================================");

        // generate curve
        if(displayROCCurve) toROCCurve(evalAll);


        //not possible to get average model to test on set-c, not possible to get the best performing classifier (which already have a build classifier run on it)
        //if(testOnSeparateHoldOutSet)
        //{
        //} // if
    } // simpleTrainTestOverMultipleRuns


    //helper method to do simply train and test
    public static void simpleTrainTest(Classifier cls, Instances train, Instances test,  int seedToRandomizeTrainingData,
                                       boolean displayROCCurve //, Instances separateHoldOutSet, boolean testOnSeparateHoldOutSet
                                        ) throws Exception
    {
        Random rand = new Random(seedToRandomizeTrainingData);   // create seeded number generator
        Instances randTrainingData = new Instances(train);   // create copy of original data
        randTrainingData.randomize(rand);         // randomize data with number generator

        System.out.println("WEKA version: " + Version.VERSION);
        System.out.println("Train => " + Utils.classImbalanceOnWekaInstances(randTrainingData));
        System.out.println("Test => " + Utils.classImbalanceOnWekaInstances(test));
        //System.out.println(randTrainingData.toSummaryString());
        //System.out.println(test.toSummaryString());


        //make copy
        Classifier clsCopy = AbstractClassifier.makeCopy(cls);
                             //Classifier.makeCopy(cls);

        clsCopy.buildClassifier(randTrainingData); //(train);
        // evaluate the classifier and print some statistics
        Evaluation eval = new Evaluation(randTrainingData); //(train);
        eval.evaluateModel(clsCopy, test);
        //System.out.println(eval.toSummaryString("\nResults\n======\n", false));
        System.out.println(eval.toSummaryString());
        //println(eval.toCumulativeMarginDistributionString());
        System.out.println(eval.toMatrixString());
        //println(eval.toClassDetailsString());
        //positive class is at index 0
        //PredictivePerformanceEvaluator predEval
        //        = new PredictivePerformanceEvaluator(eval.numTruePositives(0), eval.numFalseNegatives(0),
        //        eval.numFalsePositives(0), eval.numTrueNegatives(0), new String[]{"1", "0"});
        //println(predEval.confusionMatrix());


        System.out.println("AUROC => " + eval.areaUnderROC(0));
        System.out.println("Weighted AUROC => " + eval.weightedAreaUnderROC());
        System.out.println("Accuracy => " + accuracy(eval));
        System.out.println("====================================================");

        // generate curve
        if(displayROCCurve) toROCCurve(eval);


//        //if testing on a separate data is requested
//        if(testOnSeparateHoldOutSet)
//        {
//            Random random = new Random(seedToRandomizeTrainingData);   // create seeded number generator
//            Instances randTrainData = new Instances(train);   // create copy of original data
//            randTrainData.randomize(random);         // randomize data with number generator
//
//            System.out.println("Train => " + Utils.classImbalanceOnWekaInstances(randTrainData));
//            System.out.println("Separate Hold Out Set => " + Utils.classImbalanceOnWekaInstances(separateHoldOutSet));
//
//            //make copy
//            Classifier clsCopy2 = AbstractClassifier.makeCopy(cls);
//                                //= Classifier.makeCopy(cls);
//
//            clsCopy2.buildClassifier(randTrainData); //(train);
//            // evaluate the classifier and print some statistics
//            Evaluation evaluation = new Evaluation(randTrainData);
//            evaluation.evaluateModel(clsCopy2, separateHoldOutSet);
//
//            System.out.println("Separate Hold Out Set, AUROC => " + evaluation.areaUnderROC(0));
//            System.out.println("Separate Hold Out Set, Weighted AUROC => " + evaluation.weightedAreaUnderROC());
//            System.out.println("Separate Hold Out Set, Accuracy => " + accuracy(evaluation));
//            System.out.println("====================================================");
//        } // if

    } // simpleTrainTest


    //TODO implement method to perform hyperparameter selection for classifier
    // write different hyperparam selection for each classifier class
    // use CVParamSelection from ParallelRadonMachine


    //helper method to generate ROC curve
    public static void toROCCurve(Evaluation eval)
    {
        try
        {
            // generate curve
            ThresholdCurve tc = new ThresholdCurve();
            int classIndex = 0;
            Instances result = tc.getCurve(eval.predictions(), classIndex);

            // plot curve
            ThresholdVisualizePanel vmc = new ThresholdVisualizePanel();
            vmc.setROCString("(Area under ROC = " +
                    weka.core.Utils.doubleToString(tc.getROCArea(result), 4) + ")");
            vmc.setName(result.relationName());
            PlotData2D tempd = new PlotData2D(result);
            tempd.setPlotName(result.relationName());
            tempd.addInstanceNumberAttribute();
            // specify which points are connected
            boolean[] cp = new boolean[result.numInstances()];
            for (int n = 1; n < cp.length; n++)
                cp[n] = true;
            tempd.setConnectPoints(cp);
            // add plot
            vmc.addPlot(tempd);

            // display curve
            String plotName = vmc.getName();
            final javax.swing.JFrame jf =
                    new javax.swing.JFrame("Weka Classifier Visualize: "+plotName);
            jf.setSize(500,400);
            jf.getContentPane().setLayout(new BorderLayout());
            jf.getContentPane().add(vmc, BorderLayout.CENTER);
            jf.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    jf.dispose();
                }
            });
            jf.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    } // toROCCurve


    //does not work as expected
    public static void visualizeData(Instances data) throws Exception
    {
        //data = toProp(data);


        // Set up a window for the plot and a Plot2D object
        JFrame jf = new javax.swing.JFrame("Visualize groups of instances using different glyphs");
        jf.setSize(500, 400);
        jf.getContentPane().setLayout(new java.awt.BorderLayout());
        Plot2D p2D = new weka.gui.visualize.Plot2D();
        jf.getContentPane().add(p2D, java.awt.BorderLayout.CENTER);

        //configure the plot
        PlotData2D pd1 = new weka.gui.visualize.PlotData2D(data);
        //ArrayList<Integer> shapeTypesForInstances = new ArrayList<Integer>(data.numInstances());
        FastVector shapeTypesForInstances = new FastVector(data.numInstances());
        //ArrayList<Integer> shapeSizesForInstances = new ArrayList<Integer>(data.numInstances());
        FastVector shapeSizesForInstances = new FastVector(data.numInstances());

        for (int index = 0; index < data.numInstances(); index ++)
        {
            Instance inst = data.instance(index);
            shapeTypesForInstances.add((int)inst.value(data.attribute("class").index())); // Change attribute giving group indicators here
            shapeSizesForInstances.add(3);
        }
//        int[] types = new int[shapeTypesForInstances.size()];
//        for(int index = 0; index < types.length; index ++)
//            types[index] = shapeTypesForInstances.get(index);
//        int[] sizes = new int[shapeSizesForInstances.size()];
//        for(int index =0; index < sizes.length; index ++)
//            sizes[index] = shapeSizesForInstances.get(index);

        pd1.setShapeType(shapeTypesForInstances);
        pd1.setShapeSize(shapeSizesForInstances);
        pd1.setCustomColour(java.awt.Color.BLACK);
        p2D.setMasterPlot(pd1);
        p2D.setXindex(data.attribute("patient_record_id").index()); // Change attribute name for X axis here
        p2D.setYindex(data.attribute("class").index()); // Change attribute name for Y axis here

        // Make plot visible
        jf.setVisible(true);
    } // visualizeData


    //shortcut method to generate the accuracy for the learned model
    public static double accuracy(Evaluation evaluation)
    {
        PredictivePerformanceEvaluator evaluator = new PredictivePerformanceEvaluator(evaluation.numTruePositives(0),
                evaluation.numFalseNegatives(0), evaluation.numFalsePositives(0),
                evaluation.numTrueNegatives(0), new String[]{"1", "0"});

        return evaluator.accuracy();
    } // accuracy


    //helper method to print weights of instances of each bag
    public static void printInstanceWeightsOfABag(Instance bagInstance)
    {
        //recordId is at attribute 0
        //bagInstance.attribute(0).value(0) => obtains value from patient_record_id attribute in the dataset description, not from the instance
        System.out.println("Bag " + bagInstance.toString(bagInstance.attribute(0)) + "'s instance weights: ");
        Instances bag = bagInstance.relationalValue(1);
        for(int index = 0; index < bag.numInstances(); index ++)
        {
            Instance thisInnerInstance = bag.instance(index);
            System.out.println("The weight of instance: " + thisInnerInstance
                    + " => " + thisInnerInstance.weight());
        } // for
    } // printInstanceWeightsOfABag



    //helper method to print bag weights of multi-instance data
    public static void printBagWeights(Instances multiInstanceData, int from, int to)
    {
        if(from < 0 || from > multiInstanceData.numInstances() || to < 0 || to > multiInstanceData.numInstances() || from > to)
            throw new RuntimeException("Please correctly set from and to");

        multiInstanceData = new Instances(multiInstanceData, from, to);

        for(int index = 0; index < multiInstanceData.numInstances(); index ++)
        {
            Instance thisBag = multiInstanceData.instance(index);
            //thisBag.attribute(0).value(index) > obtains value from patient_record_id attribute in the dataset description, not from the instance
            System.out.println(thisBag.toString(thisBag.attribute(0)) + "'s Weight => "
                    + thisBag.weight());
        } // for
    } // printBagWeights


    //helper method to remove ts from the given data
    public static Instances removeTs(Instances data)
    {
        if(!data.attribute(1).relation().attribute(0).name().contains("ts"))
        {
            throw new RuntimeException("First attribute is not timestamp");
        } // if

        //take copy of the instance before manipulation
        data = new Instances(data);

        for(int index = 0; index < data.numInstances(); index ++)
        {
            //data.instance(index).attribute(0).value(0) => obtains value from patient_record_id attribute in the dataset description, not from the instance
            //System.out.println(data.instance(index).toString(bagInstance.attribute(0) + "'s Weight => " + data.instance(index).weight());
            //System.out.println(data.instance(index).toString(bagInstance.attribute(0) + "'s bag => "
            //        + data.instance(index).relationalValue(1));

            Instances bagOfThisBagInstance = data.instance(index).relationalValue(1);

            //remove ts minutes
            bagOfThisBagInstance.deleteAttributeAt(0);
            //System.out.println(bagOfThisBagInstance.numAttributes());
            //System.out.println("\n====== After deletion =====\n");
            //printWekaInstances(bagOfThisBagInstance);

            //break;
        } // for

        //remove attribute name from schema; (tsMinutes)
        //bag relational is at index 1, and index 0 of that relation is tsMinutes
        data.attribute(1).relation().deleteAttributeAt(0);

        return data;
    } // removeTs


    //helper method to reweight instances inside each bag by ts through keeping multiInstance format
    public static Instances reweightInstancesOfEachBagByTs(Instances data) throws Exception
    {
        //take copy of the instance before manipulation
        data = new Instances(data);


        for(int index = 0; index < data.numInstances(); index ++)
        {
            //data.instance(index).attribute(0).value(0) => obtains value from patient_record_id attribute in the dataset description, not from the instance
            //System.out.println(data.instance(index).toString(bagInstance.attribute(0) + "'s Weight => " + data.instance(index).weight());
            //System.out.println(data.instance(index).toString(bagInstance.attribute(0) + "'s bag => "
            //        + data.instance(index).relationalValue(1));

            Instances bagOfThisBagInstance = data.instance(index).relationalValue(1);

            double sumOfTsMinutes = 0;
            for(int innerIndex = 0; innerIndex < bagOfThisBagInstance.numInstances(); innerIndex ++)
            {
                Instance thisInnerInstance = bagOfThisBagInstance.instance(innerIndex);
                double timeStampOfThisInstance = thisInnerInstance.value(0); // ts at index 0
                sumOfTsMinutes += timeStampOfThisInstance;
            }


            //System.out.println("\n ===== Before reweighting =====\n");
            //for(int innerIndex = 0; innerIndex < bagOfThisBagInstance.numInstances(); innerIndex ++)
            //{
            //    Instance thisInnerInstance = bagOfThisBagInstance.instance(innerIndex);
                //System.out.println("The weight of instance with ts: " + thisInnerInstance.value(0)
                //                                    + " => " + thisInnerInstance.weight());
                //System.out.println(thisInnerInstance);
            //} // for


            for(int innerIndex = 0; innerIndex < bagOfThisBagInstance.numInstances(); innerIndex ++)
            {
                Instance thisInnerInstance = bagOfThisBagInstance.instance(innerIndex);
                double timeStampOfThisInstance = thisInnerInstance.value(0); // ts at index 0
                thisInnerInstance.setWeight((timeStampOfThisInstance / sumOfTsMinutes) ); // TODO +1 disabled
            } // for


            //System.out.println("\n ===== After reweighting =====\n");
            //for(int innerIndex = 0; innerIndex < bagOfThisBagInstance.numInstances(); innerIndex ++)
            //{
            //    Instance thisInnerInstance = bagOfThisBagInstance.instance(innerIndex);
                //System.out.println("The weight of instance with ts: " + thisInnerInstance.value(0)
                //        + " => " + thisInnerInstance.weight());
                //System.out.println(thisInnerInstance);
            //} // for


            //remove ts minutes
            bagOfThisBagInstance.deleteAttributeAt(0);
            //System.out.println(bagOfThisBagInstance.numAttributes());
            //System.out.println("\n====== After deletion =====\n");
            //printWekaInstances(bagOfThisBagInstance);

            //break;
        } // for

        //remove attribute name from schema; (tsMinutes)
        //bag relational is at index 1, and index 0 of that relation is tsMinutes
        data.attribute(1).relation().deleteAttributeAt(0);

        //System.out.println(new Instances(data, 0, 100));

        return data;
    } // reweightInstancesOfEachBagByTs


    //helper method to reweight instances obtained by multi-instance filter
    public static Instances transformMIDataToProp(Instances miData, boolean removeTs, boolean keepPropFormat, boolean reweightByTs) throws Exception
    {
        if (miData.numInstances() == 0 || miData.instance(0).relationalValue(1) == null)
            throw new RuntimeException("Given data is either empty or is not multi-instance data");

        //apply mi to prop
        MultiInstanceToPropositional miToProp = new MultiInstanceToPropositional();
        //-A <num>
        //  The type of weight setting for each prop. instance:
        //0.weight = original single bag weight /Total number of
        //prop. instance in the corresponding bag;
        //1.weight = 1.0;
        //2.weight = 1.0/Total number of prop. instance in the
        //corresponding bag;
        //3. weight = Total number of prop. instance / (Total number
        //of bags * Total number of prop. instance in the
        //corresponding bag).
        //(default:0)
        miToProp.setOptions(weka.core.Utils.splitOptions("-A 1"));
        miToProp.setInputFormat(miData);
        Instances newData = Filter.useFilter(miData, miToProp);
        //System.out.println("After applying miToProp filter newData => " + Utils.classImbalanceOnWekaInstances(newData));
        //System.out.println(newData);

        if(reweightByTs)
        {
            LinkedHashMap<String, List<Instance>> bagIdInstanceMap = new LinkedHashMap<>();

            for(int index = 0; index < newData.numInstances(); index ++)
            {
                Instance thisPropInstance = newData.instance(index);
                String bagId = thisPropInstance.toString(thisPropInstance.attribute(0));

                //if the bagId is not there, create a new list and apply there
                if(!bagIdInstanceMap.containsKey(bagId))
                {
                    List<Instance> instanceList = new ArrayList<>();
                    instanceList.add(thisPropInstance);
                    bagIdInstanceMap.put(bagId, instanceList);
                } // if
                else
                {
                    bagIdInstanceMap.get(bagId).add(thisPropInstance);
                } // else

                //System.out.println(bagId + ", Ts: " + thisPropInstance.toString(thisPropInstance.attribute(1)) + " => " + thisPropInstance.toString());
            } // for


            //for each bagId, reweight the instances by their ts
            for(String bagId : bagIdInstanceMap.keySet())
            {
                //compute the sum of timestampMinutes
                List<Instance> instanceList = bagIdInstanceMap.get(bagId);
                double sumOfTsMinutes = 0;
                for(Instance instance : instanceList)
                    sumOfTsMinutes += Double.parseDouble(instance.toString(instance.attribute(1)));

                //now reweight
                for(Instance instance : instanceList)
                {
                    double tsMinutes = Double.parseDouble(instance.toString(instance.attribute(1)));
                    double weight = tsMinutes / sumOfTsMinutes;
                    instance.setWeight(weight); //TODO disabled +1 works in 3.7.0 and 3.7.2; weightedInstanceHandlers cannot handle weighted data interestingly in weka 3.9.4
                } // for

                //instanceList.forEach(System.out::println);
            } // for each bagId
        } // if

        //System.exit(0);


        //ts should be removed before bag_id attribute
        if(removeTs)
        {
            if (!newData.attribute(1).relation().attribute(0).name().contains("ts"))
            {
                System.out.println("There is no ts attribute, not removing ts");
            } // if
            else
                newData.deleteAttributeAt(1);
        }

        if(!keepPropFormat) // attribute 0 is a bag_id attribute (contains all bag_ids in {})
        {
            //remove bag_id attribute
            newData.deleteAttributeAt(0);
            newData = buildInstances(newData); // build instances one by one to be in mono-instance format
        }

        return newData;
    } // transformMiDataToProp



    //transform List<LabeledPoint> to Instances
    private static Instances buildInstances(Instances propData)
    {
        //create an empty arraylist first
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        // for one labeledPoint in a partition
        // do get feature vector and create attributes from them

        for (int i = 0; i < propData.numAttributes() - 1; i++)
        {
            // attribute name will be x1, x2, x3 etc...
            Attribute attribute = new Attribute(propData.attribute(i).name().replace("bag_", ""));
            attributes.add(attribute);
        } // for

        // Declare the class attribute along with its values
        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement("1");
        fvClassVal.addElement("0");
        Attribute label = new Attribute("class", fvClassVal);

        // Declare the feature vector, first add class label attribute
        FastVector fvWekaAttributes = new FastVector(4);
        //then for each attribute in an attributes add them to wekaAttributes
        for (Attribute attribute : attributes)
        {
            fvWekaAttributes.addElement(attribute);
        } // for
        fvWekaAttributes.addElement(label);


        // Create an empty training set
        Instances newData = new Instances("new_relation" //propData.relationName()
                , fvWekaAttributes, 10);
        // Set class index
        newData.setClassIndex(fvWekaAttributes.size() - 1);


        //for each labeledPoint in partition, create an instance
        //from that labeled point
        for (int i = 0; i < propData.numInstances(); i++)
        {
            Instance thisPropInstance = propData.instance(i);

            // Create the instance, number of attributes will be #features + label
            Instance instance = //new Instance(attributes.size() + 1); // weka 3.7.0
                                new DenseInstance(attributes.size() + 1);

            //class label of labeled point
            double lbl = thisPropInstance.classValue();

            //first set class label for the attribute
            instance.setValue((Attribute)fvWekaAttributes.elementAt(fvWekaAttributes.size() - 1), lbl);


            for (int index = 0; index < thisPropInstance.numAttributes() - 1; index++)
            {
                instance.setValue((Attribute) fvWekaAttributes
                        .elementAt(index), Double.parseDouble(thisPropInstance.toString(thisPropInstance.attribute(index))));

                instance.setWeight(thisPropInstance.weight());
            } // for

            // add the instance
            newData.add(instance);
        } // for

						 /*instance.setValue((Attribute)fvWekaAttributes.elementAt(0), 1.0);
						 instance.setValue((Attribute)fvWekaAttributes.elementAt(1), 0.5);
						 instance.setValue((Attribute)fvWekaAttributes.elementAt(2), "gray");
						 instance.setValue((Attribute)fvWekaAttributes.elementAt(3), "positive");*/

        return newData;
    } // buildInstances




    //transform List<LabeledPoint> to Instances
    private static Instances buildInstances(Instances propData, HashSet<Integer> instanceIndicesToDelete, HashSet<String> instanceToRemoveByBagID)
    {
        //create an empty arraylist first
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        // for one labeledPoint in a partition
        // do get feature vector and create attributes from them

//        List<String> attrValues = new ArrayList<>();
//        for(int index = 0; index < propData.attribute(0).numValues(); index++)
//        {
//            String bagId = propData.attribute(0).value(index);
//            //if(!instanceToRemoveByBagID.contains(bagId))
//                attrValues.add(bagId);
//        }

        //weka 3.7.0
        FastVector fAttrValues = new FastVector();
        for(int index = 0; index < propData.attribute(0).numValues(); index++)
        {
            String bagId = propData.attribute(0).value(index);
            //if(!instanceToRemoveByBagID.contains(bagId))
            fAttrValues.addElement(bagId);
        }


        Attribute attr = new Attribute(propData.attribute(0).name(), fAttrValues, propData.attribute(0).getMetadata());
        attributes.add(attr);
        for (int i = 1; i < propData.numAttributes() - 1; i++)
        {
            // attribute name will be x1, x2, x3 etc...
            Attribute attribute = new Attribute(propData.attribute(i).name()//.replace("bag_", "")
                                );
            attributes.add(attribute);
        } // for

        // Declare the class attribute along with its values
        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement("1");
        fvClassVal.addElement("0");
        Attribute label = new Attribute("class", fvClassVal);

        // Declare the feature vector, first add class label attribute
        FastVector fvWekaAttributes = new FastVector(4);
        //then for each attribute in an attributes add them to wekaAttributes
        for (Attribute attribute : attributes)
        {
            fvWekaAttributes.addElement(attribute);
        } // for
        fvWekaAttributes.addElement(label);


        // Create an empty training set
        Instances newData = new Instances(//"new_relation"
                                                propData.relationName()
                                , fvWekaAttributes, 10);
        // Set class index
        newData.setClassIndex(fvWekaAttributes.size() - 1);


        //for each labeledPoint in partition, create an instance
        //from that labeled point
        for (int i = 0; i < propData.numInstances(); i++)
        {
            Instance thisPropInstance = propData.instance(i);

            if(!(instanceIndicesToDelete == null || instanceIndicesToDelete.isEmpty()) && instanceIndicesToDelete.contains(i))
                continue; // do not add this intance


            String bagId = thisPropInstance.toString(thisPropInstance.attribute(0));
            if(!(instanceToRemoveByBagID == null || instanceToRemoveByBagID.isEmpty()) && instanceToRemoveByBagID.contains(bagId))
                continue; // do not add this instance

            // Create the instance, number of attributes will be #features + label
            Instance instance = //new Instance(attributes.size() + 1); // weka 3.7.0
                                new DenseInstance(attributes.size() + 1);

            //class label of labeled point
            double lbl = thisPropInstance.classValue();

            //first set class label for the attribute
            instance.setValue((Attribute)fvWekaAttributes.elementAt(fvWekaAttributes.size() - 1), lbl);

            instance.setValue((Attribute) fvWekaAttributes
                    .elementAt(0), thisPropInstance.toString(thisPropInstance.attribute(0)));

            for (int index = 1; index < thisPropInstance.numAttributes() - 1; index++)
            {
                instance.setValue((Attribute) fvWekaAttributes
                        .elementAt(index), Double.parseDouble(thisPropInstance.toString(thisPropInstance.attribute(index))));

                instance.setWeight(thisPropInstance.weight());
            } // for

            // add the instance
            newData.add(instance);
        } // for

						 /*instance.setValue((Attribute)fvWekaAttributes.elementAt(0), 1.0);
						 instance.setValue((Attribute)fvWekaAttributes.elementAt(1), 0.5);
						 instance.setValue((Attribute)fvWekaAttributes.elementAt(2), "gray");
						 instance.setValue((Attribute)fvWekaAttributes.elementAt(3), "positive");*/

        return newData;
    } // buildInstances



    //helper method to demo MIBoost bag weighting
    public static void demoMIBoostWeighting(Instances data)
    {
        for(int index = 0; index < data.numInstances(); index ++)
        {
            System.out.println(data.instance(index).attribute(0).value(index) + "'s Weight => " + data.instance(index).weight());
        } // for

        //Initialize the bags' weights
        double N = (double)data.numInstances(), sumNi=0;
        for(int i=0; i<N; i++)
            sumNi += data.instance(i).relationalValue(1).numInstances();
        for(int i=0; i<N; i++){
            data.instance(i).setWeight(sumNi/N);
        }

        System.out.println("================ AFTER REWEIGHTING =================");
        for(int index = 0; index < data.numInstances(); index ++)
        {
            System.out.println(data.instance(index).attribute(0).value(index) + "'s Weight => " + data.instance(index).weight());
        } // for
    } // demoMIBoostWeighting


    //helper method to find class imbalance in weka instances
    public static String classImbalanceOnWekaInstances(Instances data)
    {
        int numPositiveInstances = 0;
        int numNegativeInstances = 0;

        //for each instance check its class and update counters
        for(int index = 0; index < data.numInstances(); index ++)
        {
            Instance thisInstance = data.instance(index);
            if(Double.compare(thisInstance.value(data.classIndex()), 0.0) == 0)
                numPositiveInstances ++;
            else
                numNegativeInstances ++;
        } // for

        return "numPositives: " + numPositiveInstances + ", numNegatives: " + numNegativeInstances + ", Imbalance: "
                + format("#.##", RoundingMode.HALF_UP, 100 * numPositiveInstances / (data.numInstances() * 1.0f))
                + "% - " +  format("#.##", RoundingMode.HALF_UP,100 * numNegativeInstances / (data.numInstances() * 1.0f)) + "%";
    } // classImbalanceOnWekaInstances



    //helper method to undersample majority class to the size of minority class (50% - 50% imbalance)
    public static Instances underSample(Instances train) throws Exception
    {
        //Undersample majority class
        //Instead of using weka.filters.supervised.instance.Resample,
        // a much easier way to achieve the same effect is to use weka.filters.supervised.SpreadSubsample instead, with distributionSpread=1.0:
        String[] filterOptions = new String[2];
        filterOptions[0] = "-M";                                               // "distributionSpread"
        filterOptions[1] = "1.0";    //1.0 for 50%-50%, 1.5 for 40%-60%, 2.0 for 1/3, 2/3, 2.333 for 30%-70%
        SpreadSubsample underSampleFilter = new SpreadSubsample();              // a new instance of filter
        underSampleFilter.setOptions(filterOptions);                           // set options
        System.out.println(underSampleFilter.getClass().getName() + " filter options: " + Arrays.toString(underSampleFilter.getOptions()));
        underSampleFilter.setInputFormat(train);                                // inform the filter about the dataset **AFTER** setting options
        train = Filter.useFilter(train, underSampleFilter);         // apply filter
        System.out.println("After undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        return train;
    } // underSample


    //helper method to oversample minority class roughly to the size of majority class
    public static Instances overSample(Instances train) throws Exception
    {
        //Weka code
        //int sampleSize = (int)((m_SampleSizePercent / 100.0) * ((1 - m_BiasToUniformClass) * numInstancesPerClass[i] +
        //        m_BiasToUniformClass * data.numInstances() / numActualClasses));

        int numPositiveInstances = 0;
        int numNegativeInstances = 0;

        //for each instance check its class and update counters
        for(int index = 0; index < train.numInstances(); index ++)
        {
            Instance thisInstance = train.instance(index);
            String bagId = thisInstance.toString(thisInstance.attribute(0));
            //System.out.println(bagId);

            if(Double.compare(thisInstance.value(train.classIndex()), 0.0) == 0)
            {
                numPositiveInstances++;
            }
            else
            {
                numNegativeInstances++;
            }
        } // for


        int bigger = Math.max(numPositiveInstances, numNegativeInstances);

        double sampleSizePercent = (bigger / (train.numInstances() * 1.0)) * 100 * 2; // multiple by 100 for percent, 2 for Y/2

        //oversample
        train = reSample(sampleSizePercent, train);

        System.out.println("After oversampling Train => " + Utils.classImbalanceOnWekaInstances(train));
        return train;
    } // overSample


    //TODO dropping performance, might be a bug of Weka.
    //helper method to oversample the minority class to the size of majority class
    public static Instances overSampleByKeepingMajorityClassUntouched(Instances train) throws Exception
    {
        //Weka code
        //int sampleSize = (int)((m_SampleSizePercent / 100.0) * ((1 - m_BiasToUniformClass) * numInstancesPerClass[i] +
        //        m_BiasToUniformClass * data.numInstances() / numActualClasses));


        int numPositiveInstances = 0;
        int numNegativeInstances = 0;

        Instances positives = new Instances(train); positives.delete();
        Instances negatives = new Instances(train); negatives.delete();
        //HashSet<String> negativeBagIds = new HashSet<>();
        //HashSet<String> positiveBagIds = new HashSet<>();
        //List<Integer> negativeIndices = new ArrayList<>();
        //HashSet<Integer> positiveIndices = new HashSet<>();

        //for each instance check its class and update counters
        for(int index = 0; index < train.numInstances(); index ++)
        {
            Instance thisInstance = train.instance(index);
            String bagId = thisInstance.toString(thisInstance.attribute(0));
            //System.out.println(bagId);

            if(Double.compare(thisInstance.value(train.classIndex()), 0.0) == 0)
            {
                //negatives.remove(thisInstance);
                positives.add(thisInstance);
                //positiveBagIds.add(bagId);
                numPositiveInstances++;
                //positiveIndices.add(index);
            }
            else
            {
                //positives.remove(thisInstance);
                negatives.add(thisInstance);
                //negativeBagIds.add(bagId);
                //negativeIndices.add(index);
                numNegativeInstances++;
            }
        } // for

        //Error:
        //for(int index : positiveIndices)
        //    train.delete(index);

//        Iterator<Instance> itr = train.iterator();
//        int count = 0;
//        while (itr.hasNext())
//        {
//            if(positiveIndices.contains(count))
//                itr.remove();
//            count ++;
//        }
//
//        System.out.println(train.numInstances());
//        System.exit(0);



//        //first negatives then positives
//        train.sort(new Comparator<Instance>() {
//            @Override
//            public int compare(Instance t1, Instance t2)
//            {
//                return Double.compare(t1.value(train.classIndex()), t2.value(train.classIndex()));
//            }
//        });
//
//
//        Instances miPositives = new Instances(train, 0, numPositiveInstances);
//        //miPositives.forEach(instance -> System.out.println(instance.value(instance.classIndex())));
//        Instances miNegatives = new Instances(train, numPositiveInstances, numNegativeInstances);
//        System.out.println("NumPositives: " + miPositives.numInstances());
//        System.out.println("NumNegatives : " + miNegatives.numInstances());
////        //System.exit(0);


//        // handle the case, when negative is a minority class
//        Instances propPositives = toProp(positives);
//        propPositives = buildInstances(propPositives, null, negativeBagIds);
//        //System.out.println(propPositives);
//        Instances propNegatives = toProp(negatives);
//        propNegatives = buildInstances(propNegatives, null, positiveBagIds);
//        //System.out.println(propNegatives);
//
//        Instances miPositives = toMi(propPositives);
//        Instances miNegatives = toMi(propNegatives);
//        System.out.println("NumPositives: " + miPositives.numInstances());
//        System.out.println("NumNegatives : " + miNegatives.numInstances());
//        //System.exit(0);



        Instances miPositives = positives;
        Instances miNegatives = negatives;
        System.out.println("NumPositives: " + miPositives.numInstances());
        System.out.println("NumNegatives : " + miNegatives.numInstances());


        int bigger = Math.max(numPositiveInstances, numNegativeInstances);

        double sampleSizePercent = (bigger / (
                            //train.numInstances()
                            miPositives.numInstances()
                            * 1.0)) * 100
                            ; // for untouched majority class
                            //* 2; // multiple by 100 for percent, 2 for Y/2


        //System.out.println((sampleSizePercent / 100) * (1.0 * train.numInstances() / train.numClasses()));
        //System.exit(0);

        //oversample
        miPositives = reSample(sampleSizePercent, miPositives);


        System.out.println("After oversampling Train (before adding negatives) => " + Utils.classImbalanceOnWekaInstances(miPositives));


        //causing error, it might be because, miPositives are oversampled instances
//        for(int index = 0; index < miNegatives.numInstances(); index ++)
//            miPositives.add(miNegatives.instance(index));

        //works when positives are added on top of negatives
        for(int index = 0; index < miPositives.numInstances(); index ++)
            miNegatives.add(miPositives.instance(index));
        miPositives = miNegatives;
        System.out.println("After adding negatives => " + Utils.classImbalanceOnWekaInstances(miPositives));


//        System.out.println(new Instances(miPositives, 0, 10));
//        miPositives = toProp(miPositives);
//        miPositives = toMi(miPositives);


        //set the weight of each bag to 1
        for(int index = 0; index < miPositives.numInstances(); index++)
            miPositives.instance(index).setWeight(1);


        List<String> attrValues = new ArrayList<>();
        for(int index = 0; index < miPositives.attribute(0).numValues(); index++)
        {
            String bagId = miPositives.attribute(0).value(index);
            //if(!instanceToRemoveByBagID.contains(bagId))
            attrValues.add(bagId);
        }
        System.out.println("New number of bags: " + attrValues.size());
        System.out.println("Num instances: " + miPositives.numInstances());


        System.out.println("After oversampling Train => " + Utils.classImbalanceOnWekaInstances(miPositives));

        return miPositives;
    } // overSampleByKeepingMajorityClassUntouched


    //method to resample the given data
    private static Instances reSample(double sampleSizePercent, Instances data) throws Exception
    {
        //To oversample the minority class so that both classes have the same number of instances,
        // use the supervised Resample filter with noReplacement=false, biasToUniformClass=1.0,
        // and sampleSizePercent=Y, where Y/2 is (approximately) the percentage of data that belongs to the majority class.
        Resample filter = new Resample();
        filter.setNoReplacement(false);
        filter.setBiasToUniformClass(1);
        filter.setSampleSizePercent(sampleSizePercent);
        System.out.println("ReSample Filter options: " + Arrays.toString(filter.getOptions()));
        filter.setInputFormat(data);
        return Filter.useFilter(data, filter);        // apply filter
    } // reSample



    //helper method to convert mi data to prop
    private static Instances toProp(Instances miData) throws Exception
    {
        //apply mi to prop
        MultiInstanceToPropositional miToProp = new MultiInstanceToPropositional();
        //-A <num>
        //  The type of weight setting for each prop. instance:
        //0.weight = original single bag weight /Total number of
        //prop. instance in the corresponding bag;
        //1.weight = 1.0;
        //2.weight = 1.0/Total number of prop. instance in the
        //corresponding bag;
        //3. weight = Total number of prop. instance / (Total number
        //of bags * Total number of prop. instance in the
        //corresponding bag).
        //(default:0)
        miToProp.setOptions(weka.core.Utils.splitOptions("-A 1")); // keep the original weighting
        miToProp.setInputFormat(miData);
        Instances propData = Filter.useFilter(miData, miToProp);

        return propData;
    } // toProp


    private static Instances toMi(Instances propData) throws Exception
    {
        PropositionalToMultiInstance propToMi = new PropositionalToMultiInstance();
        //propToMi.setDoNotWeightBags(true);
        propToMi.setOptions(weka.core.Utils.splitOptions("-no-weights")); // weka 3.7.0
        propToMi.setInputFormat(propData);
        Instances miData = Filter.useFilter(propData, propToMi);
        return miData;
    } // toMi


    //helper method to oversample with smote
    //TODO SMOTE is not available for MI data giving null-pointer exception on distance calculation.
    public static Instances smote(Instances train) throws Exception
    {
        //Instances propTrain = toProp(train);

        SMOTE smote = new SMOTE();
        System.out.println("Filter options: " + Arrays.toString(smote.getOptions()));
        smote.setInputFormat(train);
        train = Filter.useFilter(train, smote);

        //Instances miTrain = toMi(propTrain);

        System.out.println("After oversampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        return train;
    } // smote
} // class Utils
