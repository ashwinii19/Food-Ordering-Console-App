package com.techlabs.foodzone.service;

import com.techlabs.foodzone.model.Menu;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuService {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/mini_food_app";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "admin#123";

    public List<Menu> getMenuByCategory(int restaurantId, String category) {
        List<Menu> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM menus WHERE restaurant_id = ? AND category = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, restaurantId);
                ps.setString(2, category);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Menu menu = new Menu(rs.getInt("menu_id"), rs.getInt("restaurant_id"),
                                rs.getString("item_name"), rs.getDouble("price"),
                                rs.getString("category"));
                        list.add(menu);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching menu: " + e.getMessage());
        }
        return list;
    }

    public void viewMenuByCategory(int restaurantId, String category) {
        List<Menu> menus = getMenuByCategory(restaurantId, category);
        if (menus.isEmpty()) {
            System.out.println("No " + category + " items found.");
            return;
        }
        System.out.printf("\n--- %s Menu ---\n", category);
        System.out.printf("%-7s %-25s %-10s\n", "MenuID", "Item", "Price");
        for (Menu m : menus) {
            System.out.printf("%-7d %-25s ₹%.2f\n", m.getMenuId(), m.getItemName(), m.getPrice());
        }
    }
    
    public int getRestaurantIdByMenuId(int menuId) {
        String sql = "SELECT restaurant_id FROM menus WHERE menu_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, menuId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("restaurant_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching restaurant_id for menu item: " + e.getMessage());
        }
        return -1; 
    }

}
