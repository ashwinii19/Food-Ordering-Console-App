package com.techlabs.foodzone.service;

import com.techlabs.foodzone.model.Restaurant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantService {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/mini_food_app";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "admin#123";

    public List<Restaurant> getAllRestaurants() {
        List<Restaurant> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM restaurants";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Restaurant r = new Restaurant(rs.getInt("restaurant_id"), rs.getString("name"),
                            rs.getString("location"), rs.getString("cuisine"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching restaurants: " + e.getMessage());
        }
        return list;
    }

    public void showAvailableRestaurants() {
        List<Restaurant> restaurants = getAllRestaurants();
        if (restaurants.isEmpty()) {
            System.out.println("No restaurants available.");
            return;
        }

        System.out.printf("%-5s %-20s %-15s %-15s%n", "ID", "Name", "Location", "Cuisine");
        System.out.println("-----------------------------------------------------");
        for (Restaurant r : restaurants) {
            System.out.printf("%-5d %-20s %-15s %-15s%n", r.getRestaurantId(), r.getName(), r.getLocation(),
                    r.getCuisine());
        }
    }
}
