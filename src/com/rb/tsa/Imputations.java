package com.rb.tsa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//helper class to handle different kinds of value imputations
public class Imputations
{
    private static Imputations singleton = null;

    public enum ImputeMethod
    {
        ZERO_IMPUTATION,
        VALID_LOW_IMPUTATION,
        NORMAL_VALUE_IMPUTATION,
        MEAN_VALUE_WITH_MASKING_VECTOR_IMPUTATION,
        LIPTON_FORWARD_FILLING_IMPUTATION
    } // enum

    private Imputations() {}
    public static Imputations getInstance()
    {
        if(singleton == null) singleton = new Imputations();
        return singleton;
    } // getInstance

    //helper method to impute the list of multivariate time series
    public List<MTSE> impute(List<MTSE> mtseList, ImputeMethod imputeMethod, VarRanges varRanges, float currentMissingValuePlaceHolder)
    {
        //obtain deep copy of the given mtse instances
        List<MTSE> dcList = new ArrayList<>();
        for(MTSE mtse : mtseList)
            dcList.add(mtse.deepCopy());


        switch(imputeMethod)
        {
            case ZERO_IMPUTATION:
                for(MTSE mtse : dcList)
                {
                    Map<String, List<Float>> varValuesInTsOrder = mtse.getVarValuesInTsOrder();
                    //for each var impute its values
                    for(String var : varValuesInTsOrder.keySet())
                    {
                        List<Float> valuesInTsOrder = varValuesInTsOrder.get(var);
                        for(int tsIndex = 0; tsIndex < valuesInTsOrder.size(); tsIndex ++)
                        {
                            if (Float.compare(valuesInTsOrder.get(tsIndex), currentMissingValuePlaceHolder) == 0)
                                valuesInTsOrder.set(tsIndex, 0.0f);
                        } // for each tsIndex


                        //update the map with changes
                        varValuesInTsOrder.put(var, valuesInTsOrder);
                    } // for each var


                    //update the mtse
                    mtse.setVarValuesInTsOrder(varValuesInTsOrder);
                } // for each mtse
                break;
            case VALID_LOW_IMPUTATION:
                for(MTSE mtse : dcList)
                {
                    Map<String, List<Float>> varValuesInTsOrder = mtse.getVarValuesInTsOrder();
                    //for each var impute its values
                    for(String var : varValuesInTsOrder.keySet())
                    {
                        List<Float> valuesInTsOrder = varValuesInTsOrder.get(var);
                        for(int tsIndex = 0; tsIndex < valuesInTsOrder.size(); tsIndex ++)
                        {
                            if (Float.compare(valuesInTsOrder.get(tsIndex), currentMissingValuePlaceHolder) == 0)
                                valuesInTsOrder.set(tsIndex, varRanges.getRanges(var).getValidLow());
                        } // for each tsIndex


                        //update the map with changes
                        varValuesInTsOrder.put(var, valuesInTsOrder);
                    } // for each var


                    //update the mtse
                    mtse.setVarValuesInTsOrder(varValuesInTsOrder);
                } // for each mtse
                break;
            case NORMAL_VALUE_IMPUTATION:
                for(MTSE mtse : dcList)
                {
                    Map<String, List<Float>> varValuesInTsOrder = mtse.getVarValuesInTsOrder();
                    //for each var impute its values
                    for(String var : varValuesInTsOrder.keySet())
                    {
                        List<Float> valuesInTsOrder = varValuesInTsOrder.get(var);
                        for(int tsIndex = 0; tsIndex < valuesInTsOrder.size(); tsIndex ++)
                        {
                            if (Float.compare(valuesInTsOrder.get(tsIndex), currentMissingValuePlaceHolder) == 0)
                                valuesInTsOrder.set(tsIndex, varRanges.getRanges(var).getNormal());
                        } // for each tsIndex


                        //update the map with changes
                        varValuesInTsOrder.put(var, valuesInTsOrder);
                    } // for each var


                    //update the mtse
                    mtse.setVarValuesInTsOrder(varValuesInTsOrder);
                } // for each mtse
                break;
            case MEAN_VALUE_WITH_MASKING_VECTOR_IMPUTATION:
                //get mean for each variable as a map
                Map<String, Float> varMeanMap = Utils.meansOfVariablesUsingMaskingVector(dcList);
                for(MTSE mtse : dcList)
                {
                    Map<String, List<Float>> varValuesInTsOrder = mtse.getVarValuesInTsOrder();
                    Map<String, List<Integer>> maskingsInTsOrder = mtse.getMaskingsInTsOrder();

                    //for each var impute its values
                    for(String var : varValuesInTsOrder.keySet())
                    {
                        List<Float> valuesInTsOrderForThisVar = varValuesInTsOrder.get(var);
                        List<Integer> maskingInTsOrderForThisVar = maskingsInTsOrder.get(var);


                        for(int tsIndex = 0; tsIndex < valuesInTsOrderForThisVar.size(); tsIndex ++)
                        {
                            float imputedVarValueInThisTs
                                    = maskingInTsOrderForThisVar.get(tsIndex)
                                    * valuesInTsOrderForThisVar.get(tsIndex)
                                    + (1 - maskingInTsOrderForThisVar.get(tsIndex)) * varMeanMap.get(var);

                                valuesInTsOrderForThisVar.set(tsIndex, imputedVarValueInThisTs);
                        } // for each tsIndex


                        //update the map with changes
                        varValuesInTsOrder.put(var, valuesInTsOrderForThisVar);
                    } // for each var


                    //update the mtse
                    mtse.setVarValuesInTsOrder(varValuesInTsOrder);
                } // for each mtse
                break;
            case LIPTON_FORWARD_FILLING_IMPUTATION:
                //TODO implement the imputation method from the paper of Lipton et al.
                break;
        } // switch

        return dcList;
    } // impute

} // class Imputations
