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

    //enum for var range values
    static class Ranges
    {
        public String OUTLIER_LOW;
        public String VALID_LOW;
        public String IMPUTE;
        public String VALID_HIGH;
        public String OUTLIER_HIGH;

        public Ranges(String OUTLIER_LOW, String VALID_LOW, String IMPUTE, String VALID_HIGH, String OUTLIER_HIGH)
        {
            this.OUTLIER_LOW = OUTLIER_LOW;
            this.VALID_LOW = VALID_LOW;
            this.IMPUTE = IMPUTE;
            this.VALID_HIGH = VALID_HIGH;
            this.OUTLIER_HIGH = OUTLIER_HIGH;
        } // Ranges

        public String toString()
        {
            return OUTLIER_LOW + "," + VALID_LOW + "," + IMPUTE + "," + VALID_HIGH + "," + OUTLIER_HIGH;
        } // toString
    } // class Ranges


    //helper method to return var and its ranges
    public static LinkedHashMap<String, Ranges> varRanges(String varRangesFilePath)
    {
        //read the variable ranges file and extract the ranges
        String varRangesFileContents = Utils.fileContentsFromLocalFilePath(varRangesFilePath);
        String[] varRangesLines = StringUtils.split(varRangesFileContents, "\r\n|\r|\n");
        //first line is the header line, extract the range variables

        //map to hold var name and its ranges
        LinkedHashMap<String, Ranges> varAndRangesMap = new LinkedHashMap<>();

        //now for each other line, extract the ranges of each variable, each line contains one variable, except first header line
        for(int index = 1; index < varRangesLines.length; index ++)
        {
            String thisLine = varRangesLines[index];

            //thisLineComponents will have the same length as of ranges
            String[] thisLineComponents = thisLine.split(",");
            for(int idx = 0; idx < thisLineComponents.length; idx ++) thisLineComponents[idx] = thisLineComponents[idx].trim(); // trim every element

            //var name is at index 0 of thisLineComponents
            String thisVar = thisLineComponents[0];
            //OUTLIER_LOW, VALID_LOW, IMPUTE, VALID_HIGH, OUTLIER_HIGH
            Ranges rangesForThisVar
                    = new Ranges(thisLineComponents[1], thisLineComponents[2], thisLineComponents[3], thisLineComponents[4], thisLineComponents[5]);

            //update the map
            varAndRangesMap.put(thisVar, rangesForThisVar);
        } // for

        return varAndRangesMap;
    } // varRanges


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

} // class Utils
