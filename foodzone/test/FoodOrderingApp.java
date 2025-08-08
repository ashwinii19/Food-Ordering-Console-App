package com.techlabs.foodzone.test;

import com.techlabs.foodzone.service.FoodAppFacade;

public class FoodOrderingApp {
    public static void main(String[] args) {
        FoodAppFacade facade = new FoodAppFacade();
        facade.start();
    }
}