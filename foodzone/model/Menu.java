package com.techlabs.foodzone.model;

public class Menu {
    private int menuId;
    private int restaurantId;
    private String itemName;
    private double price;
    private String category;

    public Menu(int menuId, int restaurantId, String itemName, double price, String category) {
        this.menuId = menuId;
        this.restaurantId = restaurantId;
        this.itemName = itemName;
        this.price = price;
        this.category = category;
    }

    public int getMenuId() {
        return menuId;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public String getItemName() {
        return itemName;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }
}
