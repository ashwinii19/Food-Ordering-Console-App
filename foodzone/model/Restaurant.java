package com.techlabs.foodzone.model;

public class Restaurant {
    private int restaurantId;
    private String name;
    private String location;
    private String cuisine;

    public Restaurant(int restaurantId, String name, String location, String cuisine) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.location = location;
        this.cuisine = cuisine;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getCuisine() {
        return cuisine;
    }
}
