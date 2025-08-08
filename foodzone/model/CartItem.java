package com.techlabs.foodzone.model;

public class CartItem {
    private int menuId;
    private int quantity;
    private double price;
    private String itemName;

    public CartItem(int menuId, int quantity, double price, String itemName) {
        this.menuId = menuId;
        this.quantity = quantity;
        this.price = price;
        this.itemName = itemName;
    }

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
