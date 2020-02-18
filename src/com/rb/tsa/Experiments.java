package com.rb.tsa;

import org.apache.commons.collections4.Bag;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.RealAdaBoost;
import weka.classifiers.mi.MIBoost;
import weka.classifiers.mi.MILR;
import weka.classifiers.mi.MIWrapper;
import weka.classifiers.mi.SimpleMI;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

import java.util.Arrays;
import java.util.List;

//list of experiments performed for each configuration
public class Experiments
{
    public static void run_MILR_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                     int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                     boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                            outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);

        //remove ts
        //setAData = Utils.removeTs(setAData);
        //setBData = Utils.removeTs(setBData);

        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);

        //------- MILR ----------------
        //NO FILTERING OCCURS INSIDE "MILR" CLASS; e.g. MultiInstanceToPropositional or PropositionalToMultiInstance
        MILR cls = new MILR(); //ridge parameter (-R) can be selected using hyperparameter selection
        //-A [0|1|2]
        //  Defines the type of algorithm (default 0):
        //   0. standard MI assumption
        //   1. collective MI assumption, arithmetic mean for posteriors
        //   2. collective MI assumption, geometric mean for posteriors
        cls.setOptions(weka.core.Utils.splitOptions("-A 2")); //geometric average, put -D for debugging output
        System.out.println(cls.getClass().getName() + " classifier options => " + Arrays.toString(cls.getOptions()) + "\n");
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_MILR_SS




    public static void run_BG_MILR_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                        int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                        boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);

        //remove ts
        //setAData = Utils.removeTs(setAData);
        //setBData = Utils.removeTs(setBData);

        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);

        //------- MILR ----------------
        //NO FILTERING OCCURS INSIDE "MILR" CLASS; e.g. MultiInstanceToPropositional or PropositionalToMultiInstance
        MILR milr = new MILR(); //ridge parameter (-R) can be selected using hyperparameter selection
        //-A [0|1|2]
        //  Defines the type of algorithm (default 0):
        //   0. standard MI assumption
        //   1. collective MI assumption, arithmetic mean for posteriors
        //   2. collective MI assumption, geometric mean for posteriors
        milr.setOptions(weka.core.Utils.splitOptions("-A 2")); //geometric average, put -D for debugging output
        Bagging cls = new Bagging(); //IMPROVES MILR PERFORMANCE with 10 iterations
        cls.setOptions(weka.core.Utils.splitOptions("-I 10 -num-slots 4")); // num execution slots in parallel
        cls.setClassifier(milr);
        System.out.println(cls.getClass().getName() + " classifier options => " + Arrays.toString(cls.getOptions()) + "\n" + cls.getClass());
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_BG_MILR_SS



    public static void run_W_MILR_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                       int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                       boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);


        //reweight the instances in each bag according to their timestamp minutes
        setAData = Utils.reweightInstancesOfEachBagByTs(setAData);
        setBData = Utils.reweightInstancesOfEachBagByTs(setBData);


        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);

        //------- MILR ----------------
        //NO FILTERING OCCURS INSIDE "MILR" CLASS; e.g. MultiInstanceToPropositional or PropositionalToMultiInstance
        MILR cls = new MILR(); //ridge parameter (-R) can be selected using hyperparameter selection
        //-A [0|1|2]
        //  Defines the type of algorithm (default 0):
        //   0. standard MI assumption
        //   1. collective MI assumption, arithmetic mean for posteriors
        //   2. collective MI assumption, geometric mean for posteriors
        cls.setOptions(weka.core.Utils.splitOptions("-A 2")); //geometric average, put -D for debugging output
        System.out.println(cls.getClass().getName() + " classifier options => " + Arrays.toString(cls.getOptions()) + "\n");
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_W_MILR_SS


    public static void run_W_BG_MILR_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                          int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                          boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);


        //reweight the instances in each bag according to their timestamp minutes
        setAData = Utils.reweightInstancesOfEachBagByTs(setAData);
        setBData = Utils.reweightInstancesOfEachBagByTs(setBData);


        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);

        //------- MILR ----------------
        //NO FILTERING OCCURS INSIDE "MILR" CLASS; e.g. MultiInstanceToPropositional or PropositionalToMultiInstance
        MILR milr = new MILR(); //ridge parameter (-R) can be selected using hyperparameter selection
        //-A [0|1|2]
        //  Defines the type of algorithm (default 0):
        //   0. standard MI assumption
        //   1. collective MI assumption, arithmetic mean for posteriors
        //   2. collective MI assumption, geometric mean for posteriors
        milr.setOptions(weka.core.Utils.splitOptions("-A 2")); //geometric average, put -D for debugging output
        Bagging cls = new Bagging(); //IMPROVES MILR PERFORMANCE with 10 iterations
        cls.setOptions(weka.core.Utils.splitOptions("-I 10 -num-slots 4")); // num execution slots in parallel
        cls.setClassifier(milr);
        System.out.println(cls.getClass().getName() + " classifier options => " + Arrays.toString(cls.getOptions()) + "\n");
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_W_BG_MILR_SS



    public static void run_MIW_RF_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                       int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                       boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);

        //remove ts
        //setAData = Utils.removeTs(setAData);
        //setBData = Utils.removeTs(setBData);

        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);

        //------- MIWrapper -----------
        MIWrapper cls = new MIWrapper();
        cls.setOptions(weka.core.Utils.splitOptions("-P 2 -A 0")); // P = 2 geometric average, A=0 original weight of instance inside bag
        RandomForest rf = new RandomForest();
        rf.setOptions(weka.core.Utils.splitOptions("-I 100")); // default I is 10 (10 trees) -num-slots 10
        cls.setClassifier(rf); // you can define other parameters for an internal classifier or perform CVParameterSelection for hyperparameter selection
        System.out.println(cls.getClass().getName() + " classifier options => " + Arrays.toString(cls.getOptions()) + "\n");
        //-P [1|2|3]
        //The method used in testing:
        //1.arithmetic average
        //2.geometric average
        //3.max probability of positive bag.
        //(default: 1)
        //
        //-A [0|1|2|3]
        //The type of weight setting for each single-instance:
        //0.keep the weight to be the same as the original value;
        //1.weight = 1.0
        //2.weight = 1.0/Total number of single-instance in the
        //corresponding bag
        //3. weight = Total number of single-instance / (Total
        //number of bags * Total number of single-instance
        //in the corresponding bag).
        //(default: 3)
        //
        //-D
        //If set, classifier is run in debug mode and
        //may output additional info to the console
        //
        //-W
        //Full name of base classifier.
        //(default: weka.classifiers.rules.ZeroR)
        //Options specific to classifier weka.classifiers.rules.ZeroR:
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_MIW_RF_SS


    public static void run_MIW_LR_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                       int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                       boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);

        //remove ts
        //setAData = Utils.removeTs(setAData);
        //setBData = Utils.removeTs(setBData);

        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);

        //------- MIWrapper -----------
        MIWrapper cls = new MIWrapper();
        cls.setOptions(weka.core.Utils.splitOptions("-P 2 -A 0")); // P = 2 geometric average, A=0 original weight of instance inside bag
        cls.setClassifier(new Logistic()); // you can define other parameters for an internal classifier or perform CVParameterSelection for hyperparameter selection
        System.out.println(cls.getClass().getName() + " classifier options => " + Arrays.toString(cls.getOptions()) + "\n");
        //-P [1|2|3]
        //The method used in testing:
        //1.arithmetic average
        //2.geometric average
        //3.max probability of positive bag.
        //(default: 1)
        //
        //-A [0|1|2|3]
        //The type of weight setting for each single-instance:
        //0.keep the weight to be the same as the original value;
        //1.weight = 1.0
        //2.weight = 1.0/Total number of single-instance in the
        //corresponding bag
        //3. weight = Total number of single-instance / (Total
        //number of bags * Total number of single-instance
        //in the corresponding bag).
        //(default: 3)
        //
        //-D
        //If set, classifier is run in debug mode and
        //may output additional info to the console
        //
        //-W
        //Full name of base classifier.
        //(default: weka.classifiers.rules.ZeroR)
        //Options specific to classifier weka.classifiers.rules.ZeroR:
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_MIW_LR_SS



    public static void run_W_MIW_RF_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                         int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                         boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);



        //reweight by ts and then remove ts
        setAData = Utils.reweightInstancesOfEachBagByTs(setAData);
        setBData = Utils.reweightInstancesOfEachBagByTs(setBData);



        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);

        //------- MIWrapper -----------
        MIWrapper cls = new MIWrapper();
        cls.setOptions(weka.core.Utils.splitOptions("-P 2 -A 0")); // P = 2 geometric average, A=0 original weight of instance inside bag
        RandomForest rf = new RandomForest();
        rf.setOptions(weka.core.Utils.splitOptions("-I 100")); // default I is 10 (10 trees) -num-slots 10
        cls.setClassifier(rf); // you can define other parameters for an internal classifier or perform CVParameterSelection for hyperparameter selection
        System.out.println(cls.getClass().getName() + " classifier options => " + Arrays.toString(cls.getOptions()) + "\n");
        //-P [1|2|3]
        //The method used in testing:
        //1.arithmetic average
        //2.geometric average
        //3.max probability of positive bag.
        //(default: 1)
        //
        //-A [0|1|2|3]
        //The type of weight setting for each single-instance:
        //0.keep the weight to be the same as the original value;
        //1.weight = 1.0
        //2.weight = 1.0/Total number of single-instance in the
        //corresponding bag
        //3. weight = Total number of single-instance / (Total
        //number of bags * Total number of single-instance
        //in the corresponding bag).
        //(default: 3)
        //
        //-D
        //If set, classifier is run in debug mode and
        //may output additional info to the console
        //
        //-W
        //Full name of base classifier.
        //(default: weka.classifiers.rules.ZeroR)
        //Options specific to classifier weka.classifiers.rules.ZeroR:
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_W_MIW_RF_SS



    public static void run_W_MIW_LR_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                         int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                         boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);


        //reweight by ts and then remove ts
        setAData = Utils.reweightInstancesOfEachBagByTs(setAData);
        setBData = Utils.reweightInstancesOfEachBagByTs(setBData);


        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);

        //------- MIWrapper -----------
        MIWrapper cls = new MIWrapper();
        cls.setOptions(weka.core.Utils.splitOptions("-P 2 -A 0")); // P = 2 geometric average, A=0 original weight of instance inside bag
        cls.setClassifier(new Logistic()); // you can define other parameters for an internal classifier or perform CVParameterSelection for hyperparameter selection
        System.out.println(cls.getClass().getName() + " classifier options => " + Arrays.toString(cls.getOptions()) + "\n");
        //-P [1|2|3]
        //The method used in testing:
        //1.arithmetic average
        //2.geometric average
        //3.max probability of positive bag.
        //(default: 1)
        //
        //-A [0|1|2|3]
        //The type of weight setting for each single-instance:
        //0.keep the weight to be the same as the original value;
        //1.weight = 1.0
        //2.weight = 1.0/Total number of single-instance in the
        //corresponding bag
        //3. weight = Total number of single-instance / (Total
        //number of bags * Total number of single-instance
        //in the corresponding bag).
        //(default: 3)
        //
        //-D
        //If set, classifier is run in debug mode and
        //may output additional info to the console
        //
        //-W
        //Full name of base classifier.
        //(default: weka.classifiers.rules.ZeroR)
        //Options specific to classifier weka.classifiers.rules.ZeroR:
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_W_MIW_LR_SS



    public static void run_RB_MIW_RF_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                          int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                          boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);

        //remove ts
        //setAData = Utils.removeTs(setAData);
        //setBData = Utils.removeTs(setBData);

        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);



        //------- MIWrapper -----------
        MIWrapper mil = new MIWrapper();
        mil.setOptions(weka.core.Utils.splitOptions("-P 2 -A 0")); // P = 2 geometric average, A = 0 original weight of instance inside bag
        RandomForest rf = new RandomForest();
        rf.setOptions(weka.core.Utils.splitOptions("-I 100")); // default I is 10 (10 trees) -num-slots 10
        mil.setClassifier(rf); // you can define other parameters for an internal classifier or perform CVParameterSelection for hyperparameter selection


        RealAdaBoost cls = new RealAdaBoost(); //improves MIW-RF and MIW-LR performance
        cls.setOptions(weka.core.Utils.splitOptions("-I 10")); // number of iterations 10
        cls.setClassifier(mil);


        System.out.println(cls.getClass().getName() + " classifier options => " + Arrays.toString(cls.getOptions()) + "\n");
        //-P [1|2|3]
        //The method used in testing:
        //1.arithmetic average
        //2.geometric average
        //3.max probability of positive bag.
        //(default: 1)
        //
        //-A [0|1|2|3]
        //The type of weight setting for each single-instance:
        //0.keep the weight to be the same as the original value;
        //1.weight = 1.0
        //2.weight = 1.0/Total number of single-instance in the
        //corresponding bag
        //3. weight = Total number of single-instance / (Total
        //number of bags * Total number of single-instance
        //in the corresponding bag).
        //(default: 3)
        //
        //-D
        //If set, classifier is run in debug mode and
        //may output additional info to the console
        //
        //-W
        //Full name of base classifier.
        //(default: weka.classifiers.rules.ZeroR)
        //Options specific to classifier weka.classifiers.rules.ZeroR:
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_RB_MIW_RF_SS


    public static void run_RB_MIW_LR_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                          int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                          boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);

        //remove ts
        //setAData = Utils.removeTs(setAData);
        //setBData = Utils.removeTs(setBData);

        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);

        //------- MIWrapper -----------
        MIWrapper mil = new MIWrapper();
        mil.setOptions(weka.core.Utils.splitOptions("-P 2 -A 0")); // P = 2 geometric average, A=0 original weight of instance inside bag
        mil.setClassifier(new Logistic()); // you can define other parameters for an internal classifier or perform CVParameterSelection for hyperparameter selection


        RealAdaBoost cls = new RealAdaBoost(); //improves MIW-RF and MIW-LR performance
        cls.setOptions(weka.core.Utils.splitOptions("-I 10"));
        cls.setClassifier(mil);


        System.out.println(cls.getClass().getName() + " classifier options => " + Arrays.toString(cls.getOptions()) + "\n");
        //-P [1|2|3]
        //The method used in testing:
        //1.arithmetic average
        //2.geometric average
        //3.max probability of positive bag.
        //(default: 1)
        //
        //-A [0|1|2|3]
        //The type of weight setting for each single-instance:
        //0.keep the weight to be the same as the original value;
        //1.weight = 1.0
        //2.weight = 1.0/Total number of single-instance in the
        //corresponding bag
        //3. weight = Total number of single-instance / (Total
        //number of bags * Total number of single-instance
        //in the corresponding bag).
        //(default: 3)
        //
        //-D
        //If set, classifier is run in debug mode and
        //may output additional info to the console
        //
        //-W
        //Full name of base classifier.
        //(default: weka.classifiers.rules.ZeroR)
        //Options specific to classifier weka.classifiers.rules.ZeroR:
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_RB_MIW_LR_SS


    public static void run_W_RB_MIW_RF_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                            int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                            boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);


        //reweight by temporal order
        setAData = Utils.reweightInstancesOfEachBagByTs(setAData);
        setBData = Utils.reweightInstancesOfEachBagByTs(setBData);


        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);



        //------- MIWrapper -----------
        MIWrapper mil = new MIWrapper();
        mil.setOptions(weka.core.Utils.splitOptions("-P 2 -A 0")); // P = 2 geometric average, A = 0 original weight of instance inside bag
        RandomForest rf = new RandomForest();
        rf.setOptions(weka.core.Utils.splitOptions("-I 100")); // default I is 10 (10 trees) -num-slots 10
        mil.setClassifier(rf); // you can define other parameters for an internal classifier or perform CVParameterSelection for hyperparameter selection


        RealAdaBoost cls = new RealAdaBoost(); //improves MIW-RF and MIW-LR performance
        cls.setOptions(weka.core.Utils.splitOptions("-I 10")); // number of iterations 10
        cls.setClassifier(mil);


        System.out.println(cls.getClass().getName() + " classifier options => " + Arrays.toString(cls.getOptions()) + "\n");
        //-P [1|2|3]
        //The method used in testing:
        //1.arithmetic average
        //2.geometric average
        //3.max probability of positive bag.
        //(default: 1)
        //
        //-A [0|1|2|3]
        //The type of weight setting for each single-instance:
        //0.keep the weight to be the same as the original value;
        //1.weight = 1.0
        //2.weight = 1.0/Total number of single-instance in the
        //corresponding bag
        //3. weight = Total number of single-instance / (Total
        //number of bags * Total number of single-instance
        //in the corresponding bag).
        //(default: 3)
        //
        //-D
        //If set, classifier is run in debug mode and
        //may output additional info to the console
        //
        //-W
        //Full name of base classifier.
        //(default: weka.classifiers.rules.ZeroR)
        //Options specific to classifier weka.classifiers.rules.ZeroR:
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_W_RB_MIW_RF_SS


    public static void run_W_RB_MIW_LR_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                            int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                            boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);


        //reweight and then remove ts
        setAData = Utils.reweightInstancesOfEachBagByTs(setAData);
        setBData = Utils.reweightInstancesOfEachBagByTs(setBData);


        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);

        //------- MIWrapper -----------
        MIWrapper mil = new MIWrapper();
        mil.setOptions(weka.core.Utils.splitOptions("-P 2 -A 0")); // P = 2 geometric average, A=0 original weight of instance inside bag
        mil.setClassifier(new Logistic()); // you can define other parameters for an internal classifier or perform CVParameterSelection for hyperparameter selection


        RealAdaBoost cls = new RealAdaBoost(); //improves MIW-RF and MIW-LR performance
        cls.setOptions(weka.core.Utils.splitOptions("-I 10"));
        cls.setClassifier(mil);


        System.out.println(cls.getClass().getName() + " classifier options => " + Arrays.toString(cls.getOptions()) + "\n");
        //-P [1|2|3]
        //The method used in testing:
        //1.arithmetic average
        //2.geometric average
        //3.max probability of positive bag.
        //(default: 1)
        //
        //-A [0|1|2|3]
        //The type of weight setting for each single-instance:
        //0.keep the weight to be the same as the original value;
        //1.weight = 1.0
        //2.weight = 1.0/Total number of single-instance in the
        //corresponding bag
        //3. weight = Total number of single-instance / (Total
        //number of bags * Total number of single-instance
        //in the corresponding bag).
        //(default: 3)
        //
        //-D
        //If set, classifier is run in debug mode and
        //may output additional info to the console
        //
        //-W
        //Full name of base classifier.
        //(default: weka.classifiers.rules.ZeroR)
        //Options specific to classifier weka.classifiers.rules.ZeroR:
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_W_RB_MIW_LR_SS



    public static void run_MIB_RF_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                       int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                       boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);

        //remove ts
        //setAData = Utils.removeTs(setAData);
        //setBData = Utils.removeTs(setBData);

        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);

        //--------- MIBoost -----------
        //MultiInstanceToPropositional to propositional filter is present inside "MIBoost" class, weight bags, but all bags have the same weights
        MIBoost cls = new MIBoost();
        //-B <num>
        //        The number of bins in discretization
        //    (default 0, no discretization)
        //
        //-R <num>
        //        Maximum number of boost iterations.
        //(default 10)
        //
        //-W <class name> (can also be done by setClassifier method)
        //Full name of classifier to boost.
        //eg: weka.classifiers.bayes.NaiveBayes
        cls.setOptions(weka.core.Utils.splitOptions("-R 10"));
        //OPTIONS FOR RANDOM FOREST:
        //-I <num>
        //  Number of iterations (i.e., the number of trees in the random forest).
        //  (default value 10) //tested 100
        //
        // -K <number of attributes>
        //  Number of attributes to randomly investigate. (default 0)
        //  (<1 = int(log_2(#predictors)+1)). //not possible, 0 is only possible value
        //
        // -S <num>
        //  Seed for random number generator.
        //  (default 1)
        //-N <num>
        // Number of folds for backfitting (default 0, no backfitting).
        //-B
        // Break ties randomly when several attributes look equally good.
        //-batch-size
        //  The desired batch size for batch prediction  (default 100).
        RandomForest rf = new RandomForest();
        rf.setOptions(weka.core.Utils.splitOptions("-I 100")); // default I is 10 (10 trees)
        cls.setClassifier(rf);
        System.out.println(cls.getClass().getName() + " classifier options" + Arrays.toString(cls.getOptions()) + "\n");
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_MIB_RF_SS


    public static void run_MIB_LR_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                       int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                       boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);

        //remove ts
        //setAData = Utils.removeTs(setAData);
        //setBData = Utils.removeTs(setBData);

        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);

        //--------- MIBoost -----------
        //MultiInstanceToPropositional to propositional filter is present inside "MIBoost" class, weight bags, but all bags have the same weights
        MIBoost cls = new MIBoost();
        //-B <num>
        //        The number of bins in discretization
        //    (default 0, no discretization)
        //
        //-R <num>
        //        Maximum number of boost iterations.
        //(default 10)
        //
        //-W <class name> (can also be done by setClassifier method)
        //Full name of classifier to boost.
        //eg: weka.classifiers.bayes.NaiveBayes
        cls.setOptions(weka.core.Utils.splitOptions("-R 10"));
        cls.setClassifier(new Logistic());
        System.out.println(cls.getClass().getName() + " classifier options" + Arrays.toString(cls.getOptions()) + "\n");
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_MIB_LR_SS


    public static void run_W_MIB_RF_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                         int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                         boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);



        //reweight by ts and then remove ts
        setAData = Utils.reweightInstancesOfEachBagByTs(setAData);
        setBData = Utils.reweightInstancesOfEachBagByTs(setBData);



        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);

        //--------- MIBoost -----------
        //MultiInstanceToPropositional to propositional filter is present inside "MIBoost" class, weight bags, but all bags have the same weights
        MIBoost cls = new MIBoost();
        //-B <num>
        //        The number of bins in discretization
        //    (default 0, no discretization)
        //
        //-R <num>
        //        Maximum number of boost iterations.
        //(default 10)
        //
        //-W <class name> (can also be done by setClassifier method)
        //Full name of classifier to boost.
        //eg: weka.classifiers.bayes.NaiveBayes
        cls.setOptions(weka.core.Utils.splitOptions("-R 10"));
        //OPTIONS FOR RANDOM FOREST:
        //-I <num>
        //  Number of iterations (i.e., the number of trees in the random forest).
        //  (default value 10) //tested 100
        //
        // -K <number of attributes>
        //  Number of attributes to randomly investigate. (default 0)
        //  (<1 = int(log_2(#predictors)+1)). //not possible, 0 is only possible value
        //
        // -S <num>
        //  Seed for random number generator.
        //  (default 1)
        //-N <num>
        // Number of folds for backfitting (default 0, no backfitting).
        //-B
        // Break ties randomly when several attributes look equally good.
        //-batch-size
        //  The desired batch size for batch prediction  (default 100).
        RandomForest rf = new RandomForest();
        rf.setOptions(weka.core.Utils.splitOptions("-I 100")); // default I is 10 (10 trees)
        cls.setClassifier(rf);
        System.out.println(cls.getClass().getName() + " classifier options" + Arrays.toString(cls.getOptions()) + "\n");
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_W_MIB_RF_SS



    public static void run_W_MIB_LR_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                         int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                         boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);


        //reweight by ts and then remove ts
        setAData = Utils.reweightInstancesOfEachBagByTs(setAData);
        setBData = Utils.reweightInstancesOfEachBagByTs(setBData);


        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);

        //--------- MIBoost -----------
        //MultiInstanceToPropositional to propositional filter is present inside "MIBoost" class, weight bags, but all bags have the same weights
        MIBoost cls = new MIBoost();
        //-B <num>
        //        The number of bins in discretization
        //    (default 0, no discretization)
        //
        //-R <num>
        //        Maximum number of boost iterations.
        //(default 10)
        //
        //-W <class name> (can also be done by setClassifier method)
        //Full name of classifier to boost.
        //eg: weka.classifiers.bayes.NaiveBayes
        cls.setOptions(weka.core.Utils.splitOptions("-R 10"));
        cls.setClassifier(new Logistic());
        System.out.println(cls.getClass().getName() + " classifier options" + Arrays.toString(cls.getOptions()) + "\n");
        //-----------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_W_MIB_LR_SS



    //-------- SMI --------
    public static void run_SMI_RF_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                   int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                   boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);

        //remove ts
        //setAData = Utils.removeTs(setAData);
        //setBData = Utils.removeTs(setBData);

        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);


        //-----------------------------------------
        SimpleMI cls = new SimpleMI();
        cls.setOptions(weka.core.Utils.splitOptions("-M 1")); // the best performing is arithmetic average
        //Instances newTrain = cls.transform(train);
        //for (int i = 0; i < newTrain.numInstances(); i++)
        //{
        //    println(newTrain.instance(i));
        //} // for
        //System.exit(0);

        //-M [1|2|3]
        //The method used in transformation:
        //1.arithmetic average; 2.geometric center;
        //3.using minimax combined features of a bag (default: 1)
        //
        //Method 3:
        //Define s to be the vector of the coordinate-wise maxima
        //and minima of X, ie.,
        //s(X)=(minx1, ..., minxm, maxx1, ...,maxxm), transform
        //the exemplars into mono-instance which contains attributes s(X)
        RandomForest rf = new RandomForest();
        rf.setOptions(weka.core.Utils.splitOptions("-I 100")); // default I is 10 (10 trees)
        cls.setClassifier(rf);
        System.out.println(Arrays.toString(cls.getOptions()) + "\n" + cls.getClass());
        //-----------------------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_SMI_RF_SS


    public static void run_SMI_LR_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                     int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                     boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);

        //remove ts
        //setAData = Utils.removeTs(setAData);
        //setBData = Utils.removeTs(setBData);

        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);


        //-----------------------------------------
        SimpleMI cls = new SimpleMI();
        cls.setOptions(weka.core.Utils.splitOptions("-M 1")); // the best performing is arithmetic average
        //Instances newTrain = cls.transform(train);
        //for (int i = 0; i < newTrain.numInstances(); i++)
        //{
        //    println(newTrain.instance(i));
        //} // for
        //System.exit(0);

        //-M [1|2|3]
        //The method used in transformation:
        //1.arithmetic average; 2.geometric center;
        //3.using minimax combined features of a bag (default: 1)
        //
        //Method 3:
        //Define s to be the vector of the coordinate-wise maxima
        //and minima of X, ie.,
        //s(X)=(minx1, ..., minxm, maxx1, ...,maxxm), transform
        //the exemplars into mono-instance which contains attributes s(X)
        cls.setClassifier(new Logistic());
        System.out.println(Arrays.toString(cls.getOptions()) + "\n" + cls.getClass());
        //-----------------------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_SMI_LR_SS


    public static void run_W_SMI_RF_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                     int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                     boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);


        //reweight and remove ts
        setAData = Utils.reweightInstancesOfEachBagByTs(setAData);
        setBData = Utils.reweightInstancesOfEachBagByTs(setBData);


        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);


        //-----------------------------------------
        SimpleMI cls = new SimpleMI();
        cls.setOptions(weka.core.Utils.splitOptions("-M 1")); // the best performing is arithmetic average
        //Instances newTrain = cls.transform(train);
        //for (int i = 0; i < newTrain.numInstances(); i++)
        //{
        //    println(newTrain.instance(i));
        //} // for
        //System.exit(0);

        //-M [1|2|3]
        //The method used in transformation:
        //1.arithmetic average; 2.geometric center;
        //3.using minimax combined features of a bag (default: 1)
        //
        //Method 3:
        //Define s to be the vector of the coordinate-wise maxima
        //and minima of X, ie.,
        //s(X)=(minx1, ..., minxm, maxx1, ...,maxxm), transform
        //the exemplars into mono-instance which contains attributes s(X)
        RandomForest rf = new RandomForest();
        rf.setOptions(weka.core.Utils.splitOptions("-I 100")); // default I is 10 (10 trees)
        cls.setClassifier(rf);
        System.out.println(Arrays.toString(cls.getOptions()) + "\n" + cls.getClass());
        //-----------------------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_W_SMI_RF_SS


    public static void run_W_SMI_LR_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                     int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                     boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);

        //reweight and remove ts
        setAData = Utils.reweightInstancesOfEachBagByTs(setAData);
        setBData = Utils.reweightInstancesOfEachBagByTs(setBData);

        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);


        //-----------------------------------------
        SimpleMI cls = new SimpleMI();
        cls.setOptions(weka.core.Utils.splitOptions("-M 1")); // the best performing is arithmetic average
        //Instances newTrain = cls.transform(train);
        //for (int i = 0; i < newTrain.numInstances(); i++)
        //{
        //    println(newTrain.instance(i));
        //} // for
        //System.exit(0);

        //-M [1|2|3]
        //The method used in transformation:
        //1.arithmetic average; 2.geometric center;
        //3.using minimax combined features of a bag (default: 1)
        //
        //Method 3:
        //Define s to be the vector of the coordinate-wise maxima
        //and minima of X, ie.,
        //s(X)=(minx1, ..., minxm, maxx1, ...,maxxm), transform
        //the exemplars into mono-instance which contains attributes s(X)
        cls.setClassifier(new Logistic());
        System.out.println(Arrays.toString(cls.getOptions()) + "\n" + cls.getClass());
        //-----------------------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_W_SMI_LR_SS


    //RB-SMI
    public static void run_RB_SMI_RF_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                     int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                     boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);

        //remove ts
        //setAData = Utils.removeTs(setAData);
        //setBData = Utils.removeTs(setBData);

        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);


        //-----------------------------------------
        SimpleMI mil = new SimpleMI();
        mil.setOptions(weka.core.Utils.splitOptions("-M 1")); // the best performing is arithmetic average
        //Instances newTrain = mil.transform(train);
        //for (int i = 0; i < newTrain.numInstances(); i++)
        //{
        //    println(newTrain.instance(i));
        //} // for
        //System.exit(0);

        //-M [1|2|3]
        //The method used in transformation:
        //1.arithmetic average; 2.geometric center;
        //3.using minimax combined features of a bag (default: 1)
        //
        //Method 3:
        //Define s to be the vector of the coordinate-wise maxima
        //and minima of X, ie.,
        //s(X)=(minx1, ..., minxm, maxx1, ...,maxxm), transform
        //the exemplars into mono-instance which contains attributes s(X)
        RandomForest rf = new RandomForest();
        rf.setOptions(weka.core.Utils.splitOptions("-I 100")); // default I is 10 (10 trees)
        mil.setClassifier(rf);

        RealAdaBoost cls = new RealAdaBoost(); // drops SMI-LR performance, //improves SMI-RF performance
        cls.setOptions(weka.core.Utils.splitOptions("-I 10"));
        cls.setClassifier(mil);

        System.out.println(Arrays.toString(cls.getOptions()) + "\n" + cls.getClass());
        //-----------------------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_RB_SMI_RF_SS

    public static void run_BG_SMI_LR_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                        int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                        boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, false, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);

        //remove ts
        //setAData = Utils.removeTs(setAData);
        //setBData = Utils.removeTs(setBData);

        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);


        //-----------------------------------------
        SimpleMI mil = new SimpleMI();
        mil.setOptions(weka.core.Utils.splitOptions("-M 1")); // the best performing is arithmetic average
        //Instances newTrain = mil.transform(train);
        //for (int i = 0; i < newTrain.numInstances(); i++)
        //{
        //    println(newTrain.instance(i));
        //} // for
        //System.exit(0);

        //-M [1|2|3]
        //The method used in transformation:
        //1.arithmetic average; 2.geometric center;
        //3.using minimax combined features of a bag (default: 1)
        //
        //Method 3:
        //Define s to be the vector of the coordinate-wise maxima
        //and minima of X, ie.,
        //s(X)=(minx1, ..., minxm, maxx1, ...,maxxm), transform
        //the exemplars into mono-instance which contains attributes s(X)
        mil.setClassifier(new Logistic());

        Bagging cls = new Bagging(); // drops SMI-LR performance, //improves SMI-RF performance
        cls.setOptions(weka.core.Utils.splitOptions("-I 10")); // define -num-slots #cores for parallel execution
        cls.setClassifier(mil);

        System.out.println(Arrays.toString(cls.getOptions()) + "\n" + cls.getClass());
        //-----------------------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_BG_SMI_LR_SS


    public static void run_W_RB_SMI_RF_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                        int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                        boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);



        //reweight and remove ts
        setAData = Utils.reweightInstancesOfEachBagByTs(setAData);
        setBData = Utils.reweightInstancesOfEachBagByTs(setBData);



        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);


        //-----------------------------------------
        SimpleMI mil = new SimpleMI();
        mil.setOptions(weka.core.Utils.splitOptions("-M 1")); // the best performing is arithmetic average
        //Instances newTrain = mil.transform(train);
        //for (int i = 0; i < newTrain.numInstances(); i++)
        //{
        //    println(newTrain.instance(i));
        //} // for
        //System.exit(0);

        //-M [1|2|3]
        //The method used in transformation:
        //1.arithmetic average; 2.geometric center;
        //3.using minimax combined features of a bag (default: 1)
        //
        //Method 3:
        //Define s to be the vector of the coordinate-wise maxima
        //and minima of X, ie.,
        //s(X)=(minx1, ..., minxm, maxx1, ...,maxxm), transform
        //the exemplars into mono-instance which contains attributes s(X)
        RandomForest rf = new RandomForest();
        rf.setOptions(weka.core.Utils.splitOptions("-I 100")); // default I is 10 (10 trees)
        mil.setClassifier(rf);

        RealAdaBoost cls = new RealAdaBoost(); // drops SMI-LR performance, //improves SMI-RF performance
        cls.setOptions(weka.core.Utils.splitOptions("-I 10"));
        cls.setClassifier(mil);

        System.out.println(Arrays.toString(cls.getOptions()) + "\n" + cls.getClass());
        //-----------------------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_W_RB_SMI_RF_SS


    public static void run_W_BG_SMI_LR_SS(Dataset dataset, List<MTSE> mtses, Outcomes outcomes, VarRanges varRanges, int setASize,
                                        int setBSize, Integer setCSize, float fMissingValuePlaceHolder, Imputations.ImputeMethod method,
                                        boolean crossValidateOnSetAInstead) throws Exception
    {
        //forward impute the multivariate time series
        mtses = Imputations.getInstance()
                .impute(mtses, method, new int[]{0, setASize}, varRanges, fMissingValuePlaceHolder);

        //System.out.println(mtses.get(0).toVerticalString());
        //System.exit(0);

        Instances setAData = Utils.mtsesToMIData(dataset, mtses.subList(0, setASize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-a");

        Instances setBData = Utils.mtsesToMIData(dataset, mtses.subList(setASize, setASize + setBSize),
                outcomes, "patient_record_id", new String[]{"1", "0"}, true, "set-b");

        //clear mtses
        mtses.clear();
        mtses = null;


        if (setAData.classIndex() == -1)
            setAData.setClassIndex(setAData.numAttributes() - 1);
        if (setBData.classIndex() == -1)
            setBData.setClassIndex(setBData.numAttributes() - 1);



        //reweight and remove ts
        setAData = Utils.reweightInstancesOfEachBagByTs(setAData);
        setBData = Utils.reweightInstancesOfEachBagByTs(setBData);


        Instances train = new Instances(setAData);
        System.out.println("Before undersampling Train => " + Utils.classImbalanceOnWekaInstances(train));

        //undersample
        train = Utils.underSample(train);

        //print weights of instances of the first bag
        System.out.println();
        Utils.printInstanceWeightsOfABag(train.instance(0));
        System.out.println("\nBag weights of the first 10 bags: ");
        //print the weights of first 10 bags
        Utils.printBagWeights(train, 0, 10);


        //copy test data
        Instances test = new Instances(setBData);


        //-----------------------------------------
        SimpleMI mil = new SimpleMI();
        mil.setOptions(weka.core.Utils.splitOptions("-M 1")); // the best performing is arithmetic average
        //Instances newTrain = mil.transform(train);
        //for (int i = 0; i < newTrain.numInstances(); i++)
        //{
        //    println(newTrain.instance(i));
        //} // for
        //System.exit(0);

        //-M [1|2|3]
        //The method used in transformation:
        //1.arithmetic average; 2.geometric center;
        //3.using minimax combined features of a bag (default: 1)
        //
        //Method 3:
        //Define s to be the vector of the coordinate-wise maxima
        //and minima of X, ie.,
        //s(X)=(minx1, ..., minxm, maxx1, ...,maxxm), transform
        //the exemplars into mono-instance which contains attributes s(X)
        mil.setClassifier(new Logistic());

        Bagging cls = new Bagging(); // drops SMI-LR performance, //improves SMI-RF performance
        cls.setOptions(weka.core.Utils.splitOptions("-I 10")); // define -num-slots #cores for parallel execution
        cls.setClassifier(mil);

        System.out.println(Arrays.toString(cls.getOptions()) + "\n" + cls.getClass());
        //-----------------------------------------


        if(crossValidateOnSetAInstead)
        {
            //cross validate on set-a over 10-fold
            Utils.crossValidate(cls, train, null, 1, 10, false);
        } // if
        else
        {
            //average the results over 10 runs
            Utils.simpleTrainTestOverMultipleRuns(cls, train, 10, test, false);
        } // else
    } // run_W_BG_SMI_LR_SS


} // Experiments
