/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.zhentao.redis.model;

public class BidHistory {
    private int count;
    private double max;
    private double sum;

    public void setCount(int count) {
        this.count = count;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public int getCount() {
        return count;
    }

    public double getMax() {
        return max;
    }

    public double getSum() {
        return sum;
    }

}
