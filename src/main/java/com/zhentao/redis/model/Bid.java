package com.zhentao.redis.model;

public class Bid {

    private final String brand;
    private final double price;

    public Bid(String brand, double price) {
        this.brand = brand;
        this.price = price;
    }

    public String getBrand() {
        return brand;
    }

    public double getPrice() {
        return price;
    }

}
