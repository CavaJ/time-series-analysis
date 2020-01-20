package com.rb.tsa;


import java.io.Serializable;
import java.util.Objects;

//class to hold outlier removal ranges for the given variable
public class Ranges implements Serializable
{
    private Dataset dataset;
    private String varName;
    private float outlierLow;
    private float validLow;
    private float normal;
    private float validHigh;
    private float outlierHigh;

    public Ranges(Dataset dataset, String varName, float outlierLow, float validLow, float normal, float validHigh, float outlierHigh)
    {
        this.dataset = dataset;
        this.varName = varName;
        this.outlierLow = outlierLow;
        this.validLow = validLow;
        this.normal = normal;
        this.validHigh = validHigh;
        this.outlierHigh = outlierHigh;
    } // Ranges

    public String toString()
    {
        return varName + "," + outlierLow + "," + validLow + "," + normal + "," + validHigh + "," + outlierHigh;
    } // toString


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ranges)) return false; // it is ok even if o is null
        return this.hashCode() == ((Ranges) o).hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataset, varName);
    }


    public String getVarName() {
        return varName;
    }

    public float getOutlierLow() {
        return outlierLow;
    }

    public float getValidLow() {
        return validLow;
    }

    public float getNormal() {
        return normal;
    }

    public float getValidHigh() {
        return validHigh;
    }

    public float getOutlierHigh() {
        return outlierHigh;
    }
} // class Ranges
